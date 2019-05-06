package com.zzwc.cms.admin.modules.product.controller;

import com.zzwc.cms.admin.modules.product.service.IProductService;
import com.zzwc.cms.common.utils.ExceptionUtils;
import com.zzwc.cms.common.utils.R;
import com.zzwc.cms.mongo.utils.DocumentUtils;
import com.zzwc.cms.mongo.utils.QueryUtils;
import org.apache.commons.lang3.StringUtils;
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
public class ProductController {

    private Logger logger = LoggerFactory.getLogger(ProductController.class);

    @Autowired
    private IProductService productService;

    @RequestMapping(value = "/sys/product", method = RequestMethod.POST)
    public Map<String, Object> create(@RequestBody Map<String, Object> comment, HttpServletRequest request,
                                      HttpServletResponse response) {
        try {
            String id = productService.insert(new Document(comment));
            return R.ok("product with id '" + id + "' has been created!").put("id", id);
        } catch (Exception e) {
            logger.error(ExceptionUtils.getExceptionStack(e));
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return R.error(e.getMessage());
        }
    }

    //获取他的id
    @RequestMapping(value = "/sys/product/{id}", method = RequestMethod.GET)
    public Object findById(HttpServletRequest request, HttpServletResponse response, @PathVariable("id") String id,
                           @RequestParam(required = false, defaultValue = "{}") String projection) {
        Map<String, Object> map = new HashMap<>();

        if (!DocumentUtils.isValidObjectId(id)) {
            return R.error("id 格式不对");
        }

        Document comment = null;

        try {
            comment = productService.findOne(new Document("_id", new ObjectId(id)),
                    QueryUtils.getDocumentFromStr(projection), null, null);
            if (comment != null) {
                return R.ok().put("data",comment);
            } else {
                return R.error("产品【" + id + "】不存在");
            }
        }  catch (Exception e) {
            logger.error(ExceptionUtils.getExceptionStack(e));
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return R.error(e.getMessage());
        }
    }

    @RequestMapping(value = "/sys/product/{id}", method = RequestMethod.PUT)
    public Map<String, Object> update(HttpServletRequest request, HttpServletResponse response,
                                      @RequestBody Map<String, Object> activity, @PathVariable("id") String id) {
        if (!DocumentUtils.isValidObjectId(id)) {
            return R.error("incorrect id format");
        }
        try {
            long result = productService.updateUseSet(id, new Document(activity));
            return  R.ok( result + " items has been updated");
        } catch (Exception e) {
            logger.error(ExceptionUtils.getExceptionStack(e));
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return R.error(e.getMessage());
        }
    }

    @RequestMapping(value = "/sys/product", method = RequestMethod.GET)
    public Object queryPage(HttpServletRequest request, HttpServletResponse response,
                            @RequestParam(required = false, defaultValue = "{}") String query,
                            @RequestParam(required = false, defaultValue = "false") boolean pageAble,
                            @RequestParam(required = false, defaultValue = "0") int pageNum,
                            @RequestParam(required = false, defaultValue = "20") int size,
                            @RequestParam(required = false, defaultValue = "{}") String projection,
                            @RequestParam(required = false, defaultValue = "{}") String sort) {
        try {
            if (pageAble) {
                return  R.ok().put("data",productService.findPage(QueryUtils.getDocumentFromStr(query),
                        QueryUtils.getDocumentFromStr(projection), QueryUtils.getDocumentFromStr(sort), pageNum-1, size,
                        null));
            } else {
                return  R.ok().put("data",productService.findList(QueryUtils.getDocumentFromStr(query),
                        QueryUtils.getDocumentFromStr(projection), QueryUtils.getDocumentFromStr(sort), null, null, null));
            }

        } catch (Exception e) {
            logger.error(ExceptionUtils.getExceptionStack(e));
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return R.error(e.getMessage());
        }
    }

    //删除的
    @RequestMapping(value = "/sys/product/{id}", method = RequestMethod.DELETE)
    public Map<String, Object> deleteById(HttpServletRequest request, HttpServletResponse response,ModelAndView model,
                                      @PathVariable("id") String id) {
        if (!DocumentUtils.isValidObjectId(id)) {
            return R.error("delete is error");
        }
        try {
            long result = productService.deleteById(id);
            return  R.ok( result + " items has been delete");
        } catch (Exception e) {
            logger.error(ExceptionUtils.getExceptionStack(e));
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); //500错误
            return R.error(e.getMessage());
        }
    }

    //updateUseSet

}
