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
package io.github.opensabe.common.utils;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class TextSimilarityUtil {
    
    /**
     * Calculates the string distance between source and target strings using
     * the Damerau-Levenshtein algorithm. The distance is case-sensitive.
     *
     * @param source The source String.
     * @param target The target String.
     * @return The distance between source and target strings.
     * @throws IllegalArgumentException If either source or target is null.
     */
    public static int getDameLevenDistance(CharSequence source, CharSequence target) {
        if (source == null || target == null) {
            throw new IllegalArgumentException("Parameter must not be null");
        }
        int sourceLength = source.length();
        int targetLength = target.length();
        if (sourceLength == 0) return targetLength;
        if (targetLength == 0) return sourceLength;
        int[][] dist = new int[sourceLength + 1][targetLength + 1];
        for (int i = 0; i < sourceLength + 1; i++) {
            dist[i][0] = i;
        }
        for (int j = 0; j < targetLength + 1; j++) {
            dist[0][j] = j;
        }
        for (int i = 1; i < sourceLength + 1; i++) {
            for (int j = 1; j < targetLength + 1; j++) {
                int cost = source.charAt(i - 1) == target.charAt(j - 1) ? 0 : 1;
                dist[i][j] = Math.min(Math.min(dist[i - 1][j] + 1, dist[i][j - 1] + 1), dist[i - 1][j - 1] + cost);
                if (i > 1 &&
                        j > 1 &&
                        source.charAt(i - 1) == target.charAt(j - 2) &&
                        source.charAt(i - 2) == target.charAt(j - 1)) {
                    dist[i][j] = Math.min(dist[i][j], dist[i - 2][j - 2] + cost);
                }
            }
        }
        return dist[sourceLength][targetLength];
    }
    
    /**
     * judge the similarity between 2 strings according to Damerau-Levenshtein Distance
     * 
     * @param str1
     * @param str2
     * @return true-similar, false-not similar
     */
    public static boolean judgeStringSimilarity(String str1, String str2, Float strSimilarity) {
        if (MATCHED_WORDS.contains(str1 + "-" + str2)) {
            return true;
        } else if (StringUtils.isBlank(str1) && StringUtils.isBlank(str2)) {
            return false;
        } else {
            str1 = str1 == null ? "" : str1;
            str2 = str2 == null ? "" : str2;
            // 去除deviceId
            str1 = str1.contains("deviceId") ? str1.substring(0, str1.lastIndexOf("deviceId")) : str1;
            str2 = str2.contains("deviceId") ? str2.substring(0, str1.lastIndexOf("deviceId")) : str2;
            // 去除不需要比较的字符串，降低levenshteinDistance
            str1 = str1.replaceAll("Mozilla", "").replaceAll("AppleWebKit", "").replaceAll("KHTML", "")
                    .replaceAll("like Gecko", "").replaceAll(" network\\/([a-z])+", "");
            str2 = str2.replaceAll("Mozilla", "").replaceAll("AppleWebKit", "").replaceAll("KHTML", "")
                    .replaceAll("like Gecko", "").replaceAll(" network\\/([a-z])+", "");
            //int levenshteinDistance = LevenshteinDistance.getDefaultInstance().apply(str1, str2);
            int distance = getDameLevenDistance(str1, str2);
            float ratio = (float) (Math.max(str1.length(), str2.length()) - distance)/(float) Math.max(str1.length(), str2.length());
            //Double ratio = new JaroWinklerSimilarity().apply(str1, str2);

            log.debug("the similarity of between {} and {} is {}", str1, str2, ratio);
            if (ratio < strSimilarity)
                return false;
            else
                return true;
        }
    }
   
    private static final List<String> MATCHED_WORDS = List.of("ADJEI-AGYEI", "AGYEI-ADJEI", "ABDUL-ABUDU", "ABUDU-ABDUL","JUNIOR-JNR","JNR-JUNIOR");
}
