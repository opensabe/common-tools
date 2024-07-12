package io.github.opensabe.spring.cloud.parent.common.log4j2;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.impl.CustomizedThrowableProxyRenderer;
import org.apache.logging.log4j.core.impl.ThrowableProxy;
import org.apache.logging.log4j.core.pattern.ConverterKeys;
import org.apache.logging.log4j.core.pattern.PatternConverter;
import org.apache.logging.log4j.core.pattern.ThrowablePatternConverter;

@Plugin(name = "CustomizedThrowablePatternConverter", category = PatternConverter.CATEGORY)
@ConverterKeys({ "cusEx", "cusThrowable", "cusException" })
public class CustomizedThrowablePatternConverter extends ThrowablePatternConverter {
	public static CustomizedThrowablePatternConverter newInstance(final Configuration config, final String[] options) {
		return new CustomizedThrowablePatternConverter(config, options);
	}
	private CustomizedThrowablePatternConverter(final Configuration config, final String[] options) {
		super("CustomizedThrowable", "throwable", options, config);
	}

	@Override
	public void format(final LogEvent event, final StringBuilder toAppendTo) {
		final ThrowableProxy proxy = event.getThrownProxy();
		final Throwable throwable = event.getThrown();
		if ((throwable != null || proxy != null) && options.anyLines()) {
			if (proxy == null) {
				super.format(event, toAppendTo);
				return;
			}
			final int len = toAppendTo.length();
			if (len > 0 && !Character.isWhitespace(toAppendTo.charAt(len - 1))) {
				toAppendTo.append(' ');
			}
			CustomizedThrowableProxyRenderer.formatExtendedStackTraceTo(proxy, toAppendTo, options.getIgnorePackages(),
					options.getTextRenderer(), getSuffix(event), options.getSeparator());
		}
	}
}
