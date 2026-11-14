package net.potatocloud.plugins.optional.npc.command;

import org.bukkit.entity.Player;

import java.util.List;

public interface SubCommand {

    String name();

    void execute(Player player, String[] args);

    default List<String> suggest(String[] args) {
        return List.of();
    }
}
