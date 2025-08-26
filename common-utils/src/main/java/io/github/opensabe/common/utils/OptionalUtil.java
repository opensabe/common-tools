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
package io.github.opensabe.common.utils;

//import lombok.extern.log4j.Log4j2;

import java.util.function.Supplier;

//@Log4j2
public class OptionalUtil {
    /**
     * 忽略Exception的获取
     *
     * @param supplier
     * @param <T>
     * @return 如果有空指针，返回null
     */
    public static <T> T orNull(Supplier<T> supplier) {
        try {
            return supplier.get();
        } catch (Throwable e) {
//            log.warn("ignored exception and return null: {}", e.getStackTrace()[0].toString());
            return null;
        }
    }

    /**
     * 忽略Exception的获取
     *
     * @param supplier
     * @param or
     * @param <T>
     * @return 如果有空指针，返回or
     */
    public static <T> T or(Supplier<T> supplier, T or) {
        try {
            T t = supplier.get();
            if (t != null) {
                return t;
            }
            return or;
        } catch (Throwable e) {
//            log.warn("ignored exception and return or: {}", e.getStackTrace()[0].toString());
            return or;
        }
    }
}
