package com.zzwc.cms.mongo.utils;

import com.google.common.base.Splitter;
import com.zzwc.cms.common.exception.ZhiZhiException;
import org.apache.commons.lang.StringUtils;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 文档工具类，主要用于从document中取值
 *
 *
 */
public class DocumentUtils {

	/**
	 * 获取指定field的int值；若field的值不是一个数值类型，则会抛ArgumentException异常
	 * 
	 * @param obj
	 *            文档
	 * @param field
	 *            字段
	 * @return int值
	 */
	public static int getIntValue(Map<String, Object> obj, String field) {
		Object valueObj = obj.get(field);
		int value;
		if (valueObj instanceof Number) {
			Number number = (Number) valueObj;
			value = number.intValue();
		} else if (valueObj instanceof String) {
			try {
				value = Integer.parseInt((String) valueObj);
			} catch (Exception e) {
				throw new ZhiZhiException("字符串字段" + field + "的值 " + valueObj + "不可转换成整数",400002002);
			}
		} else {
			throw new ZhiZhiException(
					"字段 " + field + " 的值 " + valueObj + " 既不是数字类型也不是可转为数字的字符串",400002002);
		}

		return value;
	}

	/**
	 * 获取指定field的int值；当指定字段不存在是，返回默认值；若field的值不是一个数值类型， 则会抛ArgumentException异常
	 * 
	 * @param obj
	 *            文档
	 * @param field
	 *            字段
	 * @param defaultValue
	 *            默认值
	 * @return
	 */
	public static int getIntValue(Map<String, Object> obj, String field, int defaultValue) {
		Object valueObj = obj.get(field);
		if (valueObj == null || StringUtils.isBlank(valueObj.toString())) {
			return defaultValue;
		}
		return getIntValue(obj, field);
	}

	/**
	 * 获取指定field的long值；若field的值不是一个数值类型，则会抛ArgumentException异常
	 * 
	 * @param obj
	 *            文档
	 * @param field
	 *            字段
	 * @return int值
	 */
	public static long getLongValue(Document obj, String field) {
		Object valueObj = obj.get(field);
		long value;
		if (valueObj instanceof Number) {
			Number number = (Number) valueObj;
			value = number.longValue();
		} else if (valueObj instanceof String) {
			try {
				value = Long.parseLong((String) valueObj);
			} catch (Exception e) {
				throw new ZhiZhiException("字符串字段" + field + "的值 " + valueObj + "不可转换成整数",400002002);
			}
		} else {
			throw new ZhiZhiException(
					"字段 " + field + " 的值 " + valueObj + " 既不是数字类型也不是可转为数字的字符串",400002002);
		}

		return value;
	}

	/**
	 * 获取指定field的long值；当指定字段不存在是，返回默认值；若field的值不是一个数值类型， 则会抛ArgumentException异常
	 * 
	 * @param obj
	 *            文档
	 * @param field
	 *            字段
	 * @param defaultValue
	 *            默认值
	 * @return
	 */
	public static long getLongValue(Document obj, String field, long defaultValue) {
		Object valueObj = obj.get(field);
		if (valueObj == null || StringUtils.isBlank(valueObj.toString())) {
			return defaultValue;
		}
		return getLongValue(obj, field);
	}

	/**
	 * 获取指定field的double值；若field的值不是一个数值类型，则会抛ArgumentException异常
	 * 
	 * @param obj
	 *            文档
	 * @param field
	 *            字段
	 * @return double值
	 */
	public static double getDoubleValue(Map<String, Object> obj, String field) {
		Object valueObj = obj.get(field);
		double value;
		if (valueObj instanceof Number) {
			Number number = (Number) valueObj;
			value = number.doubleValue();
		} else if (valueObj instanceof String) {
			try {
				value = Double.parseDouble((String) valueObj);
			} catch (Exception e) {
				throw new ZhiZhiException(
						"字符串字段" + field + "的值 " + valueObj + "不可转换成数字",400002002);
			}
		} else {
			throw new ZhiZhiException(
					"字段 " + field + " 的值 " + valueObj + " 既不是数字类型也不是可转为数字的字符串",400002002);
		}

		return value;
	}

