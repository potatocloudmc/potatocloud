package net.potatocloud.api.event.events.cluster;

import net.potatocloud.api.event.Event;

/**
 * Event sent after a node left the cluster.
 *
 * @param nodeName the node name
 */
public record ClusterNodeLeftEvent(String nodeName) implements Event {
}
