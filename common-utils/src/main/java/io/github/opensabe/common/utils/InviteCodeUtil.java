package io.github.opensabe.common.utils;

import java.util.concurrent.ThreadLocalRandom;

public class InviteCodeUtil {

    public static String[] RANDOM_AREA = new String[]{"2", "3", "4", "5", "6", "7", "8", "9", "Q", "W", "E", "R", "T", "Y", "U", "O", "P", "A", "S", "D", "F", "G", "H", "J", "K", "Z", "X", "C", "V", "B", "N", "M"};

    public static final ThreadLocalRandom THREAD_LOCAL_RANDOM = ThreadLocalRandom.current();
    public static String generateInviteCode(int length) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int nextInt = THREAD_LOCAL_RANDOM.nextInt(RANDOM_AREA.length);
            result.append(RANDOM_AREA[nextInt]);
        }
        return result.toString();
    }

}
