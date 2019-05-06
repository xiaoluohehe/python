package com.zzwc.cms;

import com.zzwc.cms.admin.listen.ApplicationReadyEventListener;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

public class ServletInitializer extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {

        System.out.println("***************************************************************");
        System.out.println("启动  zzwc-cms-admin...");

        application.sources(AdminApplication.class)

                .listeners(new ApplicationReadyEventListener());


        return application;
    }

}