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

import java.util.concurrent.ThreadLocalRandom;

public class InviteCodeUtil {

    public static String[] RANDOM_AREA = new String[]{"2", "3", "4", "5", "6", "7", "8", "9", "Q", "W", "E", "R", "T", "Y", "U", "O", "P", "A", "S", "D", "F", "G", "H", "J", "K", "Z", "X", "C", "V", "B", "N", "M"};

    public static final ThreadLocalRandom THREAD_LOCAL_RANDOM = ThreadLocalRandom.current();
    public static String generateInviteCode(int length) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int nextInt = THREAD_LOCAL_RANDOM.nextInt(RANDOM_AREA.length);
            result.append(RANDOM_AREA[nextInt]);
        }
        return result.toString();
    }

}
