package dev.mastern.plugins.manager;

import dev.mastern.plugins.HypeUp;
import dev.mastern.plugins.utils.ColorUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessageManager {
    
    private final HypeUp plugin;
    private FileConfiguration messageConfig;
    private String prefix;
    
    public MessageManager(HypeUp plugin) {
        this.plugin = plugin;
        loadMessages();
    }
    
    public void loadMessages() {
        File messageFile = new File(plugin.getDataFolder(), "message.yml");
        
        if (!messageFile.exists()) {
            plugin.saveResource("message.yml", false);
        }
        
        messageConfig = YamlConfiguration.loadConfiguration(messageFile);
        prefix = messageConfig.getString("prefix", "&#FF6B6B&l[&#FFD93D&lHypeUp&#FF6B6B&l]&r");
    }
    
    public void reload() {
        loadMessages();
    }
    
    public String getMessage(String path, Map<String, String> placeholders) {
        String message = messageConfig.getString(path, "Message not found: " + path);
        
        message = message.replace("{prefix}", prefix);
        
        if (placeholders != null) {
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                message = message.replace("{" + entry.getKey() + "}", entry.getValue());
            }
        }
        
        return message;
    }
    
    public String getMessage(String path) {
        return getMessage(path, null);
    }
    
    public Component getComponent(String path, Map<String, String> placeholders) {
        String message = getMessage(path, placeholders);
        return ColorUtils.colorize(message);
    }
    
    public Component getComponent(String path) {
        return getComponent(path, null);
    }
    
    public void sendMessage(Player player, String path, Map<String, String> placeholders) {
        player.sendMessage(getComponent(path, placeholders));
    }
    
    public void sendMessage(Player player, String path) {
        sendMessage(player, path, null);
    }
    
    public void sendMultilineMessage(Player player, String path, Map<String, String> placeholders) {
        String message = getMessage(path, placeholders);
        String[] lines = message.split("\n");
        
        for (String line : lines) {
            player.sendMessage(ColorUtils.colorize(line));
        }
    }
    
    public List<String> getMessageList(String path) {
        return messageConfig.getStringList(path);
    }
    
    public List<Component> getComponentList(String path, Map<String, String> placeholders) {
        List<String> messages = getMessageList(path);
        return messages.stream()
            .map(msg -> {
                if (placeholders != null) {
                    for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                        msg = msg.replace("{" + entry.getKey() + "}", entry.getValue());
                    }
                }
                return ColorUtils.colorize(msg.replace("{prefix}", prefix));
            })
            .toList();
    }
    
    public static Map<String, String> placeholders() {
        return new HashMap<>();
    }
    
    public static Map<String, String> placeholder(String key, String value) {
        Map<String, String> map = new HashMap<>();
        map.put(key, value);
        return map;
    }
}
