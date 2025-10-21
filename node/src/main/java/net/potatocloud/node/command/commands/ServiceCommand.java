package net.potatocloud.node.command.commands;

import lombok.RequiredArgsConstructor;
import net.potatocloud.api.group.ServiceGroupManager;
import net.potatocloud.api.service.ServiceManager;
import net.potatocloud.node.command.Command;
import net.potatocloud.node.command.CommandInfo;
import net.potatocloud.node.command.commands.service.*;
import net.potatocloud.node.console.Logger;

@RequiredArgsConstructor
@CommandInfo(name = "service", description = "Manage services", aliases = {"ser"})
public class ServiceCommand extends Command {

    public ServiceCommand(Logger logger, ServiceManager serviceManager, ServiceGroupManager groupManager) {
        addSubCommand(new ServiceCopySubCommand(serviceManager, logger));
        addSubCommand(new ServiceEditSubCommand(serviceManager, logger));
        addSubCommand(new ServiceExecuteSubCommand(serviceManager, logger));
        addSubCommand(new ServiceInfoSubCommand(serviceManager, logger));
        addSubCommand(new ServiceListSubCommand(serviceManager, logger));
        addSubCommand(new ServicePropertySubCommand(serviceManager, logger));
        addSubCommand(new ServiceScreenSubCommand(serviceManager, logger));
        addSubCommand(new ServiceStartSubCommand(serviceManager, groupManager, logger));
        addSubCommand(new ServiceStopSubCommand(serviceManager, logger));
    }

    @Override
    public void execute(String[] args) {
        sendHelp();
    }
}