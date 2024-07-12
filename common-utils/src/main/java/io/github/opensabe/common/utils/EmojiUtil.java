package io.github.opensabe.common.utils;

import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class EmojiUtil {
    private static final Pattern pattern = Pattern.compile("\\P{M}\\p{M}*+");

    /**
     * Split emoji string to list
     * @param s
     * @return
     */
    public static List<String> splitEmojiString(String s) {
        return pattern.matcher(s)
                .results()
                .map(MatchResult::group)
                .collect(Collectors.toList());
    }
}
