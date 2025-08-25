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
package io.github.opensabe.youtobe.constants;

/**
 * 使用google data api查询youtobe资源中type参数支持的类型
 * 详情见：https://developers.google.com/youtube/v3/docs/search/list?hl=zh-cn&apix_params=%7B%22part%22%3A%5B%22snippet%22%5D%2C%22q%22%3A%22HIGHTLIGHTS%22%7D
 */
public enum SearchTypeEnum {

    VIDEO("video"),
    CHANNEL("channel"),
    PLAYLIST("playlist"),

    ;

    private String name;

    public String getName() {
        return name;
    }

    SearchTypeEnum(String name) {
        this.name = name;
    }
}
