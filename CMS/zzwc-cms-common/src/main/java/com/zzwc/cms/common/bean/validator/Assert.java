package com.zzwc.cms.common.bean.validator;


import com.zzwc.cms.common.exception.ZhiZhiException;
import org.apache.commons.lang.StringUtils;

/**
 * 数据校验
 */
public abstract class Assert {

    public static void isBlank(String str, String message) {
        if (StringUtils.isBlank(str)) {
            throw new ZhiZhiException(message);
        }
    }

    public static void isNull(Object object, String message) {
        if (object == null) {
            throw new ZhiZhiException(message);
        }
    }
}
