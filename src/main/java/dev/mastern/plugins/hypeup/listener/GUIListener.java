package dev.mastern.plugins.hypeup.listener;

import dev.mastern.plugins.hypeup.HypeUp;
import dev.mastern.plugins.hypeup.gui.GUIManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

import java.util.List;

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
            
            if (shouldCancelClick(guiType, event.getSlot())) {
                event.setCancelled(true);
            }
        }
    }
    
    private void handleGUIClick(Player player, String guiType, int slot) {
        switch (guiType) {
            case "gift" -> handleGiftGUIClick(player, slot);
            case "list" -> handleListGUIClick(player, slot);
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
        // Implementation for list GUI clicks
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
