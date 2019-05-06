package com.zzwc.cms.common.utils;

/**
 * 参数相关错误码
 * 
 * @author
 *
 */
public enum ArgumentErrorCode{

	ARGUMENT_MISSING(400002001, "参数缺失"), ARGUMENT_FORMATE_ERROR(400002002, "参数格式错误"), ARGUMENT_UNACCEPTABLE(400002003,
			"参数不被接受");

	// =================================================

	private int code;
	private String message;

	private ArgumentErrorCode(int code, String message) {
		this.code = code;
		this.message = message;
	}

	@Override
	public String toString() {
		return "错误码：" + code + ", 错误信息：" + message;
	}
}
