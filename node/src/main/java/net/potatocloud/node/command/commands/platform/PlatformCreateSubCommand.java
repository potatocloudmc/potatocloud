package net.potatocloud.node.command.commands.platform;

import lombok.RequiredArgsConstructor;
import net.potatocloud.node.Node;
import net.potatocloud.node.command.SubCommand;
import net.potatocloud.node.command.SubCommandInfo;
import net.potatocloud.node.setup.setups.PlatformConfigurationSetup;

@RequiredArgsConstructor
@SubCommandInfo(name = "create", description = "Create a new platform", usage = "platform create")
public class PlatformCreateSubCommand extends SubCommand {

    @Override
    public void execute(String[] args) {
        final Node node = Node.getInstance();

        node.getSetupManager().startSetup(new PlatformConfigurationSetup(node.getConsole(), node.getScreenManager(), node.getPlatformManager(), node.getLogger()));
    }
}
