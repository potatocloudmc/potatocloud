package net.potatocloud.webinterface.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.Map;

@Provider
public class ValidationExceptionMapper
        implements ExceptionMapper<ConstraintViolationException> {

    @Override
    public Response toResponse(ConstraintViolationException exception) {

        var errors = exception.getConstraintViolations()
                .stream()
                .map(v -> new ApiValidationError(
                        extractField(v),
                        v.getMessage()
                ))
                .toList();

        return Response.status(Response.Status.BAD_REQUEST)
                .entity(Map.of("errors", errors))
                .build();
    }

    private String extractField(ConstraintViolation<?> v) {
        String path = v.getPropertyPath().toString();

        if (path.contains(".")) {
            return path.substring(path.lastIndexOf('.') + 1);
        }

        return path;
    }
}