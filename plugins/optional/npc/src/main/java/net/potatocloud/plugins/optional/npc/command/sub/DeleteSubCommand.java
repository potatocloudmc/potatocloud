package net.potatocloud.plugins.optional.npc.command.sub;

import net.potatocloud.plugins.optional.npc.command.NpcSubCommand;
import net.potatocloud.plugins.optional.npc.npc.Npc;
import net.potatocloud.plugins.optional.npc.npc.NpcService;
import net.potatocloud.plugins.shared.MessagesConfig;
import org.bukkit.entity.Player;

public final class DeleteSubCommand extends NpcSubCommand {

    public DeleteSubCommand(NpcService npcService, MessagesConfig messages) {
        super(npcService, messages, 0);
    }

    @Override
    protected void execute(Player player, Npc npc, String[] args) {
        npcService.delete(npc);
        player.sendMessage(messages.get("deleted", "%name%", npc.name()));
    }

    @Override
    public String name() {
        return "delete";
    }
}
