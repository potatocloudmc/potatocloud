package net.potatocloud.webinterface.api.rest;

import io.javalin.apibuilder.ApiBuilder;
import lombok.RequiredArgsConstructor;
import net.potatocloud.webinterface.service.StatsService;

@RequiredArgsConstructor
public class StatsRestController extends BaseRestController {

    private final StatsService statsService;

    @Override
    public void register() {
        ApiBuilder.get("/api/stats", ctx -> ctx.json(statsService.getStats()));
        ApiBuilder.get("/api/stats/joins", ctx -> ctx.json(statsService.getJoinStats()));
        ApiBuilder.get("/api/stats/players", ctx -> ctx.json(statsService.getPlayerCount()));
        ApiBuilder.get("/api/stats/services", ctx -> ctx.json(statsService.getServiceStats()));
    }
}
