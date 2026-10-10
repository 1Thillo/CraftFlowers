package cm.ptks.craftflowers.util;

import cm.ptks.craftflowers.CraftFlowers;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.regex.Pattern;

/**
 * The language files and config use legacy formatting codes. This class turns those strings into
 * Adventure components at the point where they leave the plugin.
 */
public final class Text {

    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacySection();
    private static final Pattern AMPERSAND_CODE = Pattern.compile("&([0-9a-fk-orA-FK-OR])");

    /**
     * Converts {@code &} codes from the config into {@code §} codes, keeping trailing codes intact
     * so text appended later still inherits them.
     */
    public static String translateAmpersand(String text) {
        return AMPERSAND_CODE.matcher(text).replaceAll(LegacyComponentSerializer.SECTION_CHAR + "$1");
    }

    public static Component legacy(String text) {
        return LEGACY.deserialize(text);
    }

    /**
     * The chat prefix followed by the message. The message inherits the prefix's trailing color.
     */
    public static Component prefixed(String message) {
        return legacy(CraftFlowers.prefix + message);
    }

    /**
     * The chat prefix followed by the message, using {@code color} wherever the message itself sets none.
     */
    public static Component prefixed(String message, TextColor color) {
        return legacy(CraftFlowers.prefix).append(legacy(message).colorIfAbsent(color));
    }

    /**
     * Item names and lore render italic by default. Legacy names used to reset that with their first color code.
     */
    public static Component item(String text) {
        return legacy(text).decoration(TextDecoration.ITALIC, false);
    }

    private Text() {
    }
}
