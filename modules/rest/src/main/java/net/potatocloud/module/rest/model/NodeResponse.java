package net.potatocloud.module.rest.model;

import net.potatocloud.api.cluster.ClusterNode;

import java.time.Instant;

public record NodeResponse(String name, String host, int port, Instant startedAt, boolean local) {

    public static NodeResponse from(ClusterNode node, ClusterNode localNode) {
        return new NodeResponse(
                node.name(),
                node.host(),
                node.port(),
                node.startedAt(),
                node.name().equals(localNode.name())
        );
    }
}
