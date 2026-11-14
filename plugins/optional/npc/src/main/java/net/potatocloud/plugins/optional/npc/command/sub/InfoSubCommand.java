package net.potatocloud.plugins.optional.npc.command.sub;

import net.potatocloud.plugins.optional.npc.action.NpcAction;
import net.potatocloud.plugins.optional.npc.command.NpcSubCommand;
import net.potatocloud.plugins.optional.npc.npc.MobNpc;
import net.potatocloud.plugins.optional.npc.npc.Npc;
import net.potatocloud.plugins.optional.npc.npc.NpcLocation;
import net.potatocloud.plugins.optional.npc.npc.NpcService;
import net.potatocloud.plugins.optional.npc.npc.PlayerNpc;
import net.potatocloud.plugins.shared.MessagesConfig;
import org.bukkit.entity.Player;

public final class InfoSubCommand extends NpcSubCommand {

    public InfoSubCommand(NpcService npcService, MessagesConfig messages) {
        super(npcService, messages, 0);
    }

    @Override
    protected void execute(Player player, Npc npc, String[] args) {
        final NpcLocation location = npc.location();

        final String type = switch (npc) {
            case PlayerNpc playerNpc -> "player (" + playerNpc.skin() + ")";
            case MobNpc mobNpc -> "mob (" + mobNpc.entityType().name().toLowerCase() + ")";
            default -> throw new IllegalStateException("Unexpected value: " + npc);
        };

        player.sendMessage(messages.get("info.name", "%name%", npc.name()));
        player.sendMessage(messages.get("info.type", "%type%", type));
        player.sendMessage(messages.get("info.group", "%group%", npc.group()));
        player.sendMessage(messages.get("info.location",
                "%world%", location.world(),
                "%x%", String.valueOf(Math.round(location.x())),
                "%y%", String.valueOf(Math.round(location.y())),
                "%z%", String.valueOf(Math.round(location.z()))));
        player.sendMessage(messages.get("info.look-at-player", "%value%", String.valueOf(npc.lookAtPlayer())));
        player.sendMessage(messages.get("info.left-click", "%action%", format(npc.leftClick())));
        player.sendMessage(messages.get("info.right-click", "%action%", format(npc.rightClick())));
        player.sendMessage(messages.get("info.hologram"));
        for (int i = 0; i < npc.hologram().size(); i++) {
            player.sendMessage(messages.get("info.hologram-line",
                    "%line%", String.valueOf(i + 1),
                    "%text%", npc.hologram().get(i)));
        }
    }

    private String format(NpcAction action) {
        final String type = action.type().name().toLowerCase();
        return action.text() == null ? type : type + " (" + action.text() + ")";
    }

    @Override
    public String name() {
        return "info";
    }
}
