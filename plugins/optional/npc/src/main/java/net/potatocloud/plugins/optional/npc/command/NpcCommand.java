package net.potatocloud.plugins.optional.npc.command;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.potatocloud.common.config.Config;
import net.potatocloud.plugins.shared.MessagesConfig;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class NpcCommand implements BasicCommand {

    private final Map<String, SubCommand> subCommands = new LinkedHashMap<>();
    private final Config config;
    private final MessagesConfig messages;

    public NpcCommand(Config config, MessagesConfig messages, List<SubCommand> subCommands) {
        this.config = config;
        this.messages = messages;
        subCommands.forEach(subCommand -> this.subCommands.put(subCommand.name(), subCommand));
    }

    @Override
    public void execute(CommandSourceStack source, String[] args) {
        if (!(source.getSender() instanceof Player player)) {
            source.getSender().sendMessage(messages.get("only-players"));
            return;
        }

        if (!hasPermission(player)) {
            player.sendMessage(messages.get("no-permission"));
            return;
        }

        final SubCommand subCommand = args.length == 0 ? null : subCommands.get(args[0].toLowerCase());
        if (subCommand == null) {
            sendHelp(player);
            return;
        }

        subCommand.execute(player, Arrays.copyOfRange(args, 1, args.length));
    }

    @Override
    public Collection<String> suggest(CommandSourceStack source, String[] args) {
        if (!hasPermission(source.getSender())) {
            return List.of();
        }

        if (args.length <= 1) {
            return filter(subCommands.keySet(), args.length == 0 ? "" : args[0]);
        }

        final SubCommand subCommand = subCommands.get(args[0].toLowerCase());
        if (subCommand == null) {
            return List.of();
        }

        return filter(subCommand.suggest(Arrays.copyOfRange(args, 1, args.length)), args[args.length - 1]);
    }

    private boolean hasPermission(CommandSender sender) {
        return sender.hasPermission(config.get("permission").asString());
    }

    private void sendHelp(Player player) {
        subCommands.keySet().forEach(name -> player.sendMessage(messages.get("help." + name)));
    }

    private List<String> filter(Collection<String> suggestions, String input) {
        return suggestions.stream()
                .filter(suggestion -> suggestion.toLowerCase().startsWith(input.toLowerCase()))
                .toList();
    }
}
