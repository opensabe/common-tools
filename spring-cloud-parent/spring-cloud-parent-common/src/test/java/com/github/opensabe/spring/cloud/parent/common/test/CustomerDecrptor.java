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
package com.github.opensabe.spring.cloud.parent.common.test;

import io.github.opensabe.common.secret.Decryptor;
import org.jspecify.annotations.Nullable;
import org.springframework.core.Ordered;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 测试用 SPI Decryptor，记录调用次数并返回 null 以触发下一解密器。
 */
public class CustomerDecrptor implements Decryptor {

    /** 解密调用计数。 */
    private static AtomicInteger run = new AtomicInteger(0);

    @Override
    public @Nullable String decrypt(String encrypted, String cipher) {
        run.incrementAndGet();
        return null;
    }


    /** @return 解密方法被调用次数 */
    int getRun() {
        return run.get();
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
