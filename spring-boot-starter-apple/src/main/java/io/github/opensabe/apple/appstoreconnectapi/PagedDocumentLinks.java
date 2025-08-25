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
 * 分页文档链接
 * 与响应文档相关的链接，包括分页链接。
 */
@Data
public class PagedDocumentLinks {

    /**
     * 文档第一页的链接。
     */
    private String first;

    /**
     * 文档下一页的链接。
     */
    private String next;

    /**
     * 生成当前文档的链接。
     */
    private String self;

}