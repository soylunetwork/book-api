package network.soylu.book_api;

import network.soylu.book_api.book.Book;
import network.soylu.book_api.book.BookBuilder;
import network.soylu.book_api.nms.NMSHandler;
import network.soylu.book_api.util.ComponentSerializer;
import network.soylu.book_api.util.MiniMessageSerializer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Logger;

@SuppressWarnings("unused")
public final class BookAPI {

    private static BookAPI instance;
    private static Plugin plugin;
    private static Logger logger;
    private static NMSHandler nmsHandler;
    private static ComponentSerializer componentSerializer;
    private static MiniMessageSerializer miniMessageSerializer;

    private BookAPI() {}

    public static void initialize(@NotNull Plugin plugin) {
        if (instance != null) {
            throw new IllegalStateException("BookAPI has already been initialized!");
        }

        BookAPI.plugin = plugin;
        BookAPI.logger = plugin.getLogger();
        BookAPI.instance = new BookAPI();

        try {
            nmsHandler = NMSHandler.create();
            componentSerializer = new ComponentSerializer();
            miniMessageSerializer = new MiniMessageSerializer();
            logger.info("BookAPI v1.0.0 with MiniMessage support initialized successfully for " + nmsHandler.getVersion());
        } catch (Exception e) {
            logger.severe("Failed to initialize BookAPI: " + e.getMessage());
            throw new RuntimeException("Failed to initialize BookAPI", e);
        }
    }

    @NotNull
    public static BookAPI getInstance() {
        if (instance == null) {
            throw new IllegalStateException("BookAPI has not been initialized! Call BookAPI.initialize(plugin) first.");
        }
        return instance;
    }

    @NotNull
    public static BookBuilder builder() {
        return new BookBuilder();
    }

    @NotNull
    public static BookBuilder miniMessageBuilder() {
        return new BookBuilder().withMiniMessage();
    }

    public void openBook(@NotNull Player player, @NotNull Book book) {
        if (nmsHandler == null) {
            throw new IllegalStateException("NMS Handler not initialized");
        }

        try {
            nmsHandler.openBook(player, book);
        } catch (Exception e) {
            logger.severe("Failed to open book for player " + player.getName() + ": " + e.getMessage());
            throw new RuntimeException("Failed to open book", e);
        }
    }

    public void openBook(@NotNull Player player, @NotNull ItemStack bookItem) {
        Book book = fromItemStack(bookItem);
        openBook(player, book);
    }

    public void openMiniMessageBook(@NotNull Player player, @NotNull String title, @NotNull String author, @NotNull String... miniMessagePages) {
        BookBuilder builder = miniMessageBuilder()
                .title(title)
                .author(author);

        for (String page : miniMessagePages) {
            builder.addMiniMessagePage(page);
        }

        openBook(player, builder.build());
    }

    public void openMenuBook(@NotNull Player player, @NotNull String title, @NotNull String menuTitle, @NotNull String... options) {
        Book book = miniMessageBuilder()
                .title(title)
                .author("System")
                .addMenuPage(menuTitle, options)
                .build();

        openBook(player, book);
    }

    @NotNull
    public Book fromItemStack(@NotNull ItemStack itemStack) {
        if (!isBook(itemStack)) {
            throw new IllegalArgumentException("ItemStack is not a book!");
        }

        BookMeta meta = (BookMeta) itemStack.getItemMeta();
        BookBuilder builder = builder();

        assert meta != null;
        if (meta.hasTitle()) {
            builder.title(meta.getTitle());
        }

        if (meta.hasAuthor()) {
            builder.author(meta.getAuthor());
        }

        if (meta.hasPages()) {
            for (String page : meta.getPages()) {
                builder.addPage(page);
            }
        }

        return builder.build();
    }

    @NotNull
    public ItemStack toItemStack(@NotNull Book book) {
        return book.toItemStack();
    }

    public boolean isBook(@NotNull ItemStack itemStack) {
        return itemStack.getItemMeta() instanceof BookMeta;
    }

    @NotNull
    public String miniMessageToJson(@NotNull String miniMessageText) {
        if (miniMessageSerializer == null) {
            throw new IllegalStateException("MiniMessage serializer not initialized");
        }
        return miniMessageSerializer.miniMessageToJson(miniMessageText);
    }

    @NotNull
    public String jsonToMiniMessage(@NotNull String jsonComponent) {
        if (miniMessageSerializer == null) {
            throw new IllegalStateException("MiniMessage serializer not initialized");
        }
        return miniMessageSerializer.jsonToMiniMessage(jsonComponent);
    }

    @NotNull
    public String legacyToMiniMessage(@NotNull String legacyText) {
        if (miniMessageSerializer == null) {
            throw new IllegalStateException("MiniMessage serializer not initialized");
        }
        return miniMessageSerializer.legacyToMiniMessage(legacyText);
    }

    @NotNull
    public String createClickableCommand(@NotNull String text, @NotNull String command) {
        if (miniMessageSerializer == null) {
            throw new IllegalStateException("MiniMessage serializer not initialized");
        }
        return miniMessageSerializer.createClickableCommand(text, command);
    }

    @NotNull
    public String createHoverText(@NotNull String text, @NotNull String hoverText) {
        if (miniMessageSerializer == null) {
            throw new IllegalStateException("MiniMessage serializer not initialized");
        }
        return miniMessageSerializer.createHoverText(text, hoverText);
    }

    @NotNull
    public String createInteractiveText(@NotNull String text, @NotNull String clickAction,
                                        @NotNull String clickValue, @NotNull String hoverText) {
        if (miniMessageSerializer == null) {
            throw new IllegalStateException("MiniMessage serializer not initialized");
        }
        return miniMessageSerializer.createInteractiveText(text, clickAction, clickValue, hoverText);
    }

    @NotNull
    public Plugin getPlugin() {
        return plugin;
    }

    @NotNull
    public Logger getLogger() {
        return logger;
    }

    @NotNull
    public NMSHandler getNMSHandler() {
        return nmsHandler;
    }

    @NotNull
    public ComponentSerializer getComponentSerializer() {
        return componentSerializer;
    }

    @NotNull
    public MiniMessageSerializer getMiniMessageSerializer() {
        return miniMessageSerializer;
    }

    @NotNull
    public String getVersion() {
        return nmsHandler != null ? nmsHandler.getVersion() : "unknown";
    }
}