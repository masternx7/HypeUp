package dev.mastern.plugins.hypeup.listener;

import dev.mastern.plugins.hypeup.HypeUp;
import dev.mastern.plugins.hypeup.data.FireStreak;
import dev.mastern.plugins.hypeup.manager.FireStreakManager;
import dev.mastern.plugins.hypeup.manager.MessageManager;
import dev.mastern.plugins.hypeup.utils.SchedulerUtil;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class ChatListener implements Listener {
    
    private final HypeUp plugin;
    private final FireStreakManager fireManager;
    private final MessageManager messages;
    
    private boolean enabled;
    private int minMessages;
    private int minDelaySeconds;
    private int maxDelaySeconds;
    private double maxDistance;
    
    private final Map<UUID, LocalDateTime> lastMessageTime = new HashMap<>();
    private final Map<UUID, UUID> lastMessageTarget = new HashMap<>();
    
    public ChatListener(HypeUp plugin, FireStreakManager fireManager, MessageManager messages) {
        this.plugin = plugin;
        this.fireManager = fireManager;
        this.messages = messages;
        loadConfig();
    }
    
    public void loadConfig() {
        enabled = plugin.getConfig().getBoolean("missions.chat.enabled", true);
        minMessages = plugin.getConfig().getInt("missions.chat.min-messages", 2);
        minDelaySeconds = plugin.getConfig().getInt("missions.chat.min-delay-seconds", 5);
        maxDelaySeconds = plugin.getConfig().getInt("missions.chat.max-delay-seconds", 10);
        maxDistance = plugin.getConfig().getDouble("missions.chat.max-distance", 50);
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onChat(AsyncChatEvent event) {
        if (!enabled) return;
        
        Player sender = event.getPlayer();
        String message = PlainTextComponentSerializer.plainText().serialize(event.message());
        
        List<Player> mentionedPlayers = findMentionedPlayers(message);
        
        for (Player target : mentionedPlayers) {
            if (target.equals(sender)) continue;
            
            if (maxDistance > 0 && !sender.getWorld().equals(target.getWorld())) {
                messages.sendMessage(sender, "missions.chat.too-far", Map.of("target", target.getName()));
                continue;
            }
            if (maxDistance > 0 && sender.getLocation().distance(target.getLocation()) > maxDistance) {
                messages.sendMessage(sender, "missions.chat.too-far", Map.of("target", target.getName()));
                continue;
            }
            
            if (isSpamming(sender, target)) {
                Map<String, String> placeholders = new HashMap<>();
                messages.sendMessage(sender, "missions.chat.spam-detected", placeholders);
                continue;
            }
            
            FireStreak streak = fireManager.getOrCreateStreak(sender.getUniqueId(), target.getUniqueId());
            
            if (fireManager.isExpired(streak)) {
                continue;
            }
            
            fireManager.checkAndResetDailyMissions(streak);
            
            if (streak.getChatProgress(sender.getUniqueId()) >= minMessages) {
                continue;
            }
            
            lastMessageTime.put(sender.getUniqueId(), LocalDateTime.now());
            lastMessageTarget.put(sender.getUniqueId(), target.getUniqueId());
            
            streak.setChatProgress(sender.getUniqueId(), streak.getChatProgress(sender.getUniqueId()) + 1);
            streak.setLastChatTime(sender.getUniqueId(), LocalDateTime.now());
            
            SchedulerUtil.runAsync(plugin, () -> {
                plugin.getDatabaseManager().saveFireStreak(streak);
            });
            
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("current", String.valueOf(streak.getChatProgress(sender.getUniqueId())));
            placeholders.put("required", String.valueOf(minMessages));
            placeholders.put("target", target.getName());
            
            messages.sendMessage(sender, "missions.chat.progress", placeholders);
            
            if (streak.getChatProgress(sender.getUniqueId()) >= minMessages) {
                messages.sendMessage(sender, "missions.chat.completed", null);
                
                checkMissionCompletion(streak, sender, target);
            }
        }
    }
    
    private List<Player> findMentionedPlayers(String message) {
        List<Player> mentioned = new ArrayList<>();
        String[] words = message.split(" ");
        
        for (String word : words) {
            // Check for @username
            if (word.startsWith("@")) {
                String username = word.substring(1);
                Player player = plugin.getServer().getPlayer(username);
                if (player != null && player.isOnline()) {
                    mentioned.add(player);
                }
            }
        }
        
        return mentioned;
    }
    
    private boolean isSpamming(Player sender, Player target) {
        if (!lastMessageTime.containsKey(sender.getUniqueId())) {
            return false;
        }
        
        LocalDateTime lastTime = lastMessageTime.get(sender.getUniqueId());
        UUID lastTarget = lastMessageTarget.get(sender.getUniqueId());
        
        if (!target.getUniqueId().equals(lastTarget)) {
            return false;
        }
        
        long secondsSince = ChronoUnit.SECONDS.between(lastTime, LocalDateTime.now());
        return secondsSince < minDelaySeconds;
    }
    
    private void checkMissionCompletion(FireStreak streak, Player player1, Player player2) {
        boolean chatEnabled = plugin.getConfig().getBoolean("missions.chat.enabled", true);
        boolean shiftEnabled = plugin.getConfig().getBoolean("missions.shift.enabled", true);
        boolean giftEnabled = plugin.getConfig().getBoolean("missions.item-gift.enabled", true);
        
        int requiredShifts = plugin.getConfig().getInt("missions.shift.required-interactions", 2);
        
        if (streak.areMissionsCompleted(chatEnabled, minMessages, shiftEnabled, requiredShifts, giftEnabled)) {
            fireManager.completeFire(streak, player1, player2);
        }
    }
    
}
