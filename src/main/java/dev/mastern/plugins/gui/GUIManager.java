package dev.mastern.plugins.gui;

import dev.mastern.plugins.HypeUp;
import dev.mastern.plugins.data.FireStreak;
import dev.mastern.plugins.gui.components.ItemBuilder;
import dev.mastern.plugins.gui.components.SoundPlayer;
import dev.mastern.plugins.manager.FireStreakManager;
import dev.mastern.plugins.manager.MessageManager;
import dev.mastern.plugins.utils.ColorUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.*;

public class GUIManager {
    
    private final HypeUp plugin;
    private final FireStreakManager fireManager;
    private final MessageManager messages;
    private final Map<String, FileConfiguration> menuConfigs;
    
    private final Map<UUID, String> openGUIs = new HashMap<>();
    private final Map<UUID, UUID> giftTargets = new HashMap<>();
    
    public GUIManager(HypeUp plugin, FireStreakManager fireManager, MessageManager messages) {
        this.plugin = plugin;
        this.fireManager = fireManager;
        this.messages = messages;
        this.menuConfigs = new HashMap<>();
        loadConfig();
    }
    
    public void loadConfig() {
        File menuFolder = new File(plugin.getDataFolder(), "menu");
        if (!menuFolder.exists()) {
            menuFolder.mkdirs();
            plugin.saveResource("menu/gift.yml", false);
            plugin.saveResource("menu/list.yml", false);
            plugin.saveResource("menu/info.yml", false);
        }
        
        menuConfigs.put("gift", YamlConfiguration.loadConfiguration(new File(menuFolder, "gift.yml")));
        menuConfigs.put("list", YamlConfiguration.loadConfiguration(new File(menuFolder, "list.yml")));
        menuConfigs.put("info", YamlConfiguration.loadConfiguration(new File(menuFolder, "info.yml")));
    }
    
    public void openGiftGUI(Player player, Player target) {
        FileConfiguration config = menuConfigs.get("gift");
        if (config == null) return;
        
        String title = applyPlaceholders(config.getString("title", "HypeUp"), player, target, null);
        Inventory inv = Bukkit.createInventory(null, config.getInt("size", 54), ColorUtils.colorize(title));
        
        addItem(inv, config, "partner-info", player, target, fireManager.getStreak(player.getUniqueId(), target.getUniqueId()));
        addItem(inv, config, "confirm", player, target, null);
        addItem(inv, config, "cancel", player, target, null);
        addBorder(inv, config);
        
        player.openInventory(inv);
        openGUIs.put(player.getUniqueId(), "gift");
        giftTargets.put(player.getUniqueId(), target.getUniqueId());
        SoundPlayer.play(player, menuConfigs.get("general"));
    }
    
    public void openListGUI(Player player) {
        FileConfiguration config = menuConfigs.get("list");
        if (config == null) return;
        
        Inventory inv = Bukkit.createInventory(null, config.getInt("size", 54), 
            ColorUtils.colorize(config.getString("title", "HypeUp - Partners")));
        
        List<FireStreak> streaks = fireManager.getPlayerStreaks(player.getUniqueId());
        
        if (streaks.isEmpty()) {
            addItem(inv, config, "no-partners", player, player, null);
        } else {
            int slot = 0;
            for (FireStreak streak : streaks) {
                if (slot >= inv.getSize() - 9) break;
                UUID partnerUUID = streak.getPartner(player.getUniqueId());
                Player partner = Bukkit.getPlayer(partnerUUID);
                
                if (partner == null) {
                    partner = Bukkit.getOfflinePlayer(partnerUUID).getPlayer();
                }
                
                if (partner != null || Bukkit.getOfflinePlayer(partnerUUID).hasPlayedBefore()) {
                    addPartnerItem(inv, slot++, config, player, Bukkit.getOfflinePlayer(partnerUUID), streak);
                }
            }
        }
        
        addItem(inv, config, "close", player, player, null);
        addBorder(inv, config);
        
        player.openInventory(inv);
        openGUIs.put(player.getUniqueId(), "list");
        SoundPlayer.play(player, menuConfigs.get("general"));
    }
    
