package dev.mastern.plugins.hypeup.manager;

import dev.mastern.plugins.hypeup.HypeUp;
import dev.mastern.plugins.hypeup.data.FireStreak;
import dev.mastern.plugins.hypeup.database.DatabaseManager;
import dev.mastern.plugins.hypeup.manager.components.FireColorHelper;
import dev.mastern.plugins.hypeup.manager.components.FireExpirationHandler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class FireStreakManager {
    
    private final HypeUp plugin;
    private final DatabaseManager database;
    private final MessageManager messages;
    private final Map<String, FireStreak> activeStreaks;
    private final FireColorHelper colorHelper;
    private FireExpirationHandler expirationHandler;
    
    private ZoneId timeZone;
    private int daysBeforeExpire;
    private int daysToRestore;
    private int maxRestoreCount;
    private boolean limitPartners;
    private int maxPartners;
    private boolean antiSameIp;
    
    public FireStreakManager(HypeUp plugin, DatabaseManager database, MessageManager messages) {
        this.plugin = plugin;
        this.database = database;
        this.messages = messages;
        this.activeStreaks = new ConcurrentHashMap<>();
        this.colorHelper = new FireColorHelper(plugin);
        loadConfig();
    }
    
    public void loadConfig() {
        String zoneIdStr = plugin.getConfig().getString("general.timezone", "Asia/Bangkok");
        try {
            timeZone = ZoneId.of(zoneIdStr);
        } catch (Exception e) {
            plugin.getLogger().warning("Invalid timezone: " + zoneIdStr + ", using Asia/Bangkok");
            timeZone = ZoneId.of("Asia/Bangkok");
        }
        
        daysBeforeExpire = plugin.getConfig().getInt("fire-streak.days-before-expire", 1);
        daysToRestore = plugin.getConfig().getInt("fire-streak.days-to-restore", 3);
        maxRestoreCount = plugin.getConfig().getInt("fire-streak.max-restore-count", 3);
        limitPartners = plugin.getConfig().getBoolean("fire-streak.limit-partners", false);
        maxPartners = plugin.getConfig().getInt("fire-streak.max-partners", 5);
        antiSameIp = plugin.getConfig().getBoolean("fire-streak.anti-same-ip", true);
        
        this.expirationHandler = new FireExpirationHandler(database, messages, daysBeforeExpire, timeZone);
    }
    
    public FireStreak getOrCreateStreak(UUID player1, UUID player2) {
        String key = getStreakKey(player1, player2);
        
        if (activeStreaks.containsKey(key)) {
            return activeStreaks.get(key);
        }
        
        FireStreak streak = database.loadFireStreak(player1, player2);
        if (streak == null) {
            streak = new FireStreak(player1, player2);
            database.saveFireStreak(streak);
        }
        
        activeStreaks.put(key, streak);
        return streak;
    }
    

    public FireStreak getStreak(UUID player1, UUID player2) {
        String key = getStreakKey(player1, player2);
        
        if (activeStreaks.containsKey(key)) {
            return activeStreaks.get(key);
        }
        
        FireStreak streak = database.loadFireStreak(player1, player2);
        if (streak != null) {
            activeStreaks.put(key, streak);
        }
        return streak;
    }
    
    public List<FireStreak> getPlayerStreaks(UUID player) {
        List<FireStreak> streaks = database.loadPlayerStreaks(player);
        
        for (FireStreak streak : streaks) {
            String key = getStreakKey(streak.getPlayer1(), streak.getPlayer2());
            activeStreaks.put(key, streak);
        }
        
        return streaks;
    }
    
    public void completeFire(FireStreak streak, Player player1, Player player2) {
        LocalDateTime now = LocalDateTime.now(timeZone);
        
        if (streak.getLastFire() != null) {
            LocalDate lastFireDate = streak.getLastFire().atZone(timeZone).toLocalDate();
            LocalDate today = now.toLocalDate();
            
            if (!today.isAfter(lastFireDate)) {
                return;
            }
        }
        
        streak.setCurrentStreak(streak.getCurrentStreak() + 1);
        streak.setLastFire(now);
        streak.setLastInteraction(now);
        streak.setExpired(false);
        
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            database.saveFireStreak(streak);
        });
        
        Map<String, String> placeholders1 = new HashMap<>();
        placeholders1.put("target", player2.getName());
        placeholders1.put("streak", String.valueOf(streak.getCurrentStreak()));
        placeholders1.putAll(colorHelper.getPlaceholders(streak.getCurrentStreak()));
        
        Map<String, String> placeholders2 = new HashMap<>();
        placeholders2.put("player", player1.getName());
        placeholders2.put("streak", String.valueOf(streak.getCurrentStreak()));
        placeholders2.putAll(colorHelper.getPlaceholders(streak.getCurrentStreak()));
        
        messages.sendMessage(player1, "fire.success", placeholders1);
        messages.sendMessage(player2, "fire.partner-success", placeholders2);
        
        // Always send ignited message and check rewards from day 1
        messages.sendMultilineMessage(player1, "fire.ignited", placeholders1);
        messages.sendMultilineMessage(player2, "fire.partner-ignited", placeholders2);
        
        plugin.getRewardManager().checkAndGiveRewards(player1, streak.getCurrentStreak());
        plugin.getRewardManager().checkAndGiveRewards(player2, streak.getCurrentStreak());
    }
    
    public Map<String, String> getFireColorPlaceholders(int streak) {
        return colorHelper.getPlaceholders(streak);
    }
    
    public Map<String, String> getFireColorPlaceholders(int streak, boolean isMissionCompletedToday) {
        return colorHelper.getPlaceholders(streak, isMissionCompletedToday);
    }
    
    public Map<String, String> getFireColorPlaceholders(FireStreak streak) {
        return colorHelper.getPlaceholders(streak.getCurrentStreak());
    }
    
    /**
     * Check if today's missions are completed for this streak
     * Returns true if fire was lit today (lastFire is today)
     */
    public boolean isMissionCompletedToday(FireStreak streak) {
        if (streak == null || streak.getLastFire() == null) {
            return false;
        }
        
        LocalDate lastFireDate = streak.getLastFire().atZone(timeZone).toLocalDate();
        LocalDate today = LocalDate.now(timeZone);
        
        return lastFireDate.equals(today);
    }
    
    public boolean hasSameIp(Player player1, Player player2) {
        if (!antiSameIp) {
            return false;
        }
        
        String ip1 = player1.getAddress() != null ? player1.getAddress().getAddress().getHostAddress() : null;
        String ip2 = player2.getAddress() != null ? player2.getAddress().getAddress().getHostAddress() : null;
        
        if (ip1 == null || ip2 == null) {
            return false;
        }
        
        return ip1.equals(ip2);
    }
    
    public boolean isExpired(FireStreak streak) {
        return expirationHandler.isExpired(streak);
    }
    
    public void expireStreak(FireStreak streak) {
        expirationHandler.expire(streak);
    }
    
    public boolean restoreStreak(FireStreak streak, Player player) {
        if (!streak.isExpired()) {
            return false;
        }
        
        if (streak.getRestoreCount() >= maxRestoreCount) {
            return false;
        }
        
        LocalDateTime now = LocalDateTime.now();
        long daysSinceExpired = ChronoUnit.DAYS.between(streak.getLastFire(), now);
        
        if (daysSinceExpired > daysToRestore + daysBeforeExpire) {
            return false;
        }
        
        streak.setExpired(false);
        streak.setRestoreCount(streak.getRestoreCount() + 1);
        streak.setLastInteraction(now);
        
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            database.saveFireStreak(streak);
        });
        
        UUID partner = streak.getPartner(player.getUniqueId());
        Player partnerPlayer = Bukkit.getPlayer(partner);
        
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("target", partnerPlayer != null ? partnerPlayer.getName() : "Unknown");
        placeholders.put("count", String.valueOf(streak.getRestoreCount()));
        placeholders.put("max", String.valueOf(maxRestoreCount));
        
        messages.sendMessage(player, "fire.restored", placeholders);
        
        if (partnerPlayer != null && partnerPlayer.isOnline()) {
            Map<String, String> partnerPlaceholders = new HashMap<>();
            partnerPlaceholders.put("player", player.getName());
            messages.sendMessage(partnerPlayer, "fire.partner-restored", partnerPlaceholders);
        }
        
        return true;
    }
    
    public boolean canStartNewFire(UUID player) {
        if (!limitPartners) {
            return true;
        }
        
        List<FireStreak> streaks = getPlayerStreaks(player);
        long activeCount = streaks.stream()
            .filter(s -> !s.isExpired() && s.getCurrentStreak() > 0)
            .count();
        
        return activeCount < maxPartners;
    }
    
    public void checkExpiredStreaks() {
        for (FireStreak streak : activeStreaks.values()) {
            if (!streak.isExpired() && expirationHandler.isExpired(streak)) {
                expirationHandler.expire(streak);
                expirationHandler.notifyExpiration(streak);
            }
        }
    }
    
    public void saveAll() {
        if (activeStreaks.isEmpty()) return;
        
        List<FireStreak> streakList = new ArrayList<>(activeStreaks.values());
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            database.saveFireStreaksBatch(streakList);
        });
    }
    
    private String getStreakKey(UUID player1, UUID player2) {
        if (player1.compareTo(player2) < 0) {
            return player1.toString() + ":" + player2.toString();
        } else {
            return player2.toString() + ":" + player1.toString();
        }
    }
    
    public int getMaxRestoreCount() {
        return maxRestoreCount;
    }
    
    public int getMaxPartners() {
        return maxPartners;
    }
    
    public Map<String, FireStreak> getActiveStreaks() {
        return Collections.unmodifiableMap(activeStreaks);
    }
    
    public ZoneId getTimeZone() {
        return timeZone;
    }
    
    public void checkAndResetDailyMissions(FireStreak streak) {
        if (streak.getLastFire() == null) return;
        
        LocalDate today = LocalDate.now(timeZone);
        LocalDate lastFireDate = streak.getLastFire().atZone(timeZone).toLocalDate();
        
        if (lastFireDate.equals(today) || lastFireDate.isAfter(today)) {
            return;
        }
        
        LocalDate lastResetDate = streak.getLastResetDate() != null 
            ? streak.getLastResetDate().atZone(timeZone).toLocalDate() 
            : null;
        
        if (lastResetDate != null && (lastResetDate.equals(today) || lastResetDate.isAfter(today))) {
            return;
        }
        
        streak.resetDailyProgress();
        
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            database.saveFireStreak(streak);
        });
    }
}
