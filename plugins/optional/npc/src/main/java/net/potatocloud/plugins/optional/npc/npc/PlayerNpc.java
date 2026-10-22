package net.potatocloud.plugins.optional.npc.npc;

import io.papermc.paper.datacomponent.item.ResolvableProfile;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mannequin;

public final class PlayerNpc extends Npc {

    private String skin;

    public PlayerNpc(String name, String skin) {
        super(name);
        this.skin = skin;
    }

    @Override
    public Entity spawn(Location location) {
        return location.getWorld().spawn(location, Mannequin.class, mannequin -> {
            mannequin.setProfile(ResolvableProfile.resolvableProfile().name(skin).build());
            mannequin.setImmovable(true);
            mannequin.setDescription(null);
        });
    }

    public String skin() {
        return skin;
    }

    public void skin(String skin) {
        this.skin = skin;
    }
}
