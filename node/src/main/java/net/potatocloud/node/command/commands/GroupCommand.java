package net.potatocloud.node.command.commands;

import net.potatocloud.api.group.ServiceGroupManager;
import net.potatocloud.node.command.Command;
import net.potatocloud.node.command.CommandInfo;
import net.potatocloud.node.command.TabCompleter;
import net.potatocloud.node.command.commands.group.*;
import net.potatocloud.node.console.Logger;

import java.util.List;
import java.util.stream.Stream;

@CommandInfo(name = "group", description = "Manage service groups", aliases = {"groups"})
public class GroupCommand extends Command {

    public GroupCommand(Logger logger, ServiceGroupManager groupManager) {
        addSubCommand(new GroupCreateSubCommand(groupManager));
        addSubCommand(new GroupDeleteSubCommand(groupManager, logger));
        addSubCommand(new GroupEditSubCommand(groupManager, logger));
        addSubCommand(new GroupInfoSubCommand(groupManager, logger));
        addSubCommand(new GroupListSubCommand(groupManager, logger));
        addSubCommand(new GroupShutdownSubCommand(groupManager, logger));
        addSubCommand(new GroupPropertySubCommand(groupManager, logger));
    }

    @Override
    public void execute(String[] args) {
        sendHelp();
    }
}
