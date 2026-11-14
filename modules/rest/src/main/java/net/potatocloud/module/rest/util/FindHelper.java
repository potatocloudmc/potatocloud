package net.potatocloud.module.rest.util;

import net.potatocloud.api.CloudAPI;
import net.potatocloud.api.cluster.ClusterNode;
import net.potatocloud.api.group.Group;
import net.potatocloud.api.platform.Platform;
import net.potatocloud.api.player.CloudPlayer;
import net.potatocloud.api.service.Service;
import net.potatocloud.api.template.Template;
import net.potatocloud.module.rest.error.ApiException;

import java.util.Optional;
import java.util.UUID;

public final class FindHelper {

    private FindHelper() {
    }

    public static Group group(String name) {
        return api().groupManager().find(name).orElseThrow(() -> ApiException.notFound("group", name));
    }

    public static Service service(String name) {
        return api().serviceManager().find(name).orElseThrow(() -> ApiException.notFound("service", name));
    }

    public static Platform platform(String name) {
        return api().platformManager().find(name).orElseThrow(() -> ApiException.notFound("platform", name));
    }

    public static ClusterNode node(String name) {
        return api().clusterManager().find(name).orElseThrow(() -> ApiException.notFound("node", name));
    }

    public static Template template(String name) {
        return api().templateManager().find(name).orElseThrow(() -> ApiException.notFound("template", name));
    }

    public static CloudPlayer player(String id) {
        Optional<CloudPlayer> player;
        try {
            player = api().playerManager().find(UUID.fromString(id));
        } catch (IllegalArgumentException e) {
            player = api().playerManager().find(id);
        }
        return player.orElseThrow(() -> ApiException.notFound("player", id));
    }

    private static CloudAPI api() {
        return CloudAPI.instance();
    }
}
