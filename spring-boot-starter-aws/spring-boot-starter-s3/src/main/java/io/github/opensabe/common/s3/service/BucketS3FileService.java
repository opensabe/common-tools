package io.github.opensabe.common.s3.service;

import java.io.File;
import java.util.List;

/**
 * 默认bucket
 * @author maheng
 *
 */
public abstract class BucketS3FileService implements FileService{

	private String defaultBucket;

	@Override
	public String putObject(File file, String profile) {
		return putObject(file, defaultBucket, profile);
	}

	@Override
	public String putObject(byte[] source, String profile, String fileName) {
		return putObject(source, defaultBucket, profile, fileName);
	}

	@Override
	public byte[] getObject(String key) {
		return getObject(key, defaultBucket);
	}

	@Override
	public List<String> listObjects(String basePath) {
		return listObjects(basePath, defaultBucket);
	}

	public String getDefaultBucket() {
		return defaultBucket;
	}

	public void setDefaultBucket(String defaultBucket) {
		this.defaultBucket = defaultBucket;
	}

	@Override
	public void deleteObject(String key) {
		deleteObject(key,defaultBucket);
	}
}
