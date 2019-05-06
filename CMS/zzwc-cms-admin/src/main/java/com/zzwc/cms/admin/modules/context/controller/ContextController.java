package com.zzwc.cms.admin.modules.context.controller;

import com.zzwc.cms.admin.modules.context.service.IContextService;
import com.zzwc.cms.common.utils.ExceptionUtils;
import com.zzwc.cms.common.utils.R;
import com.zzwc.cms.mongo.utils.DocumentUtils;
import com.zzwc.cms.mongo.utils.QueryUtils;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ContextController {

    private Logger logger = LoggerFactory.getLogger(ContextController.class);

    @Autowired
    private IContextService contextService;

    //数据插入
    @RequestMapping(value = "/sys/context", method = RequestMethod.POST)
    public Map<String, Object> create(@RequestBody Map<String, Object> comment, HttpServletRequest request,
                                      HttpServletResponse response) {
        try {
            String id = contextService.insert(new Document(comment));
            return R.ok("context with id '" + id + "' has been created!").put("id", id);
        } catch (Exception e) {
            logger.error(ExceptionUtils.getExceptionStack(e));
            //抛出500错误
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return R.error(e.getMessage());
        }
    }

    //获取他的id
    @RequestMapping(value = "/sys/context/{id}", method = RequestMethod.GET)
    public Object findById(HttpServletRequest request, HttpServletResponse response, @PathVariable("id") String id,
                           @RequestParam(required = false, defaultValue = "{}") String projection) {
        Map<String, Object> map = new HashMap<>();

        if (!DocumentUtils.isValidObjectId(id)) {
            return R.error("id 格式不对");
        }
        //hash格式
        Document comment = null;

        try {
            comment = contextService.findOne(new Document("_id", new ObjectId(id)),
                    QueryUtils.getDocumentFromStr(projection), null, null);
            if (comment != null) {
                return R.ok().put("data", comment);
            } else {
                return R.error("产品【" + id + "】不存在");
            }
        } catch (Exception e) {
            logger.error(ExceptionUtils.getExceptionStack(e));
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return R.error(e.getMessage());
        }


    }

    //修改
    @RequestMapping(value = "/sys/context/{id}", method = RequestMethod.PUT)
    public Map<String, Object> update(HttpServletRequest request, HttpServletResponse response,
                                      @RequestBody Map<String, Object> activity, @PathVariable("id") String id) {
        if (!DocumentUtils.isValidObjectId(id)) {
            return R.error("incorrect id format");
        }
        try {
            long result = contextService.updateUseSet(id, new Document(activity));
            return R.ok(result + " items has been updated");
        } catch (Exception e) {
            logger.error(ExceptionUtils.getExceptionStack(e));
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return R.error(e.getMessage());
        }
    }


    //删除byid
    @RequestMapping(value = "/sys/context/{id}", method = RequestMethod.DELETE)
    public Map<String, Object> deleteById(HttpServletRequest request, HttpServletResponse response, ModelAndView model,
                                          @PathVariable("id") String id) {
        if (!DocumentUtils.isValidObjectId(id)) {
            return R.error("delete is error");
        }
        try {
            long result = contextService.deleteById(id);
            return R.ok(result + " items has been delete");
        } catch (Exception e) {
            logger.error(ExceptionUtils.getExceptionStack(e));
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); //500错误
            return R.error(e.getMessage());
        }
    }

    //updateUseSet

    /**
     * 查询
     */

    //分页查询
    @RequestMapping(value = "/sys/context", method = RequestMethod.GET)
    public Object queryPage(HttpServletRequest request, HttpServletResponse response,
                            @RequestParam(required = false, defaultValue = "{}") String query,
                            @RequestParam(required = false, defaultValue = "false") boolean pageAble,
                            @RequestParam(required = false, defaultValue = "0") int pageNum,
                            @RequestParam(required = false, defaultValue = "20") int size,
                            @RequestParam(required = false, defaultValue = "{}") String projection,
                            @RequestParam(required = false, defaultValue = "{}") String sort) {
        try {
            if (pageAble) {
                return R.ok().put("data", contextService.findPage(QueryUtils.getDocumentFromStr(query),
                        QueryUtils.getDocumentFromStr(projection), QueryUtils.getDocumentFromStr(sort), pageNum - 1, size,
                        null));
            } else {
                return R.ok().put("data", contextService.findList(QueryUtils.getDocumentFromStr(query),
                        QueryUtils.getDocumentFromStr(projection), QueryUtils.getDocumentFromStr(sort), null, null, null));
            }

        } catch (Exception e) {
            logger.error(ExceptionUtils.getExceptionStack(e));
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return R.error(e.getMessage());
        }
    }



    //查询符合的条数
   /* public Object findCount(
            @RequestParam(required = false, defaultValue = "0") Map<String, Object> query, HttpServletResponse response) {
        try {
            return R.ok().put("成功", contextService.findCount(query));
        }catch (Exception e){
            logger.error(ExceptionUtils.getExceptionStack(e));
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return R.error(e.getMessage());
        }
    }*/

    //查询符合的条数,查询符合条件的数量
    /*public Object findCountNative(
            @RequestParam(required = false, defaultValue = "0") Map<String, Object> query, HttpServletResponse response) {

        try {
            return R.ok().put("成功", contextService.findCountNative(query));
        }catch (Exception e){
            logger.error(ExceptionUtils.getExceptionStack(e));
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return R.error(e.getMessage());
        }
    }*/


   /* public Object updateNative(
            @RequestParam(required = false, defaultValue = "{}") Document query,
            @RequestParam(required = false, defaultValue = "{}") Document obj,
            @RequestParam(required = false, defaultValue = "false") boolean multi, HttpServletResponse response){
        try {
            return R.ok().put("成功", contextService.updateNative(query, obj, multi));
        }catch (Exception e){
            logger.error(ExceptionUtils.getExceptionStack(e));
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return R.error(e.getMessage());
        }

    }*/

    public Map<String, Object> findAndModify(HttpServletResponse response,
            @RequestParam(required = false, defaultValue = "{}") Map<String, Object> query,
            @RequestParam(required = false, defaultValue = "{}") Map<String, Object> update,
            @RequestParam(required = false, defaultValue = "{}") Map<String, Object> projection,
            @RequestParam(required = false, defaultValue = "{}") Map<String, Object> sort,
            @RequestParam(required = false, defaultValue = "false")  boolean after) {

        try {
            return R.ok().put("data" , contextService.findAndModify(query,update,projection,sort,after));
        }catch (Exception e){
            logger.error(ExceptionUtils.getExceptionStack(e));
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return R.error(e.getMessage());
        }
    }
}
