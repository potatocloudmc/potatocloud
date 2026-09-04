package net.potatocloud.webinterface.exception;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.jackson.Jacksonized;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.Instant;

@Schema(name = "APIError", description = "Represents an error response from the API.")
@Getter
@Setter
@Accessors(fluent = true, chain = true)
@Jacksonized
public class ApiError {

    private String code;
    private String message;
    private Instant timestamp;

    public ApiError() {
        this.timestamp = Instant.now();
    }

    public ApiError(String code, String message) {
        this.code = code;
        this.message = message;
        this.timestamp = Instant.now();
    }

}
