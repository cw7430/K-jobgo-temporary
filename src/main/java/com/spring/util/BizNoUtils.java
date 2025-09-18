package com.spring.util;

public class BizNoUtils {
    private BizNoUtils() {}

    /** 1234567890 -> 123-45-67890, 그 외 길이면 원본 리턴 */
    public static String normalize(String raw) {
        if (raw == null) return null;
        String digits = raw.replaceAll("[^0-9]", "");
        if (digits.length() != 10) return raw; // 길이 안 맞으면 그대로
        return String.format("%s-%s-%s",
                digits.substring(0,3), digits.substring(3,5), digits.substring(5));
    }
}
