package com.zzwc.cms.mongo.crud;

import org.bson.Document;

import java.util.Map;

/**
 * 查询解析器，每种解析器都有自定义的规则，客户端按照规则组织查询条件，经过解析后生成mongodb可执行的查询语句
 * 
 * @author weirdor
 *
 */
public interface QueryParser {

	/**
	 * 根据客户端条件，解析出mongodb查询语句
	 * 
	 * @param query
	 *            按照解析器规则组织好的客户端查询条件
	 * @return
	 * @author weirdor
	 */
	Document parse(Map<String, Object> query);
}
