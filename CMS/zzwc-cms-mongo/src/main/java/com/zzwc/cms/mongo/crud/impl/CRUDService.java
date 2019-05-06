package com.zzwc.cms.mongo.crud.impl;

import com.google.gson.Gson;
import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.MongoWriteException;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;
import com.zzwc.cms.common.bean.Page;
import com.zzwc.cms.common.exception.ZhiZhiException;
import com.zzwc.cms.common.utils.ExceptionUtils;
import com.zzwc.cms.common.utils.ZhiZhiStringUtils;
import com.zzwc.cms.mongo.crud.CRUD;
import com.zzwc.cms.mongo.crud.ItemProcessor;
import com.zzwc.cms.mongo.crud.QueryParser;
import com.zzwc.cms.mongo.utils.DocumentUtils;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * CRUD工具类，查询解析使用 {@link MongoQueryParser}
 * 
 * @author
 *
 */
public class CRUDService implements CRUD<String> {

	// 数据库链接
	private MongoClient mongo;

	// 数据库名称
	private String dbName;

	// 操作的集合名称
	private String collectionName;

	private Logger logger = LoggerFactory.getLogger(CRUDService.class);

	private QueryParser queryParser = new MongoQueryParser();

	public CRUDService(MongoClient mongo, String dbName, String collectionName) {

		super();
		Assert.notNull(mongo, "mongodbClient不可为空");
		Assert.notNull(dbName, "数据库名称不可为空");
		Assert.notNull(collectionName, "集合名称不可为空");

		this.mongo = mongo;
		this.dbName = dbName;
		this.collectionName = collectionName;
	}

	public QueryParser getQueryParser() {
		return this.queryParser;
	}

	@Override
	public MongoCollection<Document> getCollection() {
		return mongo.getDatabase(getDbName()).getCollection(getCollectionName());
	}

	public String getDbName() {
		return dbName;
	}

	public String getCollectionName() {
		return collectionName;
	}

	// ------------------------------------------------------------------------------------------------------

	@Override
	public List<Document> findList(Map<String, Object> query, Map<String, Object> projection, Map<String, Object> sort,
			Integer skip, Integer limit, List<ItemProcessor> itemProcessors) {

		if (query == null) {
			throw new NullPointerException("查询条件不可为空");
		}
		Gson gson = new Gson();
		logger.debug("客户端查询参数：query={}，projection={}，sort={}，skip={}，limit={}", gson.toJson(query),
				gson.toJson(projection), gson.toJson(sort), skip, limit);
		
		FindIterable<Document> cursor = findCursor(query, skip, limit, projection, sort);
		List<Document> list = new ArrayList<>();

		if (itemProcessors != null && !itemProcessors.isEmpty()) {
			cursor.forEach(new Block<Document>() {
				@Override
				public void apply(Document t) {
					t = applyProcessor(itemProcessors, t);
					list.add(t);
				}
			});
		} else {
			cursor.into(list);
		}

		return list;
	}

	/**
	 * 应用数据处理器
	 * 
	 * @param itemProcessors
	 * @param t
	 * @return
	 */
	private Document applyProcessor(List<ItemProcessor> itemProcessors, Document t) {
		if (itemProcessors != null && itemProcessors.size() > 0) {
			for (ItemProcessor p : itemProcessors) {
				t = p.process(t);
			}
		}
		return t;
	}

