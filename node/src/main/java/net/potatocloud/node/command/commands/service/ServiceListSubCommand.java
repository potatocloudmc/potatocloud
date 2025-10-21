package net.potatocloud.node.command.commands.service;

import lombok.RequiredArgsConstructor;
import net.potatocloud.api.service.Service;
import net.potatocloud.api.service.ServiceManager;
import net.potatocloud.node.command.SubCommand;
import net.potatocloud.node.command.SubCommandInfo;
import net.potatocloud.node.console.Logger;

import java.util.List;

@RequiredArgsConstructor
@SubCommandInfo(name = "list", description = "List all services", usage = "service list")
public class ServiceListSubCommand extends SubCommand {

    private final ServiceManager serviceManager;
    private final Logger logger;

    @Override
    public void execute(String[] args) {
        final List<Service> services = serviceManager.getAllServices();
        if (services.isEmpty()) {
            logger.info("There are &cno &7services");
            return;
        }
        logger.info("All services&8:");
        for (Service service : services) {
            logger.info("&8» &a" + service.getName() + " &7- Group: &a" + service.getServiceGroup().getName() + " &7- Status: &a" + service.getStatus());
        }
    }
}
