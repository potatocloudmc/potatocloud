package net.potatocloud.node.service.start;

import net.potatocloud.api.cluster.ClusterNode;
import net.potatocloud.api.event.EventBus;
import net.potatocloud.api.event.events.property.PropertyChangedEvent;
import net.potatocloud.api.group.Group;
import net.potatocloud.api.group.GroupManager;
import net.potatocloud.api.property.DefaultProperties;
import net.potatocloud.node.config.NodeConfig;
import net.potatocloud.node.service.NodeServiceManager;
import net.potatocloud.node.service.start.condition.MinOnlineCondition;
import net.potatocloud.node.service.start.condition.PlayerUsageCondition;
import net.potatocloud.node.service.start.condition.ServiceStartCondition;
import net.potatocloud.node.service.start.rule.GroupMaxOnlineRule;
import net.potatocloud.node.service.start.rule.MaxServicesRule;
import net.potatocloud.node.service.start.rule.MaxStartingRule;
import net.potatocloud.node.service.start.rule.ServiceStartRule;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public final class ServiceStartScheduler {

    private final NodeConfig config;

    private final GroupManager groupManager;
    private final NodeServiceManager serviceManager;

    private final List<ServiceStartRule> rules;
    private final List<ServiceStartCondition> conditions;

    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(Thread.ofVirtual().factory());

    public ServiceStartScheduler(
            NodeConfig config,
            GroupManager groupManager,
            NodeServiceManager serviceManager,
            EventBus eventBus
    ) {
        this.config = config;
        this.groupManager = groupManager;
        this.serviceManager = serviceManager;

        this.rules = List.of(
                new GroupMaxOnlineRule(),
                new MaxServicesRule(config, serviceManager),
                new MaxStartingRule(config, serviceManager)
        );

        this.conditions = List.of(
                new MinOnlineCondition(),
                new PlayerUsageCondition()
        );

        eventBus.subscribe(PropertyChangedEvent.class, event -> {
            if (!event.propertyName().equals(DefaultProperties.GAME_STATE.name())) {
                return;
            }

            if (event.newValue() == null || !event.newValue().equals("INGAME")) {
                return;
            }

            serviceManager.find(event.holderName()).ifPresent(service -> {
                final Group group = service.group();
                final int onlineServices = group.services().size();

                if (onlineServices >= group.maxServices()) {
                    return;
                }

                serviceManager.start(service.group());
            });
        });
    }

    public void start() {
        executor.scheduleAtFixedRate(this::run, 0, 1, TimeUnit.SECONDS);
    }

    private void run() {
        groupManager.groups().stream()
                .filter(group -> groupManager.exists(group.name()))
                .filter(this::isLocalNode)
                .sorted(Comparator.<Group>comparingInt(Group::startPriority).reversed())
                .forEach(group -> {
                    if (rules.stream().allMatch(rule -> rule.allows(group)) && conditions.stream().anyMatch(condition -> condition.shouldStart(group))) {
                        serviceManager.start(group);
                    }
                });
    }

    private boolean isLocalNode(Group group) {
        if (!config.cluster().enabled()) {
            return true;
        }
        final String nodeName = group.node().map(ClusterNode::name).orElse(null);
        return nodeName == null || nodeName.equals(config.cluster().name());
    }

    public void close() {
        executor.shutdownNow();
    }
}
