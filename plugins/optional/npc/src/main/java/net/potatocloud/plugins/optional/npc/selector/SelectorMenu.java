package net.potatocloud.plugins.optional.npc.selector;

import net.potatocloud.api.group.Group;
import net.potatocloud.api.service.Service;
import net.potatocloud.common.config.Config;
import net.potatocloud.plugins.optional.npc.connect.ServiceConnector;
import net.potatocloud.plugins.optional.npc.util.Placeholders;
import net.potatocloud.plugins.optional.npc.util.TextUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.UnaryOperator;

public final class SelectorMenu {

    private final Map<UUID, OpenSelector> openSelectors = new HashMap<>();
    private final Config config;
    private final ServiceConnector connector;

    public SelectorMenu(Config config, ServiceConnector connector) {
        this.config = config;
        this.connector = connector;
    }

    public void open(Player player, Group group) {
        final int rows = Math.clamp(config.get("selector.rows").asInt(), 1, 6);
        final String title = config.get("selector.title").asString().replace("%group%", group.name());
        final Inventory inventory = Bukkit.createInventory(null, rows * 9, TextUtils.component(title));

        final OpenSelector selector = new OpenSelector(group.name(), 0, inventory);
        fill(selector);
        player.openInventory(inventory);
        openSelectors.put(player.getUniqueId(), selector);
    }

    public void close(Player player) {
        openSelectors.remove(player.getUniqueId());
    }

    public boolean isOpen(Player player, Inventory inventory) {
        final OpenSelector selector = openSelectors.get(player.getUniqueId());
        return selector != null && selector.inventory() == inventory;
    }

    public void refresh() {
        openSelectors.values().forEach(this::fill);
    }

    public void click(Player player, int clickedSlot) {
        final OpenSelector selector = openSelectors.get(player.getUniqueId());
        final List<Service> services = services(selector.group());
        final List<Integer> serviceSlots = serviceSlots();
        final int slot = clickedSlot + 1;

        if (slot == pageSlot("previous-page") && selector.page() > 0) {
            changePage(player, selector, selector.page() - 1);
            return;
        }

        if (slot == pageSlot("next-page") && hasNextPage(selector.page(), services.size(), serviceSlots.size())) {
            changePage(player, selector, selector.page() + 1);
            return;
        }

        final int slotIndex = serviceSlots.indexOf(slot);
        if (slotIndex == -1) {
            return;
        }

        final int serviceIndex = selector.page() * serviceSlots.size() + slotIndex;
        if (serviceIndex >= services.size()) {
            return;
        }

        player.closeInventory();
        connector.connect(player, services.get(serviceIndex));
    }

    private void changePage(Player player, OpenSelector selector, int page) {
        final OpenSelector changed = new OpenSelector(selector.group(), page, selector.inventory());
        openSelectors.put(player.getUniqueId(), changed);
        fill(changed);
    }

    private void fill(OpenSelector selector) {
        final Inventory inventory = selector.inventory();
        final List<Service> services = services(selector.group());
        final List<Integer> serviceSlots = serviceSlots();
        final int page = selector.page();
        final int pages = Math.max(1, (int) Math.ceil((double) services.size() / serviceSlots.size()));

        inventory.clear();

        for (String slot : config.get("selector.fixed-items").keys()) {
            setItem(inventory, Integer.parseInt(slot), SelectorItem.load(config, "selector.fixed-items." + slot), text -> text);
        }

        for (int i = 0; i < serviceSlots.size(); i++) {
            final int serviceIndex = page * serviceSlots.size() + i;
            if (serviceIndex >= services.size()) {
                break;
            }

            final Service service = services.get(serviceIndex);
            setItem(inventory, serviceSlots.get(i), serviceItem(service), text -> Placeholders.service(text, service));
        }

        if (page > 0) {
            setPageItem(inventory, "previous-page", page, pages);
        }

        if (hasNextPage(page, services.size(), serviceSlots.size())) {
            setPageItem(inventory, "next-page", page, pages);
        }
    }

    private void setPageItem(Inventory inventory, String name, int page, int pages) {
        setItem(inventory, pageSlot(name), SelectorItem.load(config, "selector." + name), text -> text
                .replace("%page%", String.valueOf(page + 1))
                .replace("%pages%", String.valueOf(pages)));
    }

    private void setItem(Inventory inventory, int slot, SelectorItem item, UnaryOperator<String> placeholders) {
        if (slot >= 1 && slot <= inventory.getSize()) {
            inventory.setItem(slot - 1, item.build(placeholders));
        }
    }

    private SelectorItem serviceItem(Service service) {
        if (service.full()) {
            return SelectorItem.load(config, "selector.full-item");
        }

        if (service.playerCount() == 0) {
            return SelectorItem.load(config, "selector.empty-item");
        }

        return SelectorItem.load(config, "selector.online-item");
    }

    private boolean hasNextPage(int page, int serviceCount, int slotsPerPage) {
        return (page + 1) * slotsPerPage < serviceCount;
    }

    private int pageSlot(String name) {
        return config.get("selector." + name + ".slot").asInt();
    }

    private List<Integer> serviceSlots() {
        return config.get("selector.service-slots").asStringList().stream().map(Integer::parseInt).toList();
    }

    private List<Service> services(String group) {
        return Placeholders.runningServices(group).stream()
                .sorted(Comparator.comparingInt(Service::id))
                .toList();
    }

    private record OpenSelector(String group, int page, Inventory inventory) {
    }
}
