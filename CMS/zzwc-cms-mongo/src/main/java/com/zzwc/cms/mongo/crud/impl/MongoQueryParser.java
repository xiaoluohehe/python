package com.zzwc.cms.mongo.crud.impl;

import com.zzwc.cms.common.exception.ZhiZhiException;
import com.zzwc.cms.common.utils.TimeUtils;
import com.zzwc.cms.mongo.crud.QueryParser;
import com.zzwc.cms.mongo.utils.DocumentUtils;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.*;
import java.util.regex.Pattern;

/**
 * 客户端查询解析器，最终会解析成mongo可执行的查询语句；由于mongodb内置的操作符以$开头，所以为了能对查询条件做到完全控制，要求<b>查询key不能以$开头</b>
 * 
 * @author
 *
 */
public class MongoQueryParser implements QueryParser {

	/* 预定义的操作常量 */
	private static final String AND = "and";
	private static final String OR = "or";

	private static final String GT = "gt";
	private static final String GTE = "gte";
	private static final String LT = "lt";
	private static final String LTE = "lte";
	private static final String NE = "ne";
	private static final String EQ = "eq";

	private static final String IN = "in";
	private static final String NOT_IN = "nin";
	private static final String ALL = "all";
	private static final String ELEM_MATCH = "elem_match";

	private static final String EXISTS = "exists";

	private static final String START = "start";
	private static final String END = "end";

	private static final String ID_FLAG = "_id";

	private static final String LIKE = "like";
	private static final String VALUE = "value";

	private static final List<String> keyWords = Arrays.asList(AND, OR, GT, GTE, LT, LTE, NE, EQ, IN, NOT_IN, ALL,
			ELEM_MATCH, EXISTS, START, END, LIKE, VALUE);

	@Override
	public Document parse(Map<String, Object> query) {
		if (query == null || query.size() == 0) {
			return new Document();
		}

		Document queryDoc = new Document();

		query.entrySet().forEach(entry -> {
			String key = entry.getKey();
			if (key.startsWith("$")) {
				throw new ZhiZhiException( "查询key不可以$开头， " + key + " 不被接受");
			}
			Object value = entry.getValue();
			doParse(key, value, queryDoc);
		});
		return queryDoc;
	}

