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