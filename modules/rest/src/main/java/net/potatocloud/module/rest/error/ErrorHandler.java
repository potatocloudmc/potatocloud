package net.potatocloud.module.rest.error;

import io.javalin.config.RoutesConfig;
import io.javalin.http.Context;
import io.javalin.http.HttpResponseException;
import net.potatocloud.api.logging.Logger;
import net.potatocloud.module.rest.model.ErrorResponse;
import tools.jackson.core.JacksonException;

public final class ErrorHandler {

    private final Logger logger;

    public ErrorHandler(Logger logger) {
        this.logger = logger;
    }

    public void register(RoutesConfig routes) {
        routes.exception(ApiException.class, (e, ctx) -> send(ctx, e.status(), e.error(), e.getMessage()));

        routes.exception(HttpResponseException.class, (e, ctx) -> send(ctx, e.getStatus(), name(e.getStatus()), e.getMessage()));

        routes.exception(JacksonException.class, (e, ctx) -> send(ctx, 400, "invalid_body", e.getOriginalMessage()));

        routes.exception(Exception.class, (e, ctx) -> {
            logger.error("REST request " + ctx.method() + " " + ctx.path() + " failed");
            logger.exception(e);
            send(ctx, 500, "internal_error", "Internal server error");
        });
    }

    private void send(Context ctx, int status, String error, String message) {
        ctx.status(status).json(new ErrorResponse(status, error, message));
    }

    private String name(int status) {
        return switch (status) {
            case 400 -> "bad_request";
            case 404 -> "not_found";
            case 405 -> "method_not_allowed";
            default -> "http_" + status;
        };
    }
}
