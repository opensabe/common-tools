/*
 * Copyright 2025 opensabe-tech
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.opensabe.common.s3.service;

import java.io.File;
import java.util.List;

/**
 * 默认bucket
 *
 * @author maheng
 *
 */
public abstract class BucketS3FileService implements FileService {

/** defaultBucket。 */
    private String defaultBucket;

    /** {@inheritDoc} */
    @Override
    public String putObject(File file, String profile) {
        return putObject(file, defaultBucket, profile);
    }

    /** {@inheritDoc} */
    @Override
    public String putObject(byte[] source, String profile, String fileName) {
        return putObject(source, defaultBucket, profile, fileName);
    }

    /** {@inheritDoc} */
    @Override
    public byte[] getObject(String key) {
        return getObject(key, defaultBucket);
    }

    /** {@inheritDoc} */
    @Override
    public List<String> listObjects(String basePath) {
        return listObjects(basePath, defaultBucket);
    }

    /**
     * @return defaultBucket
     */
    public String getDefaultBucket() {
        return defaultBucket;
    }

    /**
     * @param defaultBucket 待设置值
     */
    public void setDefaultBucket(String defaultBucket) {
        this.defaultBucket = defaultBucket;
    }

    /** {@inheritDoc} */
    @Override
    public void deleteObject(String key) {
        deleteObject(key, defaultBucket);
    }
}
