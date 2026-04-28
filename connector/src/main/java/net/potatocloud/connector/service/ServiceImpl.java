package net.potatocloud.connector.service;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.potatocloud.api.group.ServiceGroup;
import net.potatocloud.api.property.Property;
import net.potatocloud.api.service.Service;
import net.potatocloud.api.service.ServiceStatus;
import net.potatocloud.connector.ConnectorAPI;
import net.potatocloud.core.networking.NetworkClient;
import net.potatocloud.core.networking.packet.packets.service.ServiceCopyPacket;
import net.potatocloud.core.networking.packet.packets.service.ServiceExecuteCommandPacket;
import net.potatocloud.core.networking.packet.packets.service.StopServicePacket;

import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class ServiceImpl implements Service {

    private final String name;
    private final int serviceId;
    private final UUID serviceUuid;
    private final int port;
    private final long startTimestamp;
    private final ServiceGroup group;
    private final Map<String, Property<?>> propertyMap;
    private ServiceStatus status;
    private int maxPlayers;
    private int usedMemory;

    private final NetworkClient client = ConnectorAPI.getInstance().getClient();

    @Override
    public UUID getServiceUuid() {
        return serviceUuid;
    }

    @Override
    public ServiceGroup getServiceGroup() {
        return group;
    }

    @Override
    public void shutdown() {
        client.send(new StopServicePacket(name));
    }

    @Override
    public boolean executeCommand(String command) {
        client.send(new ServiceExecuteCommandPacket(name, command));
        return false;
    }

    @Override
    public void copy(String template, String filter) {
        client.send(new ServiceCopyPacket(getName(), template, filter));
    }

    @Override
    public String getPropertyHolderName() {
        return getName();
    }
}
