package net.potatocloud.node.command.commands.player;

import lombok.RequiredArgsConstructor;
import net.potatocloud.api.player.CloudPlayer;
import net.potatocloud.api.player.CloudPlayerManager;
import net.potatocloud.api.service.Service;
import net.potatocloud.api.service.ServiceManager;
import net.potatocloud.node.command.SubCommand;
import net.potatocloud.node.command.SubCommandInfo;
import net.potatocloud.node.command.TabCompleter;
import net.potatocloud.node.console.Logger;

import java.util.List;

@RequiredArgsConstructor
@SubCommandInfo(name = "connect", description = "Connect the given player to the given service", usage = "player connect &8[&aplayer&8] [&aservice&8]")
public class PlayerConnectSubCommand extends SubCommand implements TabCompleter {

    private final CloudPlayerManager playerManager;
    private final Logger logger;
    private final ServiceManager serviceManager;

    @Override
    public void execute(String[] args) {
        if (args.length < 2) {
            sendUsage();
            return;
        }

        final String playerName = args[0];
        final String serviceName = args[1];

        final CloudPlayer player = playerManager.getCloudPlayer(playerName);
        if (player == null) {
            logger.info("&cNo player found with the name &a" + playerName);
            return;
        }

        final Service service = serviceManager.getService(serviceName);
        if (service == null) {
            logger.info("&cNo service found with the name &a" + serviceName);
            return;
        }

        if (player.getConnectedServiceName().equalsIgnoreCase(service.getName())) {
            logger.info("Player &a" + player.getUsername() + " &7is already connected to &a" + service.getName());
            return;
        }

        player.connectWithService(service);
        logger.info("Successfully connected player &a" + player.getUsername() + " &7to service &a" + service.getName());
    }

    @Override
    public List<String> complete(String[] args) {
        if (args.length == 1) {
            return playerManager.getOnlinePlayers().stream().map(CloudPlayer::getUsername).
                    filter(input -> input.startsWith(args[0])).toList();
        }
        if (args.length == 2) {
            return serviceManager.getAllServices().stream()
                    .filter(service -> !service.getServiceGroup().getPlatform().isProxy())
                    .map(Service::getName)
                    .filter(name -> name.startsWith(args[1]))
                    .toList();
        }
        return List.of();
    }
}
