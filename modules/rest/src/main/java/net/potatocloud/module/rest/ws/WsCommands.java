package net.potatocloud.module.rest.ws;

import net.potatocloud.api.service.Service;
import net.potatocloud.api.service.ServiceManager;
import net.potatocloud.module.rest.auth.Scope;

// Lets a client run a console command on a service
public final class WsCommands {

    private final ServiceManager serviceManager;

    public WsCommands(ServiceManager serviceManager) {
        this.serviceManager = serviceManager;
    }

    public void run(WsClient client, String serviceName, String command) {
        if (!client.allowed(Scope.SERVICE_COMMAND)) {
            return;
        }
        if (serviceName == null || command == null) {
            client.error("service and command are required");
            return;
        }

        final Service service = serviceManager.find(serviceName).orElse(null);
        if (service == null) {
            client.error("No service named " + serviceName);
            return;
        }

        serviceManager.execute(service, command);
        client.ok("Command sent to " + serviceName);
    }
}
