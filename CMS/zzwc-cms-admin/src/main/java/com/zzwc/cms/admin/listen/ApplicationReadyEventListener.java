package com.zzwc.cms.admin.listen;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;

/**
 * 应用就绪事件监听器
 *
 */
public class ApplicationReadyEventListener implements ApplicationListener<ApplicationReadyEvent> {

    private Logger logger = LoggerFactory.getLogger(ApplicationReadyEventListener.class);

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        logger.info("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
        logger.info("+++++++++++++++++++++++  zzwc-cms-admin启动完成   +++++++++++++++++++++++");
        logger.info("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
    }
}