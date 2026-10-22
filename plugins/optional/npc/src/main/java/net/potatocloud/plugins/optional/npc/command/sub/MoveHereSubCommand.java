package net.potatocloud.plugins.optional.npc.command.sub;

import net.potatocloud.plugins.optional.npc.command.NpcSubCommand;
import net.potatocloud.plugins.optional.npc.npc.Npc;
import net.potatocloud.plugins.optional.npc.npc.NpcLocation;
import net.potatocloud.plugins.optional.npc.npc.NpcService;
import net.potatocloud.plugins.shared.MessagesConfig;
import org.bukkit.entity.Player;

public final class MoveHereSubCommand extends NpcSubCommand {

    public MoveHereSubCommand(NpcService npcService, MessagesConfig messages) {
        super(npcService, messages, 0);
    }

    @Override
    protected void execute(Player player, Npc npc, String[] args) {
        npc.location(NpcLocation.of(player.getLocation()));
        npcService.update(npc);
        player.sendMessage(messages.get("moved", "%name%", npc.name()));
    }

    @Override
    public String name() {
        return "movehere";
    }
}
