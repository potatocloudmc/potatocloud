package net.potatocloud.node.command.commands.service;

import lombok.RequiredArgsConstructor;
import net.potatocloud.api.service.Service;
import net.potatocloud.api.service.ServiceManager;
import net.potatocloud.node.Node;
import net.potatocloud.node.command.SubCommand;
import net.potatocloud.node.command.SubCommandInfo;
import net.potatocloud.node.command.TabCompleter;
import net.potatocloud.node.console.Logger;
import net.potatocloud.node.screen.Screen;
import net.potatocloud.node.service.ServiceImpl;

import java.util.List;

@RequiredArgsConstructor
@SubCommandInfo(name = "screen", description = "Switch to the screen of the given service", usage = "service screen &8[&aservice8]")
public class ServiceScreenSubCommand extends SubCommand implements TabCompleter {

    private final ServiceManager serviceManager;
    private final Logger logger;

    @Override
    public void execute(String[] args) {
        if (args.length < 2) {
            sendUsage();
            return;
        }

        final String name = args[0];
        final Service service = serviceManager.getService(name);
        if (service == null) {
            logger.info("&cNo service found with the name &a" + name);
            return;
        }

        if (service instanceof ServiceImpl impl) {
            final Screen screen = Node.getInstance().getScreenManager().getScreen(impl.getScreen().getName());
            if (screen == null) {
                logger.error("Cant switch to screen of service " + name);
                return;
            }
            Node.getInstance().getScreenManager().switchScreen(screen.getName());
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
        return List.of();
    }
}
