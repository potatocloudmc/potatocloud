package net.potatocloud.webinterface.api.rest;

import io.javalin.apibuilder.ApiBuilder;
import lombok.RequiredArgsConstructor;
import net.potatocloud.webinterface.service.ServerService;

@RequiredArgsConstructor
public class ServiceRestController extends BaseRestController {

    private final ServerService serverService;

    @Override
    public void register() {
        ApiBuilder.get("/api/services", ctx -> ctx.json(serverService.getServices()));
        ApiBuilder.get("/api/services/{name}", ctx -> ctx.json(serverService.getService(ctx.pathParam("name"))));
        ApiBuilder.post("/api/services/{name}/stop", ctx -> {
            serverService.stopService(ctx.pathParam("name"));
            ctx.status(204);
        });
    }

}
