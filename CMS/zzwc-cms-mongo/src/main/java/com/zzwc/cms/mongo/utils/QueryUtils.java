package com.zzwc.cms.mongo.utils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mongodb.BasicDBList;
import com.zzwc.cms.common.exception.ZhiZhiException;
import com.zzwc.cms.common.utils.ZhiZhiStringUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Pattern;

/**
 * 查询工具类，用于解析前台请求参数，转换为mongodb查询语句
 * 
 * @author weirdor
 *
 */
public class QueryUtils {

	private static Logger logger = LoggerFactory.getLogger(QueryUtils.class);

	/**
	 * 字符串转Document
	 * 
	 * @param jsonStr
	 *            json字符串
	 * @param strict
	 *            true时当json字符串有错是会抛异常，false时会忽略错误并返回一个空的Document
	 * @return
	 */
	public static Document getDocumentFromStr(String jsonStr, boolean strict){
		return new Document(getMapFromStr(jsonStr, strict));
	}

	/**
	 * 字符串转Document，如果json格式有错，将返回一个空的Document
	 * 
	 * @param jsonStr
	 * @return
	 */
	public static Document getDocumentFromStr(String jsonStr) {
		return getDocumentFromStr(jsonStr, false);
	}

	/**
	 * Document转json字符串
	 * 
	 * @param doc
	 * @return
	 * @throws
	 */
	public static String getJsonStrFromDocument(Document doc) {
		return doc.toJson();
	}

	/**
	 * 处理用户条件，生成数据库查询语句
	 * 
	 * @param doc
	 *            查询条件的json字符串
	 * @param likeFlag
	 *            模糊查询标记
	 * @return
	 */
	@Deprecated
	public static Document generateQuery(Document doc, Boolean likeFlag) {

		if (doc == null) {
			return new Document();
		}

		// 考虑到兼容性，默认开启模糊查询
		likeFlag = likeFlag == null ? true : likeFlag;

		// 特殊处理_id，当查询中有_id时，要将其构造成ObjectId到数据库中查询
		testAndSetObjectId(doc);

		// 如果指定了模糊查询，则特殊处理值为字符串的查询条件，将其都变为模糊条件
		if (likeFlag) {
			Set<Entry<String, Object>> entrySet = doc.entrySet();
			for (Entry<String, Object> entry : entrySet) {
				if (entry.getValue() instanceof String) {
					String stringVal = (String) entry.getValue();
					// 如果字符串中包含正则表达式中特有的字符，如小括号，则会报错，所以要预先处理一下
					stringVal = ZhiZhiStringUtils.transformRegexCharacter(stringVal);
					doc.put(entry.getKey(), Pattern.compile(stringVal, Pattern.CASE_INSENSITIVE));
				}
			}
		}

		generateGtQuery(doc);

		generateGteQuery(doc);

		generateLtQuery(doc);

		generateLteQuery(doc);

		generateNeQuery(doc);

		generateTimeQuery(doc);

		generateAndQuery(doc, likeFlag);

		generateOrQuery(doc, likeFlag);

		generateExistsQuery(doc);
		generateEqQuery(doc);

		logger.debug("处理后的查询语句：{}", doc.toJson());

		return doc;
	}

	/**
	 * 测试查询条件中是否包含_id，如果有则将其转换为ObjectId并设置在查询条件中，如果在转换和验证的过程中出错则直接抛异常
	 *
	 * @param doc
	 *            查询条件
	 */
	private static void testAndSetObjectId(Map<String, Object> doc) {
		Object maybeId = doc.remove("_id");
		if (maybeId != null) {
			if (maybeId instanceof String) {
				String idStr = (String) maybeId;
				if (!DocumentUtils.isValidObjectId(idStr)) {
					throw new ZhiZhiException( "_id格式错误");
				}

				doc.put("_id", new ObjectId(idStr));
			} else {
				throw new ZhiZhiException("类型错误");
			}
		}
	}

