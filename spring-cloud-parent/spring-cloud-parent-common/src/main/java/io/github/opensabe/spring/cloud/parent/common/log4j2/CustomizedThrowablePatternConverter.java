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
