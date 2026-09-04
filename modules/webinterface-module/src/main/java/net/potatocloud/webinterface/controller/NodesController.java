package net.potatocloud.webinterface.controller;

import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import net.potatocloud.webinterface.model.ApiClusterNode;
import net.potatocloud.webinterface.service.NodeService;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;

@Path("/api/v1/nodes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Authenticated
@SecurityRequirement(name = "ApiKeyAuth")
@Tag(name = "Nodes", description = "Endpoints related to node management and information")
public class NodesController {

    @Inject
    NodeService nodeService;

    @GET
    @Operation(summary = "List all cluster nodes", description = "Returns a summry list of cluster nodes.")
    @APIResponse(responseCode = "200", description = "List of all cluster nodes", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiClusterNode.class, type = SchemaType.ARRAY)))
    public List<ApiClusterNode> allNodes() {
        return nodeService.clusterNodes();
    }

}
