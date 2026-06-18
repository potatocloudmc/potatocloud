package net.potatocloud.webinterface.controller;

import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import net.potatocloud.webinterface.exception.ApiError;
import net.potatocloud.webinterface.model.ApiService;
import net.potatocloud.webinterface.service.impl.ServerServiceImpl;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;

@Path("/api/v1/services")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Authenticated
@SecurityRequirement(name = "ApiKeyAuth")
@Tag(name = "Services", description = "Endpoints related to service information and management")
public class ServiceController {

    @Inject
    ServerServiceImpl serverService;

    @GET
    @Operation(summary = "List all services", description = "Returns a list of all services currently running in the cloud environment.")
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "List of all services",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiService.class, type = SchemaType.ARRAY))
            ),
    })
    public List<ApiService> allServices() {
        return serverService.findAll();
    }

    @Path("/{name}")
    @GET
    @Operation(summary = "Get service by name", description = "Returns the service information for the specified service name.")
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Service information for the specified name",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiService.class))
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Service not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            )
    })
    public Response getService(String name) {
        ApiService service = serverService.findByName(name);

        if (service == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ApiError("NOT_FOUND", "Service with name '" + name + "' not found"))
                    .build();
        }

        return Response.ok(service).build();
    }

    @Path("/{name}/stop")
    @GET
    @Operation(summary = "Stop a service", description = "Stops the service with the specified name.")
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "204",
                    description = "Service stopped successfully"
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Service not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            )
    })
    public Response stopService(String name) {
        if (!serverService.shutdown(name)) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ApiError("NOT_FOUND", "Service with name '" + name + "' not found"))
                    .build();
        }
        return Response.ok().build();
    }

}
