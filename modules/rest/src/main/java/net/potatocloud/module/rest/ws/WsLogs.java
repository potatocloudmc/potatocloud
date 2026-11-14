package net.potatocloud.module.rest.ws;

import net.potatocloud.api.service.LogListener;
import net.potatocloud.api.service.Service;
import net.potatocloud.api.service.ServiceManager;
import net.potatocloud.module.rest.auth.Scope;

// Sends the live logs of a service to a client
public final class WsLogs {

    private final ServiceManager serviceManager;

    public WsLogs(ServiceManager serviceManager) {
        this.serviceManager = serviceManager;
    }

    public void start(WsClient client, String serviceName) {
        if (!client.allowed(Scope.SERVICE_LOGS)) {
            return;
        }

        if (serviceName == null) {
            client.error("service is required");
            return;
        }

        final Service service = serviceManager.find(serviceName).orElse(null);
        if (service == null) {
            client.error("No service named " + serviceName);
            return;
        }

        if (!client.logListeners().containsKey(serviceName)) {
            final LogListener logListener = line -> client.send(WsMessage.log(serviceName, line));

            client.logListeners().put(serviceName, logListener);
            serviceManager.addLogListener(service, logListener);
        }

        client.ok("Listening logs of " + serviceName);
    }

    public void stop(WsClient client, String serviceName) {
        if (serviceName == null) {
            client.error("service is required");
            return;
        }

        remove(client, serviceName);
        client.ok("Stopped listening logs of " + serviceName);
    }

    // called when a client disconnects
    public void stopAll(WsClient client) {
        client.logListeners().keySet().forEach(serviceName -> remove(client, serviceName));
    }

    private void remove(WsClient client, String serviceName) {
        final LogListener logListener = client.logListeners().remove(serviceName);
        if (logListener == null) {
            return;
        }

        serviceManager.find(serviceName).ifPresent(service -> serviceManager.removeLogListener(service, logListener));
    }
}
