package net.potatocloud.node.utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public final class HashUtils {

    private static final HexFormat HEX_FORMAT = HexFormat.of();

    private HashUtils() {
    }

    public static String sha256(String input) {
        return hash("SHA-256", input.getBytes(StandardCharsets.UTF_8));
    }

    public static String sha256(Path path) {
        return hash("SHA-256", readBytes(path));
    }

    public static String md5(Path path) {
        return hash("MD5", readBytes(path));
    }

    private static String hash(String algorithm, byte[] input) {
        try {
            final MessageDigest messageDigest = MessageDigest.getInstance(algorithm);
            return HEX_FORMAT.formatHex(messageDigest.digest(input));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Hash algorithm " + algorithm + " was not found", e);
        }
    }

    private static byte[] readBytes(Path path) {
        try {
            return Files.readAllBytes(path);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file: " + path, e);
        }
    }
}