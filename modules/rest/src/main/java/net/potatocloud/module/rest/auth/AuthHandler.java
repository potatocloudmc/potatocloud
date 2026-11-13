package net.potatocloud.module.rest.auth;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.security.RouteRole;
import net.potatocloud.module.rest.config.ApiKey;
import net.potatocloud.module.rest.config.RestConfig;
import net.potatocloud.module.rest.error.ApiException;

public final class AuthHandler implements Handler {

    public static final String KEY_ATTRIBUTE = "api-key";

    private final RestConfig config;

    public AuthHandler(RestConfig config) {
        this.config = config;
    }

    @Override
    public void handle(Context ctx) {
        if (ctx.routeRoles().isEmpty()) {
            return;
        }

        final ApiKey key = find(ctx.header("X-API-Key"));
        if (key == null) {
            throw new ApiException(401, "unauthorized", "Missing or invalid API key");
        }

        for (RouteRole role : ctx.routeRoles()) {
            if (role instanceof Scope scope && !key.allows(scope)) {
                throw new ApiException(403, "forbidden", "API key is missing the scope " + scope.id());
            }
        }

        ctx.attribute(KEY_ATTRIBUTE, key);
    }

    private ApiKey find(String token) {
        return config.keys().stream().filter(key -> key.key().equals(token)).findFirst().orElse(null);
    }
}
