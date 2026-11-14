package net.potatocloud.plugins.optional.npc.command.sub;

import net.potatocloud.api.CloudAPI;
import net.potatocloud.plugins.optional.npc.command.Arguments;
import net.potatocloud.plugins.optional.npc.command.NpcSubCommand;
import net.potatocloud.plugins.optional.npc.npc.MobNpc;
import net.potatocloud.plugins.optional.npc.npc.Npc;
import net.potatocloud.plugins.optional.npc.npc.NpcService;
import net.potatocloud.plugins.optional.npc.npc.PlayerNpc;
import net.potatocloud.plugins.shared.MessagesConfig;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;

public final class EditSubCommand extends NpcSubCommand {

    public EditSubCommand(NpcService npcService, MessagesConfig messages) {
        super(npcService, messages, 2);
    }

    @Override
    protected void execute(Player player, Npc npc, String[] args) {
        final String option = args[0].toLowerCase();
        final String value = args[1];

        final boolean changed;

        switch (option) {
            case "group":
                changed = editGroup(player, npc, value);
                break;

            case "skin":
                changed = editSkin(player, npc, value);
                break;

            case "entity":
                changed = editEntity(player, npc, value);
                break;

            case "look-at":
                changed = editLookAt(player, npc, value);
                break;

            default:
                sendUsage(player);
                changed = false;
                break;
        }

        if (changed) {
            npcService.update(npc);
            player.sendMessage(messages.get("edited", "%name%", npc.name(), "%option%", option, "%value%", value));
        }
    }

    private boolean editGroup(Player player, Npc npc, String group) {
        if (!CloudAPI.instance().groupManager().exists(group)) {
            player.sendMessage(messages.get("no-group", "%name%", group));
            return false;
        }

        npc.group(group);
        return true;
    }

    private boolean editSkin(Player player, Npc npc, String skin) {
        if (!(npc instanceof PlayerNpc playerNpc)) {
            player.sendMessage(messages.get("only-player-npc"));
            return false;
        }

        if (!Arguments.isSkinName(skin)) {
            player.sendMessage(messages.get("invalid-skin", "%value%", skin));
            return false;
        }

        playerNpc.skin(skin);
        return true;
    }

    private boolean editEntity(Player player, Npc npc, String entity) {
        if (!(npc instanceof MobNpc mobNpc)) {
            player.sendMessage(messages.get("only-mob-npc"));
            return false;
        }

        final Optional<EntityType> entityType = Arguments.parseMob(entity);
        if (entityType.isEmpty()) {
            player.sendMessage(messages.get("invalid-entity", "%value%", entity));
            return false;
        }

        mobNpc.entityType(entityType.get());
        return true;
    }

    private boolean editLookAt(Player player, Npc npc, String value) {
        if (!value.equalsIgnoreCase("true") && !value.equalsIgnoreCase("false")) {
            sendUsage(player);
            return false;
        }

        npc.lookAtPlayer(Boolean.parseBoolean(value));
        return true;
    }

    @Override
    protected List<String> suggestArgs(String[] args) {
        if (args.length == 1) {
            return List.of("group", "skin", "entity", "look-at");
        }

        if (args.length != 2) {
            return List.of();
        }

        return switch (args[0].toLowerCase()) {
            case "group" -> Arguments.groups();
            case "entity" -> Arguments.mobs();
            case "look-at" -> List.of("true", "false");
            default -> List.of();
        };
    }

    @Override
    public String name() {
        return "edit";
    }
}
