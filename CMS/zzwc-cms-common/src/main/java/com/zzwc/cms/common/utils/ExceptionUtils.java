package com.zzwc.cms.common.utils;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * created by weirdor
 */
public class ExceptionUtils {
    /**
     * 获取异常栈信息
     *
     * @param e
     * @return
     */
    public static String getExceptionStack(Throwable e) {
        try (StringWriter sw = new StringWriter()) {
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            return sw.toString();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return null;
    }
}
