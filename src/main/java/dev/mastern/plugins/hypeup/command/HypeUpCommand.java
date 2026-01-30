package dev.mastern.plugins.hypeup.command;

import dev.mastern.plugins.hypeup.HypeUp;
import dev.mastern.plugins.hypeup.data.FireStreak;
import dev.mastern.plugins.hypeup.gui.GUIManager;
import dev.mastern.plugins.hypeup.manager.FireStreakManager;
import dev.mastern.plugins.hypeup.manager.MessageManager;
import dev.mastern.plugins.hypeup.utils.ColorUtils;
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
                sender.sendMessage(ColorUtils.colorize(messages.getMessage("general.only-player")));
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
                    sender.sendMessage(ColorUtils.colorize(messages.getMessage("general.only-player")));
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
                
                if (fireManager.hasSameIp(player, target)) {
                    messages.sendMessage(player, "fire.same-ip-blocked", null);
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
                    sender.sendMessage(ColorUtils.colorize(messages.getMessage("general.only-player")));
                    return true;
                }
                
                if (args.length < 2) {
                    Map<String, String> placeholders = new HashMap<>();
                    placeholders.put("usage", "/hypeup info <player>");
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
                
                guiManager.openInfoGUI(player, target);
                return true;
            }
            
            case "list" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(ColorUtils.colorize(messages.getMessage("general.only-player")));
                    return true;
                }
                
                guiManager.openListGUI(player);
                return true;
            }
            
            case "msg" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(ColorUtils.colorize(messages.getMessage("general.only-player")));
                    return true;
                }
                
                if (args.length < 3) {
                    Map<String, String> placeholders = new HashMap<>();
                    placeholders.put("usage", "/hypeup msg <player> <message>");
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
                
                if (fireManager.hasSameIp(player, target)) {
                    messages.sendMessage(player, "fire.same-ip-blocked", null);
                    return true;
                }
                
                double maxDistance = plugin.getConfig().getDouble("missions.chat.max-distance", 50);
                if (maxDistance > 0) {
                    if (!player.getWorld().equals(target.getWorld())) {
                        messages.sendMessage(player, "missions.chat.too-far", Map.of("target", target.getName()));
                        return true;
                    }
                    if (player.getLocation().distance(target.getLocation()) > maxDistance) {
                        messages.sendMessage(player, "missions.chat.too-far", Map.of("target", target.getName()));
                        return true;
                    }
                }
                
                String message = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
                
                messages.sendMessage(target, "missions.chat.message-received", Map.of(
                    "player", player.getName(),
                    "message", message
                ));
                
                FireStreak streak = fireManager.getOrCreateStreak(player.getUniqueId(), target.getUniqueId());
                
                if (fireManager.isExpired(streak)) {
                    messages.sendMessage(player, "fire.restore-failed", Map.of("max", String.valueOf(fireManager.getMaxRestoreCount()), "days", String.valueOf(plugin.getConfig().getInt("fire-streak.days-to-restore", 3))));
                    return true;
                }
                
                fireManager.checkAndResetDailyMissions(streak);
                
                int minMessages = plugin.getConfig().getInt("missions.chat.min-messages", 2);
                
                if (streak.getChatProgress(player.getUniqueId()) < minMessages) {
                    streak.setChatProgress(player.getUniqueId(), streak.getChatProgress(player.getUniqueId()) + 1);
                    streak.setLastChatTime(player.getUniqueId(), java.time.LocalDateTime.now());
                    
                    Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                        plugin.getDatabaseManager().saveFireStreak(streak);
                    });
                    
                    Map<String, String> placeholders = new HashMap<>();
                    placeholders.put("current", String.valueOf(streak.getChatProgress(player.getUniqueId())));
                    placeholders.put("required", String.valueOf(minMessages));
                    placeholders.put("target", target.getName());
                    
                    messages.sendMessage(player, "missions.chat.progress", placeholders);
                    
                    if (streak.getChatProgress(player.getUniqueId()) >= minMessages) {
                        messages.sendMessage(player, "missions.chat.completed", null);
                        
                        boolean chatEnabled = plugin.getConfig().getBoolean("missions.chat.enabled", true);
                        boolean shiftEnabled = plugin.getConfig().getBoolean("missions.shift.enabled", true);
                        boolean giftEnabled = plugin.getConfig().getBoolean("missions.item-gift.enabled", true);
                        int requiredShifts = plugin.getConfig().getInt("missions.shift.required-interactions", 2);
                        
                        if (streak.areMissionsCompleted(chatEnabled, minMessages, shiftEnabled, requiredShifts, giftEnabled)) {
                            fireManager.completeFire(streak, player, target);
                        }
                    }
                }
                
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
            
            case "admin" -> {
                if (!sender.hasPermission("hypeup.admin")) {
                    if (sender instanceof Player player) {
                        messages.sendMessage(player, "general.no-permission");
                    }
                    return true;
                }
                
                if (args.length < 2) {
                    if (sender instanceof Player player) {
                        messages.sendMultilineMessage(player, "admin.help", null);
                    } else {
                        sender.sendMessage(ColorUtils.colorize(messages.getMessage("admin.help")));
                    }
                    return true;
                }
                
                String adminAction = args[1].toLowerCase();
                
                switch (adminAction) {
                    case "set" -> {
                        if (args.length < 5) {
                            sender.sendMessage(ColorUtils.colorize(messages.getMessage("admin.set.usage")));
                            return true;
                        }
                        
                        Player player1 = Bukkit.getPlayer(args[2]);
                        Player player2 = Bukkit.getPlayer(args[3]);
                        
                        if (player1 == null || player2 == null) {
                            sender.sendMessage(ColorUtils.colorize(messages.getMessage("admin.players-not-found")));
                            return true;
                        }
                        
                        int amount;
                        try {
                            amount = Integer.parseInt(args[4]);
                        } catch (NumberFormatException e) {
                            sender.sendMessage(ColorUtils.colorize(messages.getMessage("admin.amount-invalid")));
                            return true;
                        }
                        
                        FireStreak streak = fireManager.getOrCreateStreak(player1.getUniqueId(), player2.getUniqueId());
                        streak.setCurrentStreak(amount);
                        if (amount > streak.getMaxStreak()) {
                            streak.setMaxStreak(amount);
                        }
                        
                        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                            plugin.getDatabaseManager().saveFireStreak(streak);
                        });
                        
                        sender.sendMessage(ColorUtils.colorize(messages.getMessage("admin.set.success")
                            .replace("{player1}", player1.getName())
                            .replace("{player2}", player2.getName())
                            .replace("{amount}", String.valueOf(amount))));
                        return true;
                    }
                    
                    case "give" -> {
                        if (args.length < 5) {
                            sender.sendMessage(ColorUtils.colorize(messages.getMessage("admin.give.usage")));
                            return true;
                        }
                        
                        Player player1 = Bukkit.getPlayer(args[2]);
                        Player player2 = Bukkit.getPlayer(args[3]);
                        
                        if (player1 == null || player2 == null) {
                            sender.sendMessage(ColorUtils.colorize(messages.getMessage("admin.players-not-found")));
                            return true;
                        }
                        
                        int amount;
                        try {
                            amount = Integer.parseInt(args[4]);
                        } catch (NumberFormatException e) {
                            sender.sendMessage(ColorUtils.colorize(messages.getMessage("admin.amount-invalid")));
                            return true;
                        }
                        
                        FireStreak streak = fireManager.getOrCreateStreak(player1.getUniqueId(), player2.getUniqueId());
                        int newAmount = streak.getCurrentStreak() + amount;
                        streak.setCurrentStreak(newAmount);
                        if (newAmount > streak.getMaxStreak()) {
                            streak.setMaxStreak(newAmount);
                        }
                        
                        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                            plugin.getDatabaseManager().saveFireStreak(streak);
                        });
                        
                        sender.sendMessage(ColorUtils.colorize(messages.getMessage("admin.give.success")
                            .replace("{player1}", player1.getName())
                            .replace("{player2}", player2.getName())
                            .replace("{amount}", String.valueOf(amount))));
                        return true;
                    }
                    
                    case "take" -> {
                        if (args.length < 5) {
                            sender.sendMessage(ColorUtils.colorize(messages.getMessage("admin.take.usage")));
                            return true;
                        }
                        
                        Player player1 = Bukkit.getPlayer(args[2]);
                        Player player2 = Bukkit.getPlayer(args[3]);
                        
                        if (player1 == null || player2 == null) {
                            sender.sendMessage(ColorUtils.colorize(messages.getMessage("admin.players-not-found")));
                            return true;
                        }
                        
                        int amount;
                        try {
                            amount = Integer.parseInt(args[4]);
                        } catch (NumberFormatException e) {
                            sender.sendMessage(ColorUtils.colorize(messages.getMessage("admin.amount-invalid")));
                            return true;
                        }
                        
                        FireStreak streak = fireManager.getStreak(player1.getUniqueId(), player2.getUniqueId());
                        if (streak == null) {
                            sender.sendMessage(ColorUtils.colorize(messages.getMessage("admin.no-streak")));
                            return true;
                        }
                        
                        int newAmount = Math.max(0, streak.getCurrentStreak() - amount);
                        streak.setCurrentStreak(newAmount);
                        
                        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                            plugin.getDatabaseManager().saveFireStreak(streak);
                        });
                        
                        sender.sendMessage(ColorUtils.colorize(messages.getMessage("admin.take.success")
                            .replace("{player1}", player1.getName())
                            .replace("{player2}", player2.getName())
                            .replace("{amount}", String.valueOf(amount))));
                        return true;
                    }
                    
                    case "expired" -> {
                        if (args.length < 4) {
                            sender.sendMessage(ColorUtils.colorize(messages.getMessage("admin.expired.usage")));
                            return true;
                        }
                        
                        Player player1 = Bukkit.getPlayer(args[2]);
                        Player player2 = Bukkit.getPlayer(args[3]);
                        
                        if (player1 == null || player2 == null) {
                            sender.sendMessage(ColorUtils.colorize(messages.getMessage("admin.players-not-found")));
                            return true;
                        }
                        
                        FireStreak streak = fireManager.getStreak(player1.getUniqueId(), player2.getUniqueId());
                        if (streak == null) {
                            sender.sendMessage(ColorUtils.colorize(messages.getMessage("admin.no-streak")));
                            return true;
                        }
                        
                        streak.setExpired(true);
                        streak.setCurrentStreak(0);
                        
                        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                            plugin.getDatabaseManager().saveFireStreak(streak);
                        });
                        
                        sender.sendMessage(ColorUtils.colorize(messages.getMessage("admin.expired.success")
                            .replace("{player1}", player1.getName())
                            .replace("{player2}", player2.getName())));
                        return true;
                    }
                    
                    case "extinguishfire" -> {
                        if (args.length < 4) {
                            sender.sendMessage(ColorUtils.colorize(messages.getMessage("admin.extinguishfire.usage")));
                            return true;
                        }
                        
                        Player player1 = Bukkit.getPlayer(args[2]);
                        Player player2 = Bukkit.getPlayer(args[3]);
                        
                        if (player1 == null || player2 == null) {
                            sender.sendMessage(ColorUtils.colorize(messages.getMessage("admin.players-not-found")));
                            return true;
                        }
                        
                        FireStreak streak = fireManager.getStreak(player1.getUniqueId(), player2.getUniqueId());
                        if (streak == null) {
                            sender.sendMessage(ColorUtils.colorize(messages.getMessage("admin.no-streak")));
                            return true;
                        }
                        
                        int currentStreak = streak.getCurrentStreak();
                        streak.resetDailyProgress();
                        streak.setLastFire(null);
                        
                        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                            plugin.getDatabaseManager().saveFireStreak(streak);
                        });
                        
                        sender.sendMessage(ColorUtils.colorize(messages.getMessage("admin.extinguishfire.success")
                            .replace("{player1}", player1.getName())
                            .replace("{player2}", player2.getName())
                            .replace("{streak}", String.valueOf(currentStreak))));
                        return true;
                    }
                    
                    case "resetmission" -> {
                        if (args.length < 4) {
                            sender.sendMessage(ColorUtils.colorize(messages.getMessage("admin.resetmission.usage")));
                            return true;
                        }
                        
                        Player player1 = Bukkit.getPlayer(args[2]);
                        Player player2 = Bukkit.getPlayer(args[3]);
                        
                        if (player1 == null || player2 == null) {
                            sender.sendMessage(ColorUtils.colorize(messages.getMessage("admin.players-not-found")));
                            return true;
                        }
                        
                        FireStreak streak = fireManager.getStreak(player1.getUniqueId(), player2.getUniqueId());
                        if (streak == null) {
                            sender.sendMessage(ColorUtils.colorize(messages.getMessage("admin.no-streak")));
                            return true;
                        }
                        
                        streak.resetDailyProgress();
                        
                        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                            plugin.getDatabaseManager().saveFireStreak(streak);
                        });
                        
                        sender.sendMessage(ColorUtils.colorize(messages.getMessage("admin.resetmission.success")
                            .replace("{player1}", player1.getName())
                            .replace("{player2}", player2.getName())));
                        return true;
                    }
                    
                    default -> {
                        sender.sendMessage(ColorUtils.colorize(messages.getMessage("admin.unknown-command")));
                        return true;
                    }
                }
            }
            
            default -> {
                if (sender instanceof Player player) {
                    messages.sendMultilineMessage(player, "commands.help", null);
                }
                return true;
            }
        }
    }
    
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                     @NotNull String alias, @NotNull String[] args) {
        
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            completions.addAll(Arrays.asList("help", "send", "info", "list", "msg"));
            
            if (sender.hasPermission("hypeup.admin")) {
                completions.add("reload");
                completions.add("admin");
            }
            
            return completions.stream()
                .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                .collect(Collectors.toList());
        }
        
        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("send") || args[0].equalsIgnoreCase("info") || args[0].equalsIgnoreCase("msg")) {
                return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
            }
            
            if (args[0].equalsIgnoreCase("admin") && sender.hasPermission("hypeup.admin")) {
                return Arrays.asList("set", "give", "take", "expired", "extinguishfire", "resetmission").stream()
                    .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
            }
        }
        
        if (args.length == 3 && args[0].equalsIgnoreCase("admin") && sender.hasPermission("hypeup.admin")) {
            return Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .filter(name -> name.toLowerCase().startsWith(args[2].toLowerCase()))
                .collect(Collectors.toList());
        }
        
        if (args.length == 4 && args[0].equalsIgnoreCase("admin") && sender.hasPermission("hypeup.admin")) {
            return Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .filter(name -> name.toLowerCase().startsWith(args[3].toLowerCase()))
                .collect(Collectors.toList());
        }
        
        return completions;
    }
    
}
