package net.potatocloud.plugins.optional.npc.npc;

import net.potatocloud.plugins.optional.npc.action.ActionType;
import net.potatocloud.plugins.optional.npc.action.NpcAction;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

import java.util.ArrayList;
import java.util.List;

public abstract class Npc {

    private final String name;
    private NpcLocation location;
    private String group;
    private boolean lookAtPlayer = true;
    private List<String> hologram = new ArrayList<>();
    private NpcAction leftClick = NpcAction.of(ActionType.CONNECT);
    private NpcAction rightClick = NpcAction.of(ActionType.SELECTOR);

    protected Npc(String name) {
        this.name = name;
    }

    public abstract Entity spawn(Location location);

    public String name() {
        return name;
    }

    public NpcLocation location() {
        return location;
    }

    public void location(NpcLocation location) {
        this.location = location;
    }

    public String group() {
        return group;
    }

    public void group(String group) {
        this.group = group;
    }

    public boolean lookAtPlayer() {
        return lookAtPlayer;
    }

    public void lookAtPlayer(boolean lookAtPlayer) {
        this.lookAtPlayer = lookAtPlayer;
    }

    public List<String> hologram() {
        return hologram;
    }

    public void hologram(List<String> hologram) {
        this.hologram = new ArrayList<>(hologram);
    }

    public NpcAction leftClick() {
        return leftClick;
    }

    public void leftClick(NpcAction leftClick) {
        this.leftClick = leftClick;
    }

    public NpcAction rightClick() {
        return rightClick;
    }

    public void rightClick(NpcAction rightClick) {
        this.rightClick = rightClick;
    }
}