	/**
	 * 生成And条件的查询
	 * 
	 * @param query
	 * @return
	 * @throws ParseException
	 */
	private static Document generateAndQuery(Document query, boolean likeFlag) {

		// 显示地指定'and'关系
		Object andObj = query.remove("and");
		if (andObj != null && andObj instanceof List) {
			@SuppressWarnings("unchecked")
			List<Object> andQuery = (List<Object>) andObj;
			BasicDBList list = new BasicDBList();
			for (Object aqObj : andQuery) {
				if (!(aqObj instanceof Map)) {
					throw new ZhiZhiException( "参数格式错误",400001003);
				}

				@SuppressWarnings("unchecked")
				Map<String, Object> aq = (Map<String, Object>) aqObj;

				// 特殊处理_id，当查询中有_id时，要将其构造成ObjectId到数据库中查询
				testAndSetObjectId(aq);

				Set<Entry<String, Object>> entrySet = aq.entrySet();

				// 如果指定了模糊查询，则特殊处理值为字符串的查询条件，将其都变为模糊条件
				if (likeFlag) {
					for (Entry<String, Object> entry : entrySet) {
						if (entry.getValue() instanceof String) {
							String stringVal = (String) entry.getValue();
							// 如果字符串中包含正则表达式中特有的字符，如小括号，则会报错，所以要预先处理一下
							stringVal = ZhiZhiStringUtils.transformRegexCharacter(stringVal);
							aq.put(entry.getKey(), Pattern.compile(stringVal, Pattern.CASE_INSENSITIVE));
						}
					}
				}

				Document andDoc = new Document(aq);

				generateGtQuery(andDoc);
				generateGteQuery(andDoc);
				generateLtQuery(andDoc);
				generateLteQuery(andDoc);
				generateNeQuery(andDoc);

				generateTimeQuery(andDoc);

				generateAndQuery(andDoc, likeFlag);
				generateOrQuery(andDoc, likeFlag);

				generateExistsQuery(andDoc);
				generateEqQuery(andDoc);
				list.add(andDoc);

			}
			if (!list.isEmpty()) {
				query.put("$and", list);
			}
		}

		return query;
	}

	/**
	 * 生成Or条件的查询
	 * 
	 * @param query
	 * @return
	 * @throws ParseException
	 */
	private static Document generateOrQuery(Document query, boolean likeFlag) {
		// 添加‘或’条件查询
		Object orObj = query.remove("or");
		if (orObj != null && orObj instanceof List) {
			@SuppressWarnings("unchecked")
			List<Object> orQuery = (List<Object>) orObj;
			BasicDBList list = new BasicDBList();
			for (Object oqObj : orQuery) {
				if (!(oqObj instanceof Map)) {
					throw new ZhiZhiException( "参数格式错误",400001003);
				}

				@SuppressWarnings("unchecked")
				Map<String, Object> oq = (Map<String, Object>) oqObj;

				// 特殊处理_id，当查询中有_id时，要将其构造成ObjectId到数据库中查询
				testAndSetObjectId(oq);

				Set<Entry<String, Object>> entrySet = oq.entrySet();

				// 如果指定了模糊查询，则特殊处理值为字符串的查询条件，将其都变为模糊条件
				if (likeFlag) {
					for (Entry<String, Object> entry : entrySet) {
						if (entry.getValue() instanceof String) {
							String stringVal = (String) entry.getValue();
							// 如果字符串中包含正则表达式中特有的字符，如小括号，则会报错，所以要预先处理一下
							stringVal = ZhiZhiStringUtils.transformRegexCharacter(stringVal);
							oq.put(entry.getKey(), Pattern.compile(stringVal, Pattern.CASE_INSENSITIVE));
						}
					}
				}

				Document orDoc = new Document(oq);

				boolean conFeelType = orDoc.containsKey("feelType");
				boolean conChatType = orDoc.containsKey("chatType");

				// 兼容app查询
				convertApp(conFeelType, conChatType, orDoc);

				orDoc = generateGtQuery(orDoc);
				orDoc = generateGteQuery(orDoc);
				orDoc = generateLtQuery(orDoc);
				orDoc = generateLteQuery(orDoc);
				orDoc = generateNeQuery(orDoc);

				orDoc = generateTimeQuery(orDoc);
				orDoc = generateAndQuery(orDoc, likeFlag);
				orDoc = generateOrQuery(orDoc, likeFlag);
				orDoc = generateExistsQuery(orDoc);
				orDoc = generateEqQuery(orDoc);
				list.add(orDoc);
			}
			if (!list.isEmpty()) {
				query.put("$or", list);
			}
		}

		return query;
	}

