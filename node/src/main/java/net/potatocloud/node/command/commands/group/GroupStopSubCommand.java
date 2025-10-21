package net.potatocloud.node.command.commands.group;

import lombok.RequiredArgsConstructor;
import net.potatocloud.api.group.ServiceGroup;
import net.potatocloud.api.group.ServiceGroupManager;
import net.potatocloud.api.service.Service;
import net.potatocloud.node.command.SubCommand;
import net.potatocloud.node.command.SubCommandInfo;
import net.potatocloud.node.command.TabCompleter;
import net.potatocloud.node.console.Logger;

import java.util.List;

@RequiredArgsConstructor
@SubCommandInfo(name = "stop", description = "Stop all services of the given service group", usage = "group stop &8[&agroup&8]")
public class GroupStopSubCommand extends SubCommand implements TabCompleter {

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

        for (Service service : group.getAllServices()) {
            service.shutdown();
        }
    }

    @Override
    public List<String> complete(String[] args) {
        if (args.length == 1) {
            return groupManager.getAllServiceGroups().stream()
                    .map(ServiceGroup::getName)
                    .filter(name -> name.startsWith(args[0]))
                    .toList();
        }
        return List.of();
    }
}
