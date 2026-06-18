package net.potatocloud.webinterface.api.rest;

import io.javalin.apibuilder.ApiBuilder;
import io.javalin.http.HttpStatus;
import lombok.RequiredArgsConstructor;
import net.potatocloud.webinterface.service.NodeService;

@RequiredArgsConstructor
public class ScreenRestController extends BaseRestController {

    private final NodeService nodeService;

    @Override
    public void register() {
        ApiBuilder.get("/api/screens", ctx -> ctx.json(nodeService.getScreens()));

        ApiBuilder.get("/api/screens/{name}/logs", ctx -> {
            String name = ctx.pathParam("name");
            int tail = parseTail(ctx.queryParam("tail"));
            var response = nodeService.getScreenLogs(name, tail);
            if (response == null) {
                ctx.status(HttpStatus.NOT_FOUND).json(error("Screen '" + name + "' not found"));
                return;
            }
            ctx.json(response);
        });
    }

    private int parseTail(String tailParam) {
        if (tailParam == null || tailParam.isBlank()) {
            return -1;
        }
        try {
            return Integer.parseInt(tailParam);
        } catch (NumberFormatException ignored) {
            return -1;
        }
    }
}
