package net.potatocloud.plugins.optional.npc.action;

import net.potatocloud.api.CloudAPI;
import net.potatocloud.api.group.Group;
import net.potatocloud.plugins.optional.npc.connect.ServiceConnector;
import net.potatocloud.plugins.optional.npc.npc.Npc;
import net.potatocloud.plugins.optional.npc.selector.SelectorMenu;
import net.potatocloud.plugins.optional.npc.util.TextUtils;
import net.potatocloud.plugins.shared.MessagesConfig;
import org.bukkit.entity.Player;

import java.util.Optional;

public final class ActionExecutor {

    private final SelectorMenu selectorMenu;
    private final ServiceConnector connector;
    private final MessagesConfig messages;

    public ActionExecutor(SelectorMenu selectorMenu, ServiceConnector connector, MessagesConfig messages) {
        this.selectorMenu = selectorMenu;
        this.connector = connector;
        this.messages = messages;
    }

    public void execute(Player player, Npc npc, NpcAction action) {
        switch (action.type()) {
            case NONE -> {
            }
            case COMMAND -> player.performCommand(withPlayer(action.text(), player));
            case MESSAGE -> player.sendMessage(TextUtils.component(withPlayer(action.text(), player)));
            case SELECTOR -> group(player, npc).ifPresent(group -> selectorMenu.open(player, group));
            case CONNECT -> group(player, npc).ifPresent(group -> connector.connectToBest(player, group));
        }
    }

    private String withPlayer(String text, Player player) {
        return text.replace("%player%", player.getName());
    }

    private Optional<Group> group(Player player, Npc npc) {
        final Optional<Group> group = CloudAPI.instance().groupManager().find(npc.group());
        if (group.isEmpty()) {
            player.sendMessage(messages.get("no-group", "%name%", npc.group()));
        }
        return group;
    }
}
