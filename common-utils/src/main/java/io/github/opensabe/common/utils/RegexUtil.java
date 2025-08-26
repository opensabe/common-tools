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

import java.util.regex.Pattern;

/**
 * Created by houpu on 2017/7/26.
 */
public class RegexUtil {

    private static final Pattern DOMAIN_PATTERN = Pattern.compile("(?<=http://|\\.)[^.]*?\\.(com|cn|net|org|biz|info|cc|tv)", Pattern.CASE_INSENSITIVE);
    private static final Pattern HOST_PATTERN = Pattern.compile("(http://|https://)?([^/]*)", Pattern.CASE_INSENSITIVE);

    /**
     * 验证Email
     *
     * @param email email地址，格式：zhangsan@zuidaima.com，zhangsan@xxx.com.cn，xxx代表邮件服务商
     * @return 验证成功返回true，验证失败返回false
     */
    public static boolean checkEmail(String email) {
        String regex = "[\\w\\.\\-]+@\\w+\\.[a-zA-Z]+(\\.[a-zA-Z]+)*";
        return Pattern.matches(regex, email);
    }
}
