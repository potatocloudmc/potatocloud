package net.potatocloud.node.command.commands.service;

import lombok.RequiredArgsConstructor;
import net.potatocloud.api.group.ServiceGroup;
import net.potatocloud.api.group.ServiceGroupManager;
import net.potatocloud.api.service.Service;
import net.potatocloud.api.service.ServiceManager;
import net.potatocloud.node.command.SubCommand;
import net.potatocloud.node.command.SubCommandInfo;
import net.potatocloud.node.command.TabCompleter;
import net.potatocloud.node.console.Logger;

import java.util.List;

@RequiredArgsConstructor
@SubCommandInfo(name = "stop", description = "Shutdown the given service", usage = "service stop &8[&aservice8]")
public class ServiceStopSubCommand extends SubCommand implements TabCompleter {

    private final ServiceManager serviceManager;
    private final Logger logger;

    @Override
    public void execute(String[] args) {
        if (args.length < 1) {
            sendUsage();
            return;
        }

        final String name = args[0];
        final Service service = serviceManager.getService(name);
        if (service == null) {
            logger.info("&cNo service found with the name &a" + name);
            return;
        }

        service.shutdown();
    }

    @Override
    public List<String> complete(String[] args) {
        if (args.length == 1) {
            return serviceManager.getAllServices().stream()
                    .map(Service::getName)
                    .filter(name -> name.startsWith(args[0]))
                    .toList();
        }
        return List.of();
    }
}
