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
package io.github.opensabe.spring.cloud.parent.common.system;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Lightweight cmdline masking for process-monitor logs only.
 * Covers curl-style header/data flags and HTTP URL query sensitive keys.
 */
public final class ProcessCmdlineMasker {

    public static final String MASK = "******";

    private static final int KEEP_EDGE = 2;
    private static final int MAX_CHARS = 100_000;

    private static final String[] HEADER_FLAGS = {"--header", "-H"};
    private static final String[] DATA_FLAGS = {
            "--data-urlencode", "--data-binary", "--data-raw", "--data", "-d"
    };

    private static final Pattern HTTP_URL_PATTERN =
            Pattern.compile("https?://[^\\s\"']+", Pattern.CASE_INSENSITIVE);

    private ProcessCmdlineMasker() {
    }

    public static String mask(String cmdline) {
        try {
            if (cmdline == null || cmdline.isEmpty()) {
                return cmdline;
            }
            if (cmdline.length() > MAX_CHARS) {
                return "[truncated,len=" + cmdline.length() + "]";
            }
            String result = maskCurlFlagArgs(cmdline, HEADER_FLAGS, ProcessCmdlineMasker::maskHeaderArg);
            result = maskCurlFlagArgs(result, DATA_FLAGS, ProcessCmdlineMasker::maskDataArg);
            result = maskHttpUrlQueries(result);
            return result;
        } catch (Throwable t) {
            return cmdline;
        }
    }

    static String maskValue(String raw) {
        if (raw == null || raw.isEmpty()) {
            return raw;
        }
        int len = raw.length();
        if (len <= KEEP_EDGE * 2) {
            return MASK;
        }
        return raw.substring(0, KEEP_EDGE) + MASK + raw.substring(len - KEEP_EDGE);
    }

    private static String maskHeaderArg(String headerArg) {
        if (headerArg == null || headerArg.isEmpty()) {
            return headerArg;
        }
        int colon = headerArg.indexOf(':');
        if (colon <= 0) {
            return headerArg;
        }
        String name = headerArg.substring(0, colon).trim();
        String value = headerArg.substring(colon + 1).trim();
        if (isSensitiveKey(name)) {
            return name + ": " + maskValue(value);
        }
        return headerArg;
    }

    private static String maskDataArg(String data) {
        if (data == null || data.isEmpty()) {
            return data;
        }
        if (data.contains("=") && (data.contains("&") || !data.contains("{"))) {
            return maskFormUrlEncoded(data);
        }
        return data;
    }

    private static boolean isSensitiveKey(String key) {
        if (key == null || key.isBlank()) {
            return false;
        }
        String n = key.toLowerCase(Locale.ROOT).replace("_", "").replace("-", "");
        return n.contains("token")
                || n.contains("password")
                || n.contains("secret")
                || n.contains("authorization")
                || n.contains("auth")
                || n.contains("apikey")
                || n.contains("cookie");
    }

    private static String maskFormUrlEncoded(String form) {
        String[] pairs = form.split("&");
        StringBuilder sb = new StringBuilder(form.length());
        for (int i = 0; i < pairs.length; i++) {
            if (i > 0) {
                sb.append('&');
            }
            String pair = pairs[i];
            int eq = pair.indexOf('=');
            if (eq <= 0) {
                sb.append(pair);
                continue;
            }
            String key = pair.substring(0, eq);
            String value = pair.substring(eq + 1);
            if (isSensitiveKey(key)) {
                sb.append(key).append('=').append(maskValue(value));
            } else {
                sb.append(pair);
            }
        }
        return sb.toString();
    }

