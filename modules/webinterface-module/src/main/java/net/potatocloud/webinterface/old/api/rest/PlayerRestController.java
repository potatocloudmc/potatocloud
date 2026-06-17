package net.potatocloud.webinterface.old.api.rest;

import io.javalin.apibuilder.ApiBuilder;
import lombok.RequiredArgsConstructor;
import net.potatocloud.webinterface.old.service.PlayerService;

@RequiredArgsConstructor
public class PlayerRestController extends BaseRestController {

    private final PlayerService playerService;

    @Override
    public void register() {
        ApiBuilder.get("/api/player", ctx -> ctx.json(playerService.getOnlinePlayers()));
        ApiBuilder.get("/api/players", ctx -> ctx.json(playerService.getOnlinePlayers()));
    }
}
