package net.potatocloud.api.cluster;

import java.time.Instant;

/**
 * Represents one node in the cluster.
 */
public interface ClusterNode {

    /**
     * Gets the name of the node.
     *
     * @return the name of the node
     */
    String name();

    /**
     * Gets the host address of the node.
     *
     * @return the host address of the node
     */
    String host();

    /**
     * Gets the port of the node.
     *
     * @return the port of the node
     */
    int port();

    /**
     * Gets the timestamp of the node start.
     *
     * @return the timestamp of the node start
     */
    Instant startedAt();

}
