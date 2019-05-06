package com.zzwc.cms.admin.config;

import com.mongodb.MongoClient;
import com.zzwc.cms.mongo.MongoClientHelper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 *需要自定义mongo client
 */
@Configuration
public class MongoConfig {

    // *************************** Mongodb 数据库连接配置 *************************
    // ============================== 开发环境配置 =============================
    @Bean(name = "cmsClient")
    @Profile("dev")
    public MongoClient mongoDevClient() throws FileNotFoundException, IOException {
        MongoClientHelper clientHelper = new MongoClientHelper();
        MongoClient client = clientHelper.getClient("db_mongo_cms_dev.properties");
        return client;
    }

    // ============================== 生产环境配置 =============================
    @Bean(name = "cmsClient")
    @Profile("prod")
    public MongoClient mongoClient() throws FileNotFoundException, IOException {
        MongoClientHelper clientHelper = new MongoClientHelper();
        MongoClient client = clientHelper.getClient("db_mongo_cms.properties");
        return client;
    }

}
