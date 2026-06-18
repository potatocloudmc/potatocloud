package net.potatocloud.webinterface.service;

import lombok.RequiredArgsConstructor;
import net.potatocloud.api.CloudAPI;
import net.potatocloud.api.event.events.player.CloudPlayerJoinEvent;
import net.potatocloud.node.Node;
import net.potatocloud.webinterface.dto.player.PlayerDto;

import java.util.List;

@RequiredArgsConstructor
public class PlayerService {

    private final CloudAPI cloudAPI;
    private final Node node;

    public List<PlayerDto> getOnlinePlayers() {
        return cloudAPI.playerManager().players().stream()
                .map(player -> PlayerDto.from(player, node.ready()))
                .toList();
    }

    private void registerListeners() {
        cloudAPI.eventBus().subscribe(CloudPlayerJoinEvent.class, event -> {

        });
    }
}

