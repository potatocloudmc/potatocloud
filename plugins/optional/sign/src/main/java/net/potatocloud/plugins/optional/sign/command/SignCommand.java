package net.potatocloud.plugins.optional.sign.command;

import net.potatocloud.api.CloudAPI;
import net.potatocloud.api.group.Group;
import net.potatocloud.api.service.Service;
import net.potatocloud.common.config.Config;
import net.potatocloud.plugins.optional.sign.sign.CloudSign;
import net.potatocloud.plugins.optional.sign.sign.SignLocation;
import net.potatocloud.plugins.optional.sign.sign.SignService;
import net.potatocloud.plugins.shared.MessagesConfig;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public final class SignCommand implements TabExecutor {

    private static final List<String> SUB_COMMANDS = List.of("create", "remove", "list", "reload");
    private static final int TARGET_RANGE = 5;

    private final SignService signService;
    private final Config config;
    private final MessagesConfig messages;

    public SignCommand(SignService signService, Config config, MessagesConfig messages) {
        this.signService = signService;
        this.config = config;
        this.messages = messages;
    }

    @Override
    public boolean onCommand(@NonNull CommandSender sender, @NonNull Command command, @NonNull String label, String @NonNull [] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(messages.get("only-players"));
            return true;
        }

        switch (args.length == 0 ? "" : args[0].toLowerCase()) {
            case "create" -> create(player, args);
            case "remove" -> remove(player);
            case "list" -> list(player);
            case "reload" -> reload(player);
            default -> SUB_COMMANDS.forEach(name -> player.sendMessage(messages.get("help." + name)));
        }

        return true;
    }

    private void create(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(messages.get("usage.create"));
            return;
        }

        final String group = args[1];
        if (!CloudAPI.instance().groupManager().exists(group)) {
            player.sendMessage(messages.get("no-group", "%name%", group));
            return;
        }

        final Block block = targetSign(player).orElse(null);
        if (block == null) {
            player.sendMessage(messages.get("no-target"));
            return;
        }

        if (signService.find(block).isPresent()) {
            player.sendMessage(messages.get("sign-exists"));
            return;
        }

        signService.create(new CloudSign(group, SignLocation.of(block)));
        player.sendMessage(messages.get("created", "%group%", group));
    }

    private void remove(Player player) {
        targetSign(player).flatMap(signService::find).ifPresentOrElse(sign -> {
            signService.delete(sign);
            player.sendMessage(messages.get("removed", "%group%", sign.group()));
        }, () -> player.sendMessage(messages.get("sign-not-found")));
    }

    private void list(Player player) {
        if (signService.signs().isEmpty()) {
            player.sendMessage(messages.get("list.empty"));
            return;
        }

        player.sendMessage(messages.get("list.header"));
        for (CloudSign sign : signService.signs()) {
            final SignLocation location = sign.location();
            player.sendMessage(messages.get("list.entry",
                    "%group%", sign.group(),
                    "%world%", location.world(),
                    "%x%", String.valueOf(location.x()),
                    "%y%", String.valueOf(location.y()),
                    "%z%", String.valueOf(location.z()),
                    "%service%", signService.service(sign).map(Service::name).orElse("-")));
        }
    }

    private void reload(Player player) {
        config.reload();
        messages.reload();
        signService.load();
        player.sendMessage(messages.get("reloaded"));
    }

    private Optional<Block> targetSign(Player player) {
        return Optional.ofNullable(player.getTargetBlockExact(TARGET_RANGE))
                .filter(block -> Tag.ALL_SIGNS.isTagged(block.getType()));
    }

    @Override
    public List<String> onTabComplete(@NonNull CommandSender sender, @NonNull Command command, @NonNull String label, String[] args) {
        if (args.length == 1) {
            return filter(SUB_COMMANDS, args[0]);
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("create")) {
            return filter(CloudAPI.instance().groupManager().groups().stream().map(Group::name).toList(), args[1]);
        }
        return List.of();
    }

    private List<String> filter(Collection<String> suggestions, String input) {
        return suggestions.stream()
                .filter(suggestion -> suggestion.toLowerCase().startsWith(input.toLowerCase()))
                .toList();
    }
}
