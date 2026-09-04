package net.potatocloud.webinterface.service.impl;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import net.potatocloud.api.CloudAPI;
import net.potatocloud.node.Node;
import net.potatocloud.webinterface.dto.response.ScreenLogsResponse;
import net.potatocloud.webinterface.mapper.NodeMapper;
import net.potatocloud.webinterface.model.ApiClusterNode;
import net.potatocloud.webinterface.service.NodeService;

import java.util.List;

@ApplicationScoped
public class NodeServiceImpl implements NodeService {

    private final CloudAPI cloudAPI = CloudAPI.instance();
    private final Node node = Node.instance();
    @Inject
    NodeMapper nodeMapper;

    @Override
    public List<String> findScreens() {
        return node.screenManager().screens().keySet().stream().sorted().toList();
    }

    @Override
    public ScreenLogsResponse screenLogs(String name, Integer tail) {
        var screen = node.screenManager().get(name);
        if (screen == null) {
            return null;
        }

        List<String> logs = screen.logs();
        if (tail != null && tail > 0 && tail < logs.size()) {
            logs = logs.subList(logs.size() - tail, logs.size());
        }

        return new ScreenLogsResponse(name, logs);
    }

    @Override
    public List<ApiClusterNode> clusterNodes() {
        return cloudAPI.clusterManager().nodes().stream().map(nodeMapper::toApiClusterNode).toList();
    }

}
