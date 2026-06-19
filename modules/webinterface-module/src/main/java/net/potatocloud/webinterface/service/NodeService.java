package net.potatocloud.webinterface.service;

import net.potatocloud.webinterface.dto.response.ScreenLogsResponse;

import java.util.List;

public interface NodeService {

    List<String> findScreens();

    ScreenLogsResponse screenLogs(String name, Integer tail);
}
