package net.potatocloud.plugins.optional.npc.command;

import net.potatocloud.plugins.optional.npc.npc.Npc;
import net.potatocloud.plugins.optional.npc.npc.NpcService;
import net.potatocloud.plugins.shared.MessagesConfig;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

public abstract class NpcSubCommand implements SubCommand {

    protected final NpcService npcService;
    protected final MessagesConfig messages;
    private final int requiredArgs;

    protected NpcSubCommand(NpcService npcService, MessagesConfig messages, int requiredArgs) {
        this.npcService = npcService;
        this.messages = messages;
        this.requiredArgs = requiredArgs;
    }

    protected abstract void execute(Player player, Npc npc, String[] args);

    protected List<String> suggestArgs(String[] args) {
        return List.of();
    }

    @Override
    public void execute(Player player, String[] args) {
        if (args.length < 1 + requiredArgs) {
            sendUsage(player);
            return;
        }

        npcService.find(args[0]).ifPresentOrElse(
                npc -> execute(player, npc, Arrays.copyOfRange(args, 1, args.length)),
                () -> player.sendMessage(messages.get("npc-not-found", "%name%", args[0])));
    }

    @Override
    public List<String> suggest(String[] args) {
        if (args.length == 1) {
            return npcService.npcs().stream().map(Npc::name).toList();
        }
        return suggestArgs(Arrays.copyOfRange(args, 1, args.length));
    }

    protected void sendUsage(Player player) {
        player.sendMessage(messages.get("usage." + name()));
    }
}
