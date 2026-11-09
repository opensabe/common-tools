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
package io.github.opensabe.common.jfr.metrics;

import java.util.List;

import jdk.jfr.consumer.RecordedFrame;
import jdk.jfr.consumer.RecordedMethod;
import jdk.jfr.consumer.RecordedStackTrace;

/**
 * Picks one stable {@code Class#method} label from a stack; copies Strings so Recorded* can be reused.
 */
final class StackFrameNormalizer {

    private static final String JVM_INTERNAL = "jvm.internal";
    private static final int MAX_LABEL_LENGTH = 180;

    private final List<String> businessPackages;

    StackFrameNormalizer(List<String> businessPackages) {
        this.businessPackages = businessPackages == null ? List.of() : List.copyOf(businessPackages);
    }

    String normalize(RecordedStackTrace stackTrace) {
        if (stackTrace == null) {
            return JVM_INTERNAL;
        }
        List<RecordedFrame> frames = stackTrace.getFrames();
        if (frames == null || frames.isEmpty()) {
            return JVM_INTERNAL;
        }
        if (!businessPackages.isEmpty()) {
            for (RecordedFrame frame : frames) {
                String label = toLabel(frame);
                if (label != null && matchesBusiness(label)) {
                    return truncate(label);
                }
            }
        }
        for (RecordedFrame frame : frames) {
            String label = toLabel(frame);
            if (label != null && !isSkipped(label)) {
                return truncate(label);
            }
        }
        String first = toLabel(frames.get(0));
        return first == null ? JVM_INTERNAL : truncate(first);
    }

    private boolean matchesBusiness(String label) {
        for (String prefix : businessPackages) {
            if (prefix != null && !prefix.isBlank() && label.startsWith(prefix.trim())) {
                return true;
            }
        }
        return false;
    }

    private static boolean isSkipped(String label) {
        return label.startsWith("java.")
                || label.startsWith("javax.")
                || label.startsWith("jdk.")
                || label.startsWith("sun.")
                || label.startsWith("com.sun.")
                || label.startsWith("reactor.")
                || label.startsWith("io.netty.");
    }

    private static String toLabel(RecordedFrame frame) {
        if (frame == null || !frame.isJavaFrame()) {
            return null;
        }
        RecordedMethod method = frame.getMethod();
        if (method == null || method.getType() == null) {
            return null;
        }
        String typeName = method.getType().getName();
        String methodName = method.getName();
        if (typeName == null || methodName == null) {
            return null;
        }
        return typeName + "#" + methodName;
    }

    private static String truncate(String label) {
        if (label.length() <= MAX_LABEL_LENGTH) {
            return label;
        }
        return label.substring(0, MAX_LABEL_LENGTH);
    }
}
