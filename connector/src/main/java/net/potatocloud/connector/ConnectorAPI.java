package net.potatocloud.connector;

import lombok.Getter;
import net.potatocloud.api.CloudAPI;
import net.potatocloud.api.group.ServiceGroupManager;
import net.potatocloud.api.platform.PlatformManager;
import net.potatocloud.api.player.CloudPlayerManager;
import net.potatocloud.api.property.PropertyHolder;
import net.potatocloud.api.service.ServiceManager;
import net.potatocloud.connector.group.ServiceGroupManagerImpl;
import net.potatocloud.connector.platform.PlatformManagerImpl;
import net.potatocloud.connector.player.CloudPlayerManagerImpl;
import net.potatocloud.connector.properties.ConnectorPropertiesHolder;
import net.potatocloud.connector.service.ServiceManagerImpl;
import net.potatocloud.core.event.ClientEventManager;
import net.potatocloud.core.networking.NetworkClient;
import net.potatocloud.core.networking.PacketManager;
import net.potatocloud.core.networking.netty.NettyNetworkClient;

/**
 * The Connector connects a node to this instance and provides API methods for running services.
 */
@Getter
public class ConnectorAPI extends CloudAPI {

    private static final String NODE_HOST = "127.0.0.1";
    private static final int NODE_PORT = Integer.parseInt(System.getProperty("potatocloud.node.port"));

    private final PacketManager packetManager;
    private final NetworkClient client;
    private final ClientEventManager eventManager;
    private final ConnectorPropertiesHolder propertiesHolder;
    private final ServiceGroupManager groupManager;
    private final ServiceManager serviceManager;
    private final PlatformManager platformManager;
    private final CloudPlayerManager playerManager;

    public ConnectorAPI() {
        packetManager = new PacketManager();

        client = new NettyNetworkClient(packetManager);
        client.connect(NODE_HOST, NODE_PORT);

        eventManager = new ClientEventManager(client);
        propertiesHolder = new ConnectorPropertiesHolder(client);
        groupManager = new ServiceGroupManagerImpl(client);
        platformManager = new PlatformManagerImpl(client);
        serviceManager = new ServiceManagerImpl(client);
        playerManager = new CloudPlayerManagerImpl(client);
    }

    public static ConnectorAPI getInstance() {
        return (ConnectorAPI) CloudAPI.getInstance();
    }

    @Override
    public ServiceGroupManager getServiceGroupManager() {
        return groupManager;
    }

    @Override
    public PropertyHolder getGlobalProperties() {
        return propertiesHolder;
    }

    public void shutdown() {
        client.disconnect();
    }
}
