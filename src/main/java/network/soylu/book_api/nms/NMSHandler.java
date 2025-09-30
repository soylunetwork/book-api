package network.soylu.book_api.nms;

import network.soylu.book_api.BookApiException;
import network.soylu.book_api.book.Book;
import network.soylu.book_api.util.ReflectionUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.logging.Logger;

/**
 * Abstract base class for handling NMS (Net Minecraft Server) operations to open books for players.
 * Provides version-specific implementations for different Minecraft server versions.
 *
 * @author sheduxdev
 * @since 1.0.0
 */
public abstract class NMSHandler {

    /** Logger instance for logging NMS-related messages. */
    protected static final Logger LOGGER = Logger.getLogger(NMSHandler.class.getName());

    /** The Minecraft server version this handler supports. */
    protected final String version;

    /**
     * Constructs an NMSHandler with the specified server version.
     *
     * @param version the Minecraft server version
     * @throws IllegalArgumentException if version is null
     */
    protected NMSHandler(@NotNull String version) {
        this.version = version;
    }

    /**
     * Creates an appropriate NMSHandler instance based on the server version.
     *
     * @return a new NMSHandler instance
     * @throws Exception if the server version is unsupported or initialization fails
     */
    @NotNull
    public static NMSHandler create() throws Exception {
        String version = ReflectionUtil.getServerVersion();

        if (ReflectionUtil.isVersionAtLeast(1, 21, 4)) {
            return new ModernNMSHandler(version);
        } else if (ReflectionUtil.isVersionAtLeast(1, 20, 0)) {
            return new LegacyNMSHandler(version);
        } else if (ReflectionUtil.isVersionAtLeast(1, 8, 0)) {
            return new LegacyNMSHandler(version); // 1.8-1.19 support
        } else {
            throw new BookApiException("Unsupported Minecraft version: " + version);
        }
    }

    /**
     * Opens a book for the specified player.
     *
     * @param player the player to open the book for
     * @param book the book to open
     * @throws Exception if the operation fails
     */
    public abstract void openBook(@NotNull Player player, @NotNull Book book) throws Exception;

    /**
     * Gets the Minecraft server version this handler supports.
     *
     * @return the server version
     */
    @NotNull
    public String getVersion() {
        return version;
    }

    /**
     * Handler for modern Minecraft versions (1.21.4 and above).
     */
    private static class ModernNMSHandler extends NMSHandler {

        private final Method sendPacketMethod;
        private final Constructor<?> openBookPacketConstructor;
        private final Method getHandleMethod;
        private final Field connectionField;

        /**
         * Constructs a ModernNMSHandler for modern Minecraft versions.
         *
         * @param version the Minecraft server version
         * @throws Exception if reflection-based initialization fails
         */
        public ModernNMSHandler(@NotNull String version) throws Exception {
            super(version);

            Class<?> craftPlayerClass = ReflectionUtil.getCraftBukkitClass("entity.CraftPlayer");
            Class<?> entityPlayerClass = ReflectionUtil.getMinecraftClass("server.level.EntityPlayer");
            Class<?> playerConnectionClass = ReflectionUtil.getMinecraftClass("server.network.PlayerConnection");
            Class<?> packetClass = ReflectionUtil.getMinecraftClass("network.protocol.Packet");
            Class<?> openBookPacketClass = ReflectionUtil.getMinecraftClass("network.protocol.game.PacketPlayOutOpenBook");
            Class<?> enumHandClass = ReflectionUtil.getMinecraftClass("world.EnumHand");

            this.getHandleMethod = ReflectionUtil.getMethod(craftPlayerClass, "getHandle");
            this.connectionField = ReflectionUtil.getField(entityPlayerClass, "c");
            this.sendPacketMethod = ReflectionUtil.getMethod(playerConnectionClass, "a", packetClass);
            this.openBookPacketConstructor = ReflectionUtil.getConstructor(openBookPacketClass, enumHandClass);
        }

