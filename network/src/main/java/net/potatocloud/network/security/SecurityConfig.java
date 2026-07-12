package net.potatocloud.network.security;

import java.nio.file.Path;
import java.nio.file.Paths;

public record SecurityConfig(boolean sslEnabled, Path securityDirectory, boolean requireClientAuth) {

    public static String SSL_ENABLED = "potatocloud.security.sslEnabled";
    public static String SECURITY_DIRECTORY = "potatocloud.security.directory";
    public static String REQUIRE_CLIENT_AUTH = "potatocloud.security.clientAuth";

    public static SecurityConfig fromProperties() {
        final boolean sslEnabled = Boolean.parseBoolean(get(SSL_ENABLED, "false"));
        final Path securityDirectory = Paths.get(get(SECURITY_DIRECTORY, "security"));
        final boolean requireClientAuth = Boolean.parseBoolean(get(REQUIRE_CLIENT_AUTH, "true"));
        return new SecurityConfig(sslEnabled, securityDirectory, requireClientAuth);
    }

    private static String get(String key, String defaultValue) {
        return System.getProperty(key, defaultValue);
    }
}