	public static double getDoubleValue(Map<String, Object> obj, String field, double defaultValue) {
		Object valueObj = obj.get(field);
		if (valueObj == null || StringUtils.isBlank(valueObj.toString())) {
			return defaultValue;
		}
		return getDoubleValue(obj, field);
	}

	public static boolean getBoolean(Map<String, Object> obj, String field) {
		Object valueObj = obj.get(field);
		if (valueObj instanceof String) {
			return Boolean.valueOf((String) valueObj);
		} else if (valueObj instanceof Boolean) {
			return ((Boolean) valueObj).booleanValue();
		} else {
			throw new ZhiZhiException( "字段 " + field + " 的值不可转换为bool值");
		}
	}

	public static boolean getBoolean(Document obj, String field, boolean defaultValue) {
		Object valueObj = obj.get(field);
		if (valueObj == null || StringUtils.isBlank(valueObj.toString())) {
			return defaultValue;
		}
		return getBoolean(obj, field);
	}

	/**
	 * 验证并设置数组类型的值
	 * <p>
	 * json中的数组元素可以是字符串、数字或者内嵌对象，为了简单，该方法只支持验证和转换 <b>字符串</b>、<b>数字</b>
	 * 元素；如果该字段不存在，则跳过不处理
	 * </p>
	 * <ul>
	 * <li>如果指定字段的值确实是数组，则验证数组元素是否为指定的类型</li>
	 * <li>如果指定字段的值不是数组，只考虑字符串和数值两种情况：
	 * <ul>
	 * <li>如果值是字符串，用指定分隔符将字符串分为数组，然后再尝试将字符串元素转换成指定类型</li>
	 * <li>如果值是数值，则尝试验证或转换成指定类型</li>
	 * </ul>
	 * </li>
	 * </ul>
	 * 
	 * @param doc
	 *            待检测的对象
	 * @param field
	 *            字段名称
	 * @param split
	 *            分隔符，若为null,则用中英文逗号和分号（,|;|，|；），当字段的值为字符串才有用
	 * @param targetEleType
	 *            期望的数组元素的类型，只支持String、Long、Integer、Float、Double、Short和Byte类型
	 */
	public static void verifyAndSetArrayValue(Map<String, Object> doc, String field, String split,
			Class<?> targetEleType) {

		// 类型校验
		if (targetEleType != String.class && !Number.class.isAssignableFrom(targetEleType)) {
			throw new ZhiZhiException( "目前只支持String、数字类型的验证和转换",400002002);
		}

		// 如果doc中不包含要校验的字段 doNothing
		if (!doc.containsKey(field)) {
			return;
		}

		Object fieldObj = doc.get(field);

		// 如果不存在该字段对应的值，则给他赋一个空数组值
		if (fieldObj != null) {
			// 如果值不是一个数组类型，则只考虑是否是字符串和数字类型，其他类型一律报错
			if (!(fieldObj instanceof List)) {
				if (fieldObj instanceof String) {

					Pattern splitPattern = null;
					if (split == null) {
						splitPattern = Pattern.compile(",|;|，|；");
					} else {
						splitPattern = Pattern.compile(split);
					}

					List<String> listValue = Splitter.on(splitPattern).trimResults().omitEmptyStrings()
							.splitToList((String) fieldObj);
					// 如果目标数组元素是String类型，直接设置值即可
					if (targetEleType == String.class) {
						doc.put(field, listValue);
					} else {
						// 把数组元素尝试转换成指定的数字类型，若转换出错则认定是客户端数据异常
						List<Object> array = new ArrayList<>();
						for (String value : listValue) {
							try {
								array.add(targetEleType.getConstructor(String.class).newInstance(value));
							} catch (Exception e) {
								e.printStackTrace();
								throw new ZhiZhiException(""+e);
							}
						}
						doc.put(field, array);
					}
				} else if (fieldObj instanceof Number) {

					// 如果期望是字符串元素，则将该数字转换为字符串
					if (targetEleType == String.class) {
						doc.put(field, Arrays.asList("" + fieldObj));
					} else {
						// 如果该数字类型就是期望的数组元素类型，则直接设置即可
						if (fieldObj.getClass() == targetEleType) {
							doc.put(field, Arrays.asList(fieldObj));
						} else {
							// 否则尝试转换为期望的数字类型
							try {
								doc.put(field, Arrays
										.asList(targetEleType.getConstructor(String.class).newInstance("" + fieldObj)));
							} catch (Exception e) {
								e.printStackTrace();
								throw new ZhiZhiException(""+e);
							}
						}
					}
				} else {
					throw new ZhiZhiException( field + " 字段数据类型错误",400002002);
				}
			} else {
				// 如果是数组类型，则验证其元素数据类型是否为期望的类型
				@SuppressWarnings("unchecked")
				List<Object> objList = (List<Object>) fieldObj;
				List<Object> list = new ArrayList<>();

				for (Object obj : objList) {
					// 去除null值，
					if (obj != null) {
						// 如果发现实际类型与预期类型不一致，尝试转换，失败则报错
						if (obj.getClass() != targetEleType) {
							// 因为预期的数据类型限制为String和数值，所以只尝试转为这两种类型
							if (targetEleType == String.class) {
								list.add(obj.toString());
							} else {
								try {
									list.add(targetEleType.getConstructor(String.class).newInstance("" + obj));
								} catch (Exception e) {
									e.printStackTrace();
									throw new ZhiZhiException(field + " 字段值的元素的期望类型为" + targetEleType.getName()
											+ ",但实际值为 " + objList + " ，无法转换");
								}
							}
						} else {
							list.add(obj);
						}
					}
				}
				doc.put(field, list);
			}
		} else {
			doc.put(field, new ArrayList<>());
		}
	}

