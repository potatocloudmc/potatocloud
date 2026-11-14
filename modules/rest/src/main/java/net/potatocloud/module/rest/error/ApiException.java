package net.potatocloud.module.rest.error;

public final class ApiException extends RuntimeException {

    private final int status;
    private final String error;

    public ApiException(int status, String error, String message) {
        super(message);
        this.status = status;
        this.error = error;
    }

    public static ApiException notFound(String type, String name) {
        return new ApiException(404, type + "_not_found", "No " + type + " named " + name);
    }

    public static ApiException badRequest(String message) {
        return new ApiException(400, "bad_request", message);
    }

    public static ApiException conflict(String message) {
        return new ApiException(409, "conflict", message);
    }

    public int status() {
        return status;
    }

    public String error() {
        return error;
    }
}
