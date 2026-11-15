package net.potatocloud.plugins.optional.sign.listener;

import net.potatocloud.api.CloudAPI;
import net.potatocloud.api.player.CloudPlayer;
import net.potatocloud.api.service.Service;
import net.potatocloud.plugins.optional.sign.sign.CloudSign;
import net.potatocloud.plugins.optional.sign.sign.SignService;
import net.potatocloud.plugins.shared.MessagesConfig;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

public final class SignListener implements Listener {

    private final SignService signService;
    private final MessagesConfig messages;
    private final String permission;

    public SignListener(SignService signService, MessagesConfig messages, String permission) {
        this.signService = signService;
        this.messages = messages;
        this.permission = permission;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getHand() != EquipmentSlot.HAND || event.getClickedBlock() == null) {
            return;
        }

        signService.find(event.getClickedBlock()).ifPresent(sign -> {
            event.setCancelled(true);
            connect(event.getPlayer(), sign);
        });
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        if (signService.find(event.getBlock()).isEmpty()) {
            return;
        }

        event.setCancelled(true);
        if (event.getPlayer().hasPermission(permission)) {
            event.getPlayer().sendMessage(messages.get("sign-protected"));
        }
    }

    private void connect(Player player, CloudSign sign) {
        final Service service = signService.service(sign).orElse(null);
        if (service == null) {
            player.sendMessage(messages.get("no-service", "%group%", sign.group()));
            return;
        }

        final CloudPlayer cloudPlayer = CloudAPI.instance().playerManager().find(player.getUniqueId()).orElse(null);
        if (cloudPlayer == null) {
            return;
        }

        if (cloudPlayer.service().filter(service::equals).isPresent()) {
            player.sendMessage(messages.get("already-connected", "%service%", service.name()));
            return;
        }

        if (!service.running() || service.full()) {
            player.sendMessage(messages.get("service-not-available", "%service%", service.name()));
            return;
        }

        player.sendMessage(messages.get("connecting", "%service%", service.name()));
        CloudAPI.instance().playerManager().connectTo(cloudPlayer, service);
    }
}
