package net.potatocloud.webinterface.controller;

import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import net.potatocloud.api.platform.Platform;
import net.potatocloud.webinterface.dto.request.PlatformCreateRequest;
import net.potatocloud.webinterface.dto.request.PlatformUpdateRequest;
import net.potatocloud.webinterface.dto.request.PlatformVersionRequest;
import net.potatocloud.webinterface.dto.response.PlatformDetailResponse;
import net.potatocloud.webinterface.dto.response.PlatformSummaryResponse;
import net.potatocloud.webinterface.exception.ApiError;
import net.potatocloud.webinterface.mapper.PlatformMapper;
import net.potatocloud.webinterface.service.PlatformService;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
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
    PlatformService platformService;

    @Inject
    PlatformMapper platformMapper;

    @GET
    @Operation(summary = "List all platforms", description = "Returns a summry list of all platforms available in the cloud environment.")
    @APIResponse(responseCode = "200", description = "List of all platforms", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PlatformSummaryResponse.class, type = SchemaType.ARRAY)))
    public List<PlatformSummaryResponse> allPlatforms() {
        List<Platform> platforms = platformService.findAllRaw();
        return platformMapper.toSummaryApi(platforms);
    }

    @POST
    @Operation(summary = "Create a new platform", description = "Creates a new custom platform with the provided configuration. The platform name must be unique.")
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "201",
                    description = "Platform created successfully"
            ),
            @APIResponse(
                    responseCode = "409",
                    description = "Platform with the given name already exists",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Invalid request body",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            )
    })
    public Response createPlatform(@Valid @RequestBody PlatformCreateRequest request) {
        if (platformService.existsByName(request.name())) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(new ApiError("CONFLICT", "Platform with name '" + request.name() + "' already exists"))
                    .build();
        }

        if (!platformService.create(request)) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ApiError("BAD_REQUEST", "Unable to create platform with the provided configuration"))
                    .build();
        }

        return Response.status(Response.Status.CREATED).build();
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

    @Path("/{name}")
    @PUT
    @Operation(summary = "Update a platform", description = "Updates the configuration of an existing platform. Only the provided fields will be updated.")
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Platform updated successfully"
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Platform with the specified name not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Unable to update platform with the provided configuration",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            )
    })
    public Response updatePlatform(@PathParam("name") String name, @Valid @RequestBody PlatformUpdateRequest request) {
        if (!platformService.existsByName(name)) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ApiError("NOT_FOUND", "Platform with name '" + name + "' does not exist"))
                    .build();
        }

        if (!platformService.update(name, request)) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ApiError("BAD_REQUEST", "Unable to update platform with the provided configuration"))
                    .build();
        }

        return Response.status(Response.Status.OK).build();
    }

    @Path("/{name}")
    @DELETE
    @Operation(summary = "Delete a platform", description = "Deletes the platform with the specified name.")
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Platform deleted successfully"
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Platform with the specified name not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            )
    })
    public Response deletePlatform(@PathParam("name") String name) {
        if (!platformService.existsByName(name)) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ApiError("NOT_FOUND", "Platform with name '" + name + "' does not exist"))
                    .build();
        }

        if (!platformService.delete(name)) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ApiError("INTERNAL_SERVER_ERROR", "Unable to delete platform with name '" + name + "'"))
                    .build();
        }

        return Response.status(Response.Status.OK).build();
    }

    @Path("/{name}/versions")
    @POST
    @Operation(summary = "Add a version to a platform", description = "Adds a new version to the specified platform.")
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "201",
                    description = "Version added successfully"
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Platform with the specified name not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            ),
            @APIResponse(
                    responseCode = "409",
                    description = "Version with the given name already exists on this platform",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            )
    })
    public Response addVersion(@PathParam("name") String name, @Valid @RequestBody PlatformVersionRequest request) {
        if (!platformService.existsByName(name)) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ApiError("NOT_FOUND", "Platform with name '" + name + "' does not exist"))
                    .build();
        }

        if (!platformService.addVersion(name, request)) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(new ApiError("CONFLICT", "Version '" + request.name() + "' already exists on platform '" + name + "'"))
                    .build();
        }

        return Response.status(Response.Status.CREATED).build();
    }

    @Path("/{name}/versions/{versionName}")
    @DELETE
    @Operation(summary = "Remove a version from a platform", description = "Removes the specified version from the platform.")
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Version removed successfully"
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Platform or version with the specified name not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            )
    })
    public Response removeVersion(@PathParam("name") String name, @PathParam("versionName") String versionName) {
        if (!platformService.existsByName(name)) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ApiError("NOT_FOUND", "Platform with name '" + name + "' does not exist"))
                    .build();
        }

        if (!platformService.removeVersion(name, versionName)) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ApiError("NOT_FOUND", "Version '" + versionName + "' not found on platform '" + name + "'"))
                    .build();
        }

        return Response.status(Response.Status.OK).build();
    }

}
