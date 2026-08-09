package net.potatocloud.api.cluster;

import java.util.List;
import java.util.Optional;

/**
 * Represents the cluster manager.
 */
public interface ClusterManager {

    /**
     * Gets the local cluster node this instance is running on.
     *
     * @return the local cluster node
     */
    ClusterNode localNode();

    /**
     * Gets all nodes in the cluster.
     *
     * @return a list of all cluster nodes
     */
    List<ClusterNode> nodes();

    /**
     * Gets a cluster node by its name.
     *
     * @param name the name of the node
     * @return the node, or an empty optional if not found
     */
    Optional<ClusterNode> find(String name);

}
