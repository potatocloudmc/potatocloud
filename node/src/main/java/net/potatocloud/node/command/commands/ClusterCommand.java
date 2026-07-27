package net.potatocloud.node.command.commands;

import net.potatocloud.api.cluster.ClusterNode;
import net.potatocloud.api.logging.Logger;
import net.potatocloud.api.utils.TimeFormatter;
import net.potatocloud.node.Node;
import net.potatocloud.node.cluster.ClusterManagerImpl;
import net.potatocloud.node.command.ArgumentType;
import net.potatocloud.node.command.Command;
import net.potatocloud.node.command.CommandInfo;
import net.potatocloud.node.command.SubCommand;

import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;

@CommandInfo(name = "cluster", description = "Manage cluster nodes", aliases = {"cl"})
public final class ClusterCommand extends Command {

    public ClusterCommand(Logger logger, ClusterManagerImpl clusterManager) {
        defaultExecutor(_ -> sendHelp());

        sub("info", "Show info about the local node")
                .executes(_ -> {
                    final ClusterNode local = clusterManager.localNode();
                    final long uptime = Duration.between(local.startedAt(), Instant.now()).toMillis();

                    logger.info("Local node&8:");
                    logger.info("&8» &7Name&8: &a" + local.name());
                    logger.info("&8» &7Address&8: &a" + local.host() + "&8:&a" + local.port());
                    logger.info("&8» &7Started At&8: &a" + TimeFormatter.formatAsDateAndTime(local.startedAt().toEpochMilli()));
                    logger.info("&8» &7Uptime&8: &a" + TimeFormatter.formatAsDuration(uptime));

                    final long connectedNodes = clusterManager.nodes().stream()
                            .filter(node -> !node.equals(local))
                            .count();

                    logger.info("&8» &7Connected nodes&8: &a" + connectedNodes);
                });

        sub("token", "Show the current cluster token")
                .executes(_ -> {
                    final String token = Node.instance().config().cluster().token();

                    logger.info("Cluster token&8:");
                    logger.info("&8» &a" + token);
                    logger.info("Use this token when adding other nodes to the cluster");
                });

        sub("list", "List all cluster nodes")
                .executes(_ -> {
                    final List<ClusterNode> nodes = clusterManager.nodes().stream()
                            .sorted(Comparator.comparing(ClusterNode::startedAt))
                            .toList();

                    final ClusterNode local = clusterManager.localNode();

                    logger.info("&7All connected cluster nodes:");
                    for (ClusterNode node : nodes) {
                        final boolean isLocal = node.equals(local);

                        logger.info("&8» &a" + node.name()+ " &8(&a" + node.host() + "&8:&a" + node.port() + "&8)" + (isLocal ? " &8[&7Local&8]" : ""));
                    }
                });

        final SubCommand nodeSub = sub("node", "Manage a specific cluster node");
        nodeSub.executes(_ -> nodeSub.sendHelp());

        nodeSub.sub("info", "Show info about a node")
                .argument(ArgumentType.ClusterNode("node"))
                .executes(ctx -> {
                    final ClusterNode node = ctx.get("node");
                    final long uptime = Duration.between(node.startedAt(), Instant.now()).toMillis();

                    logger.info("Node &a" + node.name() + "&8:");
                    logger.info("&8» &7Name&8: &a" + node.name());
                    logger.info("&8» &7Address&8: &a" + node.host() + "&8:&a" + node.port());
                    logger.info("&8» &7Started At&8: &a" + TimeFormatter.formatAsDateAndTime(node.startedAt().toEpochMilli()));
                    logger.info("&8» &7Uptime&8: &a" + TimeFormatter.formatAsDuration(uptime));
                });
    }
}
