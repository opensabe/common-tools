package io.github.opensabe.common.s3.service;

import java.io.File;
import java.util.List;

public interface FileService {
	/**
	 * 上传文件
	 * @param file	文件
	 * @param bucket	桶
	 * @param profile	local,test
	 * @param fileName 文件名
	 * @return	文件目录
	 */
	String putObject(File file, String bucket, String profile,String fileName);
	default String putObject(File file, String bucket, String profile) {
		return putObject(file, bucket, profile, file.getName());
	}
	String putObject(File file, String profile);
	
	/**
	 * 上传文件
	 * @param source	文件
	 * @param bucket	桶
	 * @param profile	local,test
	 * @param fileName	文件名
	 * @return	文件目录
	 */
	String putObject(byte[] source, String bucket, String profile, String fileName);
	String putObject(byte[] source, String profile, String fileName);
	void putObjectAssignedPath(byte[] source, String fileName,String contentType);
	/**
	 * 下载文件
	 * @param key	文件路径
	 * @param bucket	桶
	 * @return 文件流
	 */
	byte[] getObject(String key,String bucket);
	byte[] getObject(String key);
	
	List<String> listObjects(String basePath,String bucket);
	List<String> listObjects(String basePath);
	List<String> listObjectsByPrefix(String prefix,String bucket);

	void deleteObject(String key,String bucket);
	void deleteObject(String key);
}
