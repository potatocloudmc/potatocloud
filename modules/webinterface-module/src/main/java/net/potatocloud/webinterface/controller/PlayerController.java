package net.potatocloud.webinterface.controller;

import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import net.potatocloud.webinterface.model.ApiPlayer;
import net.potatocloud.webinterface.service.impl.PlayerServiceImpl;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;

@Path("/api/v1/players")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Authenticated
@SecurityRequirement(name = "ApiKeyAuth")
@Tag(name = "Players", description = "Endpoints related to player information and management")
public class PlayerController {

    @Inject
    PlayerServiceImpl playerService;

    @GET
    @Operation(summary = "List all players", description = "Returns a list of all players currently connected to the cloud environment.")
    @APIResponse(responseCode = "200", description = "List of all players", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiPlayer.class, type = SchemaType.ARRAY)))
    public List<ApiPlayer> findAll() {
        return playerService.findAll();
    }


}
