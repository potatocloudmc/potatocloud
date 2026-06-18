package net.potatocloud.webinterface.controller;

import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import net.potatocloud.api.CloudAPI;
import net.potatocloud.api.group.Group;
import net.potatocloud.api.platform.Platform;
import net.potatocloud.webinterface.mapper.PlatformMapper;
import net.potatocloud.webinterface.model.ApiPlatform;
import net.potatocloud.webinterface.service.impl.PlatformServiceImpl;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;
import java.util.Map;

@Path("/api/v1/test")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Authenticated
@SecurityRequirement(name = "ApiKeyAuth")
@Tag(name = "Test", description = "Endpoints for testing purposes")
public class TestController {

    @Inject
    PlatformMapper platformMapper;

    @Inject
    PlatformServiceImpl platformService;

    @GET
    @Operation(summary = "Print the current date and time", description = "Returns the current date and time in ISO 8601 format.")
    @APIResponse(responseCode = "200", description = "Current date and time returned successfully")
    public Response getCurrentDateTime() {
        String currentDateTime = java.time.ZonedDateTime.now().toString();
        return Response.ok().entity(Map.of("value", currentDateTime)).build();
    }

    @GET
    @Path("/groups")
    public Response getGroups() {
        List<Group> groups = CloudAPI.instance().groupManager().groups();
        List<String> groupNames = groups.stream().map(Group::name).toList();

        return Response.ok().entity(groupNames).build();
    }

    @GET
    @Path("/map-test")
    public Response getMapTest() {
        Platform platform = CloudAPI.instance().groupManager().find("lobby").get().platform();
        ApiPlatform apiPlatform = platformMapper.toApi(platform);
        return Response.ok().entity(apiPlatform).build();
    }

}
