package net.potatocloud.plugins.optional.npc.selector;

import net.potatocloud.common.config.Config;
import net.potatocloud.plugins.optional.npc.util.TextUtils;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.function.UnaryOperator;

public record SelectorItem(Material material, String displayName, List<String> lore) {

    public static SelectorItem load(Config config, String path) {
        return new SelectorItem(
                Material.valueOf(config.get(path + ".material").asString()),
                config.get(path + ".display-name").asString(),
                config.get(path + ".lore").asStringList());
    }

    public ItemStack build(UnaryOperator<String> placeholders) {
        final ItemStack item = new ItemStack(material);
        final ItemMeta meta = item.getItemMeta();

        meta.displayName(TextUtils.itemComponent(placeholders.apply(displayName)));
        meta.lore(lore.stream().map(line -> TextUtils.itemComponent(placeholders.apply(line))).toList());
        meta.addItemFlags(ItemFlag.values());

        item.setItemMeta(meta);
        return item;
    }
}
