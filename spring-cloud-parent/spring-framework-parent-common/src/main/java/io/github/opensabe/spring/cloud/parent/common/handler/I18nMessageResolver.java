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
package io.github.opensabe.spring.cloud.parent.common.handler;

import java.util.Locale;
import java.util.function.Supplier;

import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;

/**
 * 国际化消息模板解析
 *
 * @author maheng
 * @see ResourceBundleMessageSource
 * @see LocaleContextHolder
 */
public class I18nMessageResolver {

    private final Supplier<Locale> localeSupplier;

    private final MessageSource messageSource;

    public I18nMessageResolver(MessageSource messageSource) {
        this(LocaleContextHolder::getLocale, messageSource);
    }

    /**
     * @param localeSupplier 怎样获取语言
     * @param messageSource  国际化消息集合
     */
    public I18nMessageResolver(Supplier<Locale> localeSupplier, MessageSource messageSource) {
        this.localeSupplier = localeSupplier;
        this.messageSource = messageSource;
    }


    /**
     * 格式化消息
     *
     * @param messageTemplate 消息模板
     * @param args            消息站位符参数
     * @return i18n message
     */
    public String resolveMessageTemplate(String messageTemplate, Object... args) {
        try {
            return messageSource.getMessage(messageTemplate, args, localeSupplier.get());
        } catch (NoSuchMessageException e) {
            return messageTemplate;
        }
    }
}
