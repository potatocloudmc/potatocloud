package net.potatocloud.plugins.cloudcommand.command;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import lombok.RequiredArgsConstructor;
import net.potatocloud.plugins.cloudcommand.Config;
import net.potatocloud.plugins.cloudcommand.MessagesConfig;

import java.util.List;

@RequiredArgsConstructor
public class CloudCommand implements SimpleCommand {

    private final MessagesConfig messages;
    private final Config config;

    @Override
    public void execute(Invocation invocation) {
        if (!(invocation.source() instanceof Player player)) {
            return;
        }

        if (!player.hasPermission(config.permission())) {
            player.sendMessage(messages.get("no-permission"));
            return;
        }

        final String[] args = invocation.arguments();

        if (args.length == 0) {
            sendHelp(player);
            return;
        }

        final String commandName = args[0].toLowerCase();

        switch (commandName) {
            case "group" -> {
                final GroupSubCommand groupSubCommand = new GroupSubCommand(player, messages);

                if (args.length < 2) {
                    groupSubCommand.sendHelpGroup(player);
                    return;
                }

                final String sub = args[1].toLowerCase();

                switch (sub) {
                    case "list" -> groupSubCommand.listGroups();
                    case "info" -> groupSubCommand.infoGroup(args);
                    case "shutdown" -> groupSubCommand.shutdownGroup(args);
                    case "edit" -> groupSubCommand.editGroup(args);
                    case "property" -> groupSubCommand.propertyGroup(args);
                    default -> groupSubCommand.sendHelpGroup(player);
                }
            }
            case "service" -> {
                final ServiceSubCommand serviceSubCommand = new ServiceSubCommand(player, messages);

                if (args.length < 2) {
                    serviceSubCommand.sendHelp(player);
                    return;
                }

                final String sub = args[1].toLowerCase();

                switch (sub) {
                    case "list" -> serviceSubCommand.listServices();
                    case "start" -> serviceSubCommand.startService(args);
                    case "stop" -> serviceSubCommand.stopService(args);
                    case "info" -> serviceSubCommand.infoService(args);
                    case "edit" -> serviceSubCommand.editService(args);
                    case "property" -> serviceSubCommand.propertyService(args);
                    case "copy" -> serviceSubCommand.copyService(args);
                }
            }
            case "player" -> {
                final PlayerSubCommand playerSubCommand = new PlayerSubCommand(player, messages);

                if (args.length < 2) {
                    playerSubCommand.sendHelp();
                    return;
                }

                final String sub = args[1].toLowerCase();

                switch (sub) {
                    case "list" -> playerSubCommand.listPlayers();
                    case "connect" -> playerSubCommand.connectPlayer(args);
                }
            }
            default -> sendHelp(player);
        }
    }

    private void sendHelp(Player player) {
        player.sendMessage(messages.get("help.group"));
        player.sendMessage(messages.get("help.service"));
        player.sendMessage(messages.get("help.player"));
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        if (!invocation.source().hasPermission("potatocloud.cloudcommand")) {
            return List.of();
        }

        final String[] args = invocation.arguments();

        if (args.length == 0) {
            return List.of("group", "service", "player");
        }

        if (args.length == 1) {
            return List.of("group", "service", "player").stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .toList();
        }

        final String commandName = args[0].toLowerCase();

        return switch (commandName) {
            case "group" -> new GroupSubCommand(null, null).suggest(args);
            case "service" -> new ServiceSubCommand(null, null).suggest(args);
            case "player" -> new PlayerSubCommand(null, null).suggest(args);
            default -> List.of();
        };
    }
}
