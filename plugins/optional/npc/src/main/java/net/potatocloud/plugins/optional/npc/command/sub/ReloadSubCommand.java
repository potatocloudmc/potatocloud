package net.potatocloud.plugins.optional.npc.command.sub;

import net.potatocloud.common.config.Config;
import net.potatocloud.plugins.optional.npc.command.SubCommand;
import net.potatocloud.plugins.optional.npc.npc.NpcService;
import net.potatocloud.plugins.shared.MessagesConfig;
import org.bukkit.entity.Player;

public final class ReloadSubCommand implements SubCommand {

    private final NpcService npcService;
    private final Config config;
    private final MessagesConfig messages;

    public ReloadSubCommand(NpcService npcService, Config config, MessagesConfig messages) {
        this.npcService = npcService;
        this.config = config;
        this.messages = messages;
    }

    @Override
    public void execute(Player player, String[] args) {
        config.reload();
        messages.reload();
        npcService.load();
        player.sendMessage(messages.get("reloaded"));
    }

    @Override
    public String name() {
        return "reload";
    }
}
