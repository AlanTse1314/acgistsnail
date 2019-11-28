package com.acgist.snail.pojo.wrapper;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.system.bencode.BEncodeDecoder;
import com.acgist.snail.system.bencode.BEncodeEncoder;
import com.acgist.snail.system.exception.PacketSizeException;
import com.acgist.snail.utils.CollectionUtils;
import com.acgist.snail.utils.StringUtils;

/**
 * <p>种子文件选择包装器</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public final class TorrentSelectorWrapper {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(TorrentSelectorWrapper.class);

	private BEncodeEncoder encoder;
	private BEncodeDecoder decoder;

	private TorrentSelectorWrapper() {
	}

	/**
	 * <p>编码器</p>
	 */
	public static final TorrentSelectorWrapper newEncoder(List<String> list) {
		final TorrentSelectorWrapper wrapper = new TorrentSelectorWrapper();
		if(CollectionUtils.isNotEmpty(list)) {
			wrapper.encoder = BEncodeEncoder.newInstance();
			wrapper.encoder.newList().put(list);
		}
		return wrapper;
	}
	
	/**
	 * <p>解析器</p>
	 */
	public static final TorrentSelectorWrapper newDecoder(String value) {
		final TorrentSelectorWrapper wrapper = new TorrentSelectorWrapper();
		if(StringUtils.isNotEmpty(value)) {
			wrapper.decoder = BEncodeDecoder.newInstance(value);
		}
		return wrapper;
	}
	
	/**
	 * <p>编码选择文件</p>
	 */
	public String serialize() {
		if(this.encoder == null) {
			return null;
		}
		return encoder.flush().toString();
	}

	/**
	 * <p>解析选择文件</p>
	 */
	public List<String> deserialize() {
		if(this.decoder == null) {
			return List.of();
		}
		try {
			return this.decoder.nextList().stream()
				.filter(object -> object != null)
				.map(object -> {
					return BEncodeDecoder.getString(object);
				})
				.collect(Collectors.toList());
		} catch (PacketSizeException e) {
			LOGGER.error("解析选择文件异常", e);
		}
		return List.of();
	}

}
