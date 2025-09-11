package network.soylu.book_api.nms;

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

public abstract class NMSHandler {

    protected static final Logger LOGGER = Logger.getLogger(NMSHandler.class.getName());

    protected final String version;

    protected NMSHandler(@NotNull String version) {
        this.version = version;
    }

    @NotNull
    public static NMSHandler create() {
        String version = ReflectionUtil.getServerVersion();
        LOGGER.info("Detected server version: " + version);

        try {
            if (ReflectionUtil.isVersionAtLeast(1, 21, 4)) {
                return new ModernNMSHandler(version);
            }
            else if (ReflectionUtil.isVersionAtLeast(1, 20, 0)) {
                return new LegacyNMSHandler(version);
            }
            else {
                throw new UnsupportedOperationException("Unsupported Minecraft version: " + version);
            }
        } catch (Exception e) {
            LOGGER.severe("Failed to create NMS handler for version " + version + ": " + e.getMessage());
            throw new RuntimeException("Failed to create NMS handler", e);
        }
    }

    public abstract void openBook(@NotNull Player player, @NotNull Book book) throws Exception;

    @NotNull
    public String getVersion() {
        return version;
    }

    private static class ModernNMSHandler extends NMSHandler {

        private final Method sendPacketMethod;
        private final Constructor<?> openBookPacketConstructor;
        private final Method getHandleMethod;
        private final Field connectionField;

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

        private void sendPacket(@NotNull Player player, @NotNull Object packet) throws Exception {
            Object entityPlayer = getHandleMethod.invoke(player);
            Object connection = connectionField.get(entityPlayer);
            sendPacketMethod.invoke(connection, packet);
        }
    }

    private static class LegacyNMSHandler extends NMSHandler {

        private final Method sendPacketMethod;
        private final Constructor<?> openBookPacketConstructor;
        private final Method getHandleMethod;
        private final Field connectionField;

        public LegacyNMSHandler(@NotNull String version) throws Exception {
            super(version);

            @SuppressWarnings("unused")
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

        private void sendPacket(@NotNull Player player, @NotNull Object packet) throws Exception {
            Object entityPlayer = getHandleMethod.invoke(player);
            Object connection = connectionField.get(entityPlayer);
            sendPacketMethod.invoke(connection, packet);
        }
    }
}