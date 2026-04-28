package net.potatocloud.node.service.start;

import net.potatocloud.api.event.EventManager;
import net.potatocloud.api.event.events.property.PropertyChangedEvent;
import net.potatocloud.api.group.ServiceGroup;
import net.potatocloud.api.group.ServiceGroupManager;
import net.potatocloud.api.property.DefaultProperties;
import net.potatocloud.api.service.Service;
import net.potatocloud.node.config.NodeConfig;
import net.potatocloud.node.service.ServiceManagerImpl;
import net.potatocloud.node.service.start.condition.ServiceStartCondition;
import net.potatocloud.node.service.start.condition.conditions.MinOnlineCondition;
import net.potatocloud.node.service.start.condition.conditions.PlayerUsageCondition;
import net.potatocloud.node.service.start.rule.ServiceStartRule;
import net.potatocloud.node.service.start.rule.rules.GroupMaxOnlineRule;
import net.potatocloud.node.service.start.rule.rules.MaxMemoryRule;
import net.potatocloud.node.service.start.rule.rules.MaxServicesRule;
import net.potatocloud.node.service.start.rule.rules.MaxStartingRule;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ServiceStartScheduler {

    private final ServiceGroupManager groupManager;
    private final ServiceManagerImpl serviceManager;

    private final List<ServiceStartRule> rules;
    private final List<ServiceStartCondition> conditions;

    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    public ServiceStartScheduler(NodeConfig config, ServiceGroupManager groupManager, ServiceManagerImpl serviceManager, EventManager eventManager) {
        this.groupManager = groupManager;
        this.serviceManager = serviceManager;

        this.rules = List.of(
                new GroupMaxOnlineRule(),
                new MaxServicesRule(),
                new MaxStartingRule(),
                new MaxMemoryRule(serviceManager)
        );

        this.conditions = List.of(
                new MinOnlineCondition(),
                new PlayerUsageCondition()
        );

        // Handle game state changes
        eventManager.on(PropertyChangedEvent.class, event -> {
            if (!event.getProperty().getName().equals(DefaultProperties.GAME_STATE.getName())) {
                return;
            }

            if (event.getNewValue() == null || !event.getNewValue().equals("INGAME")) {
                return;
            }

            final Service service = serviceManager.getService(event.getHolderName());
            if (service == null) {
                return;
            }

            final ServiceGroup group = service.getServiceGroup();
            if (group.getOnlineServiceCount() >= group.getMaxOnlineCount()) {
                return;
            }

            serviceManager.startService(service.getServiceGroup());
        });
    }

    public void start() {
        executor.scheduleAtFixedRate(this::run, 0, 1, TimeUnit.SECONDS);
    }

    private void run() {
        groupManager.getAllServiceGroups().stream()
                .filter(group -> groupManager.existsServiceGroup(group.getName()))
                .sorted(Comparator.comparingInt(ServiceGroup::getStartPriority).reversed())
                .forEach(group -> {
                    if (rules.stream().allMatch(rule -> rule.allows(group)) && conditions.stream().anyMatch(condition -> condition.shouldStart(group))) {
                        serviceManager.startService(group);
                    }
                });
    }

    public void close() {
        executor.shutdownNow();
    }
}
