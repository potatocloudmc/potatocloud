package net.potatocloud.node.player.listeners;

import lombok.RequiredArgsConstructor;
import net.potatocloud.api.player.CloudPlayer;
import net.potatocloud.core.networking.NetworkConnection;
import net.potatocloud.core.networking.packet.PacketListener;
import net.potatocloud.core.networking.packet.packets.player.CloudPlayerRemovePacket;
import net.potatocloud.node.Node;
import net.potatocloud.node.player.CloudPlayerManagerImpl;

@RequiredArgsConstructor
public class CloudPlayerRemoveListener implements PacketListener<CloudPlayerRemovePacket> {

    private final CloudPlayerManagerImpl playerManager;

    @Override
    public void onPacket(NetworkConnection connection, CloudPlayerRemovePacket packet) {
        final CloudPlayer player = playerManager.getCloudPlayer(packet.getPlayerUniqueId());
        if (player == null) {
            return;
        }
        playerManager.unregisterPlayer(player);

        final Node node = Node.getInstance();

        node.getServer().getConnectedSessions().stream()
                .filter(networkConnection -> !networkConnection.equals(connection))
                .forEach(networkConnection -> networkConnection.send(packet));

        if (node.getConfig().isDebug() && !node.isStopping()) {
            node.getLogger().info("Player &a" + player.getUsername()
                    + " &7disconnected &7from the network &8[&7UUID&8: &a" + player.getUniqueId() + "&8]");
        }
    }
}
