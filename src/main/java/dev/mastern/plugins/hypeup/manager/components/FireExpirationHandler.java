package dev.mastern.plugins.hypeup.manager.components;

import dev.mastern.plugins.hypeup.data.FireStreak;
import dev.mastern.plugins.hypeup.database.DatabaseManager;
import dev.mastern.plugins.hypeup.manager.MessageManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

public class FireExpirationHandler {
    
    private final DatabaseManager database;
    private final MessageManager messages;
    private final int daysBeforeExpire;
    private final ZoneId timeZone;
    
    public FireExpirationHandler(DatabaseManager database, MessageManager messages, int daysBeforeExpire, ZoneId timeZone) {
        this.database = database;
        this.messages = messages;
        this.daysBeforeExpire = daysBeforeExpire;
        this.timeZone = timeZone;
    }
    
    public boolean isExpired(FireStreak streak) {
        if (streak.getLastFire() == null) return false;
        
        LocalDate lastFireDate = streak.getLastFire().atZone(timeZone).toLocalDate();
        LocalDate today = LocalDate.now(timeZone);
        
        long daysSinceLastFire = ChronoUnit.DAYS.between(lastFireDate, today);
        return daysSinceLastFire > daysBeforeExpire;
    }
    
    public void expire(FireStreak streak) {
        streak.setExpired(true);
        streak.setCurrentStreak(0);
        
        Bukkit.getScheduler().runTaskAsynchronously(JavaPlugin.getProvidingPlugin(getClass()), () -> {
            database.saveFireStreak(streak);
        });
    }
    
    public void notifyExpiration(FireStreak streak) {
        Player player1 = Bukkit.getPlayer(streak.getPlayer1());
        Player player2 = Bukkit.getPlayer(streak.getPlayer2());
        
        LocalDate lastFireDate = streak.getLastFire().atZone(timeZone).toLocalDate();
        LocalDate today = LocalDate.now(timeZone);
        long days = ChronoUnit.DAYS.between(lastFireDate, today);
        
        if (player1 != null && player1.isOnline()) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("target", player2 != null ? player2.getName() : "Unknown");
            placeholders.put("days", String.valueOf(days));
            messages.sendMessage(player1, "fire.expired", placeholders);
        }
        
        if (player2 != null && player2.isOnline()) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("player", player1 != null ? player1.getName() : "Unknown");
            placeholders.put("days", String.valueOf(days));
            messages.sendMessage(player2, "fire.partner-expired", placeholders);
        }
    }
}
