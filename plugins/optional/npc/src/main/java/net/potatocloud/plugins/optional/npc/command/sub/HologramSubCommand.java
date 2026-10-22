package net.potatocloud.plugins.optional.npc.command.sub;

import net.potatocloud.plugins.optional.npc.command.NpcSubCommand;
import net.potatocloud.plugins.optional.npc.npc.Npc;
import net.potatocloud.plugins.optional.npc.npc.NpcService;
import net.potatocloud.plugins.shared.MessagesConfig;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

public final class HologramSubCommand extends NpcSubCommand {

    public HologramSubCommand(NpcService npcService, MessagesConfig messages) {
        super(npcService, messages, 1);
    }



    @Override
    protected void execute(Player player, Npc npc, String[] args) {
        final List<String> lines = npc.hologram();

        switch (args[0].toLowerCase()) {
            case "add" -> {
                if (args.length < 2) {
                    sendUsage(player);
                    return;
                }
                lines.add(text(args, 1));
            }
            case "set" -> {
                final int index = index(args, lines);
                if (index == -1 || args.length < 3) {
                    sendUsage(player);
                    return;
                }
                lines.set(index, text(args, 2));
            }
            case "remove" -> {
                final int index = index(args, lines);
                if (index == -1) {
                    sendUsage(player);
                    return;
                }
                lines.remove(index);
            }
            case "clear" -> lines.clear();
            default -> {
                sendUsage(player);
                return;
            }
        }

        npcService.update(npc);
        player.sendMessage(messages.get("hologram-edited", "%name%", npc.name()));
    }



    private String text(String[] args, int start) {
        return String.join(" ", Arrays.copyOfRange(args, start, args.length));
    }

    private int index(String[] args, List<String> lines) {
        if (args.length < 2) {
            return -1;
        }
        try {
            final int index = Integer.parseInt(args[1]) - 1;
            return index >= 0 && index < lines.size() ? index : -1;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    @Override
    protected List<String> suggestArgs(String[] args) {
        return args.length == 1 ? List.of("add", "set", "remove", "clear") : List.of();
    }

    @Override
    public String name() {
        return "hologram";
    }
}
