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
package io.github.opensabe.apple.appstoreconnectapi.subscriptiongroup;

import io.github.opensabe.apple.appstoreconnectapi.PagedDocumentLinks;
import io.github.opensabe.apple.appstoreconnectapi.PagingInformation;
import lombok.Data;

import java.util.List;

/**
 * 订阅组响应
 */
@Data
public class SubscriptionGroupsResponse {

    /**
     * 应用内购买V2 集合
     */
    private List<SubscriptionGroup> data;

    /**
     * 分页文档链接
     */
    private PagedDocumentLinks links;

    /**
     * 数据响应的分页信息。
     */
    private PagingInformation meta;
}
