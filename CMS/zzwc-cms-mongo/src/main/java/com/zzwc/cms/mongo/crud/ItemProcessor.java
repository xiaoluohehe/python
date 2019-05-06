package com.zzwc.cms.mongo.crud;

import org.bson.Document;

/**
 * 数据单项处理逻辑，在列表时，有时需要对表中的单项做特殊处理
 * 
 * @author weirdor
 *
 */
@FunctionalInterface
public interface ItemProcessor {

	/**
	 * 单条数据处理逻辑
	 * 
	 * @param doc
	 *            单项数据
	 * @return 处理后的结果
	 * @author weirdor
	 */
	Document process(Document doc);
}
