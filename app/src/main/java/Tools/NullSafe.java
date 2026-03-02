package tools;

import java.util.function.Function;

public class NullSafe {
    public static <T, R> R get(T obj, Function<T, R> getter) {
        return obj == null ? null : getter.apply(obj);
    }
}