package net.potatocloud.network.security;

import java.nio.file.Path;

public final class CertificatePaths {

    private CertificatePaths() {
    }

    public static Path serverCertificate(SecurityConfig config) {
        return config.securityDirectory().resolve("server.pem");
    }

    public static Path serverKey(SecurityConfig config) {
        return config.securityDirectory().resolve("server.key");
    }

    public static Path clientCertificate(SecurityConfig config) {
        return config.securityDirectory().resolve("client.pem");
    }

    public static Path clientKey(SecurityConfig config) {
        return config.securityDirectory().resolve("client.key");
    }
}
