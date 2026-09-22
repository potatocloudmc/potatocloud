package net.potatocloud.webinterface.mapper;

import jakarta.enterprise.context.ApplicationScoped;
import net.potatocloud.api.cluster.ClusterNode;
import net.potatocloud.webinterface.model.ApiClusterNode;

@ApplicationScoped
public class NodeMapper {

    public ApiClusterNode toApiClusterNode(ClusterNode node) {
        ApiClusterNode apiClusterNode = new ApiClusterNode();
        apiClusterNode.name(node.name());
        apiClusterNode.host(node.host());
        apiClusterNode.port(node.port());
        apiClusterNode.startedAt(node.startedAt());
        return apiClusterNode;
    }

}
