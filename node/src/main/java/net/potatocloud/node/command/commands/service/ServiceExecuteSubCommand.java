package net.potatocloud.node.command.commands.service;

import lombok.RequiredArgsConstructor;
import net.potatocloud.api.service.Service;
import net.potatocloud.api.service.ServiceManager;
import net.potatocloud.node.command.SubCommand;
import net.potatocloud.node.command.SubCommandInfo;
import net.potatocloud.node.command.TabCompleter;
import net.potatocloud.node.console.Logger;

import java.util.Arrays;
import java.util.List;

@RequiredArgsConstructor
@SubCommandInfo(name = "execute", description = "Execute a command on the given service", usage = "service execute &8[&aservice&8]")
public class ServiceExecuteSubCommand extends SubCommand implements TabCompleter {

    private final ServiceManager serviceManager;
    private final Logger logger;

    @Override
    public void execute(String[] args) {
        if (args.length < 2) {
            sendUsage();
            return;
        }

        final String name = args[0];
        final Service service = serviceManager.getService(name);
        if (service == null) {
            logger.info("&cNo service found with the name &a" + name);
            return;
        }

        if (!service.isOnline()) {
            logger.info("Service &a" + name + " &7is &coffline");
            return;
        }

        final String commandToExecute = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

        if (service.executeCommand(commandToExecute)) {
            logger.info("&7Executed command on service &a" + name + "&8: &7" + commandToExecute);
        } else {
            logger.info("&cFailed to execute command on service &a" + name);
        }
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
