package net.potatocloud.webinterface.controller;

import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import net.potatocloud.api.platform.Platform;
import net.potatocloud.webinterface.dto.response.PlatformDetailResponse;
import net.potatocloud.webinterface.dto.response.PlatformSummaryResponse;
import net.potatocloud.webinterface.exception.ApiError;
import net.potatocloud.webinterface.mapper.PlatformMapper;
import net.potatocloud.webinterface.service.impl.PlatformServiceImpl;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;

@Path("/api/v1/platforms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Authenticated
@SecurityRequirement(name = "ApiKeyAuth")
@Tag(name = "Platform", description = "Endpoints related to platform information and management")
public class PlatformController {

    @Inject
    PlatformServiceImpl platformService;

    @Inject
    PlatformMapper platformMapper;

    @GET
    @Operation(summary = "List all platforms", description = "Returns a summry list of all platforms available in the cloud environment.")
    @APIResponse(responseCode = "200", description = "List of all platforms", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PlatformSummaryResponse.class, type = SchemaType.ARRAY)))
    public List<PlatformSummaryResponse> allPlatforms() {
        List<Platform> platforms = platformService.findAllRaw();
        return platformMapper.toSummaryApi(platforms);
    }

    @Path("/{name}")
    @GET
    @Operation(summary = "Get platform by name", description = "Returns the platform information for the specified platform name.")
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Platform information for the specified name",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = PlatformDetailResponse.class))
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Service not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            )
    })
    public Response getPlatformByName(String name) {
        if (!platformService.existsByName(name)) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ApiError("NOT_FOUND", "Platform with name '" + name + "' not found"))
                    .build();
        }

        Platform platform = platformService.findByNameRaw(name);
        PlatformDetailResponse response = platformMapper.toDetailApi(platform);

        return Response.ok(response).build();
    }

}