    private static String maskHttpUrlQueries(String input) {
        Matcher matcher = HTTP_URL_PATTERN.matcher(input);
        StringBuilder sb = new StringBuilder(input.length());
        while (matcher.find()) {
            matcher.appendReplacement(sb, Matcher.quoteReplacement(maskHttpUrl(matcher.group())));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private static String maskHttpUrl(String url) {
        int q = url.indexOf('?');
        if (q < 0 || q >= url.length() - 1) {
            return url;
        }
        String base = url.substring(0, q + 1);
        String rest = url.substring(q + 1);
        int hash = rest.indexOf('#');
        if (hash >= 0) {
            return base + maskFormUrlEncoded(rest.substring(0, hash)) + rest.substring(hash);
        }
        return base + maskFormUrlEncoded(rest);
    }

    private static String maskCurlFlagArgs(String input, String[] flags, java.util.function.Function<String, String> masker) {
        StringBuilder out = new StringBuilder(input.length());
        int i = 0;
        while (i < input.length()) {
            FlagHit hit = findNextFlag(input, i, flags);
            if (hit == null) {
                out.append(input, i, input.length());
                break;
            }
            out.append(input, i, hit.flagStart);
            out.append(input, hit.flagStart, hit.argRawStart);
            String masked = masker.apply(hit.arg);
            if (hit.quote != 0) {
                out.append(hit.quote).append(masked == null ? "" : masked).append(hit.quote);
            } else {
                out.append(masked == null ? "" : masked);
            }
            i = hit.argEnd;
        }
        return out.toString();
    }

    private static FlagHit findNextFlag(String s, int from, String[] flags) {
        FlagHit best = null;
        for (String flag : flags) {
            int at = indexOfFlag(s, from, flag);
            if (at < 0) {
                continue;
            }
            if (best != null && at > best.flagStart) {
                continue;
            }
            if (best != null && at == best.flagStart && flag.length() < best.flagLength) {
                continue;
            }
            int argRawStart = skipWs(s, at + flag.length());
            if (argRawStart >= s.length()) {
                continue;
            }
            ArgSpan argSpan = extractArg(s, argRawStart);
            best = new FlagHit(at, flag.length(), argRawStart, argSpan.arg, argSpan.quote, argSpan.end);
        }
        return best;
    }

    private static int indexOfFlag(String s, int from, String flag) {
        int max = s.length() - flag.length();
        for (int i = from; i <= max; i++) {
            if (!s.regionMatches(true, i, flag, 0, flag.length())) {
                continue;
            }
            if (i > 0 && !Character.isWhitespace(s.charAt(i - 1))) {
                continue;
            }
            int after = i + flag.length();
            if (after < s.length() && !Character.isWhitespace(s.charAt(after))) {
                continue;
            }
            return i;
        }
        return -1;
    }

    private static int skipWs(String s, int from) {
        int i = from;
        while (i < s.length() && Character.isWhitespace(s.charAt(i))) {
            i++;
        }
        return i;
    }

    private static ArgSpan extractArg(String s, int start) {
        char first = s.charAt(start);
        if (first == '\'' || first == '"') {
            int end = start + 1;
            while (end < s.length() && s.charAt(end) != first) {
                end++;
            }
            String arg = s.substring(start + 1, Math.min(end, s.length()));
            int argEnd = end < s.length() ? end + 1 : end;
            return new ArgSpan(arg, first, argEnd);
        }
        int end = start;
        while (end < s.length() && !isFlagBoundary(s, end)) {
            end++;
        }
        // Drop trailing whitespace from the arg value, but leave it in the stream so the
        // next flag still has a whitespace boundary (e.g. "...value -d ...").
        int contentEnd = end;
        while (contentEnd > start && Character.isWhitespace(s.charAt(contentEnd - 1))) {
            contentEnd--;
        }
        return new ArgSpan(s.substring(start, contentEnd), (char) 0, contentEnd);
    }

    private static boolean isFlagBoundary(String s, int idx) {
        if (idx <= 0 || idx >= s.length()) {
            return false;
        }
        if (!Character.isWhitespace(s.charAt(idx - 1)) || s.charAt(idx) != '-') {
            return false;
        }
        if (idx + 1 >= s.length()) {
            return false;
        }
        char next = s.charAt(idx + 1);
        if (next == '-') {
            return idx + 2 < s.length() && Character.isLetter(s.charAt(idx + 2));
        }
        return Character.isLetter(next);
    }

    private static final class FlagHit {
        final int flagStart;
        final int flagLength;
        final int argRawStart;
        final String arg;
        final char quote;
        final int argEnd;

        FlagHit(int flagStart, int flagLength, int argRawStart, String arg, char quote, int argEnd) {
            this.flagStart = flagStart;
            this.flagLength = flagLength;
            this.argRawStart = argRawStart;
            this.arg = arg;
            this.quote = quote;
            this.argEnd = argEnd;
        }
    }

    private static final class ArgSpan {
        final String arg;
        final char quote;
        final int end;

        ArgSpan(String arg, char quote, int end) {
            this.arg = arg;
            this.quote = quote;
            this.end = end;
        }
    }
}
