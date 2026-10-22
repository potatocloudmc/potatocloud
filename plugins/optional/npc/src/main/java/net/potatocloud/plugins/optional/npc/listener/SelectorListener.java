package net.potatocloud.plugins.optional.npc.listener;

import net.potatocloud.plugins.optional.npc.selector.SelectorMenu;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

public final class SelectorListener implements Listener {

    private final SelectorMenu selectorMenu;

    public SelectorListener(SelectorMenu selectorMenu) {
        this.selectorMenu = selectorMenu;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        if (!selectorMenu.isOpen(player, event.getView().getTopInventory())) {
            return;
        }

        event.setCancelled(true);

        if (event.getClickedInventory() == event.getView().getTopInventory()) {
            selectorMenu.click(player, event.getSlot());
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (event.getWhoClicked() instanceof Player player && selectorMenu.isOpen(player, event.getView().getTopInventory())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player player && selectorMenu.isOpen(player, event.getInventory())) {
            selectorMenu.close(player);
        }
    }
}
