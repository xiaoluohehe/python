package com.zzwc.cms.common.utils;



import com.zzwc.cms.common.exception.ZhiZhiException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;

public class TimeUtils {
	
	public static final String PATTERN_DATE="yyyy-MM-dd";
	public static final String PATTERN_TIME="HH:mm:ss";
	public static final String PATTERN_DATE_TIME="yyyy-MM-dd HH:mm:ss";
	/**
	 * 将字符串转为date
	 * 
	 * @param dateStr
	 *            日期字符串
	 * @param pattern
	 *            日期模式串
	 *
	 */
	public static Date getDateFromStr(String dateStr, String pattern) {
		SimpleDateFormat sf = new SimpleDateFormat(pattern);
		try {
			return dateStr == null ? null : sf.parse(dateStr);
		} catch (ParseException e) {
			e.printStackTrace();
			throw new ZhiZhiException(
					"日期解析出错，请检查日期字符串【" + dateStr + "】与模式【" + pattern + "】是否匹配",e);
		}
	}

	/**
	 * 将日期转为制定格式的字符串
	 * 
	 * @param date
	 *            日期对象
	 * @param pattern
	 *            日期字符串模式
	 * @return 格式化后的日期
	 *
	 */
	public static String getDateStr(Date date, String pattern) {
		SimpleDateFormat sf = new SimpleDateFormat(pattern);
		try {
			return sf.format(date);
		} catch (Exception e) {
			throw new ZhiZhiException( "不支持此日期模式【" + pattern + "】");
		}
	}

	/**
	 * 获取两个日期的间隔天数，2016-01-01~2016-01-05天数间隔4天
	 * 
	 * @param date1
	 * @param date2
	 * @return
	 */
	public static int getDaysBetweenDates(Date date1, Date date2) {
		String datePattern = "yyyy-MM-dd";
		// 去时分秒
		String date1Str = TimeUtils.getDateStr(date1, datePattern);
		String date2Str = TimeUtils.getDateStr(date2, datePattern);

		date1 = TimeUtils.getDateFromStr(date1Str, datePattern);
		date2 = TimeUtils.getDateFromStr(date2Str, datePattern);

		long timeInMis = Math.abs(date1.getTime() - date2.getTime());
		// 1天86400000毫秒
		return (int) timeInMis / 86400000;
	}

	/**
	 * 获取两个日期的间隔天数，2016-01-01~2016-01-05天数间隔4天
	 * 
	 * @param dateStr1
	 * @param dateStr2
	 * @param pattern
	 *            日期字符串模式
	 * @return
	 * @throws ParseException
	 */
	public static int getDaysBetweenDates(String dateStr1, String dateStr2, String pattern) {
		Date date1 = getDateFromStr(dateStr1, pattern);
		Date date2 = getDateFromStr(dateStr2, pattern);
		return getDaysBetweenDates(date1, date2);
	}

	/**
	 * 获取指定时间的UTC日期时间
	 * 
	 * @param date
	 * @return
	 */
	public static Date getUTCDate(Date date) {

		// 1、取得本地时间：
		Calendar cal = Calendar.getInstance();

		cal.setTime(date);

		// 2、取得时区偏移量：
		int zoneOffset = cal.get(Calendar.ZONE_OFFSET);

		// 3、取得夏令时差：
		int dstOffset = cal.get(Calendar.DST_OFFSET);

		// 4、从本地时间里扣除这些差量，即可以取得UTC时间：
		cal.add(Calendar.MILLISECOND, -(zoneOffset + dstOffset));

		return cal.getTime();
	}

	public static String getUTCDateStr(Date date) {
		Date utcDate = getUTCDate(date);
		Calendar cal = Calendar.getInstance();
		cal.setTime(utcDate);

		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONTH) + 1;
		String monthStr = month < 10 ? "0" + month : "" + month;

		int day = cal.get(Calendar.DATE);
		String dayStr = day < 10 ? "0" + day : "" + day;

		int hour = cal.get(Calendar.HOUR_OF_DAY);
		String hourStr = hour < 10 ? "0" + hour : "" + hour;

		int minute = cal.get(Calendar.MINUTE);
		String minuteStr = minute < 10 ? "0" + minute : "" + minute;

		int second = cal.get(Calendar.SECOND);
		String secondStr = second < 10 ? "0" + second : "" + second;
		int mills = cal.get(Calendar.MILLISECOND);

