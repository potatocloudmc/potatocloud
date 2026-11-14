package net.potatocloud.module.rest.player;

import io.javalin.config.RoutesConfig;
import io.javalin.http.Context;
import net.potatocloud.api.CloudAPI;
import net.potatocloud.api.player.CloudPlayer;
import net.potatocloud.api.service.Service;
import net.potatocloud.module.rest.RestServer;
import net.potatocloud.module.rest.auth.Scope;
import net.potatocloud.module.rest.error.ApiException;
import net.potatocloud.module.rest.util.FindHelper;
import net.potatocloud.module.rest.util.RequestHelper;

public final class PlayerRoutes {

    private final CloudAPI api;

    public PlayerRoutes(CloudAPI api) {
        this.api = api;
    }

    public void register(RoutesConfig routes) {
        final String path = RestServer.PREFIX + "/players";

        routes.get(path, this::list, Scope.PLAYER_READ);
        routes.get(path + "/{player}", this::get, Scope.PLAYER_READ);
        routes.post(path + "/{player}/connect", this::connect, Scope.PLAYER_CONNECT);
        routes.post(path + "/{player}/kick", this::kick, Scope.PLAYER_KICK);
    }

    private void list(Context ctx) {
        ctx.json(api.playerManager().players().stream().map(PlayerResponse::from).toList());
    }

    private void get(Context ctx) {
        ctx.json(PlayerResponse.from(FindHelper.player(ctx.pathParam("player"))));
    }

    private void connect(Context ctx) {
        final CloudPlayer player = FindHelper.player(ctx.pathParam("player"));
        final ConnectRequest request = RequestHelper.read(ctx, ConnectRequest.class);

        RequestHelper.require(request.service() != null, "service is required");

        final Service service = FindHelper.service(request.service());

        RequestHelper.require(!service.group().platform().proxy(), "Players cannot be connected to a proxy");

        if (player.service().map(current -> current.name().equals(service.name())).orElse(false)) {
            throw ApiException.conflict("Player " + player.username() + " is already connected to " + service.name());
        }

        api.playerManager().connectTo(player, service);
        ctx.status(204);
    }

    private void kick(Context ctx) {
        final CloudPlayer player = FindHelper.player(ctx.pathParam("player"));
        final String reason = ctx.body().isBlank() ? null : ctx.bodyAsClass(KickRequest.class).reason();

        if (reason == null) {
            api.playerManager().kick(player);
        } else {
            api.playerManager().kick(player, reason);
        }
        ctx.status(204);
    }
}
