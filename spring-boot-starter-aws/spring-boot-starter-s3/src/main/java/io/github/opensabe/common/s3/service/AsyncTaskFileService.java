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
import java.util.concurrent.Future;
import java.util.function.Consumer;

public interface AsyncTaskFileService {

    /**
     * 上传字节数组，返回future
     *
     * @param lockKey  分布式锁key
     * @param provider 提供字节数组的function
     * @param country  ng,gh
     * @param fileName xxx.xlsx
     * @return
     */
    Future<String> upload(Provider<byte[]> provider, String country, String fileName);

    /**
     * 长传字节数组，以及回调函数
     *
     * @param lockKey  分布式锁key
     * @param provider 提供字节数组的function
     * @param country  ng,gh
     * @param fileName xxx.xlsx
     * @param consumer 回调函数，参数为上传后的文件路径
     */
    void upload(Provider<byte[]> provider, String country, String fileName, Consumer<String> consumer);


    /**
     * 上传file
     *
     * @param lockKey
     * @param provider
     * @param country
     * @param fileName
     * @return
     */
    Future<String> uploadFile(Provider<File> provider, String country, String fileName);


    /**
     * 上传file
     *
     * @param lockKey
     * @param provider
     * @param country
     * @param fileName
     * @param consumer
     */
    void uploadFile(Provider<File> provider, String country, String fileName, Consumer<String> consumer);
}
