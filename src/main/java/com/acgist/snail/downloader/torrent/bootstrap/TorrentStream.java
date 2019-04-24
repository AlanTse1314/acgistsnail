package com.acgist.snail.downloader.torrent.bootstrap;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.pojo.bean.TorrentPiece;
import com.acgist.snail.system.config.DownloadConfig;
import com.acgist.snail.utils.CollectionUtils;
import com.acgist.snail.utils.FileUtils;

/**
 * <p>Torrent下载文件流</p>
 * <p>
 * 下载：每次下载必须是一个完整的Piece。
 * （除了文件开头和结尾可能不是一个完整的Piece）
 * </p>
 * <p>
 * 块是否下载：判断每个块前面10字节数据。
 * </p>
 * TODO：文件校验
 * 
 * @author acgist
 * @since 1.0.0
 */
public class TorrentStream {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(TorrentStream.class);

	/**
	 * 校验文件的字节数
	 */
	private static final int VERIFY_SIZE = 10;
//	private static final int VERIFY_SIZE_CHECK = 20; // 再次矫正
	
	// 固定值
	private final long pieceLength; // 每个块的大小
	private final TorrentStreamGroup torrentStreamGroup; // 下载文件组
	
	// 初始值：变化值
	private AtomicLong fileBuffer; // 缓冲大小：插入queue时修改
	private AtomicLong fileDownloadSize; // 已下载大小：写入文件时修改
	private BlockingQueue<TorrentPiece> filePieces; // Piece队列
	
	// 初始值：不变值
	private String file; // 文件路径
	private long fileSize; // 文件大小
	private long fileBeginPos; // 文件开始偏移：包含该值
	private long fileEndPos; // 文件结束偏移：不包含该值
	private RandomAccessFile fileStream; // 文件流
	
	// 初始值：计算
	private int filePieceSize; // 文件Piece数量
	private int fileBeginPieceIndex; // 文件Piece开始索引
	private int fileEndPieceIndex; // 文件Piece结束索引
	private BitSet pieces; // 当前文件位图
	private BitSet badPieces; // 上次下载失败位图：下次请求后清除
	private BitSet downloadPieces; // 下载中位图
	
	private TorrentStream(long pieceLength, TorrentStreamGroup torrentStreamGroup) {
		this.pieceLength = pieceLength;
		this.torrentStreamGroup = torrentStreamGroup;
	}
	
	public static final TorrentStream newInstance(long pieceLength, TorrentStreamGroup torrentStreamGroup) {
		return new TorrentStream(pieceLength, torrentStreamGroup);
	}
	