		return year + "-" + monthStr + "-" + dayStr + "T" + hourStr + ":" + minuteStr + ":" + secondStr + "." + mills
				+ "Z";
	}


	/**
	 * 获取去除时间部分后的日期
	 * 
	 * @param timestamp
	 * @return
	 */
	public static Date getDateWithoutTime(long timestamp) {
		return getDateWithoutTime(new Date(timestamp));
	}

	public static Date getDateWithoutTime(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);

		return calendar.getTime();
	}

	// public static void main(String[] args) {
	// Date now = new Date();
	// System.out.println(now);
	//
	// System.out.println(getDateWithoutTime(now.getTime()));
	// }

	// ---------------------------------------------------------------------

	/**
	 * 得到当前系统时间，毫秒
	 */
	public static long getSystemMillisTime() {
		return System.currentTimeMillis();
	}

	/**
	 * 日期计算
	 * 
	 * @param date
	 *            起始日期
	 * @param yearNum
	 *            年增减数
	 * @param monthNum
	 *            月增减数
	 * @param dateNum
	 *            日增减数
	 */
	public static String calDate(String date, int yearNum, int monthNum, int dateNum) {
		String pattern = "yyyy-MM-dd";
		return calDate(getDateFromStr(date, pattern), yearNum, monthNum, dateNum);
	}

	public static String calDate(Date date, int yearNum, int monthNum, int dateNum) {
		String pattern = "yyyy-MM-dd";

		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.MONTH, monthNum);
		cal.add(Calendar.YEAR, yearNum);
		cal.add(Calendar.DATE, dateNum);
		return getDateStr(cal.getTime(), pattern);
	}

	public static String calDate(Date date, int yearNum, int monthNum, int dateNum, String returnFormat) {
		String result = "";
		try {
			SimpleDateFormat sd = new SimpleDateFormat(returnFormat);
			Calendar cal = Calendar.getInstance();
			cal.setTime(date);
			cal.add(Calendar.MONTH, monthNum);
			cal.add(Calendar.YEAR, yearNum);
			cal.add(Calendar.DATE, dateNum);
			result = sd.format(cal.getTime());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 得到当前月的最后一天
	 * 
	 * @param
	 * @return
	 */
	public static Date getLastDayOfMonth(Date date) {
		Calendar cDay1 = Calendar.getInstance();
		cDay1.setTime(date);
		final int lastDay = cDay1.getActualMaximum(Calendar.DAY_OF_MONTH);

		cDay1.set(Calendar.DAY_OF_MONTH, lastDay);

		return cDay1.getTime();
	}

	/**
	 * 得到当前月的第一天
	 * 
	 * @param
	 * @return
	 */
	public static Date getFirstDayOfMonth(Date date) {
		Calendar cDay1 = Calendar.getInstance();
		cDay1.setTime(date);
		final int firstDay = cDay1.getActualMinimum(Calendar.DAY_OF_MONTH);

		cDay1.set(Calendar.DAY_OF_MONTH, firstDay);

		return cDay1.getTime();
	}

	/**
	 * 将普通对象解析为日期对象
	 * 
	 * @param dateObj
	 * @return
	 */
	public static Date getDate(Object dateObj) {

		Date date = null;
		if (dateObj instanceof Date) {
			return (Date) dateObj;
		} else if (dateObj instanceof Number) {
			date = new Date(((Number) dateObj).longValue());
		} else if (dateObj instanceof String) {
			String dateStr = (String) dateObj;

			// 尝试转成long
			try {
				date = new Date(Long.valueOf(dateStr));
			} catch (NumberFormatException e) {
				// 尝试各种格式的时间字符串解析
				// 因为SimpleDateFormat对于45月78号25点74分97秒等错误格式依然可以解析，所以只验证格式，不验证取值
				Map<String, String> patternMap = new HashMap<>();
				patternMap.put("yyyy-MM-dd", "\\d{4}-\\d{1,2}-\\d{1,2}\\s*");
				patternMap.put("yyyy-MM-dd HH:mm:ss",
						"\\d{4}-[0|1]{0,1}[0-9]-\\d{1,2}\\s+\\d{1,2}:\\d{1,2}:\\d{1,2}\\s*");

				patternMap.put("yyyy/MM/dd", "\\d{4}/\\d{1,2}/\\d{1,2}\\s*");
				patternMap.put("yyyy/MM/dd HH:mm:ss", "\\d{4}/\\d{1,2}/\\d{1,2}\\s+\\d{1,2}:\\d{1,2}:\\d{1,2}\\s*");

				patternMap.put("yyyy-MM-dd'T'HH:mm:ss.S'Z'",
						"\\d{4}-\\d{1,2}-\\d{1,2}T\\d{1,2}:\\d{1,2}:\\d{1,2}\\.\\d{3}Z\\s*");

				String pattern = null;

				Set<Entry<String, String>> entrySet = patternMap.entrySet();

				for (Entry<String, String> entry : entrySet) {
					if (dateStr.matches(entry.getValue())) {
						pattern = entry.getKey();
						break;
					}
				}

				if (pattern == null) {
					throw new ZhiZhiException(
							"暂时没有适合的时间匹配模式，无法将值  " + dateObj + "  解析为日期");
				}

				date = TimeUtils.getDateFromStr(dateStr, pattern);
			}
		} else {
			throw new ZhiZhiException( "提供的对象无法解析为日期");
		}

		return date;
	}

	/**
	 * 获取前n个月的今天
	 *
	 * @param numb
	 *            前几个月
	 * @return Calendar
	 */
	public static Calendar getLastDate(int numb) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		Calendar lastDate = (Calendar) calendar.clone();
		lastDate.add(Calendar.MONTH, numb);
		return lastDate;
	}
	
	/**
	 * 获取给定日期的23点59分59秒对应的日期对象
	 * 
	 * @param date
	 * @return
	 */
	public static Date getEndDateTimeOfDay(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);

		cal.set(Calendar.HOUR_OF_DAY, 23);
		cal.set(Calendar.MINUTE, 59);
		cal.set(Calendar.SECOND, 59);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}
	
	/**
	 * 获取给定日期的0点0分0秒对应的日期对象
	 * @param date
	 * @return
	 */
	public static Date getStartDateTimeOfDay(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);

		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}
}
