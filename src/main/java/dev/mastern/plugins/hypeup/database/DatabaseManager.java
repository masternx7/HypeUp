package dev.mastern.plugins.hypeup.database;

import dev.mastern.plugins.hypeup.HypeUp;
import dev.mastern.plugins.hypeup.data.FireStreak;
import dev.mastern.plugins.hypeup.database.dao.DatabaseConnection;
import org.bukkit.Bukkit;

import java.sql.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class DatabaseManager {
    
    private final HypeUp plugin;
    private final DatabaseConnection connection;
    
    public DatabaseManager(HypeUp plugin) {
        this.plugin = plugin;
        this.connection = new DatabaseConnection(plugin);
    }
    
    public void connect() {
        connection.connect();
        createTables();
    }
    
    private void createTables() {
        String fireStreaksTable = """
            CREATE TABLE IF NOT EXISTS fire_streaks (
                id INT AUTO_INCREMENT PRIMARY KEY,
                player1 VARCHAR(36) NOT NULL,
                player2 VARCHAR(36) NOT NULL,
                current_streak INT DEFAULT 0,
                max_streak INT DEFAULT 0,
                last_interaction TIMESTAMP,
                last_fire TIMESTAMP,
                restore_count INT DEFAULT 0,
                expired BOOLEAN DEFAULT FALSE,
                chat_progress INT DEFAULT 0,
                shift_progress INT DEFAULT 0,
                gift_completed BOOLEAN DEFAULT FALSE,
                last_chat_time TIMESTAMP,
                UNIQUE KEY unique_pair (player1, player2),
                INDEX idx_player1 (player1),
                INDEX idx_player2 (player2)
            )
        """;
        
        String playerStatsTable = """
            CREATE TABLE IF NOT EXISTS player_stats (
                uuid VARCHAR(36) PRIMARY KEY,
                total_partners INT DEFAULT 0,
                max_streak INT DEFAULT 0,
                total_fires INT DEFAULT 0,
                last_update TIMESTAMP
            )
        """;
        
        try (Connection conn = connection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(fireStreaksTable);
            stmt.execute(playerStatsTable);
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to create tables: " + e.getMessage());
        }
    }
    
    public void saveFireStreak(FireStreak streak) {
        String sql = """
            INSERT INTO fire_streaks 
            (player1, player2, current_streak, max_streak, last_interaction, 
             last_fire, restore_count, expired, chat_progress, shift_progress, gift_completed, last_chat_time)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE
            current_streak = VALUES(current_streak),
            max_streak = VALUES(max_streak),
            last_interaction = VALUES(last_interaction),
            last_fire = VALUES(last_fire),
            restore_count = VALUES(restore_count),
            expired = VALUES(expired),
            chat_progress = VALUES(chat_progress),
            shift_progress = VALUES(shift_progress),
            gift_completed = VALUES(gift_completed),
            last_chat_time = VALUES(last_chat_time)
        """;
        
        try (Connection conn = connection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, streak.getPlayer1().toString());
            stmt.setString(2, streak.getPlayer2().toString());
            stmt.setInt(3, streak.getCurrentStreak());
            stmt.setInt(4, streak.getMaxStreak());
            stmt.setTimestamp(5, Timestamp.valueOf(streak.getLastInteraction()));
            stmt.setTimestamp(6, streak.getLastFire() != null ? Timestamp.valueOf(streak.getLastFire()) : null);
            stmt.setInt(7, streak.getRestoreCount());
            stmt.setBoolean(8, streak.isExpired());
            stmt.setInt(9, streak.getChatProgress());
            stmt.setInt(10, streak.getShiftProgress());
            stmt.setBoolean(11, streak.isGiftCompleted());
            stmt.setTimestamp(12, streak.getLastChatTime() != null ? Timestamp.valueOf(streak.getLastChatTime()) : null);
            
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to save fire streak: " + e.getMessage());
        }
    }
    
    public CompletableFuture<Void> saveFireStreakAsync(FireStreak streak) {
        return CompletableFuture.runAsync(() -> saveFireStreak(streak));
    }
    
    public void saveFireStreaksBatch(List<FireStreak> streaks) {
        if (streaks.isEmpty()) return;
        
        String sql = """
            INSERT INTO fire_streaks 
            (player1, player2, current_streak, max_streak, last_interaction, 
             last_fire, restore_count, expired, chat_progress, shift_progress, gift_completed, last_chat_time)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE
            current_streak = VALUES(current_streak),
            max_streak = VALUES(max_streak),
            last_interaction = VALUES(last_interaction),
            last_fire = VALUES(last_fire),
            restore_count = VALUES(restore_count),
            expired = VALUES(expired),
            chat_progress = VALUES(chat_progress),
            shift_progress = VALUES(shift_progress),
            gift_completed = VALUES(gift_completed),
            last_chat_time = VALUES(last_chat_time)
        """;
        
        try (Connection conn = connection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            conn.setAutoCommit(false);
            
            for (FireStreak streak : streaks) {
                stmt.setString(1, streak.getPlayer1().toString());
                stmt.setString(2, streak.getPlayer2().toString());
                stmt.setInt(3, streak.getCurrentStreak());
                stmt.setInt(4, streak.getMaxStreak());
                stmt.setTimestamp(5, Timestamp.valueOf(streak.getLastInteraction()));
                stmt.setTimestamp(6, streak.getLastFire() != null ? Timestamp.valueOf(streak.getLastFire()) : null);
                stmt.setInt(7, streak.getRestoreCount());
                stmt.setBoolean(8, streak.isExpired());
                stmt.setInt(9, streak.getChatProgress());
                stmt.setInt(10, streak.getShiftProgress());
                stmt.setBoolean(11, streak.isGiftCompleted());
                stmt.setTimestamp(12, streak.getLastChatTime() != null ? Timestamp.valueOf(streak.getLastChatTime()) : null);
                
                stmt.addBatch();
            }
            
            stmt.executeBatch();
            conn.commit();
            conn.setAutoCommit(true);
            
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to batch save fire streaks: " + e.getMessage());
        }
    }
    
    public FireStreak loadFireStreak(UUID player1, UUID player2) {
        String sql = """
            SELECT * FROM fire_streaks 
            WHERE (player1 = ? AND player2 = ?) OR (player1 = ? AND player2 = ?)
        """;
        
        try (Connection conn = connection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, player1.toString());
            stmt.setString(2, player2.toString());
            stmt.setString(3, player2.toString());
            stmt.setString(4, player1.toString());
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return resultSetToFireStreak(rs);
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to load fire streak: " + e.getMessage());
        }
        
        return null;
    }
    
    public List<FireStreak> loadPlayerStreaks(UUID player) {
        String sql = """
            SELECT * FROM fire_streaks 
            WHERE player1 = ? OR player2 = ?
            ORDER BY current_streak DESC, last_fire DESC
        """;
        
        List<FireStreak> streaks = new ArrayList<>();
        
        try (Connection conn = connection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, player.toString());
            stmt.setString(2, player.toString());
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                streaks.add(resultSetToFireStreak(rs));
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to load player streaks: " + e.getMessage());
        }
        
        return streaks;
    }
    
    private FireStreak resultSetToFireStreak(ResultSet rs) throws SQLException {
        UUID player1 = UUID.fromString(rs.getString("player1"));
        UUID player2 = UUID.fromString(rs.getString("player2"));
        
        FireStreak streak = new FireStreak(player1, player2);
        streak.setCurrentStreak(rs.getInt("current_streak"));
        streak.setMaxStreak(rs.getInt("max_streak"));
        
        Timestamp lastInteraction = rs.getTimestamp("last_interaction");
        if (lastInteraction != null) streak.setLastInteraction(lastInteraction.toLocalDateTime());
        
        Timestamp lastFire = rs.getTimestamp("last_fire");
        if (lastFire != null) streak.setLastFire(lastFire.toLocalDateTime());
        
        streak.setRestoreCount(rs.getInt("restore_count"));
        streak.setExpired(rs.getBoolean("expired"));
        streak.setChatProgress(rs.getInt("chat_progress"));
        streak.setShiftProgress(rs.getInt("shift_progress"));
        streak.setGiftCompleted(rs.getBoolean("gift_completed"));
        
        Timestamp lastChatTime = rs.getTimestamp("last_chat_time");
        if (lastChatTime != null) streak.setLastChatTime(lastChatTime.toLocalDateTime());
        
        return streak;
    }
    
    public void deleteFireStreak(UUID player1, UUID player2) {
        String sql = """
            DELETE FROM fire_streaks 
            WHERE (player1 = ? AND player2 = ?) OR (player1 = ? AND player2 = ?)
        """;
        
        try (Connection conn = connection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, player1.toString());
            stmt.setString(2, player2.toString());
            stmt.setString(3, player2.toString());
            stmt.setString(4, player1.toString());
            
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to delete fire streak: " + e.getMessage());
        }
    }
    
    public void disconnect() {
        connection.close();
    }
}
