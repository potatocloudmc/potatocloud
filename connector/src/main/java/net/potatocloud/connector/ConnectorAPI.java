package net.potatocloud.connector;

import net.potatocloud.api.CloudAPI;
import net.potatocloud.api.cluster.ClusterManager;
import net.potatocloud.api.event.EventBus;
import net.potatocloud.api.group.GroupManager;
import net.potatocloud.api.logging.Logger;
import net.potatocloud.api.platform.PlatformManager;
import net.potatocloud.api.player.CloudPlayerManager;
import net.potatocloud.api.property.PropertyHolder;
import net.potatocloud.api.service.ServiceManager;
import net.potatocloud.connector.cluster.ClusterManagerImpl;
import net.potatocloud.connector.group.GroupManagerImpl;
import net.potatocloud.connector.logging.ConnectorLogger;
import net.potatocloud.connector.platform.PlatformManagerImpl;
import net.potatocloud.connector.player.CloudPlayerManagerImpl;
import net.potatocloud.connector.properties.ConnectorPropertiesHolder;
import net.potatocloud.connector.service.ServiceManagerImpl;
import net.potatocloud.eventbus.ClientEventBus;
import net.potatocloud.network.NetworkClient;
import net.potatocloud.network.netty.NettyNetworkClient;
import net.potatocloud.network.security.SecurityConfig;

/**
 * The Connector connects a node to this instance and provides API methods for running services.
 */
public class ConnectorAPI extends CloudAPI {

    private static final String NODE_HOST = System.getProperty("potatocloud.node.host", "127.0.0.1");
    private static final int NODE_PORT = Integer.parseInt(System.getProperty("potatocloud.node.port"));

    private final NetworkClient client;
    private ConnectorLogger logger;
    private ClusterManager clusterManager;
    private ClientEventBus eventBus;
    private ConnectorPropertiesHolder propertiesHolder;
    private GroupManager groupManager;
    private ServiceManager serviceManager;
    private PlatformManager platformManager;
    private CloudPlayerManager playerManager;

    public ConnectorAPI() {
        this.client = new NettyNetworkClient(SecurityConfig.fromProperties());

        client.onConnected(() -> {
            this.logger = new ConnectorLogger(client);
            this.clusterManager = new ClusterManagerImpl(client);
            this.eventBus = new ClientEventBus(client);
            this.propertiesHolder = new ConnectorPropertiesHolder(client);
            this.platformManager = new PlatformManagerImpl(client);
            this.groupManager = new GroupManagerImpl(client);
            this.serviceManager = new ServiceManagerImpl(client);
            this.playerManager = new CloudPlayerManagerImpl(client);
        });

        client.connect(NODE_HOST, NODE_PORT);
    }

    @Override
    public Logger logger() {
        return logger;
    }

    public static ConnectorAPI getInstance() {
        return (ConnectorAPI) CloudAPI.instance();
    }

    @Override
    public GroupManager groupManager() {
        return groupManager;
    }

    @Override
    public ServiceManager serviceManager() {
        return serviceManager;
    }

    @Override
    public PlatformManager platformManager() {
        return platformManager;
    }

    @Override
    public EventBus eventBus() {
        return eventBus;
    }

    @Override
    public CloudPlayerManager playerManager() {
        return playerManager;
    }

    @Override
    public PropertyHolder globalProperties() {
        return propertiesHolder;
    }

    @Override
    public ClusterManager clusterManager() {
        return clusterManager;
    }

    public NetworkClient client() {
        return client;
    }

    public void shutdown() {
        client.close();
    }
}
