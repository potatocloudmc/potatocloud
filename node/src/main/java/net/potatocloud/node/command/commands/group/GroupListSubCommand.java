package net.potatocloud.node.command.commands.group;

import lombok.RequiredArgsConstructor;
import net.potatocloud.api.group.ServiceGroup;
import net.potatocloud.api.group.ServiceGroupManager;
import net.potatocloud.node.command.SubCommand;
import net.potatocloud.node.command.SubCommandInfo;
import net.potatocloud.node.console.Logger;

import java.util.List;

@RequiredArgsConstructor
@SubCommandInfo(name = "list", description = "List all service groups", usage = "group list")
public class GroupListSubCommand extends SubCommand {

    private final ServiceGroupManager groupManager;
    private final Logger logger;

    @Override
    public void execute(String[] args) {
        List<ServiceGroup> groups = groupManager.getAllServiceGroups();
        if (groups.isEmpty()) {
            logger.info("There are &cno &7service groups");
            return;
        }
        logger.info("All loaded groups&8:");
        for (ServiceGroup group : groups) {
            logger.info("&8» &a" + group.getName());
        }
    }
}
