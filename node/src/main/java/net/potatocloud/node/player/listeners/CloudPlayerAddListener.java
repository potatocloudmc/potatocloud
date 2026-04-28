package net.potatocloud.node.player.listeners;

import lombok.RequiredArgsConstructor;
import net.potatocloud.api.player.CloudPlayer;
import net.potatocloud.api.player.impl.CloudPlayerImpl;
import net.potatocloud.api.property.Property;
import net.potatocloud.common.PropertyUtil;
import net.potatocloud.core.networking.NetworkConnection;
import net.potatocloud.core.networking.NetworkServer;
import net.potatocloud.core.networking.packet.PacketListener;
import net.potatocloud.core.networking.packet.packets.player.CloudPlayerAddPacket;
import net.potatocloud.core.networking.packet.packets.player.CloudPlayerUpdatePacket;
import net.potatocloud.node.Node;
import net.potatocloud.node.config.NodeConfig;
import net.potatocloud.node.player.CloudPlayerManagerImpl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;
import java.util.UUID;

@RequiredArgsConstructor
public class CloudPlayerAddListener implements PacketListener<CloudPlayerAddPacket> {

    private final CloudPlayerManagerImpl playerManager;
    private final NetworkServer server;

    @Override
    public void onPacket(NetworkConnection connection, CloudPlayerAddPacket packet) {
        final CloudPlayer player = new CloudPlayerImpl(packet.getUsername(), "", packet.getUniqueId(), (Locale) packet.getPropertyMap().get("locale").getValue(), packet.getConnectedProxyName());

        playerManager.registerPlayer(player);

        final Node node = Node.getInstance();
        node.getLogger().debug("test");

        node.getLogger().info(packet.getUniqueId().toString());

        String sql = "SELECT * FROM `potato_players` WHERE uuid = '"+packet.getUniqueId()+"'";
        try (Connection conn = Node.getInstance().getMySQLHandler().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    UUID uuid = UUID.fromString(rs.getString("uuid"));
                    String username = rs.getString("username");
                    String nickname = rs.getString("nickname");
                    Node.getInstance().getConsole().println(uuid+" "+username+" "+nickname);

                    if (!nickname.isEmpty()) {
                        packet.getPropertyMap().put("nickname", Property.ofString("nickname", nickname));
                    }

                    if (!player.getUsername().equals(username)) {
                        // TODO update username in db
                        // TODO add log that the user changed his name
                    }

                    // TODO replace locale with setting locale
                    // packet.getPropertyMap().put("locale", Property.ofLocale("locale", player.getLocale()));
                } else {
                    // TODO create user
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        for (Property<?> property : packet.getPropertyMap().values()) {
            PropertyUtil.setPropertyUnchecked(player, property);
        }

        player.getPropertyMap().forEach((s, property) -> node.getLogger().info(s + property.getValue()));

        // TODO maby optimize this an dont send this packet if nothing changed
        connection.send(new CloudPlayerUpdatePacket(packet.getUniqueId(), packet.getConnectedProxyName(), packet.getConnectedServiceName(), packet.getPropertyMap()));

        server.generateBroadcast().exclude(connection).broadcast(packet);

        final NodeConfig config = node.getConfig();
        if (config.isDebug()) {
            node.getLogger().info("Player &a" + player.getUsername() + " &7connected to the network &8[&7UUID&8: &a"
                    + player.getUniqueId() + "&8, &7Proxy&8: &a" + player.getConnectedProxyName() + "&8]");
        }
    }
}
