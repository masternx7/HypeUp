package dev.mastern.plugins.hypeup.manager.components;

import dev.mastern.plugins.hypeup.HypeUp;

import java.util.HashMap;
import java.util.Map;

public class FireColorHelper {
    
    private final HypeUp plugin;
    
    public FireColorHelper(HypeUp plugin) {
        this.plugin = plugin;
    }
    
    public Map<String, String> getPlaceholders(int streak) {
        Map<String, String> placeholders = new HashMap<>();
        
        if (plugin.getConfig().contains("fire-streak.colors.days")) {
            for (String rangeKey : plugin.getConfig().getConfigurationSection("fire-streak.colors.days").getKeys(false)) {
                String[] range = rangeKey.split("-");
                int min = Integer.parseInt(range[0]);
                int max = Integer.parseInt(range[1]);
                
                if (streak >= min && streak <= max) {
                    String path = "fire-streak.colors.days." + rangeKey;
                    placeholders.put("fire-color", plugin.getConfig().getString(path + ".color", "&#FF0000"));
                    placeholders.put("fire-display", plugin.getConfig().getString(path + ".display", ""));
                    placeholders.put("fire-description", plugin.getConfig().getString(path + ".description", ""));
                    return placeholders;
                }
            }
        }
        
        placeholders.put("fire-color", plugin.getConfig().getString("fire-streak.colors.days.extinguished.color", "&#FFFFFF"));
        placeholders.put("fire-display", plugin.getConfig().getString("fire-streak.colors.days.extinguished.display", ""));
        placeholders.put("fire-description", plugin.getConfig().getString("fire-streak.colors.days.extinguished.description", ""));
        return placeholders;
    }
}
