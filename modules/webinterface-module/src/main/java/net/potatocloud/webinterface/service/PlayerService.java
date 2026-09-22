package net.potatocloud.webinterface.service;

import net.potatocloud.webinterface.model.ApiPlayer;

import java.util.List;

public interface PlayerService {

    List<ApiPlayer> findAll();

}
