package net.potatocloud.plugin.api.impl.service;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.potatocloud.api.group.ServiceGroup;
import net.potatocloud.api.property.Property;
import net.potatocloud.api.service.Service;
import net.potatocloud.api.service.ServiceStatus;
import net.potatocloud.core.networking.NetworkClient;
import net.potatocloud.core.networking.packets.service.ServiceCopyPacket;
import net.potatocloud.core.networking.packets.service.ServiceExecuteCommandPacket;
import net.potatocloud.core.networking.packets.service.StopServicePacket;
import net.potatocloud.plugin.api.impl.PluginCloudAPI;

import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
public class ServiceImpl implements Service {

    private final String name;
    private final int serviceId;
    private final int port;
    private final long startTimestamp;
    private final ServiceGroup group;
    private final Set<Property> properties;
    private ServiceStatus status;
    private int maxPlayers;

    private final NetworkClient client = PluginCloudAPI.getInstance().getClient();

    @Override
    public int getUsedMemory() {
        return 0; //todo request from node;
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
