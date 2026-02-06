package dev.mastern.plugins.hypeup;

import dev.mastern.plugins.hypeup.command.HypeUpCommand;
import dev.mastern.plugins.hypeup.database.DatabaseManager;
import dev.mastern.plugins.hypeup.expansion.HypeUpPlaceholder;
import dev.mastern.plugins.hypeup.gui.GUIManager;
import dev.mastern.plugins.hypeup.libraries.LibraryLoader;
import dev.mastern.plugins.hypeup.listener.ChatListener;
import dev.mastern.plugins.hypeup.listener.GUIListener;
import dev.mastern.plugins.hypeup.listener.ShiftListener;
import dev.mastern.plugins.hypeup.manager.FireStreakManager;
import dev.mastern.plugins.hypeup.manager.MessageManager;
import dev.mastern.plugins.hypeup.manager.RewardManager;
import dev.mastern.plugins.hypeup.utils.SchedulerUtil;
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
    
    private SchedulerUtil.SchedulerTask expirationCheckTask;

    @Override
    public void onLoad() {
        new LibraryLoader(this).loadLibraries();
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        saveResource("message.yml", false);
        
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
        
        if (expirationCheckTask != null) {
            expirationCheckTask.cancel();
        }
        
        if (fireStreakManager != null) {
            getLogger().info("Saving all fire streaks synchronously...");
            List<dev.mastern.plugins.hypeup.data.FireStreak> streakList = new ArrayList<>(fireStreakManager.getActiveStreaks().values());
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
        long delayMinutes = checkInterval;
        
        expirationCheckTask = SchedulerUtil.runAsyncRepeating(this, () -> {
            fireStreakManager.checkExpiredStreaks();
        }, delayMinutes, delayMinutes, java.util.concurrent.TimeUnit.MINUTES);
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
