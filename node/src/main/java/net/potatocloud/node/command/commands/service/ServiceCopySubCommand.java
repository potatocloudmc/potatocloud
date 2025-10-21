package net.potatocloud.node.command.commands.service;

import lombok.RequiredArgsConstructor;
import net.potatocloud.api.service.Service;
import net.potatocloud.api.service.ServiceManager;
import net.potatocloud.node.command.SubCommand;
import net.potatocloud.node.command.SubCommandInfo;
import net.potatocloud.node.command.TabCompleter;
import net.potatocloud.node.console.Logger;

import java.util.List;

@RequiredArgsConstructor
@SubCommandInfo(name = "copy", description = "Copy files from the given service to the given template", usage = "service copy &8[&aservice&8] &8[&atemplate8]")
public class ServiceCopySubCommand extends SubCommand implements TabCompleter {

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

        final String template = args[1];
        String filter;
        if (args.length >= 3) {
            filter = args[2];
        } else {
            filter = "";
        }

        if (filter.isEmpty()) {
            service.copy(template);
        } else {
            service.copy(template, filter);
        }

        logger.info("Copied &a" + (filter.isEmpty() ? "all service files" : filter) + " &7to template: &a" + template);
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
            final Service service = serviceManager.getService(args[0]);
            if (service == null) {
                return List.of();
            }
            return service.getServiceGroup().getServiceTemplates().stream().toList();
        }
        return List.of();
    }
}
