package net.potatocloud.webinterface.api.rest;

import io.javalin.apibuilder.ApiBuilder;
import lombok.RequiredArgsConstructor;
import net.potatocloud.webinterface.service.PlatformService;

@RequiredArgsConstructor
public class PlatformRestController extends BaseRestController {

    private final PlatformService platformService;

    @Override
    public void register() {
        ApiBuilder.get("/api/platform", ctx -> ctx.json(platformService.getPlatforms()));
        ApiBuilder.get("/api/platforms", ctx -> ctx.json(platformService.getPlatforms()));
    }
}
