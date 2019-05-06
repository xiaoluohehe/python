package com.zzwc.cms.mongo.utils;

import com.mongodb.MongoClient;
import com.zzwc.cms.common.exception.ZhiZhiException;
import com.zzwc.cms.mongo.crud.impl.CRUDService;
import org.bson.Document;
import org.springframework.util.Assert;

/**
 * @author weirdor
 *
 */
public class MongoSequence {

	private CRUDService crud;

	private static long SEQ_START = 1L;

	public MongoSequence(MongoClient client, String dbName) {
		this.crud = new CRUDService(client, dbName, "mongo_sequence");
	}

	/**
	 * 获取序列的下一个值
	 * 
	 * @return
	 * @author
	 */
	public long next(String category) {

		Document doc = crud.findAndModify(new Document("category", category),
				new Document("$inc", new Document("value", 1)), null, null, false);
		if (doc != null) {
			return DocumentUtils.getLongValue(doc, "value");
		}

		throw new ZhiZhiException(category + " 序列不存在");
	}

	public void createSequence(String category, long initValue) {

		Assert.notNull(category, "序列类目不可为空");

		Document dbDoc = crud.findOne(new Document("category", category), null, null, null);

		if (dbDoc != null) {
			throw new ZhiZhiException( "序列" + category + "已存在");
		}

		Document doc = new Document("category", category).append("value", initValue);
		crud.insert(doc);
	}

	/**
	 * 获取当前序列值
	 * 线程同步, 并且不抛异常
	 */
	public synchronized long getValueSync(String category) {

		Document seq = crud.findAndModify(new Document("category", category),
				new Document("$inc", new Document("value", 1)), null, null, false);

		long value = SEQ_START;

		// 虽然该方法加锁了，但在分布式环境下当seq==null时仍会有问题
		if (seq == null) {
			Document doc = new Document("category", category).append("value", value);
			crud.insert(doc);
		} else {
			value = DocumentUtils.getLongValue(seq, "value");
		}
		return value;
	}



}
