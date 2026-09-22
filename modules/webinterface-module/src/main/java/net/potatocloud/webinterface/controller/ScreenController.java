package net.potatocloud.webinterface.controller;

import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import net.potatocloud.webinterface.dto.response.ScreenLogsResponse;
import net.potatocloud.webinterface.service.NodeService;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.resteasy.reactive.RestQuery;

import java.util.List;

@Path("/api/v1/screens")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Authenticated
@SecurityRequirement(name = "ApiKeyAuth")
@Tag(name = "Screens", description = "Endpoints related to screen information and management")
public class ScreenController {

    @Inject
    NodeService nodeService;

    @APIResponse(responseCode = "200", description = "List of active screens",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = String.class, type = SchemaType.ARRAY))
    )
    @GET
    public List<String> screens() {
        return nodeService.findScreens();
    }

    @GET
    @Path("/{name}/logs")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Logs of the specified screen",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ScreenLogsResponse.class))),
            @APIResponse(responseCode = "204", description = "Screen not found or no logs available")
    })
    public ScreenLogsResponse logs(@PathParam("name") String name, @RestQuery("tail") Integer tail) {
        return nodeService.screenLogs(name, tail);
    }

}
