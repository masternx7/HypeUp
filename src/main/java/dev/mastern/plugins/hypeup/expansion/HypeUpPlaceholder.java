package dev.mastern.plugins.hypeup.expansion;

import dev.mastern.plugins.hypeup.HypeUp;
import dev.mastern.plugins.hypeup.data.FireStreak;
import dev.mastern.plugins.hypeup.manager.FireStreakManager;
import dev.mastern.plugins.hypeup.manager.MessageManager;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class HypeUpPlaceholder extends PlaceholderExpansion {
    
    private final HypeUp plugin;
    private final FireStreakManager streakManager;
    private final MessageManager messageManager;
    
    public HypeUpPlaceholder(HypeUp plugin, FireStreakManager streakManager, MessageManager messageManager) {
        this.plugin = plugin;
        this.streakManager = streakManager;
        this.messageManager = messageManager;
    }
    
    @Override
    public @NotNull String getIdentifier() {
        return "hypeup";
    }
    
    @Override
    public @NotNull String getAuthor() {
        return String.join(", ", plugin.getDescription().getAuthors());
    }
    
    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }
    
    @Override
    public boolean persist() {
        return true;
    }
    
    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null) {
            return "";
        }
        
        List<FireStreak> streaks = streakManager.getPlayerStreaks(player.getUniqueId());
        
        if (params.equals("total_partners")) {
            return String.valueOf(streaks.size());
        }
        
        if (params.equals("max_streak")) {
            return String.valueOf(streaks.stream()
                    .mapToInt(FireStreak::getMaxStreak)
                    .max()
                    .orElse(0));
        }
        
        if (params.equals("total_active")) {
            return String.valueOf(streaks.stream()
                    .filter(s -> !s.isExpired() && s.getCurrentStreak() > 0)
                    .count());
        }
        
        if (params.startsWith("partner_")) {
            String[] parts = params.split("_");
            if (parts.length < 3) {
                return "";
            }
            
            int index = Integer.parseInt(parts[1]) - 1;
            String type = parts[2];
            
            if (index < 0 || index >= streaks.size()) {
                return "";
            }
            
            FireStreak streak = streaks.get(index);
            UUID partnerId = streak.getPartner(player.getUniqueId());
            
            return switch (type) {
                case "name" -> {
                    OfflinePlayer partner = Bukkit.getOfflinePlayer(partnerId);
                    yield partner.getName() != null ? partner.getName() : "Unknown";
                }
                case "streak" -> String.valueOf(streak.getCurrentStreak());
                case "max" -> String.valueOf(streak.getMaxStreak());
                case "color" -> {
                    Map<String, String> placeholders = streakManager.getFireColorPlaceholders(streak);
                    yield messageManager.getMessage(placeholders.get("fire-color"));
                }
                case "display" -> {
                    Map<String, String> placeholders = streakManager.getFireColorPlaceholders(streak);
                    yield messageManager.getMessage(placeholders.get("fire-display"));
                }
                case "expired" -> String.valueOf(streak.isExpired());
                default -> "";
            };
        }
        
        FireStreak topStreak = streaks.stream()
                .max((s1, s2) -> Integer.compare(s1.getCurrentStreak(), s2.getCurrentStreak()))
                .orElse(null);
        
        if (topStreak == null) {
            return params.equals("has_fire") ? "false" : "";
        }
        
        UUID partnerId = topStreak.getPartner(player.getUniqueId());
        OfflinePlayer partner = Bukkit.getOfflinePlayer(partnerId);
        
        return switch (params) {
            case "has_fire" -> "true";
            case "top_partner" -> partner.getName() != null ? partner.getName() : "Unknown";
            case "top_streak" -> String.valueOf(topStreak.getCurrentStreak());
            case "top_max" -> String.valueOf(topStreak.getMaxStreak());
            case "top_color" -> {
                Map<String, String> placeholders = streakManager.getFireColorPlaceholders(topStreak);
                yield messageManager.getMessage(placeholders.get("fire-color"));
            }
            case "top_display" -> {
                Map<String, String> placeholders = streakManager.getFireColorPlaceholders(topStreak);
                yield messageManager.getMessage(placeholders.get("fire-display"));
            }
            case "top_description" -> {
                Map<String, String> placeholders = streakManager.getFireColorPlaceholders(topStreak);
                yield messageManager.getMessage(placeholders.get("fire-description"));
            }
            case "restore_count" -> String.valueOf(topStreak.getRestoreCount());
            default -> "";
        };
    }
}
