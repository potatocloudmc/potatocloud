package net.potatocloud.node.command.commands.group;

import lombok.RequiredArgsConstructor;
import net.potatocloud.api.group.ServiceGroupManager;
import net.potatocloud.node.Node;
import net.potatocloud.node.command.SubCommand;
import net.potatocloud.node.command.SubCommandInfo;
import net.potatocloud.node.setup.setups.GroupConfigurationSetup;

@RequiredArgsConstructor
@SubCommandInfo(name = "create", description = "Create a new service group", usage = "group create")
public class GroupCreateSubCommand extends SubCommand {

    private final ServiceGroupManager groupManager;

    @Override
    public void execute(String[] args) {
        final Node node = Node.getInstance();

        node.getSetupManager().startSetup(new GroupConfigurationSetup(node.getConsole(), node.getScreenManager(), groupManager, node.getPlatformManager()));
    }
}
