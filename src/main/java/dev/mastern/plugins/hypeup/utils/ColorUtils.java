package dev.mastern.plugins.hypeup.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColorUtils {
    
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
    private static final Pattern MINI_HEX_PATTERN = Pattern.compile("<#([A-Fa-f0-9]{6})>");
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    
    public static Component colorize(String text) {
        if (text == null || text.isEmpty()) {
            return Component.empty();
        }
        
        Matcher matcher = HEX_PATTERN.matcher(text);
        text = matcher.replaceAll("<#$1>");
        
        text = text.replace("&0", "<black>")
                   .replace("&1", "<dark_blue>")
                   .replace("&2", "<dark_green>")
                   .replace("&3", "<dark_aqua>")
                   .replace("&4", "<dark_red>")
                   .replace("&5", "<dark_purple>")
                   .replace("&6", "<gold>")
                   .replace("&7", "<gray>")
                   .replace("&8", "<dark_gray>")
                   .replace("&9", "<blue>")
                   .replace("&a", "<green>")
                   .replace("&b", "<aqua>")
                   .replace("&c", "<red>")
                   .replace("&d", "<light_purple>")
                   .replace("&e", "<yellow>")
                   .replace("&f", "<white>")
                   .replace("&k", "<obfuscated>")
                   .replace("&l", "<bold>")
                   .replace("&m", "<strikethrough>")
                   .replace("&n", "<underlined>")
                   .replace("&o", "<italic>")
                   .replace("&r", "<reset>");
        
        return MINI_MESSAGE.deserialize(text);
    }
    
    public static String colorizeLegacy(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        
        Matcher matcher = HEX_PATTERN.matcher(text);
        StringBuffer buffer = new StringBuffer();
        
        while (matcher.find()) {
            String hex = matcher.group(1);
            matcher.appendReplacement(buffer, "§x§" + 
                hex.charAt(0) + "§" + hex.charAt(1) + "§" +
                hex.charAt(2) + "§" + hex.charAt(3) + "§" +
                hex.charAt(4) + "§" + hex.charAt(5));
        }
        matcher.appendTail(buffer);
        
        return buffer.toString().replace('&', '§');
    }
}
