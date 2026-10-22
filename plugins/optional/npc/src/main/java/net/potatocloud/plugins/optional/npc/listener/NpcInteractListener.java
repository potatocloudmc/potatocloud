package net.potatocloud.plugins.optional.npc.listener;

import io.papermc.paper.event.player.PrePlayerAttackEntityEvent;
import net.potatocloud.plugins.optional.npc.action.ActionExecutor;
import net.potatocloud.plugins.optional.npc.entity.NpcEntityService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;

public final class NpcInteractListener implements Listener {

    private final NpcEntityService entityService;
    private final ActionExecutor actionExecutor;

    public NpcInteractListener(NpcEntityService entityService, ActionExecutor actionExecutor) {
        this.entityService = entityService;
        this.actionExecutor = actionExecutor;
    }

    @EventHandler
    public void onRightClick(PlayerInteractEntityEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        entityService.find(event.getRightClicked()).ifPresent(npc -> {
            event.setCancelled(true);
            actionExecutor.execute(event.getPlayer(), npc, npc.rightClick());
        });
    }

    @EventHandler
    public void onLeftClick(PrePlayerAttackEntityEvent event) {
        entityService.find(event.getAttacked()).ifPresent(npc -> {
            event.setCancelled(true);
            actionExecutor.execute(event.getPlayer(), npc, npc.leftClick());
        });
    }
}