    public void confirmGift(Player player) {
        UUID targetUUID = giftTargets.get(player.getUniqueId());
        if (targetUUID == null) return;
        
        Player target = Bukkit.getPlayer(targetUUID);
        if (target == null || !target.isOnline()) {
            messages.sendMessage(player, "general.player-not-found", MessageManager.placeholder("player", "Target"));
            return;
        }
        
        Inventory topInv = player.getOpenInventory().getTopInventory();
        List<ItemStack> items = collectGiftItems(topInv);
        if (items.isEmpty()) return;
        
        int totalItems = items.stream().mapToInt(ItemStack::getAmount).sum();
        if (totalItems > 2304) {
            messages.sendMessage(player, "missions.gift.too-many-items", null);
            return;
        }
        
        List<ItemStack> overflow = new ArrayList<>();
        for (ItemStack item : items) {
            if (item == null || item.getType() == Material.AIR) continue;
            
            HashMap<Integer, ItemStack> leftover = target.getInventory().addItem(item.clone());
            if (!leftover.isEmpty()) {
                overflow.addAll(leftover.values());
            }
        }
        
        for (int slot : menuConfigs.get("gift").getIntegerList("item-slots")) {
            topInv.setItem(slot, null);
        }
        
        if (!overflow.isEmpty()) {
            for (ItemStack item : overflow) {
                target.getWorld().dropItemNaturally(target.getLocation(), item);
            }
            messages.sendMessage(player, "missions.gift.inventory-full", null);
        }
        
        FireStreak streak = fireManager.getOrCreateStreak(player.getUniqueId(), targetUUID);
        streak.setGiftCompleted(true);
        
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            plugin.getDatabaseManager().saveFireStreak(streak);
        });
        
        messages.sendMessage(player, "missions.gift.sent", Map.of("target", target.getName()));
        messages.sendMessage(target, "missions.gift.received", Map.of("player", player.getName()));
        messages.sendMessage(player, "missions.gift.completed", null);
        
        player.closeInventory();
        SoundPlayer.play(player, menuConfigs.get("gift").getConfigurationSection("confirm"));
        checkMissionCompletion(streak, player, target);
    }
    
    private List<ItemStack> collectGiftItems(Inventory inv) {
        List<ItemStack> items = new ArrayList<>();
        FileConfiguration config = menuConfigs.get("gift");
        for (int slot : config.getIntegerList("item-slots")) {
            ItemStack item = inv.getItem(slot);
            if (item != null && item.getType() != Material.AIR) {
                items.add(item.clone());
            }
        }
        return items;
    }
    
    private void addItem(Inventory inv, FileConfiguration config, String key, Player player, Player target, FireStreak streak) {
        ConfigurationSection itemConfig = config.getConfigurationSection(key);
        if (itemConfig == null) return;
        
        Map<String, String> placeholders = createPlaceholders(player, target, streak);
        ItemStack item = ItemBuilder.createSkull(itemConfig, target, placeholders);
        inv.setItem(itemConfig.getInt("slot", 0), item);
    }
    
    private void addPartnerItem(Inventory inv, int slot, FileConfiguration config, Player player, OfflinePlayer partner, FireStreak streak) {
        ConfigurationSection itemConfig = config.getConfigurationSection("partner-item");
        if (itemConfig == null) return;
        
        Map<String, String> placeholders = createPlaceholders(player, partner, streak);
        inv.setItem(slot, ItemBuilder.createSkull(itemConfig, partner, placeholders));
    }
    
    private void addBorder(Inventory inv, FileConfiguration config) {
        ConfigurationSection borderConfig = config.getConfigurationSection("border");
        if (borderConfig == null || !borderConfig.getBoolean("enabled", true)) return;
        
        Material material = Material.valueOf(borderConfig.getString("material", "GRAY_STAINED_GLASS_PANE"));
        Integer customModelData = borderConfig.contains("custom-model-data") ? borderConfig.getInt("custom-model-data") : null;
        ItemStack border = ItemBuilder.createBorder(material, customModelData);
        
        borderConfig.getIntegerList("slots").forEach(slot -> inv.setItem(slot, border));
    }
    
    private Map<String, String> createPlaceholders(Player player, OfflinePlayer target, FireStreak streak) {
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("player", player.getName());
        placeholders.put("target", target.getName() != null ? target.getName() : "Unknown");
        
        if (streak != null) {
            placeholders.put("streak", String.valueOf(streak.getCurrentStreak()));
            placeholders.put("restore", String.valueOf(streak.getRestoreCount()));
            placeholders.put("max", String.valueOf(fireManager.getMaxRestoreCount()));
            placeholders.putAll(fireManager.getFireColorPlaceholders(streak.getCurrentStreak()));
        } else {
            placeholders.put("streak", "0");
            placeholders.put("restore", "0");
            placeholders.put("max", String.valueOf(fireManager.getMaxRestoreCount()));
            placeholders.put("fire-color", "&#FFFFFF");
            placeholders.put("fire-display", "ไม่มีไฟ");
            placeholders.put("fire-description", "");
        }
        
        return placeholders;
    }
    
    private String applyPlaceholders(String text, Player player, Player target, FireStreak streak) {
        if (text == null) return "";
        Map<String, String> placeholders = createPlaceholders(player, target, streak);
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            text = text.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return text;
    }
    
    private void checkMissionCompletion(FireStreak streak, Player player1, Player player2) {
        if (streak.getLastFire() != null) {
            java.time.ZoneId timeZone = fireManager.getTimeZone();
            java.time.LocalDate lastFireDate = streak.getLastFire().atZone(timeZone).toLocalDate();
            java.time.LocalDate today = java.time.LocalDate.now(timeZone);
            
            if (today.isAfter(lastFireDate) && (streak.getChatProgress() > 0 || streak.getShiftProgress() > 0 || streak.isGiftCompleted())) {
                streak.resetDailyProgress();
                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                    plugin.getDatabaseManager().saveFireStreak(streak);
                });
            }
        }
        
        boolean chatEnabled = plugin.getConfig().getBoolean("missions.chat.enabled", true);
        boolean shiftEnabled = plugin.getConfig().getBoolean("missions.shift.enabled", true);
        boolean giftEnabled = plugin.getConfig().getBoolean("missions.item-gift.enabled", true);
        int minMessages = plugin.getConfig().getInt("missions.chat.min-messages", 2);
        int requiredShifts = plugin.getConfig().getInt("missions.shift.required-interactions", 2);
        
        if (streak.areMissionsCompleted(chatEnabled, minMessages, shiftEnabled, requiredShifts, giftEnabled)) {
            fireManager.completeFire(streak, player1, player2);
        }
    }
    
    public String getOpenGUI(UUID player) { return openGUIs.get(player); }
    public UUID getGiftTarget(UUID player) { return giftTargets.get(player); }
    public void closeGUI(UUID player) { openGUIs.remove(player); giftTargets.remove(player); }
    public List<Integer> getItemSlots() { return menuConfigs.get("gift").getIntegerList("item-slots"); }
    public int getConfirmSlot() { return menuConfigs.get("gift").getInt("confirm.slot", 48); }
    public int getCancelSlot() { return menuConfigs.get("gift").getInt("cancel.slot", 50); }
}
