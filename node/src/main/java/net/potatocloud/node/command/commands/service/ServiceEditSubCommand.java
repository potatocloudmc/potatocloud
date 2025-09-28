package net.potatocloud.node.command.commands.service;

import lombok.RequiredArgsConstructor;
import net.potatocloud.api.service.Service;
import net.potatocloud.api.service.ServiceManager;
import net.potatocloud.node.command.SubCommand;
import net.potatocloud.node.command.SubCommandInfo;
import net.potatocloud.node.command.TabCompleter;
import net.potatocloud.node.console.Logger;

import java.util.List;
import java.util.stream.Stream;

@RequiredArgsConstructor
@SubCommandInfo(name = "edit", description = "Edit the given service", usage = "service edit [&akey&8] [&avalue&8]")
public class ServiceEditSubCommand extends SubCommand implements TabCompleter {

    private final ServiceManager serviceManager;
    private final Logger logger;

    @Override
    public void execute(String[] args) {
        if (args.length < 3) {
            sendUsage();
            return;
        }

        final String name = args[0];
        final Service service = serviceManager.getService(name);
        if (service == null) {
            logger.info("&cNo service found with the name &a" + name);
            return;
        }

        final String key = args[1].toLowerCase();
        final String value = args[2];

        try {
            if (key.equals("maxplayers")) {
                service.setMaxPlayers(Integer.parseInt(value));
            } else {
                sendUsage();
                return;
            }
            service.update();
            logger.info("Updated &a" + key + " &7for service &a" + name + "&7 to &a" + value);
        } catch (NumberFormatException ex) {
            sendUsage();
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

        if (args.length == 2) {
            return Stream.of("maxPlayers")
                    .filter(key -> key.startsWith(args[1].toLowerCase()))
                    .toList();
        }
        return List.of();
    }
}
