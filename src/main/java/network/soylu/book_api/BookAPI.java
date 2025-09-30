package network.soylu.book_api;

import network.soylu.book_api.book.Book;
import network.soylu.book_api.book.BookBuilder;
import network.soylu.book_api.nms.NMSHandler;
import network.soylu.book_api.util.ComponentSerializer;
import network.soylu.book_api.util.MiniMessageSerializer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

/**
 * Main API class for creating and managing books in Minecraft.
 * Supports versions 1.8 to 1.21.8.
 *
 * @author sheduxdev
 */
public final class BookAPI {

    private static BookAPI instance;
    private final Plugin plugin;
    private final NMSHandler nmsHandler;
    private final ComponentSerializer componentSerializer;
    private final MiniMessageSerializer miniMessageSerializer;

    /**
     * Private constructor. Use {@link #create(Plugin)} to get an instance.
     */
    private BookAPI(@NotNull Plugin plugin) {
        this.plugin = plugin;

        try {
            this.nmsHandler = NMSHandler.create();
            this.componentSerializer = new ComponentSerializer();
            this.miniMessageSerializer = new MiniMessageSerializer();
        } catch (Exception e) {
            throw new BookApiException("Failed to initialize BookAPI", e);
        }
    }

    /**
     * Creates a new BookAPI instance.
     *
     * @param plugin The plugin instance
     * @return New BookAPI instance
     * @throws BookApiException if initialization fails
     */
    @NotNull
    public static BookAPI create(@NotNull Plugin plugin) {
        if (instance != null) {
            throw new BookApiException("BookAPI instance already exists!");
        }

        instance = new BookAPI(plugin);
        plugin.getLogger().info("BookAPI initialized successfully for " + instance.getVersion());
        return instance;
    }

    /**
     * Gets the BookAPI instance.
     *
     * @return The BookAPI instance
     * @throws BookApiException if not initialized
     */
    @NotNull
    public static BookAPI getInstance() {
        if (instance == null) {
            throw new BookApiException("BookAPI has not been initialized! Call BookAPI.create(plugin) first.");
        }
        return instance;
    }

    /**
     * Creates a new BookBuilder instance.
     *
     * @return New BookBuilder
     */
    @NotNull
    public static BookBuilder builder() {
        return new BookBuilder();
    }

    /**
     * Creates a new BookBuilder instance with MiniMessage support.
     *
     * @return New BookBuilder with MiniMessage enabled
     */
    @NotNull
    public static BookBuilder miniMessageBuilder() {
        return new BookBuilder().withMiniMessage();
    }

    /**
     * Opens a book for a player.
     *
     * @param player The player to open the book for
     * @param book The book to open
     * @throws BookApiException if opening fails
     */
    public void openBook(@NotNull Player player, @NotNull Book book) {
        try {
            nmsHandler.openBook(player, book);
        } catch (Exception e) {
            throw new BookApiException("Failed to open book for player " + player.getName(), e);
        }
    }

    /**
     * Opens a book item for a player.
     *
     * @param player The player to open the book for
     * @param bookItem The book item to open
     * @throws BookApiException if opening fails
     */
    public void openBook(@NotNull Player player, @NotNull ItemStack bookItem) {
        Book book = fromItemStack(bookItem);
        openBook(player, book);
    }

    /**
     * Opens a MiniMessage formatted book for a player.
     *
     * @param player The player to open the book for
     * @param title The book title
     * @param author The book author
     * @param miniMessagePages The MiniMessage formatted pages
     * @throws BookApiException if opening fails
     */
    public void openMiniMessageBook(@NotNull Player player, @NotNull String title,
                                    @NotNull String author, @NotNull String... miniMessagePages) {
        BookBuilder builder = miniMessageBuilder()
                .title(title)
                .author(author);

        for (String page : miniMessagePages) {
            builder.addMiniMessagePage(page);
        }

        openBook(player, builder.build());
    }

    /**
     * Opens a menu book with clickable options.
     *
     * @param player The player to open the book for
     * @param title The book title
     * @param menuTitle The menu title
     * @param options The menu options in "display:command" format
     * @throws BookApiException if opening fails
     */
    public void openMenuBook(@NotNull Player player, @NotNull String title,
                             @NotNull String menuTitle, @NotNull String... options) {
        Book book = miniMessageBuilder()
                .title(title)
                .author("System")
                .addMiniMessagePage(buildMenuPage(menuTitle, options))
                .build();

        openBook(player, book);
    }

