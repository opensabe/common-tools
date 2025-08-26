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
package io.github.opensabe.apple.appstoreconnectapi.inapppurchasesv2;

import io.github.opensabe.apple.appstoreconnectapi.PagingInformation;
import io.github.opensabe.apple.appstoreconnectapi.ResourceLinks;
import lombok.Data;

/**
 * 应用内购买V2
 */
@Data
public class InAppPurchaseV2 {

    /**
     * 应用内购买V2 .属性
     */
    private Attributes attributes;

    /**
     * 必需的
     */
    private String id;

    /**
     * 资源链接
     */
    private ResourceLinks links;

    /**
     * 应用内购买V2 .关系
     */
    private Relationships relationships;

    /**
     * 必需的。值：inAppPurchases
     */
    private String type;

    /**
     * 应用内购买V2 .属性
     */
    @Data
    public static class Attributes {

        private Boolean contentHosting;

        private Boolean familySharable;

        /**
         * 应用内购买类型
         * 可能的值:
         * CONSUMABLE
         * NON_CONSUMABLE
         * NON_RENEWING_SUBSCRIPTION
         */
        private String inAppPurchaseType;

        private String name;

        private String productId;

        private String reviewNote;

        /**
         * 在应用程序购买状态
         * 可能的值:
         * APPROVED
         * DEVELOPER_ACTION_NEEDED
         * DEVELOPER_REMOVED_FROM_SALE
         * IN_REVIEW
         * MISSING_METADATA
         * PENDING_BINARY_APPROVAL
         * PROCESSING_CONTENT
         * READY_TO_SUBMIT
         * REJECTED
         * REMOVED_FROM_SALE
         * WAITING_FOR_REVIEW
         * WAITING_FOR_UPLOAD
         */
        private String state;
    }

    /**
     * 应用内购买V2 .关系
     */
    @Data
    public static class Relationships {

        /**
         * 应用程序内购买V2 .关系.内容
         */
        private Content content;

        /**
         * 应用程序内购买V2.关系.应用程序内购买本地化
         */
        private InAppPurchaseLocalizations inAppPurchaseLocalizations;

        /**
         * 应用内购买V2 .关系.价格点
         */
        private PricePoints pricePoints;

        /**
         * 应用内购买V2 .关系.促销购买
         */
        private PromotedPurchase promotedPurchase;

        /**
         * 应用内购买V2.Relationships.App Store评论截图
         */
        private AppStoreReviewScreenshot appStoreReviewScreenshot;

        /**
         * 应用内购买V2.Relationships.Iap价格表
         */
        private IapPriceSchedule iapPriceSchedule;

        /**
         * 应用程序内购买V2.关系.应用程序内购买可用性
         */
        private InAppPurchaseAvailability inAppPurchaseAvailability;

        /**
         * 应用程序内购买V2 .关系.内容
         */
        @Data
        public static class Content {

            private Data data;

            private Links links;

            /**
             * 应用程序内购买V2 .关系.内容.数据
             */
            @lombok.Data
            static class Data {

                private String id;

                private String type;
            }

            /**
             * 应用内购买V2 .关系.内容.链接
             */
            @lombok.Data
            static class Links {

                private String related;

                private String self;
            }
        }

        /**
         * 应用程序内购买V2.关系.应用程序内购买本地化
         */
        @Data
        public static class InAppPurchaseLocalizations {

            /**
             * 应用程序内购买V2 .关系.应用程序内购买本地化.数据
             */
            private Data data;

            /**
             * 应用程序内购买V2 .关系.应用程序内购买本地化.链接
             */
            private Links links;

            /**
             * 数据响应的分页信息。
             */
            private PagingInformation meta;

            /**
             * 应用程序内购买V2 .关系.应用程序内购买本地化.数据
             */
            @lombok.Data
            static class Data {

                private String id;

                private String type;
            }

            /**
             * 应用程序内购买V2 .关系.应用程序内购买本地化.链接
             */
            @lombok.Data
            static class Links {

                private String related;

                private String self;
            }
        }

        /**
         * 应用内购买V2 .关系.价格点
         */
        @Data
        public static class PricePoints {
            /**
             * 应用内购买V2 .关系.价格点.数据
             */
            private InAppPurchaseLocalizations.Data data;

            /**
             * 应用内购买V2 .关系.价格点.链接
             */
            private InAppPurchaseLocalizations.Links links;

            /**
             * 数据响应的分页信息。
             */
            private PagingInformation meta;

            /**
             * 应用内购买V2 .关系.价格点.数据
             */
            @lombok.Data
            static class Data {

                private String id;

                private String type;
            }

            /**
             * 应用内购买V2 .关系.价格点.链接
             */
            @lombok.Data
            static class Links {

                private String related;

                private String self;
            }
        }

        /**
         * 应用内购买V2 .关系.促销购买
         */
        @Data
        public static class PromotedPurchase {

            private Data data;

            private Links links;

            /**
             * 应用内购买V2 .关系.促销购买.数据
             */
            @lombok.Data
            static class Data {

                private String id;

                private String type;
            }

            /**
             * 应用内购买V2 .关系.促销购买.链接
             */
            @lombok.Data
            static class Links {

                private String related;

                private String self;
            }
        }

        /**
         * 应用内购买V2.Relationships.App Store评论截图
         */
        @Data
        public static class AppStoreReviewScreenshot {

            private Data data;

            private Links links;

            /**
             * 应用内购买V2.Relationships.App Store评论截图.数据
             */
            @lombok.Data
            static class Data {

                private String id;

                private String type;
            }

            /**
             * 应用内购买V2.Relationships.App Store评论截图.链接
             */
            @lombok.Data
            static class Links {

                private String related;

                private String self;
            }
        }

        /**
         * 应用内购买V2.Relationships.Iap价格表
         */
        @Data
        public static class IapPriceSchedule {

            private Data data;

            private Links links;

            /**
             * 应用内购买V2.Relationships.Iap价格表.数据
             */
            @lombok.Data
            static class Data {

                private String id;

                private String type;
            }

            /**
             * 应用内购买V2.Relationships.Iap价格表.链接
             */
            @lombok.Data
            static class Links {

                private String related;

                private String self;
            }
        }

        /**
         * 应用程序内购买V2.关系.应用程序内购买可用性
         */
        @Data
        public static class InAppPurchaseAvailability {

            private Data data;

            private Links links;

            /**
             * 应用程序内购买V2.关系.应用程序内购买可用性.数据
             */
            @lombok.Data
            static class Data {

                private String id;

                private String type;
            }

            /**
             * 应用程序内购买V2.关系.应用程序内购买可用性.链接
             */
            @lombok.Data
            static class Links {

                private String related;

                private String self;
            }
        }
    }
}
