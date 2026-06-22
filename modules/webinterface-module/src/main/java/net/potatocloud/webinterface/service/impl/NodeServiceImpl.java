package net.potatocloud.webinterface.service.impl;

import jakarta.enterprise.context.ApplicationScoped;
import net.potatocloud.api.CloudAPI;
import net.potatocloud.node.Node;
import net.potatocloud.webinterface.dto.response.ScreenLogsResponse;
import net.potatocloud.webinterface.service.NodeService;

import java.util.List;

@ApplicationScoped
public class NodeServiceImpl implements NodeService {

    private final CloudAPI cloudAPI = CloudAPI.instance();
    private final Node node = Node.instance();

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
}
