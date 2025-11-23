package net.potatocloud.node.command.commands.service;

import lombok.RequiredArgsConstructor;
import net.potatocloud.api.group.ServiceGroup;
import net.potatocloud.api.group.ServiceGroupManager;
import net.potatocloud.api.service.ServiceManager;
import net.potatocloud.node.command.SubCommand;
import net.potatocloud.node.command.SubCommandInfo;
import net.potatocloud.node.command.TabCompleter;
import net.potatocloud.node.command.TabCompleters;
import net.potatocloud.node.console.Logger;

import java.util.List;

@RequiredArgsConstructor
@SubCommandInfo(name = "start", description = "Start new service(s)", usage = "service start &8[&agroup&8] (&aamount&8)")
public class ServiceStartSubCommand extends SubCommand implements TabCompleter {

    private final ServiceManager serviceManager;
    private final ServiceGroupManager groupManager;
    private final Logger logger;

    @Override
    public void execute(String[] args) {
        if (args.length < 1) {
            sendUsage();
            return;
        }

        final String name = args[0];
        final ServiceGroup group = groupManager.getServiceGroup(name);
        if (group == null) {
            logger.info("&cNo service group found with the name &a" + name);
            return;
        }

        int amount = 1;
        if (args.length >= 3) {
            try {
                amount = Integer.parseInt(args[2]);
                if (amount <= 0) {
                    sendUsage();
                    return;
                }
            } catch (NumberFormatException e) {
                sendUsage();
                return;
            }
        }

        serviceManager.startServices(group, amount);

        logger.info("&7Starting " + amount + " new service(s) of group &a" + group.getName());
    }

    @Override
    public List<String> complete(String[] args) {
        if (args.length == 1) {
            return TabCompleters.group(args);
        }
        return List.of();
    }
}
