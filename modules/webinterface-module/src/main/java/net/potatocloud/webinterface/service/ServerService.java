package net.potatocloud.webinterface.service;

import net.potatocloud.webinterface.model.ApiService;

import java.util.List;

public interface ServerService {

    List<ApiService> findAll();

    ApiService findByName(String name);

    boolean exists(String name);

    boolean shutdown(String name);

    void execute(String serviceName, String command);

}
