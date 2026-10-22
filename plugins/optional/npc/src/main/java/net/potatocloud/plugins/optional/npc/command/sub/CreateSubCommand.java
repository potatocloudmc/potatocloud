package net.potatocloud.plugins.optional.npc.command.sub;

import net.potatocloud.api.CloudAPI;
import net.potatocloud.common.config.Config;
import net.potatocloud.plugins.optional.npc.command.Arguments;
import net.potatocloud.plugins.optional.npc.command.SubCommand;
import net.potatocloud.plugins.optional.npc.npc.MobNpc;
import net.potatocloud.plugins.optional.npc.npc.Npc;
import net.potatocloud.plugins.optional.npc.npc.NpcLocation;
import net.potatocloud.plugins.optional.npc.npc.NpcService;
import net.potatocloud.plugins.optional.npc.npc.PlayerNpc;
import net.potatocloud.plugins.shared.MessagesConfig;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;

public final class CreateSubCommand implements SubCommand {

    private final NpcService npcService;
    private final Config config;
    private final MessagesConfig messages;

    public CreateSubCommand(NpcService npcService, Config config, MessagesConfig messages) {
        this.npcService = npcService;
        this.config = config;
        this.messages = messages;
    }

    @Override
    public void execute(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(messages.get("usage.create"));
            return;
        }

        final String name = args[0];
        final String group = args[1];

        if (!Arguments.isNpcName(name)) {
            player.sendMessage(messages.get("invalid-name", "%name%", name));
            return;
        }

        if (npcService.find(name).isPresent()) {
            player.sendMessage(messages.get("npc-exists", "%name%", name));
            return;
        }

        if (!CloudAPI.instance().groupManager().exists(group)) {
            player.sendMessage(messages.get("no-group", "%name%", group));
            return;
        }

        final Optional<Npc> createdNpc;

        switch (args[2].toLowerCase()) {
            case "player":
                createdNpc = playerNpc(player, name, args);
                break;

            case "mob":
                createdNpc = mobNpc(player, name, args);
                break;

            default:
                player.sendMessage(messages.get("invalid-type", "%value%", args[2]));
                createdNpc = Optional.empty();
                break;
        }

        if (createdNpc.isEmpty()) {
            return;
        }

        final Npc npc = createdNpc.get();

        npc.group(group);
        npc.location(NpcLocation.of(player.getLocation()));
        npc.hologram(config.get("default-hologram").asStringList());

        npcService.create(npc);
        player.sendMessage(messages.get("created", "%name%", name));
    }

    @Override
    public List<String> suggest(String[] args) {
        return switch (args.length) {
            case 2 -> Arguments.groups();
            case 3 -> List.of("player", "mob");
            case 4 -> "mob".equalsIgnoreCase(args[2]) ? Arguments.mobs() : List.of();
            default -> List.of();
        };
    }

    private Optional<Npc> playerNpc(Player player, String name, String[] args) {
        final String skin = args.length > 3 ? args[3] : player.getName();
        if (!Arguments.isSkinName(skin)) {
            player.sendMessage(messages.get("invalid-skin", "%value%", skin));
            return Optional.empty();
        }
        return Optional.of(new PlayerNpc(name, skin));
    }

    private Optional<Npc> mobNpc(Player player, String name, String[] args) {
        if (args.length <= 3) {
            return Optional.of(new MobNpc(name, EntityType.VILLAGER));
        }

        final Optional<EntityType> entityType = Arguments.parseMob(args[3]);
        if (entityType.isEmpty()) {
            player.sendMessage(messages.get("invalid-entity", "%value%", args[3]));
        }
        return entityType.map(type -> new MobNpc(name, type));
    }

    @Override
    public String name() {
        return "create";
    }
}
