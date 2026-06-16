package net.potatocloud.node.service.runtime;

import net.potatocloud.api.service.ServiceManager;

import java.util.Map;

public final class ServiceProcessMonitor implements Runnable {

    private final Map<String, ServiceRuntime> runtimes;
    private final ServiceManager serviceManager;

    public ServiceProcessMonitor(Map<String, ServiceRuntime> runtimes, ServiceManager serviceManager) {
        this.runtimes = runtimes;
        this.serviceManager = serviceManager;
    }

    @Override
    public void run() {
        for (Map.Entry<String, ServiceRuntime> entry : runtimes.entrySet()) {
            final String name = entry.getKey();
            final ServiceRuntime runtime = entry.getValue();

            if (runtime.isAlive()) {
                continue;
            }

            serviceManager.find(name).ifPresent(service -> {
                if (service.running()) {
                    serviceManager.stop(service);
                }
            });
        }
    }
}
