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
package io.github.opensabe.spring.boot.starter.rocketmq.test.common;

import com.github.javafaker.Faker;

public record MessageRecordPojo(
    String text, Integer number, Long timestamp, Double cost
) {


    public static MessageRecordPojo getSecretMessageRecordPojo() {
        return new MessageRecordPojo(
                "this is a secret message",
                new Faker().number().randomDigit(),
                System.currentTimeMillis(),
                new Faker().number().randomDouble(10, 1, 1000)
        );
    }
}
