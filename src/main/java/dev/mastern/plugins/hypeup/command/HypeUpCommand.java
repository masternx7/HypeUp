package dev.mastern.plugins.hypeup.command;

import dev.mastern.plugins.hypeup.HypeUp;
import dev.mastern.plugins.hypeup.data.FireStreak;
import dev.mastern.plugins.hypeup.gui.GUIManager;
import dev.mastern.plugins.hypeup.manager.FireStreakManager;
import dev.mastern.plugins.hypeup.manager.MessageManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class HypeUpCommand implements CommandExecutor, TabCompleter {
    
    private final HypeUp plugin;
    private final FireStreakManager fireManager;
    private final MessageManager messages;
    private final GUIManager guiManager;
    
    public HypeUpCommand(HypeUp plugin, FireStreakManager fireManager, MessageManager messages, GUIManager guiManager) {
        this.plugin = plugin;
        this.fireManager = fireManager;
        this.messages = messages;
        this.guiManager = guiManager;
    }
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, 
                            @NotNull String label, @NotNull String[] args) {
        
        if (args.length == 0) {
            if (sender instanceof Player player) {
                guiManager.openListGUI(player);
            } else {
                messages.sendMessage((Player) sender, "general.only-player");
            }
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "help" -> {
                if (sender instanceof Player player) {
                    messages.sendMultilineMessage(player, "commands.help", null);
                }
                return true;
            }
            
            case "send" -> {
                if (!(sender instanceof Player player)) {
                    messages.sendMessage((Player) sender, "general.only-player");
                    return true;
                }
                
                if (args.length < 2) {
                    Map<String, String> placeholders = new HashMap<>();
                    placeholders.put("usage", "/hypeup send <player>");
                    messages.sendMessage(player, "commands.usage", placeholders);
                    return true;
                }
                
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null || !target.isOnline()) {
                    Map<String, String> placeholders = new HashMap<>();
                    placeholders.put("player", args[1]);
                    messages.sendMessage(player, "general.player-not-found", placeholders);
                    return true;
                }
                
                if (target.equals(player)) {
                    messages.sendMessage(player, "general.no-permission");
                    return true;
                }
                
                if (!fireManager.canStartNewFire(player.getUniqueId())) {
                    Map<String, String> placeholders = new HashMap<>();
                    placeholders.put("max", String.valueOf(fireManager.getMaxPartners()));
                    messages.sendMessage(player, "fire.limit-reached", placeholders);
                    return true;
                }
                
                guiManager.openGiftGUI(player, target);
                return true;
            }
            
            case "info" -> {
                if (!(sender instanceof Player player)) {
                    messages.sendMessage((Player) sender, "general.only-player");
                    return true;
                }
                
                Player target = player;
                if (args.length >= 2) {
                    target = Bukkit.getPlayer(args[1]);
                    if (target == null || !target.isOnline()) {
                        Map<String, String> placeholders = new HashMap<>();
                        placeholders.put("player", args[1]);
                        messages.sendMessage(player, "general.player-not-found", placeholders);
                        return true;
                    }
                }
                
                showInfo(player, target);
                return true;
            }
            
            case "list" -> {
                if (!(sender instanceof Player player)) {
                    messages.sendMessage((Player) sender, "general.only-player");
                    return true;
                }
                
                guiManager.openListGUI(player);
                return true;
            }
            
            case "reload" -> {
                if (!sender.hasPermission("hypeup.admin")) {
                    if (sender instanceof Player player) {
                        messages.sendMessage(player, "general.no-permission");
                    }
                    return true;
                }
                
                plugin.reloadConfig();
                fireManager.loadConfig();
                messages.reload();
                guiManager.loadConfig();
                
                if (sender instanceof Player player) {
                    messages.sendMessage(player, "general.reload-success");
                } else {
                    sender.sendMessage("HypeUp reloaded successfully!");
                }
                return true;
            }
            
            default -> {
                if (sender instanceof Player player) {
                    messages.sendMultilineMessage(player, "commands.help", null);
                }
                return true;
            }
        }
    }
    
    private void showInfo(Player viewer, Player target) {
        List<FireStreak> streaks = fireManager.getPlayerStreaks(target.getUniqueId());
        
        if (streaks.isEmpty()) {
            messages.sendMessage(viewer, "fire.info", MessageManager.placeholder("target", target.getName()));
            return;
        }
        
        for (FireStreak streak : streaks) {
            UUID partnerUUID = streak.getPartner(target.getUniqueId());
            Player partner = Bukkit.getPlayer(partnerUUID);
            
            if (partner == null) continue;
            
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("target", partner.getName());
            placeholders.put("streak", String.valueOf(streak.getCurrentStreak()));
            placeholders.put("restore", String.valueOf(streak.getRestoreCount()));
            placeholders.put("max", String.valueOf(fireManager.getMaxRestoreCount()));
            
            placeholders.putAll(fireManager.getFireColorPlaceholders(streak.getCurrentStreak()));
            
            messages.sendMultilineMessage(viewer, "fire.info", placeholders);
        }
    }
    
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                     @NotNull String alias, @NotNull String[] args) {
        
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            completions.addAll(Arrays.asList("help", "send", "info", "list"));
            
            if (sender.hasPermission("hypeup.admin")) {
                completions.add("reload");
            }
            
            return completions.stream()
                .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                .collect(Collectors.toList());
        }
        
        if (args.length == 2 && (args[0].equalsIgnoreCase("send") || args[0].equalsIgnoreCase("info"))) {
            return Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                .collect(Collectors.toList());
        }
        
        return completions;
    }
}
