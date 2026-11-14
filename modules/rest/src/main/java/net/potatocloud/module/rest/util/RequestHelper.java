package net.potatocloud.module.rest.util;

import io.javalin.http.Context;
import net.potatocloud.module.rest.error.ApiException;

public final class RequestHelper {

    private RequestHelper() {
    }

    public static <T> T read(Context ctx, Class<T> type) {
        require(!ctx.body().isBlank(), "Request body is required");
        return ctx.bodyAsClass(type);
    }

    public static void require(boolean condition, String message) {
        if (!condition) {
            throw ApiException.badRequest(message);
        }
    }

    public static void requireName(String name) {
        require(name != null && name.matches("[A-Za-z0-9_-]+"), "name is required and may only contain letters, numbers, - and _");
    }

    public static int intParam(Context ctx, String name, int fallback) {
        final String value = ctx.queryParam(name);
        if (value == null) {
            return fallback;
        }

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw ApiException.badRequest(name + " must be a number");
        }
    }
}