	private FindIterable<Document> findCursor(Map<String, Object> query, Integer skip, Integer limit,
			Map<String, Object> projection, Map<String, Object> sort) {

		Document finalQuery = queryParser.parse(query);
		// 默认只返回isAvailable=true的
		if (!finalQuery.containsKey("isAvailable")) {
			finalQuery.put("isAvailable", true);
		}

		Document projectionDoc = new Document();
		if (projection != null && projection.size() > 0) {
			// projection中只可都为1或者都为0
			Set<Integer> pvSet = new HashSet<>();
			projection.entrySet().stream().forEach(entry -> {
				Object value = entry.getValue();
				Number numberValue = null;
				if (value instanceof Number) {
					numberValue = (Number) value;
				} else if (value instanceof String) {
					try {
						final String text = (String) value;
						numberValue = NumberFormat.getInstance().parse(text);
					} catch (final ParseException e) {
					}
				} else {
					throw new ZhiZhiException( "投影字段格式错误");
				}

				if (numberValue == null) {
					throw new ZhiZhiException(
							"投影字段值 " + value + " 非数字且不可转为数字");
				}

				int pv = numberValue.intValue();
				if (pv != 0 && pv != 1) {
					throw new ZhiZhiException("投影字段值" + pv + "不允许");
				}

				pvSet.add(pv);
				if (pvSet.size() > 1) {
					throw new ZhiZhiException( "投影字段值不一致");
				}

				projectionDoc.put(entry.getKey(), pv);
			});
		}

		Document sortDoc = new Document();
		if (sort == null) {
			sortDoc.append("createTime", -1);
		} else if (sort.size() == 0) {
			sortDoc.put("createTime", -1);
		} else {
			// sort中只可为1和-1
			sort.entrySet().stream().forEach(entry -> {
				Object value = entry.getValue();
				Number numberValue = null;
				if (value instanceof Number) {
					numberValue = (Number) value;
				} else if (value instanceof String) {
					try {
						final String text = (String) value;
						numberValue = NumberFormat.getInstance().parse(text);
					} catch (final ParseException e) {
					}
				} else {
					throw new ZhiZhiException( "排序格式错误");
				}
				if (numberValue == null) {
					throw new ZhiZhiException(
							"排序字段值 " + value + " 非数字且不可转为数字");
				}
				int sortDir = numberValue.intValue();
				if (sortDir != -1 && sortDir != 1) {
					throw new ZhiZhiException( "排序方向 " + sortDir + " 错误");
				}
				sortDoc.put(entry.getKey(), sortDir);
			});
		}

		skip = skip == null ? 0 : skip;
		skip = skip < 0 ? 0 : skip;

		limit = limit == null ? 0 : limit;
		limit = limit < 0 ? 0 : limit;

		logger.debug("解析后的查询参数：query={}，projection={}，sort={}，skip={}，limit={}", finalQuery.toJson(),
				projectionDoc.toJson(), sortDoc.toJson(), skip, limit);

		return getCollection().find(finalQuery).projection(projectionDoc).sort(sortDoc).skip(skip).limit(limit);
	}

	@Override
	public Document findOne(Map<String, Object> query, Map<String, Object> projection, Map<String, Object> sort,
			List<ItemProcessor> itemProcessors) {
		Document firstDoc = findCursor(query, null, null, projection, null).first();

		if (firstDoc != null) {
			firstDoc = applyProcessor(itemProcessors, firstDoc);
		}

		return firstDoc;

	}

	@Override
	public Document findById(String id, Map<String, Object> projection, List<ItemProcessor> itemProcessors) {
		Map<String, Object> query = new HashMap<>();
		query.put("_id", id);
		return findOne(query, projection, null, itemProcessors);

	}

	@Override
	public long findCount(Map<String, Object> query) {
		Document queryDoc = queryParser.parse(query);
		if (!queryDoc.containsKey("isAvailable")) {
			queryDoc.put("isAvailable", true);
		}
		logger.debug("解析后的查询参数：" + queryDoc.toJson());
		return getCollection().count(queryDoc);
	}

	@Override
	public long findCountNative(Map<String, Object> query) {
		return getCollection().count(new Document(query));
	}

	@Override
	public Page<Document> findPage(Map<String, Object> query, Map<String, Object> projection, Map<String, Object> sort,
								   Integer pageNume, Integer pageSize, List<ItemProcessor> itemProcessors) {

		long totalCount = findCount(query);

		int totalPage = (int)Math.ceil((double)totalCount/pageSize);

		List<Document> rows = null;
		if (totalCount > 0) {
			pageSize = pageSize == null ? 0 : pageSize;
			pageSize = pageSize <= 0 ? 20 : pageSize;
			rows = findList(query, projection, sort, pageNume * pageSize, pageSize, itemProcessors);
		} else {
			rows = new ArrayList<>();
			logger.debug("该分页条件对应的记录数为0，无需再次进行详细查询，直接返回空集合");
		}

		return new Page<>(totalCount, rows,pageNume,totalPage,pageSize);
	}

