package dev.mastern.plugins;

import dev.mastern.plugins.command.HypeUpCommand;
import dev.mastern.plugins.database.DatabaseManager;
import dev.mastern.plugins.expansion.HypeUpPlaceholder;
import dev.mastern.plugins.gui.GUIManager;
import dev.mastern.plugins.listener.ChatListener;
import dev.mastern.plugins.listener.GUIListener;
import dev.mastern.plugins.listener.ShiftListener;
import dev.mastern.plugins.manager.FireStreakManager;
import dev.mastern.plugins.manager.MessageManager;
import dev.mastern.plugins.manager.RewardManager;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public final class HypeUp extends JavaPlugin {

    private DatabaseManager databaseManager;
    private MessageManager messageManager;
    private FireStreakManager fireStreakManager;
    private RewardManager rewardManager;
    private GUIManager guiManager;
    
    private int expirationCheckTask;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        saveResource("message.yml", false);
        saveResource("gui.yml", false);
        
        getLogger().info("╔═══════════════════════════════════════════╗");
        getLogger().info("║         HypeUp - Initializing...          ║");
        getLogger().info("╚═══════════════════════════════════════════╝");
        
        try {
            databaseManager = new DatabaseManager(this);
            databaseManager.connect();
            getLogger().info("Database connected!");
            
            messageManager = new MessageManager(this);
            rewardManager = new RewardManager(this, messageManager);
            fireStreakManager = new FireStreakManager(this, databaseManager, messageManager);
            guiManager = new GUIManager(this, fireStreakManager, messageManager);
            
            getLogger().info("Managers initialized!");
            
            registerListeners();
            getLogger().info("Listeners registered!");
            
            registerCommands();
            getLogger().info("Commands registered!");
            
            startTasks();
            getLogger().info("Tasks started!");
            
            if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
                new HypeUpPlaceholder(this, fireStreakManager, messageManager).register();
                getLogger().info("PlaceholderAPI hooked!");
            }
            
            // Initialize bStats
            new Metrics(this, 29137);
            
            getLogger().info("HypeUp enabled successfully!");
            
        } catch (Exception e) {
            getLogger().severe("Failed to enable HypeUp: " + e.getMessage());
            e.printStackTrace();
            Bukkit.getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {

        getLogger().info("╔═══════════════════════════════════════════╗");
        getLogger().info("║         HypeUp - Shutting Down...         ║");
        getLogger().info("╚═══════════════════════════════════════════╝");
        
        if (expirationCheckTask != -1) {
            Bukkit.getScheduler().cancelTask(expirationCheckTask);
        }
        
        if (fireStreakManager != null) {
            getLogger().info("Saving all fire streaks synchronously...");
            List<dev.mastern.plugins.data.FireStreak> streakList = new ArrayList<>(fireStreakManager.getActiveStreaks().values());
            if (!streakList.isEmpty()) {
                databaseManager.saveFireStreaksBatch(streakList);
            }
            getLogger().info("Saved all fire streaks!");
        }
        
        if (databaseManager != null) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            databaseManager.disconnect();
            getLogger().info("Database disconnected!");
        }
        
        getLogger().info("HypeUp disabled successfully!");
    }
    
    private void registerListeners() {
        ChatListener chatListener = new ChatListener(this, fireStreakManager, messageManager);
        ShiftListener shiftListener = new ShiftListener(this, fireStreakManager, messageManager);
        GUIListener guiListener = new GUIListener(this, guiManager);
        
        Bukkit.getPluginManager().registerEvents(chatListener, this);
        Bukkit.getPluginManager().registerEvents(shiftListener, this);
        Bukkit.getPluginManager().registerEvents(guiListener, this);
    }
    
    private void registerCommands() {
        HypeUpCommand commandExecutor = new HypeUpCommand(this, fireStreakManager, messageManager, guiManager);
        
        var command = getCommand("hypeup");
        if (command != null) {
            command.setExecutor(commandExecutor);
            command.setTabCompleter(commandExecutor);
        }
    }
    
    private void startTasks() {
        int checkInterval = getConfig().getInt("general.check-interval-minutes", 5);
        long ticks = checkInterval * 60 * 20L;
        
        expirationCheckTask = Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
            fireStreakManager.checkExpiredStreaks();
        }, ticks, ticks).getTaskId();
    }
    
    // Getters
    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }
    
    public MessageManager getMessageManager() {
        return messageManager;
    }
    
    public FireStreakManager getFireStreakManager() {
        return fireStreakManager;
    }
    
    public RewardManager getRewardManager() {
        return rewardManager;
    }
    
    public GUIManager getGuiManager() {
        return guiManager;
    }
}
