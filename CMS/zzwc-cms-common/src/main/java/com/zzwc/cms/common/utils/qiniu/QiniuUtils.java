package com.zzwc.cms.common.utils.qiniu;

import com.qiniu.common.QiniuException;
import com.qiniu.common.Zone;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import com.qiniu.util.UrlSafeBase64;
import com.zzwc.cms.common.exception.ZhiZhiException;
import com.zzwc.cms.common.utils.TimeUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.*;

/**
 * 七牛存储相关操作工具类
 *
 */
public class QiniuUtils {

	private static Logger logger = LoggerFactory.getLogger(QiniuUtils.class);

	/**
	 * 上传文件（图片、音频等各种文件）到七牛
	 * 
	 * @param file
	 *            待上传的文件
	 * @param bucketName
	 *            七牛上的空间名称
	 * @param key
	 *            指定的文件key
	 * @return 文件在七牛上的url，若上传失败则返回null
	 * @throws QiniuException
	 * @throws FileNotFoundException
	 *
	 */
	public static String uploadFileToQiuNiu(File file, String bucketName, String key)
			throws QiniuException, FileNotFoundException {
		if (file == null || !file.exists()) {
			throw new ZhiZhiException( "上传的文件不存在");
		}

		UploadManager uploadManager = new UploadManager(new Configuration(Zone.autoZone()));

		String token = getToken(bucketName, null);

		Response res = uploadManager.put(file, key, token);
		if (res.statusCode == 200 && res.isOK()) {
			logger.debug("上传成功");
			return domainMap.get(bucketName)+key;
		}
		logger.debug("upload error!!!");
		return null;
	}

	/**
	 * 获取七牛token，七牛不会检测指定空间名是否存在；猜测token是根据空间名计算出来的
	 * 
	 * @param bucketName
	 *            空间名
	 * @return
	 */
	public static String getToken(String bucketName, String key) {
		return getToken(bucketName, null, null, key);
	}

	private final static String ACCESS_KEY = "5WX4HInoIwWiR8dFy0Zakq2CehgXcKzS06hOsoGW";
	private final static String SECRET_KEY = "BgbtKnG5JLpx88iEnGtNLeGUFrf9Jxh3J2VyPkio";

	/**
	 * 获取七牛上传凭证
	 * 
	 * @param bucketName
	 *            上传到的空间名称
	 * @param fileType
	 *            文件类型，file,video,image,music
	 * @param params
	 *            文件类型对应的配置参数
	 * @return
	 */
	public static String getToken(String bucketName, String fileType, Map<String, String> params, String key) {

		if (StringUtils.isEmpty(bucketName)) {
			throw new ZhiZhiException( "未指定七牛空间名称");
		}

		if (params == null) {
			params = new HashMap<>();
		}

		Auth auth = Auth.create(ACCESS_KEY, SECRET_KEY);

		StringMap policy = new StringMap();
		policy.put("returnBody",
				"{\"key\": $(key), \"hash\": $(etag), \"mimeType\": $(mimeType),\"persistentId\":$(persistentId)}");

		List<String> persistentOps = new ArrayList<>();

		if ("video".equals(fileType)) {
			// 是否需要截帧，默认true
			boolean isNeedFrame = MapUtils.getBooleanValue(params, "isNeedFrame", true);
			if (isNeedFrame) {
				StringBuffer vframeFop = new StringBuffer("vframe/");
				// 如果需要截帧，截成什么格式，默认jpg
				String frameFormat = MapUtils.getString(params, "frameFormat", "jpg");
				vframeFop.append(frameFormat);

				// 如果需要截帧，从哪截，默认0.2秒处
				String frameStartTime = MapUtils.getString(params, "frameStartTime", "0.2");
				vframeFop.append("/offset/").append(frameStartTime);

				// 保存到哪，bucket默认为方法参数中bucketName,key默认为video_poster_yyyyMMddHHmmss.截图格式
				String frameSaveBucket = MapUtils.getString(params, "frameSaveBucket", bucketName);
				String frameSaveKey = MapUtils.getString(params, "frameSaveKey",
						"video_poster_" + TimeUtils.getDateStr(new Date(), "yyyyMMddHHmmss") + "." + frameFormat);
				String bucketAndKeyUrlSafeBase64 = UrlSafeBase64.encodeToString(frameSaveBucket + ":" + frameSaveKey);
				vframeFop.append("|saveas/").append(bucketAndKeyUrlSafeBase64);

				persistentOps.add(vframeFop.toString());
			}

			// 是否需要转码，默认true
			boolean isNeedTranscode = MapUtils.getBooleanValue(params, "isNeedTranscode", true);
			if (isNeedTranscode) {
				StringBuffer avthumbFop = new StringBuffer("avthumb/");
				// 如果需要转码，转成什么格式
				String transcodeFormat = MapUtils.getString(params, "transcodeFormat");
				if (StringUtils.isEmpty(transcodeFormat)) {
					throw new ZhiZhiException( "未指定视频转码格式");
				}
				avthumbFop.append(transcodeFormat);

				// 视频帧率多少，默认每秒23帧
				String frameRate = MapUtils.getString(params, "frameRate", "23");
				avthumbFop.append("/r/").append(frameRate);

				// 视频码率多少，默认2048kbps
				String videoBitRate = MapUtils.getString(params, "videoBitRate", "2048k");
				avthumbFop.append("/vb/").append(videoBitRate);

				// 保存到哪
				String vidoeSaveBucket = MapUtils.getString(params, "vidoeSaveBucket", bucketName);
				String videoSaveKey = MapUtils.getString(params, "videoSaveKey",
						"video_thumb_" + TimeUtils.getDateStr(new Date(), "yyyyMMddHHmmss") + "." + transcodeFormat);
				String bucketAndKeyUrlSafeBase64 = UrlSafeBase64.encodeToString(vidoeSaveBucket + ":" + videoSaveKey);
				avthumbFop.append("|saveas/").append(bucketAndKeyUrlSafeBase64);

				persistentOps.add(avthumbFop.toString());
			}
		} else if ("image".equals(fileType)) {
			// 图片处理
			StringBuffer imageFop = new StringBuffer("imageMogr2/auto-orient");

			// 旋转
			String rotate = MapUtils.getString(params, "rotate");
			if (params.containsKey("rotate") && StringUtils.isEmpty(rotate)) {
				throw new ZhiZhiException( "未指定旋转值");
			} else {
				imageFop.append("/rotate/").append(rotate);
			}
			// TODO 其他操作需要时再处理
			// 缩放
			// 裁剪

			// 保存到哪,默认空间为方法参数中bucketName，key为"mogr_image_yyyyMMddHHmmss.jpg"
			String imageSaveBucket = MapUtils.getString(params, "imageSaveBucket", bucketName);
			String imageSaveKey = MapUtils.getString(params, "imageSaveKey",
					"mogr_image_" + TimeUtils.getDateStr(new Date(), "yyyyMMddHHmmss") + ".jpg");
			String bucketAndKeyUrlSafeBase64 = UrlSafeBase64.encodeToString(imageSaveBucket + ":" + imageSaveKey);
			imageFop.append("|saveas/").append(bucketAndKeyUrlSafeBase64);

			persistentOps.add(imageFop.toString());
		}

		if (!persistentOps.isEmpty()) {
			if (persistentOps.size() == 1) {
				policy.put("persistentOps", persistentOps.get(0));
			} else {
				policy.put("persistentOps", String.join(";", persistentOps));
			}
			// 私有转码队列
			String persistentPipeline = MapUtils.getString(params, "persistentPipeline");
			if (!StringUtils.isEmpty(persistentPipeline)) {
				policy.put("persistentPipeline", persistentPipeline);
			}
		}

		if (StringUtils.isBlank(key)) {
			key = null;
		}
		return auth.uploadToken(bucketName, key, 3600, policy);

	}

