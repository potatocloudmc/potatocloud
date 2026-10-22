package net.potatocloud.plugins.optional.npc.command.sub;

import net.potatocloud.plugins.optional.npc.action.ActionType;
import net.potatocloud.plugins.optional.npc.action.NpcAction;
import net.potatocloud.plugins.optional.npc.command.NpcSubCommand;
import net.potatocloud.plugins.optional.npc.npc.Npc;
import net.potatocloud.plugins.optional.npc.npc.NpcService;
import net.potatocloud.plugins.shared.MessagesConfig;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public final class ActionSubCommand extends NpcSubCommand {

    public ActionSubCommand(NpcService npcService, MessagesConfig messages) {
        super(npcService, messages, 2);
    }

    @Override
    protected void execute(Player player, Npc npc, String[] args) {
        final String click = args[0].toLowerCase();
        final Optional<ActionType> type = Arrays.stream(ActionType.values())
                .filter(value -> value.name().equalsIgnoreCase(args[1]))
                .findFirst();

        if (type.isEmpty()) {
            player.sendMessage(messages.get("invalid-action", "%value%", args[1]));
            return;
        }

        final String text = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
        if (type.get().needsText() == text.isBlank()) {
            sendUsage(player);
            return;
        }

        final NpcAction action = new NpcAction(type.get(), text.isBlank() ? null : text);

        if (click.equals("left")) {
            npc.leftClick(action);
        } else if (click.equals("right")) {
            npc.rightClick(action);
        } else {
            sendUsage(player);
            return;
        }

        npcService.update(npc);
        player.sendMessage(messages.get("action-set", "%name%", npc.name(), "%click%", click, "%action%", type.get().name().toLowerCase()));
    }

    @Override
    protected List<String> suggestArgs(String[] args) {
        return switch (args.length) {
            case 1 -> List.of("left", "right");
            case 2 -> Arrays.stream(ActionType.values()).map(value -> value.name().toLowerCase()).toList();
            default -> List.of();
        };
    }

    @Override
    public String name() {
        return "action";
    }
}
