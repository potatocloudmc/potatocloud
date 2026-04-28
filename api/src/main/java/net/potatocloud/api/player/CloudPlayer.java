package net.potatocloud.api.player;

import net.potatocloud.api.CloudAPI;
import net.potatocloud.api.property.PropertyHolder;
import net.potatocloud.api.service.Service;

import java.util.Locale;
import java.util.UUID;

public interface CloudPlayer extends PropertyHolder {

    /**
     * Gets the username of the player.
     *
     * @return the username of the player
     */
    String getUsername();

    /**
     * Gets the nickname of the player.
     *
     * @return the nickname of the player
     */
    String getNickname();

    /**
     * Gets the unique id of the player.
     *
     * @return the unique id of the player
     */
    UUID getUniqueId();

    /**
     * Gets the locale of the player.
     *
     * @return the locale of the player
     */
    Locale getLocale();

    /**
     * Gets the session start unix timestamp.
     *
     * @return the unix timestamp of the session start
     */
    Long getSessionStartTime();

    /**
     * Gets the session id of the player.
     *
     * @return random session id that belongs to the player
     */
    UUID getSessionId();

    /**
     * Gets the connected proxy name of the player.
     *
     * @return the connected proxy name of the player
     */
    String getConnectedProxyName();

    /**
     * Gets the connected service name of the player.
     *
     * @return the connected service name of the player
     */
    String getConnectedServiceName();

    /**
     * Gets the connected proxy of the player.
     *
     * @return the connected proxy of the player
     */
    default Service getConnectedProxy() {
        return CloudAPI.getInstance().getServiceManager().getService(getConnectedProxyName());
    }

    /**
     * Gets the connected service of the player.
     *
     * @return the connected service of the player
     */
    default Service getConnectedService() {
        return CloudAPI.getInstance().getServiceManager().getService(getConnectedServiceName());
    }

    /**
     * Connects the player with a service.
     *
     * @param service the service to connect with
     */
    default void connectWithService(Service service) {
        CloudAPI.getInstance().getPlayerManager().connectPlayerWithService(this, service);
    }

    /**
     * Connects the player with a service.
     *
     * @param serviceName the service name to connect with
     */
    default void connectWithService(String serviceName) {
        connectWithService(CloudAPI.getInstance().getServiceManager().getService(serviceName));
    }

    /**
     * Updates the player.
     */
    default void update() {
        CloudAPI.getInstance().getPlayerManager().updatePlayer(this);
    }
}