	/**
	 * 生成exists条件的查询
	 * 
	 * @param query
	 * @return
	 */
	private static Document generateExistsQuery(Document query) {

		Object existsObj = query.remove("exists");
		if (existsObj != null && existsObj instanceof Map) {
			@SuppressWarnings("unchecked")
			Map<String, Object> exists = (Map<String, Object>) existsObj;
			for (Entry<String, Object> entry : exists.entrySet()) {
				query.put(entry.getKey(), new Document("$exists", entry.getValue()));
			}
		}
		return query;
	}

	/**
	 * 生成精确匹配
	 *
	 * @param query
	 * @return
	 */
	private static Document generateEqQuery(Document query) {

		Object existsObj = query.remove("eq");
		if (existsObj != null && existsObj instanceof Map) {
			@SuppressWarnings("unchecked")
			Map<String, Object> eq = (Map<String, Object>) existsObj;
			for (Entry<String, Object> entry : eq.entrySet()) {
				query.put(entry.getKey(), entry.getValue());
			}
		}
		return query;
	}

	/**
	 * 生成时间查询条件
	 * 
	 * @param query
	 * @return
	 * @throws ParseException
	 */
	private static Document generateTimeQuery(Document query) {
		// 创建日期
		putTimeQuery(query, "createTime");

		// 上次修改日期
		putTimeQuery(query, "lastModified");

		// 行程提交日期
		putTimeQuery(query, "submitTime");

		// 行程审批通过日期
		putTimeQuery(query, "approTime");

		putTimeQuery(query, "startDate");

		putTimeQuery(query, "endDate");

		// 服务创建时间
		putTimeQuery(query, "serviceCreateTime");
		// 服务上架时间
		putTimeQuery(query, "onShelfDate");

		// 订单成单时间
		putTimeQuery(query, "orderSuccessDate");
		// 订单服务开始时间
		putTimeQuery(query, "serviceStartDate");
		// 订单服务结束时间
		putTimeQuery(query, "serviceEndDate");

		return query;
	}

