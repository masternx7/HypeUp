package dev.mastern.plugins.hypeup.listener;

import dev.mastern.plugins.hypeup.HypeUp;
import dev.mastern.plugins.hypeup.data.FireStreak;
import dev.mastern.plugins.hypeup.manager.FireStreakManager;
import dev.mastern.plugins.hypeup.manager.MessageManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;

import java.time.LocalDateTime;
import java.util.*;

public class ShiftListener implements Listener {
    
    private final HypeUp plugin;
    private final FireStreakManager fireManager;
    private final MessageManager messages;
    

    private boolean enabled;
    private int requiredInteractions;
    private double maxDistance;

    private final Map<String, LocalDateTime> recentInteractions = new HashMap<>();
    
    public ShiftListener(HypeUp plugin, FireStreakManager fireManager, MessageManager messages) {
        this.plugin = plugin;
        this.fireManager = fireManager;
        this.messages = messages;
        loadConfig();
    }
    
    public void loadConfig() {
        enabled = plugin.getConfig().getBoolean("missions.shift.enabled", true);
        requiredInteractions = plugin.getConfig().getInt("missions.shift.required-interactions", 2);
        maxDistance = plugin.getConfig().getDouble("missions.shift.max-distance", 3.0);
    }
    
    @EventHandler
    public void onPlayerSneak(PlayerToggleSneakEvent event) {
        if (!enabled) return;
        if (!event.isSneaking()) return;
        
        Player player = event.getPlayer();
        
        List<Player> nearbyPlayers = findNearbyPlayers(player);
        
        for (Player target : nearbyPlayers) {
            FireStreak streak = fireManager.getOrCreateStreak(player.getUniqueId(), target.getUniqueId());
            if (streak.getShiftProgress() >= requiredInteractions) {
                continue;
            }
            
            String interactionKey = getInteractionKey(player.getUniqueId(), target.getUniqueId());
            streak.setShiftProgress(streak.getShiftProgress() + 1);
            recentInteractions.put(interactionKey, LocalDateTime.now());
            
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                plugin.getDatabaseManager().saveFireStreak(streak);
            });
            
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("current", String.valueOf(streak.getShiftProgress()));
            placeholders.put("required", String.valueOf(requiredInteractions));
            placeholders.put("target", target.getName());
            
            messages.sendMessage(player, "missions.shift.progress", placeholders);
            
            if (streak.getShiftProgress() >= requiredInteractions) {
                messages.sendMessage(player, "missions.shift.completed", null);
                
                checkMissionCompletion(streak, player, target);
            }
        }
    }
    
    private List<Player> findNearbyPlayers(Player player) {
        List<Player> nearby = new ArrayList<>();
        
        for (Player other : player.getWorld().getPlayers()) {
            if (other.equals(player)) continue;
            
            double distance = player.getLocation().distance(other.getLocation());
            if (distance <= maxDistance) {
                nearby.add(other);
            }
        }
        
        return nearby;
    }
    
    private String getInteractionKey(UUID player1, UUID player2) {
        if (player1.compareTo(player2) < 0) {
            return player1.toString() + ":" + player2.toString();
        } else {
            return player2.toString() + ":" + player1.toString();
        }
    }
    
    private void checkMissionCompletion(FireStreak streak, Player player1, Player player2) {
        if (streak.getLastFire() != null) {
            java.time.ZoneId timeZone = fireManager.getTimeZone();
            java.time.LocalDate lastFireDate = streak.getLastFire().atZone(timeZone).toLocalDate();
            java.time.LocalDate today = java.time.LocalDate.now(timeZone);
            
            if (today.isAfter(lastFireDate) && (streak.getChatProgress() > 0 || streak.getShiftProgress() > 0 || streak.isGiftCompleted())) {
                streak.resetDailyProgress();
                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                    plugin.getDatabaseManager().saveFireStreak(streak);
                });
            }
        }
        
        boolean chatEnabled = plugin.getConfig().getBoolean("missions.chat.enabled", true);
        boolean shiftEnabled = plugin.getConfig().getBoolean("missions.shift.enabled", true);
        boolean giftEnabled = plugin.getConfig().getBoolean("missions.item-gift.enabled", true);
        
        int minMessages = plugin.getConfig().getInt("missions.chat.min-messages", 2);
        
        if (streak.areMissionsCompleted(chatEnabled, minMessages, shiftEnabled, requiredInteractions, giftEnabled)) {
            fireManager.completeFire(streak, player1, player2);
        }
    }
}
