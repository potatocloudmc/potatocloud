package net.potatocloud.webinterface.service.impl;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import lombok.RequiredArgsConstructor;
import net.potatocloud.api.CloudAPI;
import net.potatocloud.api.event.EventBus;
import net.potatocloud.api.event.events.player.CloudPlayerJoinEvent;
import net.potatocloud.api.service.Service;
import net.potatocloud.api.service.ServiceState;
import net.potatocloud.node.Node;
import net.potatocloud.webinterface.model.ApiJoinPoint;
import net.potatocloud.webinterface.model.ApiJoinStats;
import net.potatocloud.webinterface.model.ApiServiceStats;
import net.potatocloud.webinterface.model.ApiStatsSummary;
import net.potatocloud.webinterface.service.StatsService;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CopyOnWriteArrayList;

@RequiredArgsConstructor
@ApplicationScoped
public class StatsServiceImpl implements StatsService {

    private static final long TWENTY_FOUR_HOURS_MS = 24 * 60 * 60 * 1000L;
    private static final DateTimeFormatter HOUR_FORMATTER = DateTimeFormatter.ofPattern("HH:00").withZone(ZoneOffset.UTC);
    private final CloudAPI cloudAPI = CloudAPI.instance();
    private final CopyOnWriteArrayList<Long> joinTimestamps = new CopyOnWriteArrayList<>();

    void start(@Observes StartupEvent startupEvent) {
        EventBus eventManager = cloudAPI.eventBus();
        eventManager.subscribe(CloudPlayerJoinEvent.class, _ -> joinTimestamps.add(System.currentTimeMillis()));
    }

    @Override
    public ApiStatsSummary statsSummary() {
        return new ApiStatsSummary()
                .uptime(Node.getInstance().startupTime())
                .groups(cloudAPI.groupManager().groups().size())
                .services(cloudAPI.serviceManager().services().size())
                .playerCount(cloudAPI.playerManager().players().size());

    }

    @Override
    public ApiJoinStats joinStats() {
        return new ApiJoinStats()
                .total(totalJoins())
                .data(getLastTwentyFourHours());
    }

    @Override
    public ApiServiceStats serviceStats() {
        int running = getServices(ServiceState.RUNNING);
        int starting = getServices(ServiceState.STARTING);
        int stopping = getServices(ServiceState.STOPPING);
        int currentMemoryUsage;

        List<Integer> memUsages = cloudAPI.serviceManager().services().stream()
                .filter(service -> service.state() == ServiceState.RUNNING || service.state() == ServiceState.STARTING)
                .map(Service::usedMemory)
                .toList();

        if (memUsages.isEmpty()) {
            currentMemoryUsage = 0;
        } else {
            currentMemoryUsage = memUsages.stream().mapToInt(Integer::intValue).sum();
        }

        return new ApiServiceStats()
                .running(running)
                .starting(starting)
                .stopping(stopping)
                .currentMemoryUsage(currentMemoryUsage);
    }

    public int getServices(ServiceState status) {
        return cloudAPI.serviceManager().services().stream()
                .filter(service -> service.state() == status)
                .toList().size();
    }

    private int totalJoins() {
        long cutoff = System.currentTimeMillis() - TWENTY_FOUR_HOURS_MS;
        return (int) joinTimestamps.stream().filter(ts -> ts >= cutoff).count();
    }

    private List<ApiJoinPoint> getLastTwentyFourHours() {
        long now = System.currentTimeMillis();
        long cutoff = now - TWENTY_FOUR_HOURS_MS;

        joinTimestamps.removeIf(ts -> ts < cutoff);

        Map<String, Integer> counts = new TreeMap<>();
        for (long offset = TWENTY_FOUR_HOURS_MS - 3600_000L; offset >= 0; offset -= 3600_000L) {
            String label = HOUR_FORMATTER.format(Instant.ofEpochMilli(now - offset));
            counts.putIfAbsent(label, 0);
        }

        for (long ts : joinTimestamps) {
            String label = HOUR_FORMATTER.format(Instant.ofEpochMilli(ts));
            counts.merge(label, 1, Integer::sum);
        }

        List<ApiJoinPoint> result = new ArrayList<>();
        counts.forEach((hour, joins) -> result.add(new ApiJoinPoint().hour(hour).joins(joins)));
        return result;
    }

}
