package net.potatocloud.webinterface.service.impl;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import net.potatocloud.api.CloudAPI;
import net.potatocloud.webinterface.mapper.PlayerMapper;
import net.potatocloud.webinterface.model.ApiPlayer;
import net.potatocloud.webinterface.service.PlayerService;

import java.util.List;

@ApplicationScoped
public class PlayerServiceImpl implements PlayerService {

    @Inject
    PlayerMapper playerMapper;

    @Override
    public List<ApiPlayer> findAll() {
        return CloudAPI.instance().playerManager().players()
                .stream()
                .map(playerMapper::toApi)
                .toList();
    }
}
