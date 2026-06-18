package net.potatocloud.webinterface.controller;

import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import net.potatocloud.webinterface.model.ApiPlatform;
import net.potatocloud.webinterface.service.impl.PlatformServiceImpl;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;
import java.util.Map;

@Path("/api/v1/platform")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Authenticated
@SecurityRequirement(name = "ApiKeyAuth")
@Tag(name = "Platform", description = "Endpoints related to platform information and management")
public class PlatformController {

    @Inject
    PlatformServiceImpl platformService;

    @GET
    @Operation(summary = "List all platforms", description = "Returns a list of all platforms available in the cloud environment.")
    public List<ApiPlatform> allPlatforms() {
        return platformService.findAll();
    }

    @Path("/{name}")
    @GET
    @Operation(summary = "Get platform by name", description = "Returns the platform information for the specified platform name.")
    public Response getPlatformByName(String name) {
        if (!platformService.existsByName(name)) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "Platform with name '" + name + "' not found"))
                    .build();
        }

        return Response.ok(platformService.findByName(name)).build();
    }

}
