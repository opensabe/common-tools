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

import io.github.opensabe.apple.appstoreconnectapi.ResourceLinks;
import lombok.Data;

/**
 * 订阅组
 */
@Data
public class SubscriptionGroup {

    /**
     * 订阅组.属性
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
     * 订阅组.关系
     */
    private Relationships relationships;

    /**
     * 必需的。值：subscriptionGroups
     */
    private String type;

    /**
     * 订阅组.属性
     */
    @Data
    public static class Attributes {

        private Boolean familySharable;

        /**
         * 订阅周期
         * 可能的值：
         * SIX_MONTHS
         */
        private String subscriptionPeriod;

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

        private Integer groupLevel;
    }

    /**
     * 订阅组.关系
     */
    @Data
    public static class Relationships {

        /**
         * 订阅组.本土化
         */
        private SubscriptionLocalizations subscriptionLocalizations;

        /**
         * 订阅组.Relationships.App Store评论截图
         */
        private AppStoreReviewScreenshot appStoreReviewScreenshot;

        /**
         * 订阅组.介绍人优惠
         */
        private IntroductoryOffers introductoryOffers;

        /**
         * 订阅组.促销优惠
         */
        private PromotionalOffers promotionalOffers;

        /**
         * 订阅组.提供代码
         */
        private OfferCodes offerCodes;

        /**
         * 订阅组.价格
         */
        private Prices prices;

        /**
         * 订阅组.价格点
         */
        private PricePoints pricePoints;

        /**
         * 订阅组.促销购买
         */
        private PromotedPurchase promotedPurchase;

        /**
         * 订阅组.订阅可用性
         */
        private SubscriptionAvailability subscriptionAvailability;

        /**
         * 订阅组.本土化
         */
        @Data
        public static class SubscriptionLocalizations {

            private SubscriptionLocalizations.Data data;

            private SubscriptionLocalizations.Links links;

            /**
             * 订阅组.本土化.数据
             */
            @lombok.Data
            static class Data {

                private String id;

                private String type;
            }

            /**
             * 订阅组.本土化.链接
             */
            @lombok.Data
            static class Links {

                private String related;

                private String self;
            }
        }

        /**
         * 订阅组.介绍人优惠
         */
        @Data
        public static class IntroductoryOffers {

            /**
             * 订阅组 .关系.应用程序内购买本地化.数据
             */
            private IntroductoryOffers.Data data;

            /**
             * 订阅组 .关系.应用程序内购买本地化.链接
             */
            private IntroductoryOffers.Links links;

            /**
             * 订阅组 .关系.应用程序内购买本地化.数据
             */
            @lombok.Data
            static class Data {

                private String id;

                private String type;
            }

            /**
             * 订阅组 .关系.应用程序内购买本地化.链接
             */
            @lombok.Data
            static class Links {

                private String related;

                private String self;
            }
        }

        /**
         * 订阅组.价格
         */
        @Data
        public static class Prices {
            /**
             * 订阅组.价格.数据
             */
            private IntroductoryOffers.Data data;

            /**
             * 订阅组.价格.链接
             */
            private IntroductoryOffers.Links links;

            /**
             * 订阅组.价格.数据
             */
            @lombok.Data
            static class Data {

                private String id;

                private String type;
            }

            /**
             * 订阅组.价格.链接
             */
            @lombok.Data
            static class Links {

                private String related;

                private String self;
            }
        }

        /**
         * 订阅组.价格点
         */
        @Data
        public static class PricePoints {
            /**
             * 订阅组.价格点.数据
             */
            private IntroductoryOffers.Data data;

            /**
             * 订阅组.价格点.链接
             */
            private IntroductoryOffers.Links links;

            /**
             * 订阅组.价格点.数据
             */
            @lombok.Data
            static class Data {

                private String id;

                private String type;
            }

            /**
             * 订阅组.价格点.链接
             */
            @lombok.Data
            static class Links {

                private String related;

                private String self;
            }
        }

        /**
         * 订阅组.促销购买
         */
        @Data
        public static class PromotedPurchase {

            private Relationships.PromotedPurchase.Data data;

            private Relationships.PromotedPurchase.Links links;

            /**
             * 订阅组.促销购买.数据
             */
            @lombok.Data
            static class Data {

                private String id;

                private String type;
            }

            /**
             * 订阅组.促销购买.链接
             */
            @lombok.Data
            static class Links {

                private String related;

                private String self;
            }
        }

        /**
         * 订阅组.订阅可用性
         */
        @Data
        public static class SubscriptionAvailability {

            private Relationships.PromotedPurchase.Data data;

            private Relationships.PromotedPurchase.Links links;

            /**
             * 订阅组.订阅可用性.数据
             */
            @lombok.Data
            static class Data {

                private String id;

                private String type;
            }

            /**
             * 订阅组.订阅可用性.链接
             */
            @lombok.Data
            static class Links {

                private String related;

                private String self;
            }
        }

        /**
         * 订阅组.Relationships.App Store评论截图
         */
        @Data
        public static class AppStoreReviewScreenshot {

            private Relationships.AppStoreReviewScreenshot.Data data;

            private Relationships.AppStoreReviewScreenshot.Links links;

            /**
             * 订阅组.Relationships.App Store评论截图.数据
             */
            @lombok.Data
            static class Data {

                private String id;

                private String type;
            }

            /**
             * 订阅组.Relationships.App Store评论截图.链接
             */
            @lombok.Data
            static class Links {

                private String related;

                private String self;
            }
        }

        /**
         * 订阅组.提供代码
         */
        @Data
        public static class OfferCodes {

            private OfferCodes.Data data;

            private OfferCodes.Links links;

            /**
             * 订阅组.提供代码.数据
             */
            @lombok.Data
            static class Data {

                private String id;

                private String type;
            }

            /**
             * 订阅组.提供代码.链接
             */
            @lombok.Data
            static class Links {

                private String related;

                private String self;
            }
        }

        /**
         * 订阅组.促销优惠
         */
        @Data
        public static class PromotionalOffers {

            private PromotionalOffers.Data data;

            private PromotionalOffers.Links links;

            /**
             * 订阅组.促销优惠.数据
             */
            @lombok.Data
            static class Data {

                private String id;

                private String type;
            }

            /**
             * 订阅组.促销优惠.链接
             */
            @lombok.Data
            static class Links {

                private String related;

                private String self;
            }
        }
    }
}
