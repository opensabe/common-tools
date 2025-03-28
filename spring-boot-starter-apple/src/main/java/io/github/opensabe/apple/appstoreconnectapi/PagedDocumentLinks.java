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