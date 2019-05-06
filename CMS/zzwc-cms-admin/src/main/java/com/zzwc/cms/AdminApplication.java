package com.zzwc.cms;

import com.zzwc.cms.admin.listen.ApplicationReadyEventListener;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;

@SpringBootApplication(exclude =  MongoAutoConfiguration.class)
public class AdminApplication {

    public static void main(String[] args){
        System.out.println("***************************************************************");
        System.out.println("启动zzwc-cms-admin...");

        SpringApplication app = new SpringApplication(AdminApplication.class);

        app.addListeners(new ApplicationReadyEventListener());
        app.run(args);
    }
}