	@Override
	public String insert(Document dbObj) {
		if (dbObj == null) {
			throw new ZhiZhiException( "入库数据不可为 null");
		}
		// 数据库标识使用数据库的ObjectId，且不允许客户端指定
		dbObj.remove("_id");
		if (dbObj.size() == 0) {
			throw new ZhiZhiException( "入库数据不可为空");
		}

		DocumentUtils.trimValue(dbObj);

		// 任何新建对象都有一个创建时间和上次修改时间，且两者相同
		Date date = new Date();
		dbObj.put("createTime", date);
		dbObj.put("lastModified", date);

		// 对象可用标识，若为指定则默认为true
		if (DocumentUtils.getBoolean(dbObj, "isAvailable", true)) {
			dbObj.put("isAvailable", true);
		}

		// 手工设置id，数据库驱动程序中也是这样设的，如果_id不存在时，创建一个ObjectId
		ObjectId dbId = new ObjectId();
		dbObj.put("_id", dbId);
		String id = dbId.toString();

		dbObj.put("shortId", ZhiZhiStringUtils.shortText(id));
		try {
			getCollection().insertOne(dbObj);
		} catch (Exception e) {
			throw new ZhiZhiException( "插入数据失败，集合：" + collectionName + "，数据："
					+ dbObj.toJson() + " 异常信息：" + ExceptionUtils.getExceptionStack(e));
		}

		logger.info("成功向集合【{}】中添加记录：{}", collectionName, id);
		return id;
	}

	public void insert(List<Document> batch) {
		if (batch == null || batch.isEmpty()) {
			throw new ZhiZhiException( "入库数据不可为 空");
		}
		Date date = new Date();

		List<Document> batchList = batch.stream().filter(d -> !d.isEmpty()).map(d -> {
			d.remove("_id");
			d.put("createTime", date);
			d.put("lastModified", date);
			if (DocumentUtils.getBoolean(d, "isAvailable", true)) {
				d.put("isAvailable", true);
			} else {
				d.put("isAvailable", false);
			}
			return d;
		}).collect(Collectors.toList());

		getCollection().insertMany(batchList);
	}

	@Override
	public long deleteById(String id) {
		return updateUseSet(id, new Document("isAvailable", false));
	}

	@Override
	public long updateUseSet(String id, Map<String, Object> obj) {
		obj.remove("_id");
		obj.remove("createTime");
		obj.remove("lastModified");
		return update(id, new Document("$set", obj));
	}

	@Override
	public long update(String id, Map<String, Object> obj) {
		Map<String, Object> query = new HashMap<>();
		query.put("_id", id);
		return update(query, obj, false);
	}

	@Override
	public long update(Map<String, Object> query, Map<String, Object> obj, boolean multi) {
		return updateNative(queryParser.parse(query), new Document(obj), multi);
	}

	/**
	 * 当前支持的修改操作符
	 */
	private static final List<String> SUPPORTED_UPDATE_OPERATORS = Arrays.asList(
			// 字段相关
			"$inc", "$mul", "$set", "$unset",
			// 数组相关
			"$addToSet", "$pop", "$pullAll", "$pull", "$pushAll", "$push");

	@Override
	public long updateNative(Document query, Document obj, boolean multi) {

		if (query == null || obj == null || obj.size() == 0) {
			throw new ZhiZhiException( "更新条件、更新内容不可为空");
		}

		DocumentUtils.trimValue(obj);

		if (query.get("isAvailable") == null) {
			query.put("isAvailable", true);
		}

		// 验证操作符
		Set<String> setToTest = new HashSet<>(obj.keySet());
		setToTest.retainAll(SUPPORTED_UPDATE_OPERATORS);
		if (setToTest.isEmpty()) {
			throw new ZhiZhiException( "更新内容中不包含支持的操作符");
		}

		obj.put("$currentDate", new Document("lastModified", true));

		logger.debug("更新集合 {} 条件： {} ，更新内容：{}", collectionName, query.toJson(), obj.toJson());

		try {
			if (multi) {
				return getCollection().updateMany(query, obj).getModifiedCount();
			}
			return getCollection().updateOne(query, obj).getModifiedCount();

		} catch ( MongoWriteException e) {
			throw new ZhiZhiException( "更新操作失败"+e.getMessage(),e);
		} catch (Exception ie) {
			throw ie;
		}

	}

	@Override
	public Document findById(String id) {
		return findById(id, null, null);
	}

	@Override
	public Document findById(String id, Map<String, Object> projection) {
		return findById(id, projection, null);
	}

	@Override
	public Document findAndModify(Map<String, Object> query, Map<String, Object> update, Map<String, Object> projection,
			Map<String, Object> sort, boolean after) {

		FindOneAndUpdateOptions options = new FindOneAndUpdateOptions();
		if (after) {
			options.returnDocument(ReturnDocument.AFTER);
		}
		if (sort != null) {
			options.sort(new Document(sort));
		}
		if (projection != null) {
			options.projection(new Document(projection));
		}

		return getCollection().findOneAndUpdate(queryParser.parse(query), new Document(update), options);

	}
}
