package net.potatocloud.webinterface.controller;

import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import net.potatocloud.webinterface.model.ApiJoinStats;
import net.potatocloud.webinterface.model.ApiServiceStats;
import net.potatocloud.webinterface.model.ApiStatsSummary;
import net.potatocloud.webinterface.service.impl.StatsServiceImpl;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/api/v1/stats")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Authenticated
@SecurityRequirement(name = "ApiKeyAuth")
@Tag(name = "Stats", description = "Endpoints related to cloud statistics and metrics")
public class StatsController {

    @Inject
    StatsServiceImpl statsService;

    @Path("/summary")
    @GET
    @APIResponse(
            responseCode = "200",
            description = "Summary of cloud statistics",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiStatsSummary.class))
    )
    public ApiStatsSummary summary() {
        return statsService.statsSummary();
    }

    @APIResponse(
            responseCode = "200",
            description = "Join statistics for the cloud",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiJoinStats.class))
    )
    @Path("/joins")
    @GET

    public ApiJoinStats joins() {
        return statsService.joinStats();
    }

    @APIResponse(
            responseCode = "200",
            description = "Service statistics for the cloud",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiServiceStats.class))
    )
    @Path("/services")
    @GET
    public ApiServiceStats services() {
        return statsService.serviceStats();
    }

}
