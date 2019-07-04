package com.acgist.snail.system.evaluation;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.pojo.entity.RangeEntity;
import com.acgist.snail.pojo.session.PeerSession;
import com.acgist.snail.repository.impl.RangeRepository;
import com.acgist.snail.utils.NetUtils;

/**
 * <p>Peer评估器</p>
 * <p>根据IP地址评估，插入Peer队列的头部还是尾部。</p>
 * <p>将所有IP（2^32个）分为65536（2^16）个区域，然后可以连接和可以下载的均给予评分，然后计算插入Peer队列位置。</p>
 * <p>系统启动时初始化分数，关闭时保存分数，得分=0的记录不保存数据库。</p>
 * 
 * @author acgist
 * @since 1.1.0
 */
public class PeerEvaluator {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(PeerEvaluator.class);

	private boolean available; // 初始完成，可用状态。
	
	/**
	 * 范围表
	 */
	private final Map<Integer, RangeEntity> map;
	/**
	 * 步长
	 */
	private static final int RANGE_STEP = 2 << 15;
	/**
	 * 最低分
	 * TODO：初始
	 */
	private int horizontal = 0;
	
	private static final PeerEvaluator INSTANCE = new PeerEvaluator();
	
	private PeerEvaluator() {
		this.map = new ConcurrentHashMap<>();
	}
	
	public static final PeerEvaluator getInstance() {
		return INSTANCE;
	}
	
	/**
	 * 初始化：初始数据，加载分数。
	 */
	public void init() {
		LOGGER.debug("初始化Peer评估器");
		this.buildRange();
		this.available = true;
	}

	/**
	 * 判断Peer插入头部还是尾部。
	 * 
	 * @param peerSession Peer
	 * 
	 * @return true：尾部（优先使用）；false：头部；
	 */
	public boolean eval(PeerSession peerSession) {
		if(!this.available) { // 没有初始化直接返回插入头部
			return false;
		}
		final long ip = NetUtils.encodeIpToLong(peerSession.host());
		final int index = (int) (ip / RANGE_STEP);
		final RangeEntity range = this.map.get(index);
		if(range == null) {
			return false;
		}
		return range.getScore() > this.horizontal;
	}

	/**
	 * 计分
	 * 不同步，运行出现误差
	 */
	public void score(PeerSession peerSession, RangeEntity.Type type) {
		if(!this.available) { // 没有初始化不计分
			return;
		}
		final long ip = NetUtils.encodeIpToLong(peerSession.host());
		final int index = (int) (ip / RANGE_STEP);
		RangeEntity range = this.map.get(index);
		if(range == null) {
			range = new RangeEntity();
			range.setIndex(index);
			range.setScore(0);
		}
		range.setChange(true);
		range.setScore(type.score() + range.getScore());
	}
	
	/**
	 * 释放资源
	 * TODO：
	 */
	public void release() {
		synchronized (this) {
			this.store();
			this.available = false;
		}
	}
	
	/**
	 * 记录数据库：只记录分值大于0的数据。
	 */
	public void store() {
		if(!this.available) { // 没有初始化不保存
			return;
		}
		synchronized (this.map) {
			final RangeRepository repository = new RangeRepository();
			this.map.values().stream()
			.filter(entity -> entity.isChange()) // 修改才会修改数据库
			.forEach(entity -> {
				repository.update(entity);
			});
		}
	}

	/**
	 * 初始数据
	 */
	private void buildRange() {
		final RangeRepository repository = new RangeRepository();
		final List<RangeEntity> list = repository.findAll();
		if(list == null) {
			LOGGER.info("Peer评估器没有数据");
			return;
		}
		LOGGER.info("Peer评估器加载数据：{}", list.size());
		list.stream()
		.forEach(entity -> {
			this.map.put(entity.getIndex(), entity);
		});
	}

}
