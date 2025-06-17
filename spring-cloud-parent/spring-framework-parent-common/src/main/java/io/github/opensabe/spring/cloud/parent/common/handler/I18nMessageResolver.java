package io.github.opensabe.spring.cloud.parent.common.handler;

import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;

import java.util.Locale;
import java.util.function.Supplier;

/**
 * 国际化消息模板解析
 * @see ResourceBundleMessageSource
 * @see LocaleContextHolder
 * @author maheng
 */
public class I18nMessageResolver {

    private final Supplier<Locale> localeSupplier;

    private final MessageSource messageSource;

    public I18nMessageResolver(MessageSource messageSource) {
        this(LocaleContextHolder::getLocale, messageSource);
    }

    /**
     * @param localeSupplier    怎样获取语言
     * @param messageSource     国际化消息集合
     */
    public I18nMessageResolver(Supplier<Locale> localeSupplier, MessageSource messageSource) {
        this.localeSupplier = localeSupplier;
        this.messageSource = messageSource;
    }


    /**
     * 格式化消息
     * @param messageTemplate   消息模板
     * @param args              消息站位符参数
     * @return  i18n message
     */
    public String resolveMessageTemplate (String messageTemplate, Object ... args) {
        try {
            return messageSource.getMessage(messageTemplate, args, localeSupplier.get());
        }catch (NoSuchMessageException e) {
            return messageTemplate;
        }
    }
}
