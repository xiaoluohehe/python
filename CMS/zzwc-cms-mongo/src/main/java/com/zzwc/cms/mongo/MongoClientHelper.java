package com.zzwc.cms.mongo;

import com.mongodb.*;
import com.zzwc.cms.mongo.utils.ConfigFileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * mongoClient帮助类，用于从配置文件获取数据库连接
 *
 * @author
 */
public class MongoClientHelper {

	private static Map<String, MongoClient> clientMap = new ConcurrentHashMap<>();

	private Logger logger = LoggerFactory.getLogger(MongoClientHelper.class);

	/**
	 * 获取mongodb数据库连接
	 * 
	 * @param configFile
	 *            数据库连接的配置文件相对于classpath的路径
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public MongoClient getClient(String configFilePath) throws IOException {

		MongoClient client = clientMap.get(configFilePath);

		if (client != null) {
			logger.info("配置文件 {} 对应的客户端已存在，将被复用", configFilePath);
			clientMap.put(configFilePath, client);
			return client;
		}
		logger.info("配置文件 {} 对应的客户端不存在，将新建并缓存已被复用", configFilePath);

		Properties config = ConfigFileUtils.getConfigProperties(configFilePath);

		List<ServerAddress> seeds = new ArrayList<ServerAddress>();

		String[] userHosts = config.getProperty("host").split(",");
		for (String userHost : userHosts) {
			ServerAddress serverAddress = new ServerAddress(userHost, Integer.valueOf(config.getProperty("port")));
			seeds.add(serverAddress);
		}

		String username = config.getProperty("username");
		String password = config.getProperty("password");

		if (username != null && !username.equals("") && password != null && !password.equals("")) {
			MongoCredential CR = MongoCredential.createScramSha1Credential(config.getProperty("username"),
					config.getProperty("db"), config.getProperty("password").toCharArray());
			List<MongoCredential> mcs = new ArrayList<MongoCredential>();
			mcs.add(CR);
			client = new MongoClient(seeds, mcs, getClientOptions(config));
			clientMap.put(configFilePath, client);
			return client;
		}

		// 未指定用户名密码
		List<MongoCredential> mcs = new ArrayList<MongoCredential>();

		client = new MongoClient(seeds, mcs, getClientOptions(config));

		return client;

	}

	/**
	 * 获取连接配置
	 * 
	 * @param config
	 *            配置属性
	 * @return
	 */
	private MongoClientOptions getClientOptions(Properties config) {
		return new MongoClientOptions.Builder()
				// 每个host允许连接的最大连接数,这些连接空闲时会放入池中,如果连接被耗尽，任何请求连接的操作会被阻塞等待连接可用；默认100
				.connectionsPerHost(200)

				// 获取连接的线程的最大等待时间；当连接数到达最大后，如果还需连接，则需要等待；默认120秒，
				// 超出则抛出MongoTimeoutException: Timeout waiting for a
				// pooled item after * MILLISECONDS
				.maxWaitTime(120000)

				// 等待线程数系数，默认5，表示有平均最多有多少个线程可以等待一个连接，等待的线程排队，队列的最大长度为此系数x最大连接数，等待线程数超过是则抛出MongoWaitQueueFullException
				.threadsAllowedToBlockForConnectionMultiplier(5)

				// 每个host最少获取的连接数，即连接池中保存的最小的连接数，最小和最大的差值即为可动态产生的连接数，默认0
				.minConnectionsPerHost(10)

				// 新建一个连接时的超时时间
				.connectTimeout(30000)

				// 连接的最大空闲时间，超过这个时间连接会被关闭
				.maxConnectionIdleTime(300000)
				
				//???
				.socketKeepAlive(false)

				// 建立连接后，传输数据，如果没有在规定的时间内返回给客户端数据则抛MongoSocketReadTimeoutException|SocketTimeOutExceptin，默认0表示无超时
				.socketTimeout(60000).readPreference(ReadPreference.nearest()).build();
	}
}
