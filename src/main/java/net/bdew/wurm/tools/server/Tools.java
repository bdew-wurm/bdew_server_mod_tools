package net.bdew.wurm.tools.server;

import java.util.stream.Stream;

public class Tools {
    public static <T> Stream<T> streamOfNullable(T v) {
        return v == null ? Stream.empty() : Stream.of(v);
    }
}
