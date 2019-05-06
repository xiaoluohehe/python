package com.zzwc.cms.common.utils;

/**
 * created by weirdor
 */
public class RedisKeys {

    public static String getSysConfigKey(String key){
        return "system:config:" + key;
    }
    public static String getShiroSessionKey(String key){
        return "sessionid:" + key;
    }

}
