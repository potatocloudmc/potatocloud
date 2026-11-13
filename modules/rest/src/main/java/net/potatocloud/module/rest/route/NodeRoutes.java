package net.potatocloud.module.rest.route;

import io.javalin.config.RoutesConfig;
import io.javalin.http.Context;
import net.potatocloud.api.CloudAPI;
import net.potatocloud.api.cluster.ClusterManager;
import net.potatocloud.api.cluster.ClusterNode;
import net.potatocloud.module.rest.RestServer;
import net.potatocloud.module.rest.auth.Scope;
import net.potatocloud.module.rest.error.ApiException;
import net.potatocloud.module.rest.model.NodeResponse;

public final class NodeRoutes {

    private final ClusterManager clusterManager;

    public NodeRoutes(CloudAPI api) {
        this.clusterManager = api.clusterManager();
    }

    public void register(RoutesConfig routes) {
        routes.get(RestServer.PREFIX + "/nodes", this::list, Scope.NODE_READ);
        routes.get(RestServer.PREFIX + "/nodes/{name}", this::get, Scope.NODE_READ);
    }

    private void list(Context ctx) {
        final ClusterNode local = clusterManager.localNode();

        ctx.json(clusterManager.nodes().stream()
                .map(node -> NodeResponse.from(node, local))
                .toList());
    }

    private void get(Context ctx) {
        final String name = ctx.pathParam("name");
        final ClusterNode node = clusterManager.find(name).orElseThrow(() -> ApiException.notFound("node", name));

        ctx.json(NodeResponse.from(node, clusterManager.localNode()));
    }
}