	private static Map<String, String> domainMap = new HashMap<>();
	static {
		domainMap.put("live", "http://pb0xhvjbh.bkt.clouddn.com/");
		domainMap.put("awaken", "http://pc1mihua7.bkt.clouddn.com/");
	}

	/**
	 * 七牛处理结果另存
	 * <a href="http://developer.qiniu.com/code/v6/api/dora-api/saveas.html">参考 </a>
	 * 
	 * @param bucketName
	 *            处理后的结果存储 空间名
	 * @param key
	 *            处理后的结果资源key
	 * @param urlWithoutSchema
	 *            无协议的原始资源url
	 * @return 处理后的子资源的url
	 * @throws IOException
	 * @throws MalformedURLException
	 * @throws Exception
	 */
	public static String saveas(String bucketName, String key, String urlWithoutSchema) throws Exception {
		Assert.notNull(bucketName, "未指定空间名");
		if (StringUtils.isEmpty(key)) {
			key = "saveas_random_key_" + TimeUtils.getDateStr(new Date(), "yyyyMMddHHmmss");
			logger.info("未指定key,生成默认key:" + key);
		}

		if (urlWithoutSchema.startsWith("http://")) {
			urlWithoutSchema = urlWithoutSchema.substring(7);
		}

		String encodedEntryURI = UrlSafeBase64.encodeToString(bucketName + ":" + key);

		String signingStr = urlWithoutSchema + "|saveas/" + encodedEntryURI;

		byte[] enbytes = Signature.hmacSHA1Encrypt(signingStr, SECRET_KEY);

		String sign = UrlSafeBase64.encodeToString(enbytes);

		String fullUrl = "http://" + signingStr + "/sign/" + ACCESS_KEY + ":" + sign;
		HttpURLConnection connection = (HttpURLConnection) new URL(fullUrl).openConnection();
		try {
			int respCode = connection.getResponseCode();
			if (respCode == 200) {

				String domain = domainMap.get(bucketName);

				String newUrl = domain + key;

				// int i = urlWithoutSchema.indexOf("/");
				// String newUrl;
				// if (i != -1) {
				// newUrl = "http://" + urlWithoutSchema.substring(0, i) + "/" +
				// key;
				// } else {
				// newUrl = "http://" + urlWithoutSchema + "/" + key;
				// }
				return newUrl;
			}
		} catch (UnknownHostException e) {
			throw new ZhiZhiException( urlWithoutSchema,e);
		}

		return null;
	}

	public static void main(String[] args) throws Exception {
		String url = saveas("test-user", "qqqqqqq3.jpg",
				"http://7xlwpk.com1.z0.glb.clouddn.com/201611291322393504_1480419304179_generateImage_20161129193457_1480419297822_background.jpg_20161129193457_1480419297871_head.jpg?imageMogr2/auto-orient/rotate/300");
		System.out.println(url);

	}
}
