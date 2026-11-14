package net.potatocloud.api.event.events.cluster;

import net.potatocloud.api.event.Event;

/**
 * Event sent after a node joined the cluster.
 *
 * @param nodeName the node name
 */
public record ClusterNodeJoinedEvent(String nodeName) implements Event {
}
