package dev.mastern.plugins.hypeup.gui.components;

import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public class SoundPlayer {
    
    public static void play(Player player, ConfigurationSection soundConfig) {
        if (soundConfig == null || !soundConfig.getBoolean("enabled", true)) return;
        
        try {
            Sound sound = Sound.valueOf(soundConfig.getString("sound", "UI_BUTTON_CLICK"));
            float volume = (float) soundConfig.getDouble("volume", 1.0);
            float pitch = (float) soundConfig.getDouble("pitch", 1.0);
            player.playSound(player.getLocation(), sound, volume, pitch);
        } catch (IllegalArgumentException ignored) {}
    }
}
