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
        
        if (plugin.getConfig().contains("fire-colors.ranges")) {
            for (String rangeKey : plugin.getConfig().getConfigurationSection("fire-colors.ranges").getKeys(false)) {
                String[] range = rangeKey.split("-");
                int min = Integer.parseInt(range[0]);
                int max = Integer.parseInt(range[1]);
                
                if (streak >= min && streak <= max) {
                    String path = "fire-colors.ranges." + rangeKey;
                    placeholders.put("fire-color", plugin.getConfig().getString(path + ".color", "&#FF0000"));
                    placeholders.put("fire-display", plugin.getConfig().getString(path + ".display", ""));
                    placeholders.put("fire-description", plugin.getConfig().getString(path + ".description", ""));
                    return placeholders;
                }
            }
        }
        
        placeholders.put("fire-color", "&#FFFFFF");
        placeholders.put("fire-display", "");
        placeholders.put("fire-description", "");
        return placeholders;
    }
}
