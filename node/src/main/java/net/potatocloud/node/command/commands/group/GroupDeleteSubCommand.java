package net.potatocloud.node.command.commands.group;

import lombok.RequiredArgsConstructor;
import net.potatocloud.api.group.ServiceGroup;
import net.potatocloud.api.group.ServiceGroupManager;
import net.potatocloud.node.command.SubCommand;
import net.potatocloud.node.command.SubCommandInfo;
import net.potatocloud.node.command.TabCompleter;
import net.potatocloud.node.command.TabCompleters;
import net.potatocloud.node.console.Logger;

import java.util.List;

@RequiredArgsConstructor
@SubCommandInfo(name = "delete", description = "Delete the given service group", usage = "group delete &8[&agroup&8]")
public class GroupDeleteSubCommand extends SubCommand implements TabCompleter {

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

        groupManager.deleteServiceGroup(group);
        logger.info("&7Service group &a" + name + " &7was deleted");
    }

    @Override
    public List<String> complete(String[] args) {
        if (args.length == 1) {
            return TabCompleters.group(args);
        }
        return List.of();
    }
}
