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
package io.github.opensabe.apple.appstoreconnectapi;

import lombok.Data;

/**
 * 数据响应的分页信息。
 */
@Data
public class PagingInformation {

    private Paging paging;

    /**
     * 分页详细信息，例如资源总数和每页限制。
     */
    @Data
    private static class Paging {

        /**
         * 符合您请求的资源总数。
         */
        private Integer total;

        /**
         * 每页返回的最大资源数，从 0 到 200。
         */
        private Integer limit;
    }
}