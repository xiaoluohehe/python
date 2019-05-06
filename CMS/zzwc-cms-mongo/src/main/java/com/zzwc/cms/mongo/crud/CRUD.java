
package com.zzwc.cms.mongo.crud;

import com.mongodb.client.MongoCollection;
import com.zzwc.cms.common.bean.Page;
import org.bson.Document;

import java.util.List;
import java.util.Map;

/**
 * 
 * @author
 *
 * @param <T>
 */
public interface CRUD<T> {

	/**
	 * 获取当前数据集合
	 * 
	 * @return
	 * @author
	 */
	MongoCollection<Document> getCollection();

	/**
	 * 插入单条数据
	 * 
	 * @param dbObj
	 * @return
	 * @author
	 */
	String insert(Document dbObj);

	/**
	 * 查询列表
	 * 
	 * @param query
	 *            查询条件，需经过{@link QueryParser}解析
	 * @param projection
	 *            投影
	 * @param sort
	 *            排序
	 * @param skip
	 *            跳过记录
	 * @param limit
	 *            限制返回记录
	 * @param itemProcessors
	 *            列表单项处理逻辑，可为null
	 * @return 记录列表，当记录数为0时，列表长度为0
	 * @author
	 */
	List<Document> findList(Map<String, Object> query, Map<String, Object> projection, Map<String, Object> sort,
                            Integer skip, Integer limit, List<ItemProcessor> itemProcessors);

	/**
	 * without itemProcessors
	 * @param query
	 * @param projection
	 * @param sort
	 * @param skip
	 * @param limit
	 * @return
	 */
	default List<Document> findList(Map<String, Object> query, Map<String, Object> projection, Map<String, Object> sort,
                                    Integer skip, Integer limit){
		return findList(query, projection, sort, skip, limit, null);
	}

	/**
	 * 查询单个记录，即使query对应的条件有多条记录，只返回按照sort排序后的第一条
	 *
	 * @param query
	 *            查询条件，需经过{@link QueryParser}解析
	 * @param projection
	 *            投影
	 * @param sort
	 *            排序
	 * @param itemProcessors
	 *            列表单项处理逻辑，可为null
	 * @return 记录，不存在时为Null
	 * @author
	 */
	Document findOne(Map<String, Object> query, Map<String, Object> projection, Map<String, Object> sort,
                     List<ItemProcessor> itemProcessors);

	default Document findOne(Map<String, Object> query){
		return findOne(query, null, null, null);
	}

	default Document findOne(Map<String, Object> query, Map<String, Object> projection){
		return findOne(query, projection, null, null);
	}

	/**
	 * 根据id查询记录
	 *
	 * @param id
	 *            记录id
	 * @param projection
	 *            投影
	 * @param itemProcessors
	 *            列表单项处理逻辑，可为null
	 * @return 记录，不存在时为Null
	 * @author
	 */
	Document findById(String id, Map<String, Object> projection, List<ItemProcessor> itemProcessors);

	/**
	 * 查询符合条件的数量
	 *
	 * @param query
	 *            查询条件，需经过{@link QueryParser}解析
	 * @return
	 * @author
	 */
	long findCount(Map<String, Object> query);

	/**
	 * 查询符合条件的数量
	 *
	 * @param query
	 *            查询条件，无需解析，mongo可直接执行的查询语句
	 * @return
	 * @author
	 */
	long findCountNative(Map<String, Object> query);

	/**
	 * 分页查询，传入的query条件会经过{@link QueryParser}解析
	 *
	 * @param query
	 *            查询条件，需经过{@link QueryParser}解析
	 * @param projection
	 *            投影
	 * @param sort
	 *            排序
	 * @param pageNume
	 *            查询页数，从0开始
	 * @param pageSize
	 *            页面大小
	 * @param itemProcessors
	 * @return
	 * @author
	 */
	Page<Document> findPage(Map<String, Object> query, Map<String, Object> projection, Map<String, Object> sort,
                            Integer pageNume, Integer pageSize, List<ItemProcessor> itemProcessors);

	/**
	 * remove itemProcessors
	 * @param query
	 * @param projection
	 * @param sort
	 * @param pageNume
	 * @param pageSize
	 * @return
	 */
	default Page<Document> findPage(Map<String, Object> query, Map<String, Object> projection, Map<String, Object> sort,
                                    Integer pageNume, Integer pageSize){
		return findPage(query, projection, sort, pageNume, pageSize, null);
	}

	/**
	 * 假删，根据ID删除对象
	 *
	 * @param id
	 *            ID
	 * @return 受影响的记录数
	 */
	long deleteById(T id);

	/**
	 * 根据 _id 使用$set做更新，更常用
	 *
	 * @param id
	 * @param obj
	 *            $set的值
	 * @return
	 * @author
	 */
	long updateUseSet(T id, Map<String, Object> obj);

	/**
	 * 根据 _id 做更新
	 *
	 * @param id
	 * @param obj
	 *            更新的内容，是否使用操作符、使用哪个操作符请按具体情况而定
	 * @return
	 * @author
	 */
	long update(T id, Map<String, Object> obj);

	/**
	 * 根据条件更新文档，条件会经过QueryParser解析
	 *
	 * @param query
	 *            更新条件，会经过 {@link QueryParser} 解析
	 * @param obj
	 *            更新内容，是否使用操作符、使用哪个操作符请按具体情况而定
	 * @param multi
	 *            是否更新多个
	 * @return
	 * @author
	 */
	long update(Map<String, Object> query, Map<String, Object> obj, boolean multi);

	/**
	 * 根据条件更新文档，条件需为mongo可执行的查询语句
	 *
	 * @param query
	 *            更新条件，无需解析，mongo可直接执行的查询语句
	 * @param obj
	 *            更新内容，为防止整体数据覆盖，必须使用操作符，参考 <a href=
	 *            'https://docs.mongodb.com/master/reference/operator/update-field/'>
	 *            mongodb修改操作符 </a>
	 * @param multi
	 *            是否更新多个
	 * @return
	 * @author
	 */
	long updateNative(Document query, Document obj, boolean multi);

	// -------------------------------- 新增老接口 ------------------------
	Document findById(String id);

	Document findById(String id, Map<String, Object> projection);

	/**
	 * 查询并更新记录，原子操作；
	 *
	 * @param query
	 *            查询条件
	 * @param update
	 *            更新的内容
	 * @param projection
	 *            字段投影
	 * @param sort
	 *            排序
	 * @param after
	 *            true返回修改后的记录，false返回修改前的记录
	 * @return 符合条件的一条记录；当query对应多条记录，则根据sort排序，更新并返回第一条
	 * @author
	 */
	Document findAndModify(Map<String, Object> query, Map<String, Object> update, Map<String, Object> projection,
                           Map<String, Object> sort, boolean after);



}
