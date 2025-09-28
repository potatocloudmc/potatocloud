package net.potatocloud.node.command.commands.service;

import lombok.RequiredArgsConstructor;
import net.potatocloud.api.group.ServiceGroup;
import net.potatocloud.api.service.Service;
import net.potatocloud.api.service.ServiceManager;
import net.potatocloud.node.command.SubCommand;
import net.potatocloud.node.command.SubCommandInfo;
import net.potatocloud.node.command.TabCompleter;
import net.potatocloud.node.console.Logger;

import java.util.List;

@RequiredArgsConstructor
@SubCommandInfo(name = "info", description = "Show details of the given service", usage = "service info &8[&aservice8]")
public class ServiceInfoSubCommand extends SubCommand implements TabCompleter {

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

        logger.info("&7Info for service &a" + service.getName() + "&8:");
        logger.info("&8» &7Group: &a" + service.getServiceGroup().getName());
        logger.info("&8» &7Port: &a" + service.getPort());
        logger.info("&8» &7Status: &a" + service.getStatus());
        logger.info("&8» &7Online Players: &a" + service.getOnlinePlayerCount());
        logger.info("&8» &7Max Players: &a" + service.getMaxPlayers());
        logger.info("&8» &7Memory usage: &a" + service.getUsedMemory() + "MB");
        logger.info("&8» &7Online Time: &a" + service.getFormattedUptime());
        logger.info("&8» &7Start Timestamp: &a" + service.getFormattedStartTimestamp());
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