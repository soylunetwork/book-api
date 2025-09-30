package network.soylu.book_api.util;

import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for reflection operations, including class, method, field, and constructor lookups
 * with caching for performance.
 *
 * @author sheduxdev
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public final class ReflectionUtil {

    /** Base package for CraftBukkit classes. */
    private static final String CRAFTBUKKIT_PACKAGE = "org.bukkit.craftbukkit";

    /** Base package for legacy NMS classes. */
    private static final String NMS_PACKAGE = "net.minecraft.server";

    /** Base package for modern Minecraft classes. */
    private static final String MINECRAFT_PACKAGE = "net.minecraft";

    /** Cache for loaded classes. */
    private static final Map<String, Class<?>> CLASS_CACHE = new HashMap<>();

    /** Cache for loaded methods. */
    private static final Map<String, Method> METHOD_CACHE = new HashMap<>();

    /** Cache for loaded fields. */
    private static final Map<String, Field> FIELD_CACHE = new HashMap<>();

    /** Cache for loaded constructors. */
    private static final Map<String, Constructor<?>> CONSTRUCTOR_CACHE = new HashMap<>();

    /** The detected server version. */
    private static final String SERVER_VERSION = getServerVersionInternal();

    /** Parsed server version numbers [major, minor, patch]. */
    private static final int[] VERSION_NUMBERS = parseVersionNumbers();

    /**
     * Private constructor to prevent instantiation.
     */
    private ReflectionUtil() {
    }

    /**
     * Gets the Minecraft server version.
     *
     * @return the server version string
     */
    @NotNull
    public static String getServerVersion() {
        return SERVER_VERSION;
    }

    /**
     * Checks if the current server version is at least the specified version.
     *
     * @param major the major version number
     * @param minor the minor version number
     * @param patch the patch version number
     * @return true if the current version is at least the specified version
     */
    public static boolean isVersionAtLeast(int major, int minor, int patch) {
        if (VERSION_NUMBERS[0] > major) return true;
        if (VERSION_NUMBERS[0] < major) return false;

        if (VERSION_NUMBERS[1] > minor) return true;
        if (VERSION_NUMBERS[1] < minor) return false;

        return VERSION_NUMBERS[2] >= patch;
    }

    /**
     * Gets a CraftBukkit class by its name.
     *
     * @param className the class name without package
     * @return the CraftBukkit class
     * @throws RuntimeException if the class is not found
     */
    @NotNull
    public static Class<?> getCraftBukkitClass(@NotNull String className) {
        return getClass(CRAFTBUKKIT_PACKAGE + "." + SERVER_VERSION + "." + className);
    }

    /**
     * Gets an NMS class by its name (for legacy versions).
     *
     * @param className the class name without package
     * @return the NMS class
     * @throws RuntimeException if the class is not found
     */
    @NotNull
    public static Class<?> getNMSClass(@NotNull String className) {
        return getClass(NMS_PACKAGE + "." + SERVER_VERSION + "." + className);
    }

    /**
     * Gets a Minecraft class by its name (for modern versions).
     *
     * @param className the class name without package
     * @return the Minecraft class
     * @throws RuntimeException if the class is not found
     */
    @NotNull
    public static Class<?> getMinecraftClass(@NotNull String className) {
        return getClass(MINECRAFT_PACKAGE + "." + className);
    }

    /**
     * Gets a class by its full name with caching.
     *
     * @param className the full class name
     * @return the class
     * @throws RuntimeException if the class is not found
     */
    @NotNull
    public static Class<?> getClass(@NotNull String className) {
        return CLASS_CACHE.computeIfAbsent(className, name -> {
            try {
                return Class.forName(name);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Gets a method from a class with caching.
     *
     * @param clazz the class containing the method
     * @param methodName the name of the method
     * @param parameterTypes the parameter types of the method
     * @return the method
     * @throws RuntimeException if the method is not found
     */
    @NotNull
    public static Method getMethod(@NotNull Class<?> clazz, @NotNull String methodName, Class<?>... parameterTypes) {
        String key = clazz.getName() + "." + methodName + "(" + java.util.Arrays.toString(parameterTypes) + ")";
        return METHOD_CACHE.computeIfAbsent(key, k -> {
            try {
                Method method = clazz.getDeclaredMethod(methodName, parameterTypes);
                method.setAccessible(true);
                return method;
            } catch (NoSuchMethodException e) {
                Class<?> currentClass = clazz.getSuperclass();
                while (currentClass != null) {
                    try {
                        Method method = currentClass.getDeclaredMethod(methodName, parameterTypes);
                        method.setAccessible(true);
                        return method;
                    } catch (NoSuchMethodException ignored) {
                        currentClass = currentClass.getSuperclass();
                    }
                }
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Gets a field from a class with caching.
     *
     * @param clazz the class containing the field
     * @param fieldName the name of the field
     * @return the field
     * @throws RuntimeException if the field is not found
     */
    @NotNull
    public static Field getField(@NotNull Class<?> clazz, @NotNull String fieldName) {
        String key = clazz.getName() + "." + fieldName;
        return FIELD_CACHE.computeIfAbsent(key, k -> {
            try {
                Field field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
                return field;
            } catch (NoSuchFieldException e) {
                Class<?> currentClass = clazz.getSuperclass();
                while (currentClass != null) {
                    try {
                        Field field = currentClass.getDeclaredField(fieldName);
                        field.setAccessible(true);
                        return field;
                    } catch (NoSuchFieldException ignored) {
                        currentClass = currentClass.getSuperclass();
                    }
                }
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Gets a constructor from a class with caching.
     *
     * @param clazz the class containing the constructor
     * @param parameterTypes the parameter types of the constructor
     * @return the constructor
     * @throws RuntimeException if the constructor is not found
     */
    @NotNull
    public static Constructor<?> getConstructor(@NotNull Class<?> clazz, Class<?>... parameterTypes) {
        String key = clazz.getName() + ".constructor(" + java.util.Arrays.toString(parameterTypes) + ")";
        return CONSTRUCTOR_CACHE.computeIfAbsent(key, k -> {
            try {
                Constructor<?> constructor = clazz.getDeclaredConstructor(parameterTypes);
                constructor.setAccessible(true);
                return constructor;
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Gets an enum value by its name.
     *
     * @param enumClass the enum class
     * @param name the name of the enum constant
     * @return the enum value
     * @throws IllegalArgumentException if the enum constant is not found
     */
    @NotNull
    public static Object getEnumValue(@NotNull Class<?> enumClass, @NotNull String name) {
        if (!enumClass.isEnum()) {
            throw new IllegalArgumentException("Class is not an enum: " + enumClass.getName());
        }

        Object[] enumConstants = enumClass.getEnumConstants();
        for (Object enumConstant : enumConstants) {
            if (((Enum<?>) enumConstant).name().equals(name)) {
                return enumConstant;
            }
        }

        throw new IllegalArgumentException("Enum constant not found: " + name + " in " + enumClass.getName());
    }

    /**
     * Invokes a method safely, handling exceptions.
     *
     * @param method the method to invoke
     * @param instance the instance to invoke on (null for static methods)
     * @param args the method arguments
     * @return the return value
     * @throws RuntimeException if the invocation fails
     */
    @Nullable
    public static Object invokeMethod(@NotNull Method method, @Nullable Object instance, Object... args) {
        try {
            return method.invoke(instance, args);
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke method: " + method.getName(), e);
        }
    }

    /**
     * Gets a field value safely, handling exceptions.
     *
     * @param field the field to get
     * @param instance the instance to get from (null for static fields)
     * @return the field value
     * @throws RuntimeException if the operation fails
     */
    @Nullable
    public static Object getFieldValue(@NotNull Field field, @Nullable Object instance) {
        try {
            return field.get(instance);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get field value: " + field.getName(), e);
        }
    }

    /**
     * Sets a field value safely, handling exceptions.
     *
     * @param field the field to set
     * @param instance the instance to set on (null for static fields)
     * @param value the value to set
     * @throws RuntimeException if the operation fails
     */
    public static void setFieldValue(@NotNull Field field, @Nullable Object instance, @Nullable Object value) {
        try {
            field.set(instance, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set field value: " + field.getName(), e);
        }
    }

    /**
     * Creates a new instance using a constructor safely.
     *
     * @param constructor the constructor to use
     * @param args the constructor arguments
     * @return the new instance
     * @throws RuntimeException if the construction fails
     */
    @NotNull
    public static Object newInstance(@NotNull Constructor<?> constructor, Object... args) {
        try {
            return constructor.newInstance(args);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create new instance with constructor: " + constructor, e);
        }
    }

    /**
     * Retrieves the server version from the Bukkit server package.
     *
     * @return the server version string
     */
    private static String getServerVersionInternal() {
        String packageName = Bukkit.getServer().getClass().getPackage().getName();
        return packageName.substring(packageName.lastIndexOf('.') + 1);
    }

    /**
     * Parses the server version into major, minor, and patch numbers.
     *
     * @return an array of [major, minor, patch] version numbers
     */
    private static int[] parseVersionNumbers() {
        Pattern pattern = Pattern.compile("v(\\d+)_(\\d+)_R(\\d+)");
        Matcher matcher = pattern.matcher(ReflectionUtil.SERVER_VERSION);

        if (matcher.matches()) {
            int major = Integer.parseInt(matcher.group(1));
            int minor = Integer.parseInt(matcher.group(2));
            int patch = Integer.parseInt(matcher.group(3));
            return new int[]{major, minor, patch};
        }

        // Fallback for modern versions
        String bukkitVersion = Bukkit.getBukkitVersion();
        Pattern fallbackPattern = Pattern.compile("(\\d+)\\.(\\d+)\\.?(\\d+)?");
        Matcher fallbackMatcher = fallbackPattern.matcher(bukkitVersion);

        if (fallbackMatcher.find()) {
            int major = Integer.parseInt(fallbackMatcher.group(1));
            int minor = Integer.parseInt(fallbackMatcher.group(2));
            int patch = fallbackMatcher.group(3) != null ?
                    Integer.parseInt(fallbackMatcher.group(3)) : 0;
            return new int[]{major, minor, patch};
        }

        return new int[]{1, 8, 0}; // Default to 1.8.0
    }
}