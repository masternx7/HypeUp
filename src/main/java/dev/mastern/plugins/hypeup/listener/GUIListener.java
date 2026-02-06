package dev.mastern.plugins.hypeup.listener;

import dev.mastern.plugins.hypeup.HypeUp;
import dev.mastern.plugins.hypeup.data.FireStreak;
import dev.mastern.plugins.hypeup.gui.GUIManager;
import dev.mastern.plugins.hypeup.utils.SchedulerUtil;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

import java.util.List;
import java.util.UUID;

public class GUIListener implements Listener {
    
    private final HypeUp plugin;
    private final GUIManager guiManager;
    
    public GUIListener(HypeUp plugin, GUIManager guiManager) {
        this.plugin = plugin;
        this.guiManager = guiManager;
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        
        String guiType = guiManager.getOpenGUI(player.getUniqueId());
        if (guiType == null) return;
        
        Inventory clickedInv = event.getClickedInventory();
        if (clickedInv == null) return;
        
        if (clickedInv.equals(event.getView().getTopInventory())) {
            handleGUIClick(player, guiType, event.getSlot());
            
            if (guiType.equals("gift")) {
                guiManager.updateGiftConfirmButton(player);
            }
            
            if (shouldCancelClick(guiType, event.getSlot())) {
                event.setCancelled(true);
            }
        }
    }
    
    private void handleGUIClick(Player player, String guiType, int slot) {
        switch (guiType) {
            case "gift" -> handleGiftGUIClick(player, slot);
            case "list" -> handleListGUIClick(player, slot);
            case "info" -> handleInfoGUIClick(player, slot);
        }
    }
    
    private void handleGiftGUIClick(Player player, int slot) {
        int confirmSlot = guiManager.getConfirmSlot();
        int cancelSlot = guiManager.getCancelSlot();
        
        if (slot == confirmSlot) {
            guiManager.confirmGift(player);
        } else if (slot == cancelSlot) {
            player.closeInventory();
        }
    }
    
    private void handleListGUIClick(Player player, int slot) {
        FileConfiguration listConfig = guiManager.getMenuConfig("list");
        if (listConfig == null) return;
        
        int closeSlot = listConfig.getInt("close.slot", 49);
        
        if (slot == closeSlot) {
            player.closeInventory();
            guiManager.playSoundFromConfig(player, listConfig.getConfigurationSection("close"));
            return;
        }
        
        List<Integer> partnerSlots = listConfig.getIntegerList("partner-slots");
        int index = partnerSlots.indexOf(slot);
        
        if (index >= 0) {
            List<FireStreak> allStreaks = plugin.getFireStreakManager().getPlayerStreaks(player.getUniqueId());
            List<FireStreak> activeStreaks = allStreaks.stream()
                .filter(s -> s.getCurrentStreak() > 0)
                .toList();
            if (index < activeStreaks.size()) {
                FireStreak streak = activeStreaks.get(index);
                UUID partnerId = streak.getPartner(player.getUniqueId());
                Player partner = Bukkit.getPlayer(partnerId);
                
                if (partner != null && partner.isOnline()) {
                    guiManager.openInfoGUI(player, partner);
                    guiManager.playSoundFromConfig(player, listConfig.getConfigurationSection("partner-item"));
                } else {
                    String partnerName = Bukkit.getOfflinePlayer(partnerId).getName();
                    if (partnerName == null) partnerName = "Unknown";
                    plugin.getMessageManager().sendMessage(player, "general.player-not-found", 
                        java.util.Map.of("player", partnerName));
                }
            }
        }
    }
    
    private void handleInfoGUIClick(Player player, int slot) {
        FileConfiguration infoConfig = guiManager.getMenuConfig("info");
        if (infoConfig == null) return;
        
        UUID targetUUID = guiManager.getGiftTarget(player.getUniqueId());
        if (targetUUID == null) return;
        
        Player target = Bukkit.getPlayer(targetUUID);
        if (target == null || !target.isOnline()) return;
        
        int sendGiftSlot = infoConfig.getInt("send-gift.slot", 22);
        int backSlot = infoConfig.getInt("back.slot", 18);
        int closeSlot = infoConfig.getInt("close.slot", 26);
        
        if (slot == sendGiftSlot) {
            guiManager.openGiftGUI(player, target);
            guiManager.playSoundFromConfig(player, infoConfig.getConfigurationSection("send-gift"));
        } else if (slot == backSlot) {
            guiManager.openListGUI(player);
            guiManager.playSoundFromConfig(player, infoConfig.getConfigurationSection("back"));
        } else if (slot == closeSlot) {
            player.closeInventory();
            guiManager.playSoundFromConfig(player, infoConfig.getConfigurationSection("close"));
        }
    }
    
    private boolean shouldCancelClick(String guiType, int slot) {
        if (guiType.equals("gift")) {
            List<Integer> itemSlots = guiManager.getItemSlots();
            return !itemSlots.contains(slot);
        }
        
        return true;
    }
    
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        
        String guiType = guiManager.getOpenGUI(player.getUniqueId());
        if (guiType != null) {
            guiManager.closeGUI(player.getUniqueId());
        }
    }
}