	/**
	 * 验证必须字段是否存在，若不存在抛PropertyException
	 * 
	 * @param doc
	 * @param requiredfileds
	 */
	public static void verifyRequiredField(Document doc, List<String> requiredfileds) {
		for (String field : requiredfileds) {
			if (!doc.containsKey(field)) {
				throw new ZhiZhiException("缺失必须字段");
			}
		}
	}

	/**
	 * <p>
	 * 验证给定参数是否是合法的ObjectId字符串
	 * </p>
	 * 原始的ObjectId.isValid()方法当遇到非法字符串时会抛出IllegalArgumentException，有时并不符合业务需求，所以这里仅仅是做了一个简单包装，只返回true
	 * 或 false,不会抛出异常
	 * 
	 * @param objectId
	 * @return
	 */
	public static boolean isValidObjectId(String objectId) {
		if (StringUtils.isEmpty(objectId)) {
			return false;
		}
		try {
			return ObjectId.isValid(objectId);
		} catch (IllegalArgumentException e) {
		}

		return false;
	}

	/**
	 * 获取map中的BigDecimal类型的值
	 * 
	 * @param update
	 * @param key
	 * @return 如果Map中不包含指定的key返回null，否则返回一个BigDecimal
	 */
	public static BigDecimal getBigDecimalValue(Map<String, Object> map, String key) {

		if (!map.containsKey(key)) {
			return null;
		}

		Object valueObj = map.get(key);
		if (valueObj == null) {
			throw new ZhiZhiException( "不存在" + key + " 对应的值",400002001);
		}

		BigDecimal value = null;
		try {
			value = new BigDecimal(valueObj.toString());
		} catch (Exception e) {
			throw new ZhiZhiException( "字段" + key + " 的值不可转为数字",400002001);
		}
		return value;
	}

	/**
	 * 删除值为null的元素，返回一个新得doc
	 * 
	 * @param map
	 * @return
	 */
	public static Document deleteNullValue(Document map) {
		Document newMap = new Document();
		map.entrySet().forEach(e -> {
			if (e.getValue() != null) {
				newMap.put(e.getKey(), e.getValue());
			}
		});
		return newMap;
	}

	/**
	 * 对doc中的字符串值去除首尾空格；递归执行
	 * 
	 * @param doc
	 *
	 */
	@SuppressWarnings("unchecked")
	public static void trimValue(Map<String, Object> doc) {
		if (doc == null || doc.isEmpty()) {
			return;
		}

		doc.keySet().forEach(key -> {
			Object value = doc.get(key);

			if (value == null) {
				return;
			}

			if (value instanceof String) {
				doc.put(key, value.toString().trim());
			} else if (Map.class.isAssignableFrom(value.getClass())) {
				trimValue((Map<String, Object>) value);
				doc.put(key, value);
			}
		});
	}
}
