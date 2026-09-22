package net.potatocloud.webinterface.service;

import net.potatocloud.webinterface.model.ApiJoinStats;
import net.potatocloud.webinterface.model.ApiServiceStats;
import net.potatocloud.webinterface.model.ApiStatsSummary;

public interface StatsService {

    ApiStatsSummary statsSummary();

    ApiJoinStats joinStats();

    ApiServiceStats  serviceStats();

}
