package net.potatocloud.webinterface.controller;

import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import net.potatocloud.webinterface.dto.request.GroupCreateRequest;
import net.potatocloud.webinterface.dto.request.GroupUpdateRequest;
import net.potatocloud.webinterface.dto.response.GroupSummaryResponse;
import net.potatocloud.webinterface.exception.ApiError;
import net.potatocloud.webinterface.mapper.GroupMapper;
import net.potatocloud.webinterface.model.ApiGroup;
import net.potatocloud.webinterface.service.GroupService;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;

@Path("/api/v1/groups")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Authenticated
@SecurityRequirement(name = "ApiKeyAuth")
@Tag(name = "Groups", description = "Endpoints related to group management and information")
public class GroupController {

    @Inject
    GroupService groupService;

    @Inject
    GroupMapper groupMapper;

    @GET
    @Operation(summary = "List all groups in a summary", description = "Summary information about a group, including name, platform version, online services and players count")
    public List<GroupSummaryResponse> allGroups() {
        return groupMapper.toSummary(groupService.findAll());
    }

    @Path("/details")
    @GET
    @Operation(summary = "List all groups with a more detailed description", description = "Detailed information about a group, including all platform informations and configurations")
    public List<ApiGroup> allGroupsDetails() {
        return groupService.findAll();
    }

    @POST
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Group created successfully"
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Unable to create group with the provided configuration",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = ApiError.class)
                    )
            )
    })
    @Operation(summary = "Create a new group", description = "Creates a new group with the provided configuration. The group name must be unique and not already exist.")
    public Response createGroup(@Valid @RequestBody GroupCreateRequest request) {
        if (!groupService.create(request)) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Unable to create group with the provided configuration")
                    .build();
        }
        return Response.status(Response.Status.CREATED).build();
    }

    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Group updated successfully"
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Group with the specified name not found",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = ApiError.class)
                    )
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Unable to update group with the provided configuration",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = ApiError.class)
                    )
            )
    })
    @Operation(summary = "Update an existing group", description = "Updates the configuration of an existing group. The group is identified by its name in the path parameter. The request body contains the new configuration for the group. If the group with the specified name does not exist, a 404 error is returned. If the update fails due to invalid configuration or other reasons, a 400 error is returned.")
    @Path("/{groupName}")
    @PUT
    public Response updateGroup(@Valid @RequestBody GroupUpdateRequest request, @PathParam("groupName") String groupName) {
        if (!groupService.exists(groupName)) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ApiError("NOT_FOUND", "Group with name '" + groupName + "' does not exist."))
                    .build();
        }

        if (!groupService.update(groupName, request)) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ApiError("BAD_REQUEST", "Unable to update group with the provided configuration"))
                    .build();
        }

        return Response.status(Response.Status.OK).build();
    }

    @Operation(summary = "Get group information by name", description = "Returns the group information for the specified group name. If the group with the specified name does not exist, a 404 error is returned.")
    @Path("/{groupName}")
    @GET
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "Group found and returned successfully",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = ApiGroup.class)
                    )
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Group with the specified name not found",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = ApiError.class)
                    )
            )
    })
    public Response groupByName(@PathParam("groupName") String groupName) {
        ApiGroup group = groupService.findByName(groupName);
        if (group == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ApiError("NOT_FOUND", "Group with name '" + groupName + "' not found"))
                    .build();
        }
        return Response.status(Response.Status.OK).entity(group).build();
    }

    @Path("/{groupName}/start")
    @Operation(summary = "Start a group", description = "Starts the group with the specified name. If the group is already running, has reached its maximum number of services or does not exist, a 400 error is returned.")
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Group started successfully"
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Unable to start group (e.g., group not found or already running or hit his maximum)",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = ApiError.class)
                    )
            )
    })
    @POST
    public Response startGroup(@PathParam("groupName") String groupName) {
        if (!groupService.exists(groupName)) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ApiError("BAD_REQUEST", "Group with name '" + groupName + "' does not exist."))
                    .build();
        }

        if (!groupService.canStartService(groupName)) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ApiError("BAD_REQUEST", "Cannot start group with name '" + groupName + "' because it has reached its maximum number of services."))
                    .build();
        }

        if (!groupService.start(groupName)) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ApiError("BAD_REQUEST", "Unable to start group with name '" + groupName + "'. It may not exist or a other error occured."))
                    .build();
        }

        return Response.status(Response.Status.OK).build();
    }

    @Path("/{groupName}/shutdown")
    @Operation(summary = "Shutdown a group", description = "Shuts down the group with the specified name. If the group is already stopped or does not exist, a 400 error is returned.")
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Group shutdown successfully"
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Group with the specified name not found",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = ApiError.class)
                    )
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Unable to shutdown group (e.g., already stopped or error occured)",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = ApiError.class)
                    )
            )
    })
    @POST
    public Response shutdownGroup(@PathParam("groupName") String groupName) {
        if (!groupService.exists(groupName)) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ApiError("NOT_FOUND", "Group with name '" + groupName + "' does not exist."))
                    .build();
        }

        if (!groupService.shutdown(groupName)) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ApiError("BAD_REQUEST", "Unable to shutdown group with name '" + groupName + "'. It may already be stopped or a other error occured."))
                    .build();
        }

        return Response.status(Response.Status.OK).build();
    }

}
