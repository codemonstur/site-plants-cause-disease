package app.util;

import java.net.URLEncoder;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.emptyList;

public enum Functions {;

    public static boolean isNullOrEmpty(final String value) {
        return value == null || value.isEmpty();
    }

    public static String encodeUrl(final String value) {
        return URLEncoder.encode(value, UTF_8);
    }

    public static <T> List<T> orEmpty(final List<T> list) {
        return list == null ? emptyList() : list;
    }

}
