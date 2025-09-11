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

@SuppressWarnings("unused")
public final class ReflectionUtil {

    private static final String CRAFTBUKKIT_PACKAGE = "org.bukkit.craftbukkit";
    private static final String NMS_PACKAGE = "net.minecraft.server";
    private static final String MINECRAFT_PACKAGE = "net.minecraft";

    private static final Map<String, Class<?>> CLASS_CACHE = new HashMap<>();
    private static final Map<String, Method> METHOD_CACHE = new HashMap<>();
    private static final Map<String, Field> FIELD_CACHE = new HashMap<>();
    private static final Map<String, Constructor<?>> CONSTRUCTOR_CACHE = new HashMap<>();

    private static final String SERVER_VERSION = getServerVersionInternal();
    private static final int[] VERSION_NUMBERS = parseVersionNumbers();

    private ReflectionUtil() {
    }

    @NotNull
    public static String getServerVersion() {
        return SERVER_VERSION;
    }

    /**
     * Checks if the current version is at least the specified version.
     *
     * @param major Major version number
     * @param minor Minor version number
     * @param patch Patch version number
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
     * Gets a CraftBukkit class by name.
     *
     * @param className The class name without package
     * @return The class
     */
    @NotNull
    public static Class<?> getCraftBukkitClass(@NotNull String className) {
        return getClass(CRAFTBUKKIT_PACKAGE + "." + SERVER_VERSION + "." + className);
    }

    /**
     * Gets an NMS class by name (for legacy versions).
     *
     * @param className The class name without package
     * @return The class
     */
    @NotNull
    public static Class<?> getNMSClass(@NotNull String className) {
        return getClass(NMS_PACKAGE + "." + SERVER_VERSION + "." + className);
    }

    /**
     * Gets a Minecraft class by name (for modern versions).
     *
     * @param className The class name without package
     * @return The class
     */
    @NotNull
    public static Class<?> getMinecraftClass(@NotNull String className) {
        return getClass(MINECRAFT_PACKAGE + "." + className);
    }

    /**
     * Gets a class by full name with caching.
     *
     * @param className The full class name
     * @return The class
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
     * Gets a method with caching.
     *
     * @param clazz The class containing the method
     * @param methodName The method name
     * @param parameterTypes The parameter types
     * @return The method
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
     * Gets a field with caching.
     *
     * @param clazz The class containing the field
     * @param fieldName The field name
     * @return The field
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
     * Gets a constructor with caching.
     *
     * @param clazz The class containing the constructor
     * @param parameterTypes The parameter types
     * @return The constructor
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
     * Gets an enum value by name.
     *
     * @param enumClass The enum class
     * @param name The enum constant name
     * @return The enum value
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
     * @param method The method to invoke
     * @param instance The instance to invoke on (null for static methods)
     * @param args The method arguments
     * @return The return value
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
     * @param field The field to get
     * @param instance The instance to get from (null for static fields)
     * @return The field value
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
     * @param field The field to set
     * @param instance The instance to set on (null for static fields)
     * @param value The value to set
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
     * @param constructor The constructor to use
     * @param args The constructor arguments
     * @return The new instance
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

    private static String getServerVersionInternal() {
        String packageName = Bukkit.getServer().getClass().getPackage().getName();
        return packageName.substring(packageName.lastIndexOf('.') + 1);
    }

    private static int[] parseVersionNumbers() {
        Pattern pattern = Pattern.compile("v(\\d+)_(\\d+)(_R\\d+)?");
        Matcher matcher = pattern.matcher(ReflectionUtil.SERVER_VERSION);

        if (matcher.matches()) {
            int major = Integer.parseInt(matcher.group(1));
            int minor = Integer.parseInt(matcher.group(2));
            int patch = 0;

            String bukkitVersion = Bukkit.getBukkitVersion();
            Pattern patchPattern = Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+)");
            Matcher patchMatcher = patchPattern.matcher(bukkitVersion);

            if (patchMatcher.find()) {
                patch = Integer.parseInt(patchMatcher.group(3));
            }

            return new int[]{major, minor, patch};
        }

        return new int[]{1, 21, 4};
    }
}