package dev.mastern.plugins.hypeup.data;

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
    
    private int chatProgress1;
    private int shiftProgress1;
    private boolean giftCompleted1;
    private LocalDateTime lastChatTime1;
    
    private int chatProgress2;
    private int shiftProgress2;
    private boolean giftCompleted2;
    private LocalDateTime lastChatTime2;
    
    private LocalDateTime lastResetDate;
    
    public FireStreak(UUID player1, UUID player2) {
        this.player1 = player1;
        this.player2 = player2;
        this.currentStreak = 0;
        this.maxStreak = 0;
        this.lastInteraction = LocalDateTime.now();
        this.lastFire = null;
        this.restoreCount = 0;
        this.expired = false;
        this.chatProgress1 = 0;
        this.shiftProgress1 = 0;
        this.giftCompleted1 = false;
        this.lastChatTime1 = null;
        this.chatProgress2 = 0;
        this.shiftProgress2 = 0;
        this.giftCompleted2 = false;
        this.lastChatTime2 = null;
        this.lastResetDate = null;
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
    
    public int getChatProgress(UUID player) {
        return player.equals(player1) ? chatProgress1 : chatProgress2;
    }
    
    public void setChatProgress(UUID player, int progress) {
        if (player.equals(player1)) {
            this.chatProgress1 = progress;
        } else {
            this.chatProgress2 = progress;
        }
    }
    
    public int getShiftProgress(UUID player) {
        return player.equals(player1) ? shiftProgress1 : shiftProgress2;
    }
    
    public void setShiftProgress(UUID player, int progress) {
        if (player.equals(player1)) {
            this.shiftProgress1 = progress;
        } else {
            this.shiftProgress2 = progress;
        }
    }
    
    public boolean isGiftCompleted(UUID player) {
        return player.equals(player1) ? giftCompleted1 : giftCompleted2;
    }
    
    public void setGiftCompleted(UUID player, boolean completed) {
        if (player.equals(player1)) {
            this.giftCompleted1 = completed;
        } else {
            this.giftCompleted2 = completed;
        }
    }
    
    public LocalDateTime getLastChatTime(UUID player) {
        return player.equals(player1) ? lastChatTime1 : lastChatTime2;
    }
    
    public void setLastChatTime(UUID player, LocalDateTime time) {
        if (player.equals(player1)) {
            this.lastChatTime1 = time;
        } else {
            this.lastChatTime2 = time;
        }
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
        this.chatProgress1 = 0;
        this.shiftProgress1 = 0;
        this.giftCompleted1 = false;
        this.lastChatTime1 = null;
        this.chatProgress2 = 0;
        this.shiftProgress2 = 0;
        this.giftCompleted2 = false;
        this.lastChatTime2 = null;
        this.lastResetDate = LocalDateTime.now();
    }
    
    public boolean areMissionsCompleted(boolean chatEnabled, int minMessages,
                                       boolean shiftEnabled, int requiredShifts,
                                       boolean giftEnabled) {
        boolean chat1Done = !chatEnabled || chatProgress1 >= minMessages;
        boolean shift1Done = !shiftEnabled || shiftProgress1 >= requiredShifts;
        boolean gift1Done = !giftEnabled || giftCompleted1;
        
        boolean chat2Done = !chatEnabled || chatProgress2 >= minMessages;
        boolean shift2Done = !shiftEnabled || shiftProgress2 >= requiredShifts;
        boolean gift2Done = !giftEnabled || giftCompleted2;
        
        return (chat1Done && shift1Done && gift1Done) && (chat2Done && shift2Done && gift2Done);
    }
    
    public int getChatProgress1() { return chatProgress1; }
    public int getShiftProgress1() { return shiftProgress1; }
    public boolean isGiftCompleted1() { return giftCompleted1; }
    public LocalDateTime getLastChatTime1() { return lastChatTime1; }
    
    public int getChatProgress2() { return chatProgress2; }
    public int getShiftProgress2() { return shiftProgress2; }
    public boolean isGiftCompleted2() { return giftCompleted2; }
    public LocalDateTime getLastChatTime2() { return lastChatTime2; }
    
    public void setChatProgress1(int progress) { this.chatProgress1 = progress; }
    public void setShiftProgress1(int progress) { this.shiftProgress1 = progress; }
    public void setGiftCompleted1(boolean completed) { this.giftCompleted1 = completed; }
    public void setLastChatTime1(LocalDateTime time) { this.lastChatTime1 = time; }
    
    public void setChatProgress2(int progress) { this.chatProgress2 = progress; }
    public void setShiftProgress2(int progress) { this.shiftProgress2 = progress; }
    public void setGiftCompleted2(boolean completed) { this.giftCompleted2 = completed; }
    public void setLastChatTime2(LocalDateTime time) { this.lastChatTime2 = time; }
    
    public LocalDateTime getLastResetDate() { return lastResetDate; }
    public void setLastResetDate(LocalDateTime date) { this.lastResetDate = date; }
}
