package network.soylu.book_api.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@SuppressWarnings("unused")
public final class ComponentSerializer {

    private static final Pattern COLOR_PATTERN = Pattern.compile("(?i)§([0-9A-FK-OR])");
    private static final Pattern HEX_PATTERN = Pattern.compile("(?i)§x§([0-9A-F])§([0-9A-F])§([0-9A-F])§([0-9A-F])§([0-9A-F])§([0-9A-F])");

    private final Gson gson;
    private final JsonParser jsonParser;

    public ComponentSerializer() {
        this.gson = new GsonBuilder().create();
        this.jsonParser = new JsonParser();
    }

    @NotNull
    public String legacyToJson(@NotNull String text) {
        if (text.isEmpty()) {
            return "{\"text\":\"\"}";
        }

        List<TextComponent> components = parseLegacyText(text);

        if (components.isEmpty()) {
            return "{\"text\":\"\"}";
        }

        if (components.size() == 1) {
            return gson.toJson(components.getFirst().toJson());
        }

        JsonObject root = new JsonObject();
        root.addProperty("text", "");
        JsonArray extra = new JsonArray();

        for (TextComponent component : components) {
            extra.add(component.toJson());
        }

        root.add("extra", extra);
        return gson.toJson(root);
    }

    @NotNull
    public String jsonToLegacy(@NotNull String json) {
        if (json.isEmpty()) {
            return "";
        }

        try {
            JsonElement element = jsonParser.parse(json);
            return parseJsonComponent(element);
        } catch (Exception e) {
            return json;
        }
    }

    @NotNull
    public String stripColors(@NotNull String text) {

        text = COLOR_PATTERN.matcher(text).replaceAll("");
        text = HEX_PATTERN.matcher(text).replaceAll("");

        return text;
    }

    @NotNull
    public String plainToJson(@NotNull String text) {
        JsonObject component = new JsonObject();
        component.addProperty("text", text);
        return gson.toJson(component);
    }

    @NotNull
    public String jsonToPlain(@NotNull String json) {
        return stripColors(jsonToLegacy(json));
    }

    public boolean isValidJson(@NotNull String json) {
        try {
            jsonParser.parse(json);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private List<TextComponent> parseLegacyText(@NotNull String text) {
        List<TextComponent> components = new ArrayList<>();

        if (text.isEmpty()) {
            return components;
        }

        TextComponent currentComponent = new TextComponent();
        StringBuilder currentText = new StringBuilder();

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            if (c == '§' && i + 1 < text.length()) {
                char code = text.charAt(i + 1);

                if (!currentText.isEmpty()) {
                    currentComponent.text = currentText.toString();
                    components.add(currentComponent);
                    currentComponent = currentComponent.copy();
                    currentText = new StringBuilder();
                }

                if (code == 'x' || code == 'X') {
                    String hexColor = extractHexColor(text, i);
                    if (hexColor != null) {
                        currentComponent.color = hexColor;
                        i += 13;
                        continue;
                    }
                }

                applyColorCode(currentComponent, code);
                i++;
            } else {
                currentText.append(c);
            }
        }

        if (!currentText.isEmpty()) {
            currentComponent.text = currentText.toString();
            components.add(currentComponent);
        }

        return components;
    }

    private String extractHexColor(@NotNull String text, int startIndex) {
        if (startIndex + 13 >= text.length()) {
            return null;
        }

        StringBuilder hex = new StringBuilder("#");
        int index = startIndex + 2;

        for (int i = 0; i < 6; i++) {
            if (index + 1 >= text.length() || text.charAt(index) != '§') {
                return null;
            }

            char hexChar = text.charAt(index + 1);
            if (!isHexChar(hexChar)) {
                return null;
            }

            hex.append(hexChar);
            index += 2;
        }

        return hex.toString();
    }

    private boolean isHexChar(char c) {
        return (c >= '0' && c <= '9') || (c >= 'A' && c <= 'F') || (c >= 'a' && c <= 'f');
    }

    private void applyColorCode(@NotNull TextComponent component, char code) {
        switch (Character.toLowerCase(code)) {
            case '0': component.color = "black"; break;
            case '1': component.color = "dark_blue"; break;
            case '2': component.color = "dark_green"; break;
            case '3': component.color = "dark_aqua"; break;
            case '4': component.color = "dark_red"; break;
            case '5': component.color = "dark_purple"; break;
            case '6': component.color = "gold"; break;
            case '7': component.color = "gray"; break;
            case '8': component.color = "dark_gray"; break;
            case '9': component.color = "blue"; break;
            case 'a': component.color = "green"; break;
            case 'b': component.color = "aqua"; break;
            case 'c': component.color = "red"; break;
            case 'd': component.color = "light_purple"; break;
            case 'e': component.color = "yellow"; break;
            case 'f': component.color = "white"; break;
            case 'k': component.obfuscated = true; break;
            case 'l': component.bold = true; break;
            case 'm': component.strikethrough = true; break;
            case 'n': component.underlined = true; break;
            case 'o': component.italic = true; break;
            case 'r':
                component.reset();
                break;
        }
    }

    private String parseJsonComponent(@NotNull JsonElement element) {
        if (element.isJsonPrimitive()) {
            return element.getAsString();
        }

        if (!element.isJsonObject()) {
            return "";
        }

        JsonObject obj = element.getAsJsonObject();
        StringBuilder result = new StringBuilder();

        if (obj.has("text")) {
            result.append(obj.get("text").getAsString());
        }

        if (obj.has("extra") && obj.get("extra").isJsonArray()) {
            JsonArray extra = obj.getAsJsonArray("extra");
            for (JsonElement extraElement : extra) {
                result.append(parseJsonComponent(extraElement));
            }
        }

        return result.toString();
    }

    private static class TextComponent {
        String text = "";
        String color = null;
        boolean bold = false;
        boolean italic = false;
        boolean underlined = false;
        boolean strikethrough = false;
        boolean obfuscated = false;

        void reset() {
            color = null;
            bold = false;
            italic = false;
            underlined = false;
            strikethrough = false;
            obfuscated = false;
        }

        TextComponent copy() {
            TextComponent copy = new TextComponent();
            copy.color = this.color;
            copy.bold = this.bold;
            copy.italic = this.italic;
            copy.underlined = this.underlined;
            copy.strikethrough = this.strikethrough;
            copy.obfuscated = this.obfuscated;
            return copy;
        }

        JsonObject toJson() {
            JsonObject json = new JsonObject();
            json.addProperty("text", text);

            if (color != null) {
                json.addProperty("color", color);
            }
            if (bold) {
                json.addProperty("bold", true);
            }
            if (italic) {
                json.addProperty("italic", true);
            }
            if (underlined) {
                json.addProperty("underlined", true);
            }
            if (strikethrough) {
                json.addProperty("strikethrough", true);
            }
            if (obfuscated) {
                json.addProperty("obfuscated", true);
            }

            return json;
        }
    }
}