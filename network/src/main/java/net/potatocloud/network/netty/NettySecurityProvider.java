package net.potatocloud.network.netty;

import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import net.potatocloud.network.security.CertificatePaths;
import net.potatocloud.network.security.SecurityConfig;
import net.potatocloud.network.security.SecurityProvider;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

public final class NettySecurityProvider implements SecurityProvider<SslContext> {

    private final SecurityConfig config;

    public NettySecurityProvider(SecurityConfig config) {
        this.config = config;
    }

    @Override
    public SslContext createServerContext() {
        if (!config.sslEnabled()) {
            return null;
        }

        try {
            return SslContextBuilder.forServer(CertificatePaths.serverCertificate(config).toFile(), CertificatePaths.serverKey(config).toFile())
                    .trustManager(CertificatePaths.clientCertificate(config).toFile())
                    .clientAuth(config.requireClientAuth() ? ClientAuth.REQUIRE : ClientAuth.NONE)
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("Failed to create server SSL context", e);
        }
    }

    @Override
    public SslContext createClientContext() {
        if (!config.sslEnabled()) {
            return null;
        }

        try {
            return SslContextBuilder.forClient()
                    .keyManager(CertificatePaths.clientCertificate(config).toFile(), CertificatePaths.clientKey(config).toFile())
                    .trustManager(CertificatePaths.serverCertificate(config).toFile())
                    .endpointIdentificationAlgorithm(null)
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("Failed to create client SSL context", e);
        }
    }

    @Override
    public void generate(String name) {
        try {
            final var certificate = config.securityDirectory().resolve(name + ".pem");
            final var key = config.securityDirectory().resolve(name + ".key");

            if (Files.exists(certificate) && Files.exists(key)) {
                checkNotExpired(certificate, name);
                return;
            }

            final var selfSigned = new SelfSignedCertificate("potatocloud-" + name);

            Files.copy(selfSigned.certificate().toPath(), certificate, StandardCopyOption.REPLACE_EXISTING);
            Files.copy(selfSigned.privateKey().toPath(), key, StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate certificate for " + name, e);
        }
    }

    private void checkNotExpired(Path certificate, String name) {
        try (InputStream in = Files.newInputStream(certificate)) {
            ((X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(in)).checkValidity();
        } catch (CertificateExpiredException e) {
            throw new RuntimeException("Certificates have expired! Delete the security folder and they will be created again automatically. " +
                            "If you use Multi Node, all nodes must have the same certificates. Copy the security folder from this node " +
                            "or from another node with valid certificates to all nodes."
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to verify " + name + " certificate", e);
        }
    }
}
