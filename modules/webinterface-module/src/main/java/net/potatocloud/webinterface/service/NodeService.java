package net.potatocloud.webinterface.service;

import net.potatocloud.webinterface.dto.response.ScreenLogsResponse;
import net.potatocloud.webinterface.model.ApiClusterNode;

import java.util.List;

public interface NodeService {

    List<ApiClusterNode> clusterNodes();

    List<String> findScreens();

    ScreenLogsResponse screenLogs(String name, Integer tail);
}
