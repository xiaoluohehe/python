package com.zzwc.cms.admin.modules.context.service.impl;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.ZipUtil;
import cn.hutool.extra.qrcode.QrCodeUtil;
import com.mongodb.MongoClient;
import com.mongodb.client.AggregateIterable;
import com.zzwc.cms.admin.modules.product.service.IProductService;
import com.zzwc.cms.common.utils.qiniu.QiniuUtils;
import com.zzwc.cms.mongo.crud.impl.CRUDService;
import com.zzwc.cms.mongo.utils.DocumentUtils;
import org.apache.commons.io.FileUtils;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static com.mongodb.client.model.Accumulators.sum;
import static com.mongodb.client.model.Aggregates.group;


@Service
public class ContextServiceImpl extends CRUDService implements IProductService {

    @Autowired
    public ContextServiceImpl(@Qualifier(value = "cmsClient") MongoClient client) {
        super(client, "cms", "product");
    }

    @Override
    public String insert(Document dbObj) {
        //检验必要的字段
        List<String> requiredField=Arrays.asList("productionName","productionDate","salesArea","manufacturer");
        DocumentUtils.verifyRequiredField(dbObj,requiredField);
        //默认移除字段
        //扫码次数
        dbObj.remove("scanNumber");
        dbObj.put("scanNumber",0);
        //扫码人
        dbObj.remove("scanUser");
        dbObj.put("scanUser","");
        //是否抽奖
        dbObj.remove("isLuckyDraw");
        dbObj.put("isLuckyDraw",0);
        //产品标识
        String identification= RandomUtil.randomNumbers(18);
        dbObj.put("identification",identification);
        QrCodeUtil.generate("http://192.168.31.133:8020/qrcode/index.html?identification="+identification, 300, 300, FileUtil.file("d:/qrcode/"+identification+".jpg"));
        return super.insert(dbObj);

    }

    @Override
    public Object insertList(Document dbObj, String number) throws IOException {
        int num=Convert.toInt(number);
        String s = UUID.randomUUID().toString();
        String filepath = FileUtil.getTmpDirPath()+s+"\\";
        FileUtil.mkdir(filepath);
        for (int i = 0; i <num ; i++) {
            //检验必要的字段
            List<String> requiredField=Arrays.asList("productionName","productionDate","salesArea","manufacturer");
            DocumentUtils.verifyRequiredField(dbObj,requiredField);
            //默认移除字段
            //扫码次数
            dbObj.remove("scanNumber");
            dbObj.put("scanNumber",0);
            //扫码人
            dbObj.remove("scanUser");
            dbObj.put("scanUser","");
            //是否抽奖
            dbObj.remove("isLuckyDraw");
            dbObj.put("isLuckyDraw",0);
            //产品标识
            String identification= RandomUtil.randomNumbers(18);
            dbObj.put("identification",identification);
            QrCodeUtil.generate("http://192.168.31.133:8020/qrcode/index.html?identification="+identification, 300, 300, FileUtil.file(filepath+identification+".jpg"));
            super.insert(dbObj);
            }
            File file=  ZipUtil.zip(filepath);
             String url= QiniuUtils.uploadFileToQiuNiu(file,"cms",file.getName());
           return url;
    }

    @Override
    public Object findcolumn() {
        List<String> columList=new ArrayList<>();
        List<Integer> count=new ArrayList<>();
        //聚合查询
        AggregateIterable<Document> cursor=  super.getCollection().aggregate(Arrays.asList(group("$manufacturer",sum("count",1))));

        for (Document document: cursor) {
           columList.add( document.get("_id").toString());
            count.add(document.getInteger("count"));
        }
        Map<String,Object> map=new HashMap<>();
        map.put("categories",columList);
        map.put("data",count);
        return map;
    }

    private ResponseEntity<byte[]> download(File file) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", new String(file.getName().getBytes("UTF-8"), "iso-8859-1"));
        return new ResponseEntity<byte[]>(FileUtils.readFileToByteArray(file), headers, HttpStatus.CREATED);
    }
}
