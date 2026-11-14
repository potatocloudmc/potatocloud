package net.potatocloud.plugins.optional.npc.command.sub;

import net.potatocloud.plugins.optional.npc.command.SubCommand;
import net.potatocloud.plugins.optional.npc.npc.Npc;
import net.potatocloud.plugins.optional.npc.npc.NpcService;
import net.potatocloud.plugins.shared.MessagesConfig;
import org.bukkit.entity.Player;

public final class ListSubCommand implements SubCommand {

    private final NpcService npcService;
    private final MessagesConfig messages;

    public ListSubCommand(NpcService npcService, MessagesConfig messages) {
        this.npcService = npcService;
        this.messages = messages;
    }

    @Override
    public void execute(Player player, String[] args) {
        if (npcService.npcs().isEmpty()) {
            player.sendMessage(messages.get("list.empty"));
            return;
        }

        player.sendMessage(messages.get("list.header"));
        for (Npc npc : npcService.npcs()) {
            player.sendMessage(messages.get("list.entry", "%name%", npc.name(), "%group%", npc.group()));
        }
    }

    @Override
    public String name() {
        return "list";
    }
}
