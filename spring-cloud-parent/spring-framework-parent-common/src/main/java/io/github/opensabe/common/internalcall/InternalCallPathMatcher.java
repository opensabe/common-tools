package io.github.opensabe.common.internalcall;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.util.AntPathMatcher;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;

/**
 * Exclude-path matching with semicolon strip, single percent-decode, and dot-segment normalization.
 */
public final class InternalCallPathMatcher {

    private final AntPathMatcher antPathMatcher = new AntPathMatcher();
    private final List<String> patterns;
    private final Cache<String, Boolean> cache;

    public InternalCallPathMatcher(Collection<String> patterns) {
        this.patterns = patterns == null ? List.of() : List.copyOf(patterns);
        this.cache = Caffeine.newBuilder().maximumSize(10_000).build();
    }

    public boolean isExcluded(String rawPath, String contextPath) {
        String key = (contextPath == null ? "" : contextPath) + '\0' + (rawPath == null ? "" : rawPath);
        return Boolean.TRUE.equals(cache.get(key, k -> matches(rawPath, contextPath)));
    }

    boolean matches(String rawPath, String contextPath) {
        String normalized = normalize(rawPath, contextPath);
        if (normalized == null) {
            return false;
        }
        for (String pattern : patterns) {
            if (pattern != null && antPathMatcher.match(pattern, normalized)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return normalized path for matching, or null if path should not be treated as exclude-hit
     *         (e.g. encoded slash that we refuse to turn into '/')
     */
    public String normalize(String rawPath, String contextPath) {
        if (rawPath == null || rawPath.isEmpty()) {
            rawPath = "/";
        }
        // Strip before and after decode so encoded ';' (%3b) cannot keep an exclude match.
        String withoutSemicolon = stripPathParameters(rawPath);
        String decoded;
        try {
            decoded = percentDecodeOnceRejectEncodedSlash(withoutSemicolon);
        } catch (IllegalArgumentException e) {
            return null;
        }
        String strippedAgain = stripPathParameters(decoded);
        String collapsed = collapseDotSegments(strippedAgain);
        return stripContextPath(collapsed, contextPath);
    }

    static String stripPathParameters(String path) {
        StringBuilder sb = new StringBuilder(path.length());
        int i = 0;
        while (i < path.length()) {
            char c = path.charAt(i);
            if (c == ';') {
                while (i < path.length() && path.charAt(i) != '/') {
                    i++;
                }
                continue;
            }
            sb.append(c);
            i++;
        }
        return sb.toString();
    }

    static String percentDecodeOnceRejectEncodedSlash(String path) {
        StringBuilder sb = new StringBuilder(path.length());
        for (int i = 0; i < path.length(); i++) {
            char c = path.charAt(i);
            if (c == '%' && i + 2 < path.length()) {
                int hi = Character.digit(path.charAt(i + 1), 16);
                int lo = Character.digit(path.charAt(i + 2), 16);
                if (hi >= 0 && lo >= 0) {
                    int value = (hi << 4) + lo;
                    if (value == '/') {
                        throw new IllegalArgumentException("Encoded slash is not allowed in path");
                    }
                    sb.append((char) value);
                    i += 2;
                    continue;
                }
            }
            sb.append(c);
        }
        // Ensure we produced valid chars; keep as Latin-1 byte interpretation like Tomcat single decode
        return new String(sb.toString().getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
    }

    static String collapseDotSegments(String path) {
        if (path.isEmpty()) {
            return "/";
        }
        boolean absolute = path.startsWith("/");
        String[] parts = path.split("/", -1);
        java.util.ArrayDeque<String> stack = new java.util.ArrayDeque<>();
        for (String part : parts) {
            if (part.isEmpty() || ".".equals(part)) {
                continue;
            }
            if ("..".equals(part)) {
                if (!stack.isEmpty()) {
                    stack.removeLast();
                }
                continue;
            }
            stack.addLast(part);
        }
        StringBuilder sb = new StringBuilder();
        if (absolute) {
            sb.append('/');
        }
        boolean first = true;
        for (String part : stack) {
            if (!first) {
                sb.append('/');
            }
            sb.append(part);
            first = false;
        }
        if (sb.length() == 0) {
            return absolute ? "/" : "";
        }
        return sb.toString();
    }

    static String stripContextPath(String path, String contextPath) {
        if (contextPath == null || contextPath.isEmpty() || "/".equals(contextPath)) {
            return path.isEmpty() ? "/" : path;
        }
        String ctx = contextPath.endsWith("/") ? contextPath.substring(0, contextPath.length() - 1) : contextPath;
        if (path.equals(ctx)) {
            return "/";
        }
        if (path.startsWith(ctx + "/")) {
            String rest = path.substring(ctx.length());
            return rest.isEmpty() ? "/" : rest;
        }
        return path.isEmpty() ? "/" : path;
    }
}
