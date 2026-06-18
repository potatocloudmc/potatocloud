package net.potatocloud.webinterface;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import jakarta.ws.rs.core.Application;
import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeIn;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeType;
import org.eclipse.microprofile.openapi.annotations.info.Contact;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.security.SecurityScheme;

@OpenAPIDefinition(
        info = @Info(
                title = "PotatoCloud - WebInterface Module",
                version = "1.0.0",
                description = "API documentation for the PotatoCloud WebInterface Module",
                contact = @Contact(name = "Fedox", email = "f3dox@proton.me", url = "https://fedox.ovh")
        )
)
@SecurityScheme(
        securitySchemeName = "ApiKeyAuth",
        type = SecuritySchemeType.APIKEY,
        apiKeyName = "X-API-Key",
        in = SecuritySchemeIn.HEADER,
        description = "API key authentication. Provide a valid API key in the X-API-Key header to access protected endpoints."
)
@QuarkusMain
public class WebInterfaceQuarkusApp extends Application implements QuarkusApplication {

    @Override
    public int run(String... args) throws Exception {
        Quarkus.waitForExit();
        return 0;
    }

}
