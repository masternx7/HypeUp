package dev.mastern.plugins.manager;

import dev.mastern.plugins.HypeUp;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RewardManager {
    
    private final HypeUp plugin;
    private final MessageManager messages;
    private final Map<Integer, List<String>> streakRewards;
    private boolean enabled;
    
    public RewardManager(HypeUp plugin, MessageManager messages) {
        this.plugin = plugin;
        this.messages = messages;
        this.streakRewards = new HashMap<>();
        loadConfig();
    }
    
    public void loadConfig() {
        enabled = plugin.getConfig().getBoolean("rewards.streak-rewards.enabled", true);
        streakRewards.clear();
        
        if (!enabled) return;
        
        var rewardsSection = plugin.getConfig().getConfigurationSection("rewards.streak-rewards");
        if (rewardsSection == null) return;
        
        for (String key : rewardsSection.getKeys(false)) {
            if (key.equals("enabled")) continue;
            
            try {
                int streak = Integer.parseInt(key);
                List<String> commands = rewardsSection.getStringList(key);
                streakRewards.put(streak, commands);
            } catch (NumberFormatException e) {
                plugin.getLogger().warning("Invalid streak number in rewards: " + key);
            }
        }
    }
    
    public void checkAndGiveRewards(Player player, int streak) {
        if (!enabled) return;
        if (!streakRewards.containsKey(streak)) return;
        
        List<String> commands = streakRewards.get(streak);
        
        for (String command : commands) {
            command = command.replace("%player%", player.getName());
            
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
        }
        
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("streak", String.valueOf(streak));
        messages.sendMultilineMessage(player, "rewards.received", placeholders);
    }
    
    public List<Integer> getRewardStreaks() {
        return streakRewards.keySet().stream().sorted().toList();
    }
}