    /**
     * Turns a menu title and an array of "display:command" entries into one MiniMessage page.
     */
    @NotNull
    private String buildMenuPage(@NotNull String title, @NotNull String... options) {
        MiniMessageSerializer mms = new MiniMessageSerializer();
        StringBuilder page = new StringBuilder(title).append("\n\n");

        for (String opt : options) {
            String[] parts = opt.split(":", 2);
            if (parts.length != 2) continue;          // skip malformed entries
            String display = parts[0];
            String command = parts[1];

            // <click:run_command:/command>display</click>
            page.append(mms.createClickableCommand(display, command)).append("\n");
        }
        return page.toString();
    }

    /**
     * Converts an ItemStack to a Book instance.
     *
     * @param itemStack The ItemStack to convert
     * @return Book instance
     * @throws BookApiException if conversion fails
     */
    @NotNull
    public Book fromItemStack(@NotNull ItemStack itemStack) {
        if (!isBook(itemStack)) {
            throw new BookApiException("ItemStack is not a book!");
        }

        return Book.fromItemStack(itemStack);
    }

    /**
     * Converts a Book to an ItemStack.
     *
     * @param book The book to convert
     * @return ItemStack representation
     */
    @NotNull
    public ItemStack toItemStack(@NotNull Book book) {
        return book.toItemStack();
    }

    /**
     * Checks if an ItemStack is a book.
     *
     * @param itemStack The ItemStack to check
     * @return true if the ItemStack is a book
     */
    public boolean isBook(@NotNull ItemStack itemStack) {
        return Book.isBook(itemStack);
    }

    // MiniMessage utility methods
    /**
     * Converts MiniMessage text to JSON.
     *
     * @param miniMessageText The MiniMessage text to convert
     * @return JSON representation
     */
    @NotNull
    public String miniMessageToJson(@NotNull String miniMessageText) {
        return miniMessageSerializer.miniMessageToJson(miniMessageText);
    }

    /**
     * Converts JSON to MiniMessage text.
     *
     * @param jsonComponent The JSON to convert
     * @return MiniMessage representation
     */
    @NotNull
    public String jsonToMiniMessage(@NotNull String jsonComponent) {
        return miniMessageSerializer.jsonToMiniMessage(jsonComponent);
    }

    /**
     * Converts legacy text to MiniMessage.
     *
     * @param legacyText The legacy text to convert
     * @return MiniMessage representation
     */
    @NotNull
    public String legacyToMiniMessage(@NotNull String legacyText) {
        return miniMessageSerializer.legacyToMiniMessage(legacyText);
    }

    /**
     * Creates clickable command text.
     *
     * @param text The display text
     * @param command The command to execute
     * @return MiniMessage formatted text
     */
    @NotNull
    public String createClickableCommand(@NotNull String text, @NotNull String command) {
        return miniMessageSerializer.createClickableCommand(text, command);
    }

    /**
     * Creates hover text.
     *
     * @param text The display text
     * @param hoverText The hover text
     * @return MiniMessage formatted text
     */
    @NotNull
    public String createHoverText(@NotNull String text, @NotNull String hoverText) {
        return miniMessageSerializer.createHoverText(text, hoverText);
    }

    /**
     * Creates interactive text with click and hover actions.
     *
     * @param text The display text
     * @param clickAction The click action type
     * @param clickValue The click value
     * @param hoverText The hover text
     * @return MiniMessage formatted text
     */
    @NotNull
    public String createInteractiveText(@NotNull String text, @NotNull String clickAction,
                                        @NotNull String clickValue, @NotNull String hoverText) {
        return miniMessageSerializer.createInteractiveText(text, clickAction, clickValue, hoverText);
    }

    /**
     * Gets the plugin instance.
     *
     * @return The plugin instance
     */
    @NotNull
    public Plugin getPlugin() {
        return plugin;
    }

    /**
     * Gets the NMS handler.
     *
     * @return The NMS handler
     */
    @NotNull
    public NMSHandler getNMSHandler() {
        return nmsHandler;
    }

    /**
     * Gets the component serializer.
     *
     * @return The component serializer
     */
    @NotNull
    public ComponentSerializer getComponentSerializer() {
        return componentSerializer;
    }

    /**
     * Gets the MiniMessage serializer.
     *
     * @return The MiniMessage serializer
     */
    @NotNull
    public MiniMessageSerializer getMiniMessageSerializer() {
        return miniMessageSerializer;
    }

    /**
     * Gets the Minecraft version.
     *
     * @return The Minecraft version
     */
    @NotNull
    public String getVersion() {
        return nmsHandler.getVersion();
    }
}