package net.potatocloud.node.service.helper;

import net.potatocloud.api.group.Group;
import net.potatocloud.api.service.Service;
import net.potatocloud.node.config.NodeConfig;
import net.potatocloud.node.utils.NetworkUtils;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class ServicePorts {

    private static final int MAX_PORT = 65535;

    private ServicePorts() {
    }

    public static int nextPort(Group group, NodeConfig config, Map<String, Service> services) {
        int port = group.platform().proxy()
                ? config.service().proxyStartPort()
                : config.service().serviceStartPort();

        final Set<Integer> usedPorts = services.values().stream()
                .map(Service::port)
                .collect(Collectors.toSet());

        while (port <= MAX_PORT) {
            if (!usedPorts.contains(port) && NetworkUtils.isPortFree(port)) {
                return port;
            }
            port++;
        }

        throw new IllegalStateException("No free port available in range " + port + " - " + MAX_PORT);
    }
}
