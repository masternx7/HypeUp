package dev.mastern.plugins.hypeup.gui.components;

import com.destroystokyo.paper.profile.PlayerProfile;
import dev.mastern.plugins.hypeup.utils.ColorUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.List;
import java.util.Map;

public class ItemBuilder {
    
    public static ItemStack createFromConfig(ConfigurationSection config, Map<String, String> placeholders) {
        Material material = Material.valueOf(config.getString("material", "STONE"));
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        if (config.contains("custom-model-data")) {
            meta.setCustomModelData(config.getInt("custom-model-data"));
        }
        
        String name = replacePlaceholders(config.getString("name", ""), placeholders);
        meta.displayName(ColorUtils.colorize(name).decoration(TextDecoration.ITALIC, false));
        
        if (config.contains("lore")) {
            List<String> lore = config.getStringList("lore");
            meta.lore(lore.stream()
                .map(line -> replacePlaceholders(line, placeholders))
                .map(ColorUtils::colorize)
                .map(component -> component.decoration(TextDecoration.ITALIC, false))
                .toList());
        }
        
        item.setItemMeta(meta);
        return item;
    }
    
    public static ItemStack createSkull(ConfigurationSection config, OfflinePlayer skullOwner, Map<String, String> placeholders) {
        Material material = Material.valueOf(config.getString("material", "STONE"));
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        if (config.contains("custom-model-data")) {
            meta.setCustomModelData(config.getInt("custom-model-data"));
        }
        
        if (material == Material.PLAYER_HEAD && meta instanceof SkullMeta skullMeta && skullOwner != null) {
            String playerName = placeholders != null ? placeholders.get("target") : null;
            if (playerName != null && !playerName.equals("Unknown")) {
                PlayerProfile profile = Bukkit.createProfile(skullOwner.getUniqueId(), playerName);
                profile.complete(false);
                skullMeta.setPlayerProfile(profile);
            } else {
                skullMeta.setOwningPlayer(skullOwner);
            }
        }
        
        String name = replacePlaceholders(config.getString("name", ""), placeholders);
        meta.displayName(ColorUtils.colorize(name).decoration(TextDecoration.ITALIC, false));
        
        if (config.contains("lore")) {
            List<String> lore = config.getStringList("lore");
            meta.lore(lore.stream()
                .map(line -> replacePlaceholders(line, placeholders))
                .map(ColorUtils::colorize)
                .map(component -> component.decoration(TextDecoration.ITALIC, false))
                .toList());
        }
        
        item.setItemMeta(meta);
        return item;
    }
    
    public static ItemStack createBorder(Material material, Integer customModelData) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(" "));
        
        if (customModelData != null) {
            meta.setCustomModelData(customModelData);
        }
        
        item.setItemMeta(meta);
        return item;
    }
    
    private static String replacePlaceholders(String text, Map<String, String> placeholders) {
        if (text == null || placeholders == null) return text;
        
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            text = text.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return text;
    }
}
