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
package io.github.opensabe.common.alive.client.message.enumeration;

public enum RetCode {
    SUCCESS(1),
    FAIL(2),
    CACHED(3),
    PARAM_ERR(4),
    NO_AUTH(5);

    private int val;

    RetCode(int val) {
        this.val = val;
    }

    public int getVal() {
        return val;
    }
}