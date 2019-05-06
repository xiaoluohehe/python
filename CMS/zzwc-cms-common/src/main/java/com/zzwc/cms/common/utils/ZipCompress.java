package com.zzwc.cms.common.utils;

import com.zzwc.cms.common.exception.ZhiZhiException;
import org.apache.commons.compress.archivers.zip.Zip64Mode;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class ZipCompress {

	/**
	 * 将文件（文件或文件夹）打包压缩为zip文件
	 * 
	 * @param source
	 *            被文件或目录
	 * @param targetPath
	 *            目标zip文件路径
	 * @return 压缩打包好的zip文件
	 *
	 */
	public static File compressFile2Zip(File source, String targetPath) {

		// 1.检查目标文件的命名是否正确
		if (!targetPath.toLowerCase().endsWith(".zip")) {
			throw new ZhiZhiException( "文件名格式错误");
		}

		File targetFile = new File(targetPath);

		try (ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(targetFile)) {
			zaos.setUseZip64(Zip64Mode.AsNeeded);

			doZip(source, zaos);

			zaos.finish();

		} catch (IOException e) {
			e.printStackTrace();
		}
		return targetFile;
	}

	/**
	 * <p>
	 * 压缩文件（加）
	 * </p>
	 * 如果要被压缩的文件是文件夹，则将文件夹里的内容递归地放到同一级下压缩打包
	 * 
	 * @param source
	 * @param zaos
	 *
	 */
	private static void doZip(File source, ZipArchiveOutputStream zaos) {
		if (source.isDirectory()) {
			File[] files = source.listFiles();
			if (files != null) {
				for (File file : files) {
					doZip(file, zaos);
				}
			}
		} else {
			ZipArchiveEntry zipArchiveEntry = new ZipArchiveEntry(source, source.getName());
			try {
				zaos.putArchiveEntry(zipArchiveEntry);

				try (InputStream is = new FileInputStream(source)) {
					byte[] buffer = new byte[1024 * 5];
					int len = -1;
					while ((len = is.read(buffer)) != -1) {
						// 把缓冲区的字节写入到ZipArchiveEntry
						zaos.write(buffer, 0, len);
					}
					zaos.closeArchiveEntry();
				} catch (Exception e) {
					e.printStackTrace();
					throw new RuntimeException(e);
				}

			} catch (IOException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}

	}
}
