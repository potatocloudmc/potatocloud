package net.potatocloud.webinterface.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.stream.Collectors;

@Provider
public class ValidationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {

    @Override
    public Response toResponse(ConstraintViolationException exception) {
        String message = exception.getConstraintViolations().stream()
                .map(this::formatValidation)
                .collect(Collectors.joining(", "));

        ApiError apiError = new ApiError("VALIDATION_ERROR", message.isBlank() ? "Validation failed" : message);
        return Response.status(Response.Status.BAD_REQUEST)
                .entity(apiError)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }

    private String formatValidation(ConstraintViolation<?> violation) {
        return violation.getPropertyPath() + ": " + violation.getMessage();
    }
}
