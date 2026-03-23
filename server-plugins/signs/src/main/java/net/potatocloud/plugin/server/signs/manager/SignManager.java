package net.potatocloud.plugin.server.signs.manager;

import net.potatocloud.plugin.server.shared.Config;
import net.potatocloud.plugin.server.signs.sign.LobbySign;
import net.potatocloud.plugin.server.signs.sign.SignLocation;
import org.bukkit.block.Block;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class SignManager {

    private final Config signConfig;
    private final List<LobbySign> lobbySignList;

    public SignManager(Config signConfig) {
        this.signConfig = signConfig;
        this.lobbySignList = new ArrayList<>();
    }

    public void loadFromConfig() {
        // TODO: load from Config
    }

    public boolean register(LobbySign lobbySign) {
        // check if there are already a sign
        final LobbySign searchLobbySign = this.getFromBlock(lobbySign.signLocation().block());
        if (searchLobbySign != null) {
            return false;
        }

        this.lobbySignList.add(lobbySign);
        // TODO: add to config!
        return true;
    }

    @Nullable
    public LobbySign getFromBlock(Block block) {
        final SignLocation signLocationFromBlock = new SignLocation(block.getLocation());
        return this.lobbySignList.stream()
                .filter(filterLobbySign -> filterLobbySign.signLocation().toString()
                        .equalsIgnoreCase(signLocationFromBlock.toString()))
                .findFirst()
                .orElse(null);
    }

    public boolean remove(Block block) {
        final LobbySign lobbySign = this.getFromBlock(block);
        if (lobbySign == null) {
            return false;
        }

        this.lobbySignList.remove(lobbySign);
        //TODO: remove from Config!!
        return true;
    }


}
