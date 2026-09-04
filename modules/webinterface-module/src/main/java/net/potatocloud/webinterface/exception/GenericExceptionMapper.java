package net.potatocloud.webinterface.exception;

import io.quarkus.logging.Log;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class GenericExceptionMapper implements ExceptionMapper<Throwable> {

    @Override
    public Response toResponse(Throwable exception) {
        if (exception instanceof WebApplicationException wae) {
            return wae.getResponse();
        }

        Log.errorf("An unexpected error occurred: %s", exception.getMessage(), exception);
        ApiError apiError = new ApiError("INTERNAL_SERVER_ERROR", "An unexpected error occurred. Please try again later.");
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(apiError)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
