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
