package net.potatocloud.plugins.optional.npc.command.sub;

import net.potatocloud.plugins.optional.npc.command.NpcSubCommand;
import net.potatocloud.plugins.optional.npc.npc.Npc;
import net.potatocloud.plugins.optional.npc.npc.NpcService;
import net.potatocloud.plugins.shared.MessagesConfig;
import org.bukkit.entity.Player;

public final class TeleportSubCommand extends NpcSubCommand {

    public TeleportSubCommand(NpcService npcService, MessagesConfig messages) {
        super(npcService, messages, 0);
    }

    @Override
    protected void execute(Player player, Npc npc, String[] args) {
        npc.location().toBukkit().ifPresentOrElse(location -> {
            player.teleport(location);
            player.sendMessage(messages.get("teleported", "%name%", npc.name()));
        }, () -> player.sendMessage(messages.get("world-not-loaded", "%world%", npc.location().world())));
    }

    @Override
    public String name() {
        return "tp";
    }
}
