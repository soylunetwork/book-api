package network.soylu.book_api.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for serializing and deserializing text using MiniMessage format.
 *
 * @author sheduxdev
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public final class MiniMessageSerializer {

    /** Pattern to match legacy Minecraft color codes. */
    private static final Pattern LEGACY_COLOR_PATTERN = Pattern.compile("(?i)§([0-9A-FK-OR])");

    /** Pattern to match legacy Minecraft hex color codes. */
    private static final Pattern HEX_PATTERN = Pattern.compile("(?i)§x§([0-9A-F])§([0-9A-F])§([0-9A-F])§([0-9A-F])§([0-9A-F])§([0-9A-F])");

    /** MiniMessage instance for parsing and serializing MiniMessage text. */
    private final MiniMessage miniMessage;

    /** Gson serializer for JSON components. */
    private final GsonComponentSerializer gsonSerializer;

    /** Legacy serializer for legacy Minecraft text. */
    private final LegacyComponentSerializer legacySerializer;

    /** JsonParser instance for parsing JSON strings. */
    private final JsonParser jsonParser;

    /**
     * Constructs a new MiniMessageSerializer instance.
     */
    public MiniMessageSerializer() {
        this.miniMessage = MiniMessage.miniMessage();
        this.gsonSerializer = GsonComponentSerializer.gson();
        this.legacySerializer = LegacyComponentSerializer.legacySection();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        this.jsonParser = new JsonParser();
    }

    /**
     * Converts MiniMessage text to JSON format.
     *
     * @param miniMessageText the MiniMessage text to convert
     * @return the JSON representation
     */
    @NotNull
    public String miniMessageToJson(@NotNull String miniMessageText) {
        if (miniMessageText.isEmpty()) {
            return "{\"text\":\"\"}";
        }

        try {
            Component component = miniMessage.deserialize(miniMessageText);
            return gsonSerializer.serialize(component);
        } catch (Exception e) {
            return "{\"text\":\"" + escapeJson(miniMessageText) + "\"}";
        }
    }

    /**
     * Converts JSON text to MiniMessage format.
     *
     * @param jsonComponent the JSON component to convert
     * @return the MiniMessage representation
     */
    @NotNull
    public String jsonToMiniMessage(@NotNull String jsonComponent) {
        if (jsonComponent.isEmpty()) {
            return "";
        }

        try {
            Component component = gsonSerializer.deserialize(jsonComponent);
            return miniMessage.serialize(component);
        } catch (Exception e) {
            return extractPlainTextFromJson(jsonComponent);
        }
    }

    /**
     * Converts legacy Minecraft text to MiniMessage format.
     *
     * @param legacyText the legacy text to convert
     * @return the MiniMessage representation
     */
    @NotNull
    public String legacyToMiniMessage(@NotNull String legacyText) {
        if (legacyText.isEmpty()) {
            return "";
        }

        try {
            Component component = legacySerializer.deserialize(legacyText);
            return miniMessage.serialize(component);
        } catch (Exception e) {
            return convertLegacyToMiniMessageManually(legacyText);
        }
    }

    /**
     * Converts MiniMessage text to legacy Minecraft text.
     *
     * @param miniMessageText the MiniMessage text to convert
     * @return the legacy Minecraft text
     */
    @NotNull
    public String miniMessageToLegacy(@NotNull String miniMessageText) {
        if (miniMessageText.isEmpty()) {
            return "";
        }

        try {
            Component component = miniMessage.deserialize(miniMessageText);
            return legacySerializer.serialize(component);
        } catch (Exception e) {
            return stripMiniMessageTags(miniMessageText);
        }
    }

    /**
     * Strips MiniMessage tags from text, returning plain text.
     *
     * @param miniMessageText the MiniMessage text to strip
     * @return the plain text
     */
    @NotNull
    public String stripMiniMessageTags(@NotNull String miniMessageText) {
        if (miniMessageText.isEmpty()) {
            return "";
        }

        try {
            Component component = miniMessage.deserialize(miniMessageText);
            return component.toString();
        } catch (Exception e) {
            return miniMessageText.replaceAll("<[^>]*>", "");
        }
    }

    /**
     * Creates clickable command text in MiniMessage format.
     *
     * @param text the display text
     * @param command the command to execute
     * @return the MiniMessage formatted clickable text
     */
    @NotNull
    public String createClickableCommand(@NotNull String text, @NotNull String command) {
        return "<click:run_command:/" + command + ">" + text + "</click>";
    }

    /**
     * Creates suggest command text in MiniMessage format.
     *
     * @param text the display text
     * @param command the command to suggest
     * @return the MiniMessage formatted suggest text
     */
    @NotNull
    public String createSuggestCommand(@NotNull String text, @NotNull String command) {
        return "<click:suggest_command:/" + command + ">" + text + "</click>";
    }

    /**
     * Creates clickable URL text in MiniMessage format.
     *
     * @param text the display text
     * @param url the URL to open
     * @return the MiniMessage formatted clickable URL text
     */
    @NotNull
    public String createClickableUrl(@NotNull String text, @NotNull String url) {
        return "<click:open_url:" + url + ">" + text + "</click>";
    }

    /**
     * Creates hover text in MiniMessage format.
     *
     * @param text the display text
     * @param hoverText the hover text to display
     * @return the MiniMessage formatted hover text
     */
    @NotNull
    public String createHoverText(@NotNull String text, @NotNull String hoverText) {
        return "<hover:show_text:'" + hoverText + "'>" + text + "</hover>";
    }

    /**
     * Creates interactive text with click and hover actions in MiniMessage format.
     *
     * @param text the display text
     * @param clickAction the click action type
     * @param clickValue the click action value
     * @param hoverText the hover text to display
     * @return the MiniMessage formatted interactive text
     */
    @NotNull
    public String createInteractiveText(@NotNull String text, @NotNull String clickAction,
                                        @NotNull String clickValue, @NotNull String hoverText) {
        return "<hover:show_text:'" + hoverText + "'>" +
                "<click:" + clickAction + ":" + clickValue + ">" +
                text +
                "</click></hover>";
    }

    /**
     * Creates gradient text in MiniMessage format.
     *
     * @param text the text to apply the gradient to
     * @param startColor the starting color (hex or name)
     * @param endColor the ending color (hex or name)
     * @return the MiniMessage formatted gradient text
     */
    @NotNull
    public String createGradientText(@NotNull String text, @NotNull String startColor, @NotNull String endColor) {
        return "<gradient:" + startColor + ":" + endColor + ">" + text + "</gradient>";
    }

    /**
     * Creates rainbow text in MiniMessage format.
     *
     * @param text the text to apply rainbow effect to
     * @return the MiniMessage formatted rainbow text
     */
    @NotNull
    public String createRainbowText(@NotNull String text) {
        return "<rainbow>" + text + "</rainbow>";
    }

    /**
     * Validates if the provided text is valid MiniMessage format.
     *
     * @param miniMessageText the MiniMessage text to validate
     * @return true if valid, false otherwise
     */
    public boolean isValidMiniMessage(@NotNull String miniMessageText) {
        try {
            miniMessage.deserialize(miniMessageText);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Converts a list of MiniMessage pages to JSON format.
     *
     * @param miniMessagePages the list of MiniMessage pages
     * @return a list of JSON pages
     */
    @NotNull
    public List<String> miniMessagePagesToJson(@NotNull List<String> miniMessagePages) {
        List<String> jsonPages = new ArrayList<>();
        for (String page : miniMessagePages) {
            jsonPages.add(miniMessageToJson(page));
        }
        return jsonPages;
    }

    /**
     * Converts a list of JSON pages to MiniMessage format.
     *
     * @param jsonPages the list of JSON pages
     * @return a list of MiniMessage pages
     */
    @NotNull
    public List<String> jsonPagesToMiniMessage(@NotNull List<String> jsonPages) {
        List<String> miniMessagePages = new ArrayList<>();
        for (String page : jsonPages) {
            miniMessagePages.add(jsonToMiniMessage(page));
        }
        return miniMessagePages;
    }

    /**
     * Escapes special characters in text for JSON compatibility.
     *
     * @param text the text to escape
     * @return the escaped text
     */
    private String escapeJson(@NotNull String text) {
        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    /**
     * Extracts plain text from a JSON component.
     *
     * @param json the JSON component
     * @return the plain text
     */
    private String extractPlainTextFromJson(@NotNull String json) {
        try {
            JsonElement element = jsonParser.parse(json);
            if (element.isJsonObject() && element.getAsJsonObject().has("text")) {
                return element.getAsJsonObject().get("text").getAsString();
            }
        } catch (Exception ignored) {
        }
        return json;
    }

    /**
     * Manually converts legacy Minecraft text to MiniMessage format.
     *
     * @param legacyText the legacy text to convert
     * @return the MiniMessage representation
     */
    private String convertLegacyToMiniMessageManually(@NotNull String legacyText) {
        String result = legacyText;

        result = result.replace("§0", "<black>");
        result = result.replace("§1", "<dark_blue>");
        result = result.replace("§2", "<dark_green>");
        result = result.replace("§3", "<dark_aqua>");
        result = result.replace("§4", "<dark_red>");
        result = result.replace("§5", "<dark_purple>");
        result = result.replace("§6", "<gold>");
        result = result.replace("§7", "<gray>");
        result = result.replace("§8", "<dark_gray>");
        result = result.replace("§9", "<blue>");
        result = result.replace("§a", "<green>");
        result = result.replace("§b", "<aqua>");
        result = result.replace("§c", "<red>");
        result = result.replace("§d", "<light_purple>");
        result = result.replace("§e", "<yellow>");
        result = result.replace("§f", "<white>");

        result = result.replace("§k", "<obfuscated>");
        result = result.replace("§l", "<bold>");
        result = result.replace("§m", "<strikethrough>");
        result = result.replace("§n", "<underlined>");
        result = result.replace("§o", "<italic>");
        result = result.replace("§r", "<reset>");

        Matcher hexMatcher = HEX_PATTERN.matcher(result);
        result = hexMatcher.replaceAll(matchResult -> {
            StringBuilder hex = new StringBuilder("#");
            for (int i = 1; i <= 6; i++) {
                hex.append(matchResult.group(i));
            }
            return "<color:" + hex + ">";
        });

        return result;
    }
}