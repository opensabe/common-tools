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
package io.github.opensabe.common.secret;

import jakarta.annotation.Nullable;
import org.springframework.core.Ordered;

/**
 * 为了支持多种 key 延展算法例如：ecb，cbc，kdf 等。使用spi 加载方式
 * 如果使用kdf等加盐的方式，需要自己从 cipher中解析salt
 * @see org.springframework.core.io.support.SpringFactoriesLoader#loadFactories(Class, ClassLoader) 
 * @author maheng
 */
public interface Decryptor extends Ordered {

    /**
     * 使用秘钥解密，如果解密失败直接返回null
     * @param encrypted 加密后的内容
     * @param cipher    秘钥
     * @return          解密后的内容，如果解密失败，返回null
     */
    @Nullable
    String decrypt(String encrypted, String cipher);
}
