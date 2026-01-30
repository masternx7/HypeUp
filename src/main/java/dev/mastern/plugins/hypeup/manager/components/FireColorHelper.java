package dev.mastern.plugins.hypeup.manager.components;

import dev.mastern.plugins.hypeup.HypeUp;

import java.util.*;
import java.util.stream.Collectors;

public class FireColorHelper {
    
    private final HypeUp plugin;
    
    public FireColorHelper(HypeUp plugin) {
        this.plugin = plugin;
    }
    
    public Map<String, String> getPlaceholders(int streak) {
        return getPlaceholders(streak, true);
    }
    
    public Map<String, String> getPlaceholders(int streak, boolean isMissionCompletedToday) {
        Map<String, String> placeholders = new HashMap<>();
        
        if (streak == 0 || (!isMissionCompletedToday && streak > 0)) {
            placeholders.put("fire-color", plugin.getConfig().getString("fire-streak.colors.days.extinguished.color", "&#323232"));
            placeholders.put("fire-display", plugin.getConfig().getString("fire-streak.colors.days.extinguished.display", "Fire"));
            placeholders.put("fire-description", plugin.getConfig().getString("fire-streak.colors.days.extinguished.description", "Extinguished Fire"));
            return placeholders;
        }
        
        if (plugin.getConfig().contains("fire-streak.colors.days")) {
            List<String> rangeKeys = plugin.getConfig().getConfigurationSection("fire-streak.colors.days")
                    .getKeys(false).stream()
                    .filter(key -> key.contains("-"))
                    .sorted((a, b) -> {
                        int minA = Integer.parseInt(a.split("-")[0]);
                        int minB = Integer.parseInt(b.split("-")[0]);
                        return Integer.compare(minA, minB);
                    })
                    .collect(Collectors.toList());
            
            for (String rangeKey : rangeKeys) {
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