	/**
	 * 设置文件信息
	 * @param file 文件路径
	 * @param size 文件大小
	 * @param pos 文件开始偏移
	 */
	public void buildFile(final String file, final long size, final long pos) throws IOException {
		if(filePieces != null && filePieces.size() > 0) {
			throw new IOException("Torrent文件未被释放");
		}
		this.file = file;
		this.fileSize = size;
		this.fileBeginPos = pos;
		this.fileEndPos = pos + size;
		fileBuffer = new AtomicLong(0);
		fileDownloadSize = new AtomicLong(0);
		filePieces = new LinkedBlockingQueue<>();
		FileUtils.buildFolder(this.file, true); // 创建文件父目录，否者会抛出FileNotFoundException
		fileStream = new RandomAccessFile(this.file, "rw");
		initFilePiece();
		initFilePieces();
		initDownloadSize();
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug(
				"TorrentStream信息，块大小：{}，文件路径：{}，文件大小：{}，文件开始偏移：{}，文件Piece数量：{}，文件Piece开始索引：{}，文件Piece结束索引：{}",
				this.pieceLength,
				this.file,
				this.fileSize,
				this.fileBeginPos,
				this.filePieceSize,
				this.fileBeginPieceIndex,
				this.fileEndPieceIndex
			);
		}
	}
	
	/**
	 * <P>添加Piece</p>
	 * <P>每次添加的必须是一个完成的Piece，如果不在该文件范围内则不操作。</p>
	 * <P>如果缓存达到缓存大小或者文件下载完成则写入文件。</p>
	 */
	public void piece(TorrentPiece piece) {
		if(!piece.contain(this.fileBeginPos, this.fileEndPos)) { // 不符合当前文件位置
			return;
		}
		List<TorrentPiece> list = null; // 写入文件的Piece
		synchronized (this) {
			if(filePieces.offer(piece)) {
				this.download(piece.getIndex());
				this.torrentStreamGroup.piece(piece.getIndex());
				final long bufferSize = this.fileBuffer.addAndGet(piece.getLength());
				final long downloadSize = this.fileDownloadSize.addAndGet(piece.getLength());
				if(
					bufferSize >= DownloadConfig.getPeerMemoryBufferByte() || // 大于缓存
					downloadSize == this.fileSize // 下载完成
				) {
					this.fileBuffer.set(0L); // 清空
					list = flush();
				}
			} else {
				// TODO：重新返回Piece位图
				LOGGER.error("Piece保存失败");
			}
		}
		write(list);
	}
	
	/**
	 * <p>选择未下载的Piece序号</p>
	 * <p>选择Peer有同时没有下载并且不处于失败和下载中的Piece，选择后清除失败的Piece数据。</p>
	 * 
	 * @param peerPieces Peer含有的位图
	 */
	public TorrentPiece pick(final BitSet peerPieces) {
		if(peerPieces.cardinality() == 0) {
			return null;
		}
		synchronized (this) {
			final BitSet pickPieces = new BitSet();
			pickPieces.or(peerPieces);
			pickPieces.andNot(this.pieces);
			pickPieces.andNot(this.badPieces);
			pickPieces.andNot(this.downloadPieces);
			this.badPieces.clear(); // 清除坏的Piece
			if(pickPieces.cardinality() == 0) {
				return null;
			}
			final int index = pickPieces.nextSetBit(this.fileBeginPieceIndex);
			if(index == -1 || index > this.fileEndPieceIndex) {
				return null;
			}
			int begin = 0;
			if(index == this.fileBeginPieceIndex) { // 第一块获取开始偏移
				begin = firstPiecePos();
			}
			int end = (int) this.pieceLength;
			if(index == this.fileEndPieceIndex) { // 最后一块获取结束偏移
				end = lastPieceSize();
			}
			return new TorrentPiece(this.pieceLength, index, begin, end);
		}
	}

	/**
	 * <p>读取块数据</p>
	 * <p>默认数据块大小：{@linkplain TorrentStream#pieceLength 块大小}，默认偏移：0</p>
	 */
	public byte[] read(int index) {
		return read(index, (int) this.pieceLength);
	}
	
	/**
	 * <p>读取块数据</p>
	 * <p>默认偏移：0</p>
	 */
	public byte[] read(int index, int size) {
		return read(index, size, 0);
	}
	
	/**
	 * <p>读取块数据</p>
	 */
	public byte[] read(int index, int size, int pos) {
		return read(index, size, pos, false);
	}
	
	/**
	 * <p>读取块数据</p>
	 * <p>如果选择的Piece不在文件范围内返回null。</p>
	 * <p>如果读取数据只有部分符合文件的范围，会自动修正范围，读取符合部分数据返回。</p>
	 * 
	 * @param ignorePieces 忽略已下载位图，读取文件验证
	 */
	private byte[] read(int index, int size, int pos, boolean ignorePieces) {
		if(!haveIndex(index)) {
			return null;
		}
		if(!ignorePieces && !havePiece(index)) {
			return null;
		}
		long seek = 0L;
		final long beginPos = pieceLength * index + pos;
		final long endPos = beginPos + size;
		if(beginPos >= this.fileEndPos) {
			return null;
		}
		if(endPos < this.fileBeginPos) {
			return null;
		}
		if(beginPos < this.fileBeginPos) {
			size = (int) (size - (this.fileBeginPos - beginPos));
		} else {
			seek = beginPos - this.fileBeginPos;
		}
		if(endPos >= this.fileEndPos) {
			size = (int) (size - (endPos - this.fileEndPos));
		}
		if(size <= 0) {
			return null;
		}
		final byte[] bytes = new byte[size];
		try {
			fileStream.seek(seek);
			fileStream.read(bytes);
		} catch (IOException e) {
			LOGGER.error("Piece读取异常：{}-{}-{}-{}", index, size, pos, ignorePieces, e);
		}
		return bytes;
	}
	
	/**
	 * 已下载大小
	 */
	public long size() {
		return this.fileDownloadSize.get();
	}
	
	/**
	 * 是否下载完成
	 */
	public boolean over() {
		return pieces.cardinality() >= this.filePieceSize;
	}
	
	/**
	 * 下载失败
	 * 
	 * @param piece 下载失败块
	 */
	public void undone(TorrentPiece piece) {
		if(!piece.contain(this.fileBeginPos, this.fileEndPos)) { // 不符合当前文件位置
			return;
		}
		synchronized (this) {
			this.badPieces.set(piece.getIndex());
			this.downloadPieces.clear(piece.getIndex());
		}
	}
	
	/**
	 * <p>释放资源</p>
	 * <p>将已下载的块写入文件，然后关闭文件流。</p>
	 */
	public void release() {
		List<TorrentPiece> list = null;
		synchronized (this) {
			list = flush();
		}
		write(list);
		try {
			fileStream.close();
		} catch (IOException e) {
			LOGGER.error("TorrentStream关闭异常", e);
		}
	}

	/**
	 * <p>刷新缓存</p>
	 * <p>将缓存队列所有的Piece块刷出。</p>
	 */
	private List<TorrentPiece> flush() {
		int size = filePieces.size();
		final List<TorrentPiece> list = new ArrayList<>(size);
		filePieces.drainTo(list, size);
		return list;
	}
	
	/**
	 * 写入硬盘
	 */
	private void write(List<TorrentPiece> list) {
		if(CollectionUtils.isEmpty(list)) {
			return;
		}
		list.stream().forEach(piece -> {
			if(
				piece.getIndex() < this.fileBeginPieceIndex ||
				piece.getIndex() > this.fileEndPieceIndex
			) {
				LOGGER.warn("写入文件索引错误");
				return;
			}
			int offset = 0;
			long seek = 0L;
			int length = piece.getLength();
			final long beginPos = piece.beginPos();
			final long endPos = piece.endPos();
			if(beginPos < this.fileBeginPos) {
				offset = (int) (this.fileBeginPos - beginPos);
				length = length - offset;
			} else {
				seek = beginPos - this.fileBeginPos;
			}
			if(endPos >= this.fileEndPos) {
				length = (int) (length - (endPos - this.fileEndPos));
			}
			if(length <= 0) {
				return;
			}
			try {
				fileStream.seek(seek);
				fileStream.write(piece.getData(), offset, length);
			} catch (IOException e) {
				LOGGER.error("TorrentStream写入异常", e);
			}
		});
	}
	
	/**
	 * 初始化：开始块序号，结束块序号等
	 */
	private void initFilePiece() {
		this.fileBeginPieceIndex = (int) (this.fileBeginPos / this.pieceLength);
		this.fileEndPieceIndex = (int) (this.fileEndPos / this.pieceLength);
		this.filePieceSize = this.fileEndPieceIndex - this.fileBeginPieceIndex;
		int endPieceSize = (int) (this.fileEndPos % this.pieceLength);
		if(endPieceSize > 0) {
			this.filePieceSize++;
		}
		pieces = new BitSet();
		badPieces = new BitSet();
		downloadPieces = new BitSet();
	}
	
	/**
	 * 初始化：已下载块
	 */
	private void initFilePieces() {
		int pos = 0;
		byte[] bytes = null;
		for (int index = this.fileBeginPieceIndex; index <= this.fileEndPieceIndex; index++) {
			if(index == this.fileBeginPieceIndex) {
				pos = firstPiecePos();
			} else {
				pos = 0;
			}
			bytes = read(index, VERIFY_SIZE, pos, true); // 第一块需要偏移
			if(haveData(bytes)) {
				download(index);
				torrentStreamGroup.piece(index);
//			} else { // 再次校验：TODO：是否需要优化
//				bytes = read(index, VERIFY_SIZE_CHECK, pos, true); // 第一块需要偏移
//				if(haveData(bytes)) {
//					download(index);
//					torrentStreamGroup.piece(index);
//				}
			}
		}
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug("当前文件已下载Piece数量：{}，剩余下载Piece数量：{}",
				pieces.cardinality(),
				this.filePieceSize - pieces.cardinality()
			);
		}
	}
	
	/**
	 * 初始化：已下载文件大小
	 */
	private void initDownloadSize() {
		long size = 0L;
		int downloadPieceSize = this.pieces.cardinality();
		if(havePiece(this.fileBeginPieceIndex)) {
			size += firstPieceSize();
			downloadPieceSize--;
		}
		if(havePiece(this.fileEndPieceIndex)) {
			size += lastPieceSize();
			downloadPieceSize--;
		}
		this.fileDownloadSize.set(size + downloadPieceSize * this.pieceLength);
	}
	
	/**
	 * 是否有数据
	 */
	private boolean haveData(byte[] bytes) {
		if(bytes == null) {
			return false;
		}
		for (byte value : bytes) {
			if(value != 0) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 下载块
	 */
	private void download(int index) {
		pieces.set(index, true); // 下载成功
		downloadPieces.clear(index); // 去掉下载状态
	}
	
	/**
	 * 第一块的偏移
	 */
	private int firstPiecePos() {
		return (int) (this.fileBeginPos - (this.fileBeginPieceIndex * this.pieceLength));
	}
	
	/**
	 * 第一块的大小
	 */
	private int firstPieceSize() {
		return (int) (this.pieceLength - firstPiecePos());
	}
	
	/**
	 * 结束块的大小=偏移
	 */
	private int lastPieceSize() {
		return (int) (this.fileEndPos - (this.pieceLength * this.fileEndPieceIndex));
	}

	/**
	 * 文件是否包含块
	 * 
	 * @param index Piece序号
	 */
	private boolean haveIndex(int index) {
		if(index < this.fileBeginPieceIndex || index > this.fileEndPieceIndex) { // 不符合当前文件位置
			return false;
		}
		return true;
	}
	
	/**
	 * 是否含有Piece数据
	 */
	private boolean havePiece(int index) {
		synchronized (this) {
			return pieces.get(index);
		}
	}

}
