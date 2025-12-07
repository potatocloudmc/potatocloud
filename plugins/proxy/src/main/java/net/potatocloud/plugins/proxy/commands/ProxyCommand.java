package net.potatocloud.plugins.proxy.commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import lombok.RequiredArgsConstructor;
import net.potatocloud.plugins.proxy.Config;
import net.potatocloud.plugins.proxy.MessagesConfig;

import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
public class ProxyCommand implements SimpleCommand {

    private final Config config;
    private final MessagesConfig messages;

    @Override
    public void execute(Invocation invocation) {
        final CommandSource source = invocation.source();
        final String[] args = invocation.arguments();

        if (!(source instanceof Player player)) {
            return;
        }

        if (!player.hasPermission(config.getPermission())) {
            player.sendMessage(messages.get("no-permission"));
            return;
        }

        if (args.length == 1) {
            switch (args[0].toLowerCase()) {
                case "toggle" -> {
                    final boolean state = config.maintenance();
                    final boolean newState = !state;
                    config.maintenance(newState);

                    player.sendMessage(newState
                            ? messages.get("now_maintenance")
                            : messages.get("now_not_maintenance"));
                }
                case "list", "info" -> {
                    player.sendMessage(messages.get("info_text"));
                    config.whitelist().forEach(name -> {
                        player.sendMessage(messages.get("info_key")
                                .replaceText(text -> text.match("%name%").replacement(name)));
                    });
                }
                case "reload" -> {
                    config.reload();
                    player.sendMessage(messages.get("reload"));
                }
                default -> sendUsage(player);
            }
            return;
        }

        if (args.length == 3) {
            if (!args[0].equalsIgnoreCase("whitelist")) {
                sendUsage(player);
                return;
            }

            final List<String> whitelist = config.whitelist();
            final String name = args[2];

            if (args[1].equalsIgnoreCase("add")) {
                if (whitelist.contains(name)) {
                    player.sendMessage(messages.get("whitelist.already"));
                    return;
                }
                whitelist.add(name);
                config.whitelist(whitelist);
                player.sendMessage(messages.get("whitelist.added")
                        .replaceText(text -> text.match("%name%").replacement(name)));
                return;
            }

            if (args[1].equalsIgnoreCase("remove")) {
                if (!whitelist.contains(name)) {
                    player.sendMessage(messages.get("whitelist.not")
                            .replaceText(text -> text.match("%name%").replacement(name)));
                    return;
                }
                whitelist.remove(name);
                config.whitelist(whitelist);
                player.sendMessage(messages.get("whitelist.removed")
                        .replaceText(text -> text.match("%name%").replacement(name)));
                return;
            }

            sendUsage(player);
            return;
        }

        sendUsage(player);
    }

    private void sendUsage(Player player) {
        player.sendMessage(messages.get("help.toggle"));
        player.sendMessage(messages.get("help.list"));
        player.sendMessage(messages.get("help.reload"));
        player.sendMessage(messages.get("help.whitelist"));
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        final CommandSource source = invocation.source();
        final String[] args = invocation.arguments();

        if (!(source instanceof Player player)) {
            return Collections.emptyList();
        }

        if (!player.hasPermission(config.getPermission())) {
            return Collections.emptyList();
        }

        if (args.length == 3) {
            if (args[1].equalsIgnoreCase("remove")) {
                return config.whitelist();
            }
        }

        if (args.length == 2) {
            return List.of("add", "remove");
        }

        if (args.length == 0 || args.length == 1) {
            return List.of("toggle", "list", "whitelist", "reload");
        }

        return Collections.emptyList();
    }
}
