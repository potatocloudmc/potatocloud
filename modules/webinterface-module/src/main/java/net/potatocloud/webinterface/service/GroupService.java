package net.potatocloud.webinterface.service;

import net.potatocloud.webinterface.dto.request.GroupCreateRequest;
import net.potatocloud.webinterface.dto.request.GroupUpdateRequest;
import net.potatocloud.webinterface.model.ApiGroup;

import java.util.List;

public interface GroupService {

    List<ApiGroup> findAll();

    boolean create(GroupCreateRequest request);

    boolean update(String groupName, GroupUpdateRequest request);

    boolean exists(String name);

    ApiGroup findByName(String name);

    boolean start(String name);

    boolean canStartService(String name);

    boolean shutdown(String name);

}
