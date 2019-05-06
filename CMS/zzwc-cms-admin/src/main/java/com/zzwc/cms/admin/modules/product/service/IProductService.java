package com.zzwc.cms.admin.modules.product.service;

import com.zzwc.cms.mongo.crud.CRUD;
import org.bson.Document;

import java.io.IOException;

public interface IProductService extends CRUD<String> {

    @Override
    String insert(Document dbObj);

    Object insertList(Document document, String number) throws IOException;

    Object findcolumn();
}
