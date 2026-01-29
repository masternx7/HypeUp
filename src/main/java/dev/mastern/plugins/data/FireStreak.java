package dev.mastern.plugins.data;

import java.time.LocalDateTime;
import java.util.UUID;

public class FireStreak {
    
    private final UUID player1;
    private final UUID player2;
    private int currentStreak;
    private int maxStreak;
    private LocalDateTime lastInteraction;
    private LocalDateTime lastFire;
    private int restoreCount;
    private boolean expired;
    private int chatProgress;
    private int shiftProgress;
    private boolean giftCompleted;
    private LocalDateTime lastChatTime;
    
    public FireStreak(UUID player1, UUID player2) {
        this.player1 = player1;
        this.player2 = player2;
        this.currentStreak = 0;
        this.maxStreak = 0;
        this.lastInteraction = LocalDateTime.now();
        this.lastFire = null;
        this.restoreCount = 0;
        this.expired = false;
        this.chatProgress = 0;
        this.shiftProgress = 0;
        this.giftCompleted = false;
        this.lastChatTime = null;
    }
    
    public UUID getPlayer1() {
        return player1;
    }
    
    public UUID getPlayer2() {
        return player2;
    }
    
    public int getCurrentStreak() {
        return currentStreak;
    }
    
    public void setCurrentStreak(int currentStreak) {
        this.currentStreak = currentStreak;
        if (currentStreak > maxStreak) {
            this.maxStreak = currentStreak;
        }
    }
    
    public int getMaxStreak() {
        return maxStreak;
    }
    
    public void setMaxStreak(int maxStreak) {
        this.maxStreak = maxStreak;
    }
    
    public LocalDateTime getLastInteraction() {
        return lastInteraction;
    }
    
    public void setLastInteraction(LocalDateTime lastInteraction) {
        this.lastInteraction = lastInteraction;
    }
    
    public LocalDateTime getLastFire() {
        return lastFire;
    }
    
    public void setLastFire(LocalDateTime lastFire) {
        this.lastFire = lastFire;
    }
    
    public int getRestoreCount() {
        return restoreCount;
    }
    
    public void setRestoreCount(int restoreCount) {
        this.restoreCount = restoreCount;
    }
    
    public boolean isExpired() {
        return expired;
    }
    
    public void setExpired(boolean expired) {
        this.expired = expired;
    }
    
    public int getChatProgress() {
        return chatProgress;
    }
    
    public void setChatProgress(int chatProgress) {
        this.chatProgress = chatProgress;
    }
    
    public int getShiftProgress() {
        return shiftProgress;
    }
    
    public void setShiftProgress(int shiftProgress) {
        this.shiftProgress = shiftProgress;
    }
    
    public boolean isGiftCompleted() {
        return giftCompleted;
    }
    
    public void setGiftCompleted(boolean giftCompleted) {
        this.giftCompleted = giftCompleted;
    }
    
    public LocalDateTime getLastChatTime() {
        return lastChatTime;
    }
    
    public void setLastChatTime(LocalDateTime lastChatTime) {
        this.lastChatTime = lastChatTime;
    }
    
    public boolean involves(UUID player) {
        return player1.equals(player) || player2.equals(player);
    }
    
    public UUID getPartner(UUID player) {
        if (player1.equals(player)) {
            return player2;
        } else if (player2.equals(player)) {
            return player1;
        }
        return null;
    }
    
    public void resetDailyProgress() {
        this.chatProgress = 0;
        this.shiftProgress = 0;
        this.giftCompleted = false;
        this.lastChatTime = null;
    }
    
    public boolean areMissionsCompleted(boolean chatEnabled, int minMessages,
                                       boolean shiftEnabled, int requiredShifts,
                                       boolean giftEnabled) {
        boolean chatDone = !chatEnabled || chatProgress >= minMessages;
        boolean shiftDone = !shiftEnabled || shiftProgress >= requiredShifts;
        boolean giftDone = !giftEnabled || giftCompleted;
        
        return chatDone && shiftDone && giftDone;
    }
}
