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

@SuppressWarnings("unused")
public final class MiniMessageSerializer {

    private static final Pattern LEGACY_COLOR_PATTERN = Pattern.compile("(?i)§([0-9A-FK-OR])");
    private static final Pattern HEX_PATTERN = Pattern.compile("(?i)§x§([0-9A-F])§([0-9A-F])§([0-9A-F])§([0-9A-F])§([0-9A-F])§([0-9A-F])");

    private final MiniMessage miniMessage;
    private final GsonComponentSerializer gsonSerializer;
    private final LegacyComponentSerializer legacySerializer;
    private final JsonParser jsonParser;

    public MiniMessageSerializer() {
        this.miniMessage = MiniMessage.miniMessage();
        this.gsonSerializer = GsonComponentSerializer.gson();
        this.legacySerializer = LegacyComponentSerializer.legacySection();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        this.jsonParser = new JsonParser();
    }

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

    @NotNull
    public String createClickableCommand(@NotNull String text, @NotNull String command) {
        return "<click:run_command:/" + command + ">" + text + "</click>";
    }

    @NotNull
    public String createSuggestCommand(@NotNull String text, @NotNull String command) {
        return "<click:suggest_command:/" + command + ">" + text + "</click>";
    }

    @NotNull
    public String createClickableUrl(@NotNull String text, @NotNull String url) {
        return "<click:open_url:" + url + ">" + text + "</click>";
    }

    @NotNull
    public String createHoverText(@NotNull String text, @NotNull String hoverText) {
        return "<hover:show_text:'" + hoverText + "'>" + text + "</hover>";
    }

    @NotNull
    public String createInteractiveText(@NotNull String text, @NotNull String clickAction,
                                        @NotNull String clickValue, @NotNull String hoverText) {

        return "<hover:show_text:'" + hoverText + "'>" +

                "<click:" + clickAction + ":" + clickValue + ">" +

                text +

                "</click></hover>";
    }

    @NotNull
    public String createGradientText(@NotNull String text, @NotNull String startColor, @NotNull String endColor) {
        return "<gradient:" + startColor + ":" + endColor + ">" + text + "</gradient>";
    }

    @NotNull
    public String createRainbowText(@NotNull String text) {
        return "<rainbow>" + text + "</rainbow>";
    }

    public boolean isValidMiniMessage(@NotNull String miniMessageText) {
        try {
            miniMessage.deserialize(miniMessageText);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @NotNull
    public List<String> miniMessagePagesToJson(@NotNull List<String> miniMessagePages) {
        List<String> jsonPages = new ArrayList<>();

        for (String page : miniMessagePages) {
            jsonPages.add(miniMessageToJson(page));
        }

        return jsonPages;
    }

    @NotNull
    public List<String> jsonPagesToMiniMessage(@NotNull List<String> jsonPages) {
        List<String> miniMessagePages = new ArrayList<>();

        for (String page : jsonPages) {
            miniMessagePages.add(jsonToMiniMessage(page));
        }

        return miniMessagePages;
    }

    private String escapeJson(@NotNull String text) {
        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

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