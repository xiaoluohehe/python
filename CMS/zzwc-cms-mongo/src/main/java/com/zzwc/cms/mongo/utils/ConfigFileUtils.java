package com.zzwc.cms.mongo.utils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigFileUtils {
	/**
	 * 获取数据库配置属性
	 * 
	 * @param configPath
	 *            配置文件位置，应位于classpath下，若为null，则 用默认参数指定
	 *            <ul>
	 *            <li>host=localhost</li>
	 *            <li>port=27017</li>
	 *            <li>db=test</li>
	 *            <li>username=</li>
	 *            <li>password=</li>
	 *            </ul>
	 * @return 数据库配置属性
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static Properties getConfigProperties(String configPath) throws FileNotFoundException, IOException {
		Properties config = new Properties();
		if (configPath == null || "".equals(configPath.trim())) {
			config.load(ConfigFileUtils.class.getClassLoader().getResourceAsStream("defaultDBConfig.properties"));
		} else {
			InputStream inputStream = ConfigFileUtils.class.getClassLoader().getResourceAsStream(configPath);
			if (inputStream == null) {
				throw new FileNotFoundException("mongodb配置文件" + configPath + "不存在");
			}
			config.load(inputStream);
		}
		return config;
	}
}
