package net.potatocloud.common;

public record Pair<K, V>(K key, V value) {

    public static <K, V> Pair<K, V> of(K key, V value) {
        return new Pair<>(key, value);
    }
}