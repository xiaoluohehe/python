package com.zzwc.cms.common.exception;

/**
 * created by weirdor
 */
public class ZhiZhiException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private String msg;
    private int code = 500;

    public ZhiZhiException(String msg) {
        super(msg);
        this.msg = msg;
    }

    public ZhiZhiException(String msg, Throwable e) {
        super(msg, e);
        this.msg = msg;
    }

    public ZhiZhiException(String msg, int code) {
        super(msg);
        this.msg = msg;
        this.code = code;
    }

    public ZhiZhiException(String msg, int code, Throwable e) {
        super(msg, e);
        this.msg = msg;
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }


}