	@SuppressWarnings("unchecked")
	private void doParse(String key, Object value, Document queryDoc) {
		if (AND.equals(key) || OR.equals(key)) {
			validateType(value, List.class);
			Collection<?> collectionValues = (Collection<?>) value;
			if (collectionValues.isEmpty()) {
				throw new ZhiZhiException("and / or 值列表为空");
			}
			List<Document> listDoc = new ArrayList<>();

			collectionValues.forEach(ele -> {
				validateType(ele, Map.class);
				Map<String, Object> origSubQuery = (Map<String, Object>) ele;
				listDoc.add(parse(origSubQuery));
			});

			if (AND.equals(key)) {
				List<Document> andList = queryDoc.get("$and", List.class);
				if (andList == null) {
					andList = listDoc;
					queryDoc.put("$and", andList);
				} else {
					andList.addAll(listDoc);
				}
			} else {
				List<Document> orList = queryDoc.get("$or", List.class);
				if (orList == null) {
					orList = listDoc;
					queryDoc.put("$or", orList);
				} else {
					orList.addAll(listDoc);
				}
			}
		} else {
			if (value instanceof Map) {

				Map<String, Object> compQueryMap = (Map<String, Object>) value;

				/**
				 * 兼容老的查询语法： {"ne":{"orderStatus":4,"source":"app"}} 变为
				 * {"orderStatus":{"ne":4},"source":{"ne":"app"}}
				 */
				if (NE.equals(key) || EQ.equals(key) || GT.equals(key) || GTE.equals(key) || LT.equals(key)
						|| LTE.equals(key) || EXISTS.equals(key)) {
					Set<String> keys = compQueryMap.keySet();

					if (keys.isEmpty()) {
						throw new ZhiZhiException( "值列表为空");
					}

					for (String newKey : keys) {
						Map<String, Object> newCompQueryMap = new HashMap<>();
						newCompQueryMap.put(key, compQueryMap.get(newKey));

						doParse(newKey, newCompQueryMap, queryDoc);
					}
					return;
				}

				Map<String, Object> compMap = new HashMap<>();

				Map<String, Object> compQuery = (Map<String, Object>) value;

				if (!keyWords.containsAll(compQuery.keySet())) {
					throw new ZhiZhiException(
							"错误的复合查询" + compQuery + " 中包含不支持的操作符");
				}

				if (compQuery.containsKey(LIKE)) {
					Object v = compQuery.get(LIKE);

					if (v instanceof Boolean) {
						Object qv = compQuery.get(VALUE);
						if (qv == null) {
							throw new IllegalArgumentException("like需和value成对出现");
						}
						if ((boolean) v) {
							String patternStr = qv.toString();
							patternStr = filterRegx(patternStr);
							compMap.put(key, Pattern.compile(patternStr, Pattern.CASE_INSENSITIVE));
						} else {
							compMap.put(key, qv);
						}
					} else {
						String patternStr = v.toString();
						patternStr = filterRegx(patternStr);
						compMap.put(key, Pattern.compile(patternStr, Pattern.CASE_INSENSITIVE));
					}
					queryDoc.putAll(compMap);

				} else {
					if (compQuery.containsKey(GT)) {
						if ("createTime".equals(key) || "lastModified".equals(key)) {
							Object dateObj = compQuery.get(GT);
							compMap.put("$gt", TimeUtils.getDate(dateObj));
						} else {
							compMap.put("$gt", compQuery.get(GT));
						}
					}
					if (compQuery.containsKey(GTE)) {
						if ("createTime".equals(key) || "lastModified".equals(key)) {
							Object dateObj = compQuery.get(GTE);
							compMap.put("$gte", TimeUtils.getDate(dateObj));
						} else {
							compMap.put("$gte", compQuery.get(GTE));
						}
					}
					if (compQuery.containsKey(LT)) {
						if ("createTime".equals(key) || "lastModified".equals(key)) {
							Object dateObj = compQuery.get(LT);
							compMap.put("$lt", TimeUtils.getDate(dateObj));
						} else {
							compMap.put("$lt", compQuery.get(LT));
						}
					}
					if (compQuery.containsKey(LTE)) {
						if ("createTime".equals(key) || "lastModified".equals(key)) {
							Object dateObj = compQuery.get(LTE);
							compMap.put("$lte", TimeUtils.getDate(dateObj));
						} else {
							compMap.put("$lte", compQuery.get(LTE));
						}
					}
					if (compQuery.containsKey(NE)) {
						if ("createTime".equals(key) || "lastModified".equals(key)) {
							Object dateObj = compQuery.get(NE);
							compMap.put("$ne", TimeUtils.getDate(dateObj));
						} else {
							compMap.put("$ne", compQuery.get(NE));
						}
					}
					if (compQuery.containsKey(EQ)) {
						if ("createTime".equals(key) || "lastModified".equals(key)) {
							Object dateObj = compQuery.get(EQ);
							compMap.put("$eq", TimeUtils.getDate(dateObj));
						} else {
							compMap.put("$eq", compQuery.get(EQ));
						}
					}
					if (compQuery.containsKey(IN)) {
						Object v = compQuery.get(IN);
						validateType(v, List.class);
						compMap.put("$in", v);
					}
					if (compQuery.containsKey(NOT_IN)) {
						Object v = compQuery.get(NOT_IN);
						validateType(v, List.class);
						compMap.put("$nin", v);
					}
					if (compQuery.containsKey(ALL)) {
						Object v = compQuery.get(ALL);
						validateType(v, List.class);
						compMap.put("$all", v);
					}
					if (compQuery.containsKey(EXISTS)) {
						Object v = compQuery.get(EXISTS);
						validateType(v, Boolean.class);
						compMap.put("$exists", v);
					}
					if (compQuery.containsKey(ELEM_MATCH)) {
						Object v = compQuery.get(ELEM_MATCH);
						validateType(v, Map.class);
						/** $elemMatch的值是一个查询，所以进一步递归 */
						compMap.put("$elemMatch", parse((Map<String, Object>) v));
					}
					if (compQuery.containsKey(START)) {
						compMap.put("$gt", TimeUtils.getDate(compQuery.get(START)));
					}
					if (compQuery.containsKey(END)) {
						compMap.put("$lt", TimeUtils.getDate(compQuery.get(END)));
					}
					queryDoc.put(key, compMap);
				}

			} else {
				if (ID_FLAG.equals(key)) {
					if (!DocumentUtils.isValidObjectId(value.toString())) {
						throw new ZhiZhiException( "_id格式错误");
					}
					queryDoc.put(key, new ObjectId(value.toString()));
				} else {
					queryDoc.put(key, value);
				}
			}

		}
	}

	/**
	 * 过滤正则表达式字符
	 * 
	 * @param patternStr
	 * @return
	 */
	private String filterRegx(String patternStr) {
		patternStr = patternStr.replaceAll("\\\\", "\\\\\\\\").replaceAll("\\*", "\\\\*").replaceAll("\\.", "\\\\.")
				.replaceAll("\\?", "\\\\?").replaceAll("\\+", "\\\\+").replaceAll("\\$", "\\\\\\$")
				.replaceAll("\\^", "\\\\^").replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]")
				.replaceAll("\\(", "\\\\(").replaceAll("\\)", "\\\\)").replaceAll("\\{", "\\\\{")
				.replaceAll("\\}", "\\\\}").replaceAll("\\|", "\\\\|").replaceAll("/", "\\\\/");
		return patternStr;
	}

	/**
	 * 验证数据类型，若验证失败将抛出IllegalArgumentException
	 * 
	 * @param value
	 * @author
	 */
	private void validateType(Object value, Class<?> cls) {
		if (value == null) {
			throw new ZhiZhiException( "值不可为空");
		}
		if (!(cls.isAssignableFrom(value.getClass()))) {
			throw new ZhiZhiException(
					"遇到类型错误，期望为 " + cls + " 类型,实际为 " + value.getClass());
		}
	}
}
