package net.potatocloud.connector;

import lombok.Getter;
import net.potatocloud.api.CloudAPI;
import net.potatocloud.api.group.ServiceGroupManager;
import net.potatocloud.api.platform.PlatformManager;
import net.potatocloud.api.player.CloudPlayerManager;
import net.potatocloud.api.property.PropertyHolder;
import net.potatocloud.api.service.ServiceManager;
import net.potatocloud.connector.group.ServiceGroupManagerImpl;
import net.potatocloud.connector.logging.ConnectorLogger;
import net.potatocloud.connector.platform.PlatformManagerImpl;
import net.potatocloud.connector.player.CloudPlayerManagerImpl;
import net.potatocloud.connector.properties.ConnectorPropertiesHolder;
import net.potatocloud.connector.service.ServiceManagerImpl;
import net.potatocloud.core.event.ClientEventManager;
import net.potatocloud.core.networking.NetworkClient;
import net.potatocloud.core.networking.netty.client.NettyNetworkClient;
import net.potatocloud.core.networking.packet.PacketManager;

/**
 * The Connector connects a node to this instance and provides API methods for running services.
 */
@Getter
public class ConnectorAPI extends CloudAPI {

    private static final String NODE_HOST = "127.0.0.1";
    private static final int NODE_PORT = Integer.parseInt(System.getProperty("potatocloud.node.port"));

    private final PacketManager packetManager;
    private final NetworkClient client;
    private ConnectorLogger logger;
    private ClientEventManager eventManager;
    private ConnectorPropertiesHolder propertiesHolder;
    private ServiceGroupManager groupManager;
    private ServiceManager serviceManager;
    private PlatformManager platformManager;
    private CloudPlayerManager playerManager;

    public ConnectorAPI() {
        packetManager = new PacketManager();

        client = new NettyNetworkClient(packetManager);

        client.addConnectionListener(() -> {
            logger = new ConnectorLogger(client);
            eventManager = new ClientEventManager(client);
            propertiesHolder = new ConnectorPropertiesHolder(client);
            platformManager = new PlatformManagerImpl(client);
            groupManager = new ServiceGroupManagerImpl(client);
            serviceManager = new ServiceManagerImpl(client);
            playerManager = new CloudPlayerManagerImpl(client);
        });

        client.connect(NODE_HOST, NODE_PORT);
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
        client.close();
    }
}
