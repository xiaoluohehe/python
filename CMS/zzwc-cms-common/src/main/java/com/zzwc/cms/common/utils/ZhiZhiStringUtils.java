package com.zzwc.cms.common.utils;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ZhiZhiStringUtils {
	/**
	 * 生成32位ID
	 * 
	 * @param origin
	 * @return
	 */
	public static String md5build(String origin) {
		if (origin == null){
			return null;
		}
		StringBuilder sb = new StringBuilder();
		MessageDigest digest = null;
		try {
			digest = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return null;
		}

		// 生成一组length=16的byte数组
		byte[] bs = digest.digest(origin.getBytes(Charset.forName("UTF-8")));

		for (int i = 0; i < bs.length; i++) {
			int c = bs[i] & 0xFF; // byte转int为了不丢失符号位， 所以&0xFF
			if (c < 16) { // 如果c小于16，就说明，可以只用1位16进制来表示， 那么在前面补一个0
				sb.append("0");
			}
			sb.append(Integer.toHexString(c));
		}
		return sb.toString();
	}

	public static String shortText(String str) {

		String[] chars = { "a", "b", "c", "d", "e", "f", "g", "h",

				"i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t",

				"u", "v", "w", "x", "y", "z", "0", "1", "2", "3", "4", "5",

				"6", "7", "8", "9", "A", "B", "C", "D", "E", "F", "G", "H",

				"I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T",

				"U", "V", "W", "X", "Y", "Z" };

		String md5 = md5build(str);
		if(md5 == null) {
			throw new RuntimeException("md5结果为null");
		}

		// 将32个字符的md5码分成4段处理，每段8个字符
		// for (int i = 0; i < 4 ; i++) {

		int offset = 3 * 8;

		String sub = md5.substring(offset, offset + 8);

		long sub16 = Long.parseLong(sub, 16); // 将sub当作一个16进制的数，转成long

		// & 0X3FFFFFFF，去掉最前面的2位，只留下30位
		sub16 &= 0X3FFFFFFF;

		StringBuilder sb = new StringBuilder();
		// 将剩下的30位分6段处理，每段5位
		for (int j = 0; j < 6; j++) {
			// 得到一个 <= 61的数字
			long t = sub16 & 0x0000003D;
			sb.append(chars[(int) t]);

			sub16 >>= 5; // 将sub16右移5位
		}

		// result[i] = sb.toString();
		// }

		return sb.toString();
	}

	/**
	 * 若字符串str中存在正则表达式中的特殊字符，将其转换
	 * 
	 * @param str
	 * @return
	 */
	public static String transformRegexCharacter(String str) {
		return str.replaceAll("\\\\", "\\\\\\\\").replaceAll("\\*", "\\\\*").replaceAll("\\.", "\\\\.")
				.replaceAll("\\?", "\\\\?").replaceAll("\\+", "\\\\+").replaceAll("\\$", "\\\\\\$")
				.replaceAll("\\^", "\\\\^").replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]")
				.replaceAll("\\(", "\\\\(").replaceAll("\\)", "\\\\)").replaceAll("\\{", "\\\\{")
				.replaceAll("\\}", "\\\\}").replaceAll("\\|", "\\\\|").replaceAll("/", "\\\\/");
	}

	/**
	 * 过滤表情符号
	 * 
	 * @param str
	 * @return
	 */
	public static String filterEmoji(String str) {
		return str.replaceAll("[^\\u0000-\\uFFFF]", "");
	}
}
