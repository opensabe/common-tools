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

package io.github.opensabe.common.internalcall;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.opensabe.common.utils.AlarmUtil;
import lombok.extern.log4j.Log4j2;

import java.util.concurrent.TimeUnit;

/**
 * Observe-only failure reporting. Never changes HTTP response. Swallows its own errors.
 */
@Log4j2
public class InternalCallFailureHandler {

    private static final String ALARM_TEMPLATE =
            "InternalCallFailureHandler-report: internal-call failure reason={} peer={} path={} caller={}";
    private static final String WARN_TEMPLATE =
            "InternalCallFailureHandler-report: internal-call failure reason={} peer={} path={} caller={}";

    private final InternalCallProperties properties;
    private final Cache<String, Boolean> dedup;

    public InternalCallFailureHandler(InternalCallProperties properties) {
        this.properties = properties;
        int seconds = properties.getFailure() == null ? 60 : Math.max(1, properties.getFailure().getAlarmDedupSeconds());
        this.dedup = Caffeine.newBuilder()
                .expireAfterWrite(seconds, TimeUnit.SECONDS)
                .maximumSize(10_000)
                .build();
    }

    public void report(
            InternalCallFailureReason reason,
            String peer,
            String path,
            String caller,
            InternalCallProperties.OnFailure onFailure
    ) {
        try {
            InternalCallFailureReason safeReason = reason == null ? InternalCallFailureReason.INTERNAL_ERROR : reason;
            String safePeer = truncate(peer, 128);
            String safePath = truncate(stripQuery(path), 256);
            String safeCaller = truncate(caller, 128);
            // path 不参与去重：/orders/{id} 等高基数路径会绕过去重
            String fingerprint = safeReason.name() + '|' + safePeer + '|' + safeCaller;
            if (dedup.asMap().putIfAbsent(fingerprint, Boolean.TRUE) != null) {
                return;
            }

            InternalCallProperties.OnFailure mode = onFailure == null ? properties.getOnFailure() : onFailure;
            if (mode == InternalCallProperties.OnFailure.ALARM) {
                AlarmUtil.fatal(ALARM_TEMPLATE, safeReason.name(), safePeer, safePath, safeCaller);
            } else {
                log.warn(WARN_TEMPLATE, safeReason.name(), safePeer, safePath, safeCaller);
            }
        } catch (Throwable t) {
            try {
                log.warn("InternalCallFailureHandler-report: failed to report: {}", t.toString());
            } catch (Throwable ignored) {
                // swallow
            }
        }
    }

    public void report(InternalCallFailureReason reason, String peer, String path, String caller) {
        report(reason, peer, path, caller, properties.getOnFailure());
    }

    static String stripQuery(String path) {
        if (path == null) {
            return "";
        }
        int q = path.indexOf('?');
        return q >= 0 ? path.substring(0, q) : path;
    }

    static String truncate(String value, int max) {
        if (value == null) {
            return "";
        }
        if (value.length() <= max) {
            return value;
        }
        return value.substring(0, max);
    }
}