        /**
         * Opens a book for the specified player by temporarily setting the book in their main hand
         * and sending an open book packet.
         *
         * @param player the player to open the book for
         * @param book the book to open
         * @throws Exception if the operation fails
         */
        @Override
        public void openBook(@NotNull Player player, @NotNull Book book) throws Exception {
            PlayerInventory inventory = player.getInventory();
            ItemStack originalMainHand = inventory.getItemInMainHand();
            ItemStack originalOffHand = inventory.getItemInOffHand();

            try {
                inventory.setItemInMainHand(book.toItemStack());

                Class<?> enumHandClass = ReflectionUtil.getMinecraftClass("world.EnumHand");
                Object mainHand = ReflectionUtil.getEnumValue(enumHandClass, "MAIN_HAND");

                Object packet = openBookPacketConstructor.newInstance(mainHand);
                sendPacket(player, packet);

                Bukkit.getScheduler().runTaskLater(
                        Bukkit.getPluginManager().getPlugins()[0],
                        () -> {
                            inventory.setItemInMainHand(originalMainHand);
                            inventory.setItemInOffHand(originalOffHand);
                        },
                        1L
                );

            } catch (Exception e) {
                inventory.setItemInMainHand(originalMainHand);
                inventory.setItemInOffHand(originalOffHand);
                throw e;
            }
        }

        /**
         * Sends a packet to the specified player.
         *
         * @param player the player to send the packet to
         * @param packet the packet to send
         * @throws Exception if the packet sending fails
         */
        private void sendPacket(@NotNull Player player, @NotNull Object packet) throws Exception {
            Object entityPlayer = getHandleMethod.invoke(player);
            Object connection = connectionField.get(entityPlayer);
            sendPacketMethod.invoke(connection, packet);
        }
    }

    /**
     * Handler for legacy Minecraft versions (1.8 to 1.20).
     */
    private static class LegacyNMSHandler extends NMSHandler {

        private final Method sendPacketMethod;
        private final Constructor<?> openBookPacketConstructor;
        private final Method getHandleMethod;
        private final Field connectionField;

        /**
         * Constructs a LegacyNMSHandler for legacy Minecraft versions.
         *
         * @param version the Minecraft server version
         * @throws Exception if reflection-based initialization fails
         */
        public LegacyNMSHandler(@NotNull String version) throws Exception {
            super(version);

            String versionString = ReflectionUtil.getServerVersion();

            Class<?> craftPlayerClass = ReflectionUtil.getCraftBukkitClass("entity.CraftPlayer");
            Class<?> entityPlayerClass = ReflectionUtil.getNMSClass("EntityPlayer");
            Class<?> playerConnectionClass = ReflectionUtil.getNMSClass("PlayerConnection");
            Class<?> packetClass = ReflectionUtil.getNMSClass("Packet");
            Class<?> openBookPacketClass = ReflectionUtil.getNMSClass("PacketPlayOutOpenBook");
            Class<?> enumHandClass = ReflectionUtil.getNMSClass("EnumHand");

            this.getHandleMethod = ReflectionUtil.getMethod(craftPlayerClass, "getHandle");
            this.connectionField = ReflectionUtil.getField(entityPlayerClass, "playerConnection");
            this.sendPacketMethod = ReflectionUtil.getMethod(playerConnectionClass, "sendPacket", packetClass);
            this.openBookPacketConstructor = ReflectionUtil.getConstructor(openBookPacketClass, enumHandClass);
        }

        /**
         * Opens a book for the specified player by temporarily setting the book in their main hand
         * and sending an open book packet.
         *
         * @param player the player to open the book for
         * @param book the book to open
         * @throws Exception if the operation fails
         */
        @Override
        public void openBook(@NotNull Player player, @NotNull Book book) throws Exception {
            PlayerInventory inventory = player.getInventory();
            ItemStack originalMainHand = inventory.getItemInMainHand();
            ItemStack originalOffHand = inventory.getItemInOffHand();

            try {
                inventory.setItemInMainHand(book.toItemStack());

                Class<?> enumHandClass = ReflectionUtil.getNMSClass("EnumHand");
                Object mainHand = ReflectionUtil.getEnumValue(enumHandClass, "MAIN_HAND");

                Object packet = openBookPacketConstructor.newInstance(mainHand);
                sendPacket(player, packet);

                Bukkit.getScheduler().runTaskLater(
                        Bukkit.getPluginManager().getPlugins()[0],
                        () -> {
                            inventory.setItemInMainHand(originalMainHand);
                            inventory.setItemInOffHand(originalOffHand);
                        },
                        1L
                );

            } catch (Exception e) {
                inventory.setItemInMainHand(originalMainHand);
                inventory.setItemInOffHand(originalOffHand);
                throw e;
            }
        }

        /**
         * Sends a packet to the specified player.
         *
         * @param player the player to send the packet to
         * @param packet the packet to send
         * @throws Exception if the packet sending fails
         */
        private void sendPacket(@NotNull Player player, @NotNull Object packet) throws Exception {
            Object entityPlayer = getHandleMethod.invoke(player);
            Object connection = connectionField.get(entityPlayer);
            sendPacketMethod.invoke(connection, packet);
        }
    }
}