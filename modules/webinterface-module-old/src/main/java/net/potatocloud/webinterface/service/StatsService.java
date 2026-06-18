package net.potatocloud.webinterface.service;

import lombok.RequiredArgsConstructor;
import net.potatocloud.api.CloudAPI;
import net.potatocloud.api.event.EventBus;
import net.potatocloud.api.event.events.player.CloudPlayerJoinEvent;
import net.potatocloud.api.service.Service;
import net.potatocloud.api.service.ServiceState;
import net.potatocloud.node.Node;
import net.potatocloud.webinterface.dto.player.PlayerCountDto;
import net.potatocloud.webinterface.dto.stats.JoinPointDto;
import net.potatocloud.webinterface.dto.stats.JoinStatsDto;
import net.potatocloud.webinterface.dto.stats.ServiceStatsDto;
import net.potatocloud.webinterface.dto.stats.StatsDto;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CopyOnWriteArrayList;

@RequiredArgsConstructor
public class StatsService {

    private static final long TWENTY_FOUR_HOURS_MS = 24 * 60 * 60 * 1000L;
    private static final DateTimeFormatter HOUR_FORMATTER = DateTimeFormatter.ofPattern("HH:00").withZone(ZoneOffset.UTC);

    private final CloudAPI cloudAPI;
    private final CopyOnWriteArrayList<Long> joinTimestamps = new CopyOnWriteArrayList<>();

    public void start() {
        EventBus eventManager = cloudAPI.eventBus();
        eventManager.subscribe(CloudPlayerJoinEvent.class, event -> joinTimestamps.add(System.currentTimeMillis()));
    }

    public StatsDto getStats() {
        return StatsDto.builder()
                .uptime(Node.getInstance().startupTime())
                .groups(cloudAPI.groupManager().groups().size())
                .services(cloudAPI.serviceManager().services().size())
                .onlinePlayers(cloudAPI.playerManager().players().size())
                .build();
    }

    public JoinStatsDto getJoinStats() {
        return JoinStatsDto.builder()
                .total(totalJoins())
                .data(getLastTwentyFourHours())
                .build();
    }

    public PlayerCountDto getPlayerCount() {
        return PlayerCountDto.builder()
                .online(cloudAPI.playerManager().players().size())
                .build();
    }

    private int totalJoins() {
        long cutoff = System.currentTimeMillis() - TWENTY_FOUR_HOURS_MS;
        return (int) joinTimestamps.stream().filter(ts -> ts >= cutoff).count();
    }

    private List<JoinPointDto> getLastTwentyFourHours() {
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

        List<JoinPointDto> result = new ArrayList<>();
        counts.forEach((hour, joins) -> result.add(JoinPointDto.builder().hour(hour).joins(joins).build()));
        return result;
    }

    public ServiceStatsDto getServiceStats() {
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

        return ServiceStatsDto.builder()
                .running(running)
                .starting(starting)
                .stopping(stopping)
                .currentMemoryUsage(currentMemoryUsage)
                .build();
    }

    public int getServices(ServiceState status) {
        return cloudAPI.serviceManager().services().stream()
                .filter(service -> service.state() == status)
                .toList().size();
    }
}

