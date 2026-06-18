package net.potatocloud.webinterface.service;

import lombok.RequiredArgsConstructor;
import net.potatocloud.api.CloudAPI;
import net.potatocloud.api.service.Service;
import net.potatocloud.node.Node;
import net.potatocloud.node.screen.Screen;
import net.potatocloud.webinterface.dto.screen.ScreenLogsDto;

import java.util.List;

@RequiredArgsConstructor
public class NodeService {

    private final CloudAPI cloudAPI;
    private final Node node;

    public long getNodeUptime() {
        return node.getUptime();
    }

    public boolean isNodeReady() {
        return node.ready();
    }

    public List<String> getScreens() {
        return node.screenManager().getScreens().keySet().stream().sorted().toList();
    }

    public ScreenLogsDto getScreenLogs(String screenName, int tail) {
        Screen screen = node.screenManager().get(screenName);
        if (screen == null) {
            return null;
        }

        List<String> logs = screen.cachedLogs();
        if (tail > 0 && tail < logs.size()) {
            logs = logs.subList(logs.size() - tail, logs.size());
        }

        return ScreenLogsDto.builder().screen(screenName).logs(logs).build();
    }

    public void executeCommandOnService(String serviceName, String command) {
        Service service = cloudAPI.serviceManager().services().stream()
                .filter(s -> s.name().equals(serviceName))
                .findFirst()
                .orElse(null);

        if (service == null) return;

        cloudAPI.serviceManager().execute(service, command);
    }
}

