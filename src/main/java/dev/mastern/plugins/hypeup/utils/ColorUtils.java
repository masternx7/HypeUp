package dev.mastern.plugins.hypeup.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColorUtils {
    
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
    private static final Pattern MINI_HEX_PATTERN = Pattern.compile("<#([A-Fa-f0-9]{6})>");
    private static final Pattern SMALLCAPS_PATTERN = Pattern.compile("<smallcaps>(.*?)</smallcaps>", Pattern.CASE_INSENSITIVE);
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    
    private static final Map<Character, Character> SMALLCAPS_MAP = new HashMap<>();
    
    static {
        SMALLCAPS_MAP.put('a', 'ᴀ');
        SMALLCAPS_MAP.put('b', 'ʙ');
        SMALLCAPS_MAP.put('c', 'ᴄ');
        SMALLCAPS_MAP.put('d', 'ᴅ');
        SMALLCAPS_MAP.put('e', 'ᴇ');
        SMALLCAPS_MAP.put('f', 'ꜰ');
        SMALLCAPS_MAP.put('g', 'ɢ');
        SMALLCAPS_MAP.put('h', 'ʜ');
        SMALLCAPS_MAP.put('i', 'ɪ');
        SMALLCAPS_MAP.put('j', 'ᴊ');
        SMALLCAPS_MAP.put('k', 'ᴋ');
        SMALLCAPS_MAP.put('l', 'ʟ');
        SMALLCAPS_MAP.put('m', 'ᴍ');
        SMALLCAPS_MAP.put('n', 'ɴ');
        SMALLCAPS_MAP.put('o', 'ᴏ');
        SMALLCAPS_MAP.put('p', 'ᴘ');
        SMALLCAPS_MAP.put('q', 'ᴋ');
        SMALLCAPS_MAP.put('r', 'ʀ');
        SMALLCAPS_MAP.put('s', 'ꜱ');
        SMALLCAPS_MAP.put('t', 'ᴛ');
        SMALLCAPS_MAP.put('u', 'ᴜ');
        SMALLCAPS_MAP.put('v', 'ᴠ');
        SMALLCAPS_MAP.put('w', 'ᴡ');
        SMALLCAPS_MAP.put('x', 'x');
        SMALLCAPS_MAP.put('y', 'ʏ');
        SMALLCAPS_MAP.put('z', 'ᴢ');
    }
    
    private static String applySmallCaps(String text) {
        Matcher matcher = SMALLCAPS_PATTERN.matcher(text);
        StringBuffer result = new StringBuffer();
        
        while (matcher.find()) {
            String content = matcher.group(1);
            StringBuilder converted = new StringBuilder();
            
            for (char c : content.toCharArray()) {
                char lower = Character.toLowerCase(c);
                converted.append(SMALLCAPS_MAP.getOrDefault(lower, c));
            }
            
            matcher.appendReplacement(result, Matcher.quoteReplacement(converted.toString()));
        }
        matcher.appendTail(result);
        
        return result.toString();
    }
    
    public static Component colorize(String text) {
        if (text == null || text.isEmpty()) {
            return Component.empty();
        }
        
        text = applySmallCaps(text);
        
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
        
        text = applySmallCaps(text);
        
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
