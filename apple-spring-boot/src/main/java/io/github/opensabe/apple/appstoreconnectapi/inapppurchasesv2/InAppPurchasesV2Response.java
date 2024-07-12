package io.github.opensabe.apple.appstoreconnectapi.inapppurchasesv2;

import io.github.opensabe.apple.appstoreconnectapi.PagedDocumentLinks;
import io.github.opensabe.apple.appstoreconnectapi.PagingInformation;
import lombok.Data;

import java.util.List;

/**
 * 应用内购买V2 Response对象
 */
@Data
public class InAppPurchasesV2Response {

    /**
     * 应用内购买V2 集合
     */
    private List<InAppPurchaseV2> data;

    /**
     * 分页文档链接
     */
    private PagedDocumentLinks links;

    /**
     * 数据响应的分页信息。
     */
    private PagingInformation meta;
}
