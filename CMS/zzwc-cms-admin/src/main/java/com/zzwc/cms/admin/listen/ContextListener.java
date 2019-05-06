package com.zzwc.cms.admin.listen;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

/**
 * created by weirdor
 */
@Component
@WebListener
public class ContextListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
    }

    private Logger logger = LoggerFactory.getLogger(ContextListener.class);

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        logger.info("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
        logger.info("++++++++++++++++++++++++++ zzwc-cms-admin  已停止 ++++++++++++++++++++");
        logger.info("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
    }

}