	private static void putTimeQuery(Document query, String timeField) {
		@SuppressWarnings("unchecked")
		Map<String, Object> timeMap = (Map<String, Object>) query.remove(timeField);
		if (timeMap != null) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Document aa = new Document();
			try {
				if (timeMap.get("start") != null) {
					Object startObj = timeMap.get("start");

					if (startObj instanceof Date) {
						aa.put("$gte", (Date) startObj);
					} else {
						aa.put("$gte", sdf.parse(startObj.toString()));
					}
				}
				if (timeMap.get("end") != null) {
					Object endObj = timeMap.get("end");
					if (endObj instanceof Date) {
						aa.put("$lte", (Date) endObj);
					} else {
						aa.put("$lte", sdf.parse(endObj.toString()));
					}
				}
			} catch (ParseException e) {
				throw new ZhiZhiException( "参数格式错误",400001003,e);
			}
			query.put(timeField, aa);
		}
	}

	/**
	 * 生成状态查询。由于有些需求在查询时不指定状态，则根据指定的默认状态查询。指定-1表示忽略状态条件。
	 * 
	 * @param query
	 *            Document
	 * @param defaultStatus
	 *            默认状态
	 * @return
	 */
	public static Document statusQuery(Document query, int defaultStatus) {
		Integer status = null;
		try {
			status = DocumentUtils.getIntValue(query, "status");
		} catch (Exception e) {
		}

		if (status == null && (!isContrainAttr(query, "status"))) {
			// 默认查询状态
			query.put("status", defaultStatus);
		}
		// 因为默认查询状态的存在，所以设置当查询status为-1时，表示查询所有状态，即忽略status条件
		else if (status != null && status == -1) {
			query.remove("status");
		}
		return query;
	}

	/**
	 * 递归判断查询条件中是否包含对应的参数
	 *
	 * @param query
	 *            queru
	 * @param attr
	 *            attr
	 * @return boolean
	 */
	@SuppressWarnings("unchecked")
	private static boolean isContrainAttr(Document query, String attr) {
		boolean isContrain = false;
		Set<String> keys = query.keySet();
		for (String key : keys) {
			if (key.equals(attr)) {
				isContrain = true;
				break;
			} else {
				if (query.get(key) instanceof List) {
					for (int i = 0; i < ((List) query.get(key)).size(); i++) {
						if (((List) query.get(key)).get(i) instanceof Map) {
							return isContrainAttr(new Document((Map<String, Object>) ((List) query.get(key)).get(i)),
									attr);
						} else {
							throw new ZhiZhiException("类型错误");
						}
					}
				}
			}
		}
		return isContrain;
	}

	/**
	 * 返回目的地查询条件。由于现在目的地结构已经成型，所以可以抽取出来。
	 * 
	 * @param queryDoc
	 *            带有目的地("destinations")的查询条件
	 * @return
	 */
	public static Document generateDestinationQuery(Document queryDoc) {
		Document destinationQuery = new Document();
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> destinations = queryDoc.get("destinations", List.class);
		queryDoc.remove("destinations");
		if (destinations != null) {
			List<String> countries = new ArrayList<>();
			List<String> regions = new ArrayList<>();
			List<String> cities = new ArrayList<>();

			List<String> orRegions = new ArrayList<>();
			List<String> orCities = new ArrayList<>();

			for (Map<String, Object> map : destinations) {
				switch (MapUtils.getInteger(map, "type")) {
				case 1:
					countries.add((String) map.get("value"));
					break;
				case 2:
					regions.add((String) map.get("value"));
					break;
				case 3:
					cities.add((String) map.get("value"));
					break;
				case 4:
					orRegions.add((String) map.get("value"));
					orCities.add((String) map.get("value"));
					break;
				}
			}

			if (!countries.isEmpty()) {
				destinationQuery.put("countryName", new Document("$in", countries));
			}
			if (!regions.isEmpty()) {
				destinationQuery.put("regional", new Document("$in", regions));
			}
			if (!cities.isEmpty()) {
				destinationQuery.put("cityName", new Document("$in", cities));
			}
			if (!orRegions.isEmpty()) {
				List<Document> both = new ArrayList<>();
				both.add(new Document("regions", new Document("$in", orRegions)));
				both.add(new Document("cities", new Document("$in", orCities)));
				destinationQuery.put("$or", both);
			}
		}
		return destinationQuery;
	}

	// /**
	// * 判断指定对象是否是数值型（byte,short, int,long,float,double）
	// *
	// * @param value
	// * @return
	// */
	// private static boolean isNumericValue(Object value) {
	// return value instanceof Number;
	// }

	/**
	 * 生成gt(大于)条件的查询
	 * 
	 * @param query
	 * @return
	 * @throws ParseException
	 */
	private static Document generateGtQuery(Document query) {

		Object gtObj = query.remove("gt");
		if (gtObj != null && gtObj instanceof Map) {
			@SuppressWarnings("unchecked")
			Map<String, Object> gt = (Map<String, Object>) gtObj;
			if (gt.size() == 1) {
				for (Entry<String, Object> entry : gt.entrySet()) {
					// if (isNumericValue(entry.getValue())) {
					if (query.containsKey(entry.getKey())) {
						query.get(entry.getKey(), Document.class).put("$gt", entry.getValue());
					} else {
						query.put(entry.getKey(), new Document("$gt", entry.getValue()));
					}
				}
				// }
			}
		}
		return query;
	}

	/**
	 * 生成gte(大于等于)条件的查询
	 * 
	 * @param query
	 * @return
	 * @throws ParseException
	 */
	private static Document generateGteQuery(Document query) {
		Object gtObj = query.remove("gte");
		if (gtObj != null && gtObj instanceof Map) {
			@SuppressWarnings("unchecked")
			Map<String, Object> gt = (Map<String, Object>) gtObj;
			if (gt.size() == 1) {
				for (Entry<String, Object> entry : gt.entrySet()) {
					// if (isNumericValue(entry.getValue())) {
					// query.put(entry.getKey(), new Document("$gte",
					// entry.getValue()));

					if (query.containsKey(entry.getKey())) {
						query.get(entry.getKey(), Document.class).put("$gte", entry.getValue());
					} else {
						query.put(entry.getKey(), new Document("$gte", entry.getValue()));
					}

					// }
				}
			}
		}
		return query;
	}

	/**
	 * 生成lt(小于)条件的查询
	 * 
	 * @param query
	 * @return
	 * @throws ParseException
	 */
	private static Document generateLtQuery(Document query) {
		Object gtObj = query.remove("lt");
		if (gtObj != null && gtObj instanceof Map) {
			@SuppressWarnings("unchecked")
			Map<String, Object> gt = (Map<String, Object>) gtObj;
			if (gt.size() == 1) {
				for (Entry<String, Object> entry : gt.entrySet()) {
					// if (isNumericValue(entry.getValue())) {
					// query.put(entry.getKey(), new Document("$lt",
					// entry.getValue()));
					if (query.containsKey(entry.getKey())) {
						query.get(entry.getKey(), Document.class).put("$lt", entry.getValue());
					} else {
						query.put(entry.getKey(), new Document("$lt", entry.getValue()));
					}
					// }
				}
			}
		}
		return query;
	}

	/**
	 * 生成lte(小于等于)条件的查询
	 * 
	 * @param query
	 * @return
	 * @throws ParseException
	 */
	private static Document generateLteQuery(Document query) {
		Object gtObj = query.remove("lte");
		if (gtObj != null && gtObj instanceof Map) {
			@SuppressWarnings("unchecked")
			Map<String, Object> gt = (Map<String, Object>) gtObj;
			if (gt.size() == 1) {
				for (Entry<String, Object> entry : gt.entrySet()) {
					// if (isNumericValue(entry.getValue())) {
					// query.put(entry.getKey(), new Document("$lte",
					// entry.getValue()));
					if (query.containsKey(entry.getKey())) {
						query.get(entry.getKey(), Document.class).put("$lte", entry.getValue());
					} else {
						query.put(entry.getKey(), new Document("$lte", entry.getValue()));
					}
					// }
				}
			}
		}
		return query;
	}

	/**
	 * 生成ne(不等于)条件的查询
	 * 
	 * @param query
	 * @return
	 * @throws ParseException
	 */
	private static Document generateNeQuery(Document query) {
		Object gtObj = query.remove("ne");
		if (gtObj != null && gtObj instanceof Map) {
			@SuppressWarnings("unchecked")
			Map<String, Object> gt = (Map<String, Object>) gtObj;
			if (gt.size() >= 1) {
				for (Entry<String, Object> entry : gt.entrySet()) {
					query.put(entry.getKey(), new Document("$ne", entry.getValue()));
				}
			}
		}
		return query;
	}

	/**
	 * <p>
	 * 对于查询，要限制返回字段信息，所以对查询时指定的projection需要过滤，处理完成后最多返回allowField中指定的字段
	 * </p>
	 * 如果查询同时指定了0和1，则只处理1,忽略0 若处理完后，若projection变空了，则返回allowField指定的字段
	 * 
	 * @param allowedFields
	 *            默认允许返回的字段，形如：{"field1":1,"field2":1}
	 * @param projection
	 *            处理前的projection,形如：{"field1":1,"field2":1}，或
	 *            {"field1":0,"field2":0}
	 * @return 过滤后的projection
	 */
	public static Document getFilteredProjection(Map<String, Object> allowedFields, Map<String, Object> projection) {

		if (projection == null || projection.size() == 0) {
			return new Document(allowedFields);
		}

		// 遍历projection
		Set<Entry<String, Object>> entrySet = projection.entrySet();
		Iterator<Entry<String, Object>> it = entrySet.iterator();

		// 如果同时指定1和0 ，只保留1
		if (projection.containsValue(1) || projection.containsValue(1.0)) {
			while (it.hasNext()) {
				Entry<String, Object> entry = it.next();
				Object value = entry.getValue();
				if (!(value instanceof Number) || ((Number) value).intValue() != 1) {
					it.remove();
					continue;
				}

				if (!allowedFields.containsKey(entry.getKey())) {
					it.remove();
				}
			}
		} else {
			// 否则指定的字段都不返回，但最多也只返回allowedFields
			Map<String, Object> fieldsToFilter = new HashMap<>(allowedFields);
			while (it.hasNext()) {
				Entry<String, Object> entry = it.next();
				fieldsToFilter.remove(entry.getKey());
			}
			projection = fieldsToFilter;
		}

		// 若projection为空，会导致返回所有字段，所以特意指定只返回_id，虽然_id总是会返回
		if (projection.size() == 0) {
			projection.put("_id", 1);
		}
		return new Document(projection);
	}

	/**
	 * chatType 查询范围扩大 恶心app查询转换
	 * 
	 * @param a
	 *            chatType
	 * @param b
	 *            feelType
	 * @param orDoc
	 *            orDoc
	 */
	private static void convertApp(boolean a, boolean b, Document orDoc) {
		if (a && b) {
			String chatType = orDoc.get("chatType").toString();
			String feelType = orDoc.get("feelType").toString();
			List<Document> orMap = new ArrayList<>();
			orMap.add(new Document("chatType", chatType));
			orMap.add(new Document("feelType", feelType));
			orMap.add(new Document("_theme", chatType));
			orMap.add(new Document("frontCategory", chatType));
			orDoc.put("$or", orMap);
			orDoc.remove("chatType");
			orDoc.remove("feelType");
			return;
		}
		if (a) {
			String feelType = orDoc.get("feelType").toString();
			List<Document> orMap = new ArrayList<>();
			orMap.add(new Document("chatType", feelType));
			orMap.add(new Document("_theme", feelType));
			orMap.add(new Document("frontCategory", feelType));
			orDoc.put("$or", orMap);
			orDoc.remove("feelType");
			return;
		}
		if (b) {
			String chatType = orDoc.get("chatType").toString();
			List<Document> orMap = new ArrayList<>();
			orMap.add(new Document("chatType", chatType));
			orMap.add(new Document("_theme", chatType));
			orMap.add(new Document("frontCategory", chatType));
			orDoc.put("$or", orMap);
			orDoc.remove("chatType");
			return;
		}
	}

	/**
	 * 字符串转Map
	 * 
	 * @param jsonStr
	 *            json字符串
	 * @param strict
	 *            true时当json字符串有错是会抛异常，false时会忽略错误并返回一个空的Map
	 * @return
	 */
	public static Map<String, Object> getMapFromStr(String jsonStr, boolean strict) {
		if (StringUtils.isEmpty(jsonStr)) {
			return new HashMap<>();
		}
		Type stringMapType = new TypeToken<Map<String, Object>>() {
		}.getType();

		if (strict) {
			try {
				return new Gson().fromJson(jsonStr, stringMapType);
			} catch (Exception e) {
				throw new ZhiZhiException(""+e);
			}
		}

		Map<String, Object> doc = null;
		try {
			doc = new Gson().fromJson(jsonStr, stringMapType);
		} catch (Exception e) {
		}
		if (doc == null) {
			doc = new HashMap<>();
		}
		return doc;
	}

	/**
	 * @param jsonStr
	 * @return
	 * @author
	 */
	public static Map<String, Object> getMapFromStr(String jsonStr) {
		return getMapFromStr(jsonStr, false);
	}
}
