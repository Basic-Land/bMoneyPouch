package com.leonardobishop.moneypouch.other;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.channels.Channel;
import java.util.*;

public class Ref {

    private static Field playerCon;
    private static Object server;
    private static Class<?> craft;
    private static Method send, handle;
    private static String channelName;

    static String ver;
    static int intVer;
    static ServerType type;

    public enum ServerType {
        BUKKIT(true), SPIGOT(true), PAPER(true), BUNGEECORD(false), VELOCITY(false), CUSTOM(false); // Is it minecraft?

        final boolean bukkit;

        ServerType(boolean bukkit) {
            this.bukkit = bukkit;
        }

        public boolean isBukkit() {
            return bukkit;
        }
    }

    public static void init(ServerType type, String serverVersion) {
        Ref.ver = serverVersion;
        if (type == ServerType.BUKKIT || type == ServerType.SPIGOT || type == ServerType.PAPER) {
            Ref.intVer = getInt(Ref.ver.split("_")[1]);
            craft = craft("entity.CraftPlayer");
            handle=Ref.method(craft, "getHandle");
            playerCon = Ref.field(nms("server.level","EntityPlayer"), isNewerThan(16)?"b":"playerConnection");
            server = invoke(invoke(cast(craft("CraftServer"), Bukkit.getServer()),"getHandle"),"getServer");
            send = Ref.method(nms("server.network","PlayerConnection"), "sendPacket", Ref.nms("network.protocol","Packet"));
            channelName=isNewerThan(16)?"k":"channel";
            if(Ref.isNewerThan(17) && (serverVersionInt()!=18 || serverVersion().endsWith("_R2"))){
                channelName="m";
            }
        }
        Ref.type = type;
    }

    public static String serverVersion() {
        return Ref.ver;
    }

    public static String getChannelName() {
        return channelName;
    }

    public static Field getPlayerConnectionField() {
        return playerCon;
    }

    public static int serverVersionInt() {
        return Ref.intVer;
    }

    public static ServerType serverType() {
        return Ref.type;
    }

    public static boolean isNewerThan(int i) {
        return Ref.intVer > i;
    }

    public static boolean isOlderThan(int i) {
        return Ref.intVer < i;
    }
    public static void sendPacket(Player to, Object packet) {
        Ref.invoke(Ref.get(Ref.player(to), playerCon), send, packet);
    }

    public static Object server() {
        return server;
    }

    public static Object player(Player a) {
        return invoke(cast(craft, a), handle);
    }

    public static Object playerCon(Player a) {
        return get(player(a), playerCon);
    }

    public static Object network(Object playercon) {
        return get(playercon, isNewerThan(16) ? "a" : "networkManager");
    }

    public static Channel channel(Object network) {
        return (Channel) get(network, channelName);
    }

    public static void set(Object main, Field f, Object o) {
        try {
            f.setAccessible(true);
            f.set(main, o);
        } catch (Exception ignored) {
        }
    }

    public static void set(Object main, String field, Object o) {
        try {
            Field f = Ref.field(main.getClass(), field);
            f.setAccessible(true);
            f.set(main, o);
        } catch (Exception ignored) {
        }
    }

    public static Class<?> getClass(String name) {
        try {
            return Class.forName(name);
        } catch (Exception ignored) {
            return null;
        }
    }

    public static boolean existsMethod(Class<?> c, String name) {
        boolean a = false;
        for (Method d : Ref.getMethods(c))
            if (d.getName().equals(name)) {
                a = true;
                break;
            }
        return a;
    }

    public static Object cast(Class<?> c, Object item) {
        try {
            return c.cast(item);
        } catch (Exception ignored) {
            return null;
        }
    }

    public static Constructor<?> constructor(Class<?> main, Class<?>... bricks) {
        try {
            return main.getDeclaredConstructor(bricks);
        } catch (Exception ignoreds) {
            return null;
        }
    }

    public static Class<?>[] getClasses(Class<?> main) {
        try {
            return main.getClasses();
        } catch (Exception ignoreds) {
            return new Class<?>[0];
        }
    }

    public static Class<?>[] getDeclaredClasses(Class<?> main) {
        try {
            return main.getDeclaredClasses();
        } catch (Exception ignoreds) {
            return new Class<?>[0];
        }
    }

    public static Field[] getFields(Class<?> main) {
        try {
            return main.getFields();
        } catch (Exception ignoreds) {
            return new Field[0];
        }
    }

    public static List<Field> getAllFields(Class<?> main) {
        List<Field> f = new ArrayList<>();
        Class<?> superclass = main;
        while (superclass != null) {
            f.addAll(Arrays.asList(Ref.getDeclaredFields(superclass)));
            superclass = superclass.getSuperclass();
        }
        return f;
    }

    public static Field[] getDeclaredFields(Class<?> main) {
        try {
            return main.getDeclaredFields();
        } catch (Exception ignoreds) {
            return new Field[0];
        }
    }

    public static Method[] getMethods(Class<?> main) {
        try {
            return main.getMethods();
        } catch (Exception ignoreds) {
            return new Method[0];
        }
    }

    public static Method[] getDeclaredMethods(Class<?> main) {
        try {
            return main.getDeclaredMethods();
        } catch (Exception ignoreds) {
            return null;
        }
    }

    public static Constructor<?>[] getConstructors(Class<?> main) {
        try {
            return main.getConstructors();
        } catch (Exception ignoreds) {
            return null;
        }
    }

    public static Constructor<?>[] getDeclaredConstructors(Class<?> main) {
        try {
            return main.getDeclaredConstructors();
        } catch (Exception ignoreds) {
            return null;
        }
    }

    public static Method method(Class<?> main, String name, Class<?>... bricks) {
        try {
            Method a = main.getDeclaredMethod(name, bricks);
            a.setAccessible(true);
            return a;
        } catch (Exception ignored) {
            return null;
        }
    }

    public static Method method(Class<?> main, String oldName, String newName, Class<?>... bricks) {
        String name = Utils.getServerVersionID() < 17 ? oldName : newName;
        try {
            Method f = main.getMethod(name, bricks);
            f.setAccessible(true);
            return f;
        } catch (Exception es) {
            try {
                Method f = main.getDeclaredMethod(name, bricks);
                f.setAccessible(true);
                return f;
            } catch (Exception e) {
                try {
                    if (main.getSuperclass() != null)
                        return method(main.getSuperclass(), name, bricks);
                } catch (Exception ignored) {
                }
                return null;
            }
        }
    }

    public static Field field(Class<?> main, String name) {
        try {
            Field f = main.getDeclaredField(name);
            f.setAccessible(true);
            return f;
        } catch (Exception ignored) {
            try {
                Field f = null;
                Class<?> c = main.getSuperclass();
                while (c != null) {
                    try {
                        f = c.getDeclaredField(name);
                    } catch (Exception ignoredrr) {
                    }
                    if (f != null)
                        break;
                    try {
                        c = c.getSuperclass();
                    } catch (Exception ignoredrr) {
                        break;
                    }
                }
                if (f != null)
                    f.setAccessible(true);
                return f;
            } catch (Exception ignoredr) {
            }
            return null;
        }
    }

    public static Field field(Class<?> main, Class<?> returnValue) {
        try {
            Class<?> mainClass = main;
            while (mainClass != null) {
                for (Field field : Ref.getDeclaredFields(mainClass))
                    if (field.getType() == returnValue) {
                        field.setAccessible(true);
                        return field;
                    }
                mainClass = mainClass.getSuperclass();
            }
            return null;
        } catch (Exception ignoreds) {
            return null;
        }
    }

    public static Field field(Class<?> main, String oldName, String newName) {
        String name = Utils.getServerVersionID() < 17 ? oldName : newName;
        try {
            Field f = main.getField(name);
            if (f != null)
                f.setAccessible(true);
            return f;
        } catch (Exception es) {
            try {
                Field f = main.getDeclaredField(name);
                f.setAccessible(true);
                return f;
            } catch (Exception e) {
                try {
                    if (main.getSuperclass() != null)
                        return field(main.getSuperclass(), name);
                } catch (Exception ignored) {
                }
                return null;
            }
        }
    }

    public static Object get(Object main, Field field) {
        try {
            field.setAccessible(true);
            return field.get(main);
        } catch (Exception ignoreds) {
            return null;
        }
    }

    public static Object get(Object main, String oldFiend, String newField) {
        String field = Utils.getServerVersionID() < 17 ? oldFiend : newField;
        try {
            return field(main.getClass(), field).get(main);
        } catch (Exception | NoSuchFieldError es) {
            return null;
        }
    }

    public static Object getNulled(Field field) {
        try {
            field.setAccessible(true);
            return field.get(null);
        } catch (Exception ignoreds) {
            return null;
        }
    }

    public static Object getNulled(Class<?> clas, String field) {
        try {
            return Ref.field(clas, field).get(null);
        } catch (Exception ignoreds) {
            return null;
        }
    }

    public static Object getStatic(Field field) {
        return Ref.getNulled(field);
    }

    public static Object getStatic(Class<?> clas, String field) {
        return Ref.getNulled(clas, field);
    }

    public static Object get(Object main, String field) {
        return Ref.get(main, Ref.field(main.getClass(), field));
    }

    public static Object invoke(Object main, Method method, Object... bricks) {
        try {
            method.setAccessible(true);
            return method.invoke(main, bricks);
        } catch (Exception | NoSuchMethodError es) {
            return null;
        }
    }

    public static Object invoke(Object main, String method, Object... bricks) {
        try {
            return Ref.findMethod(main.getClass(), method, bricks).invoke(main, bricks);
        } catch (Exception ignoreds) {
            return null;
        }
    }

    public static Object invoke(Object main, String oldMethod, String newMethod, Object... bricks) {
        try {
            return findMethod(main.getClass(), (Utils.getServerVersionID() < 17 ? oldMethod : newMethod), bricks).invoke(main, bricks);
        } catch (Exception es) {
            return null;
        }
    }

    public static Object get(Object main, Class<?> returnValue) {
        return Ref.get(main, field(main.getClass(), returnValue));
    }

    public static Object invokeNulled(Class<?> classInMethod, String method, Object... bricks) {
        try {
            return Ref.findMethod(classInMethod, method, bricks).invoke(null, bricks);
        } catch (Exception ignoreds) {
            return null;
        }
    }

    public static Object invokeNulled(Method method, Object... bricks) {
        try {
            return method.invoke(null, bricks);
        } catch (Exception ignoreds) {
            return null;
        }
    }

    public static Object invokeStatic(Class<?> classInMethod, String method, Object... bricks) {
        return Ref.invokeNulled(classInMethod, method, bricks);
    }

    public static Object invokeStatic(Method method, Object... bricks) {
        return Ref.invokeNulled(method, bricks);
    }

    public static Method findMethod(Object c, String name, Object... bricks) {
        return Ref.findMethod(c.getClass(), name, bricks);
    }

    public static Method findMethodByName(Class<?> c, String name) {
        Method a = null;
        Class<?> d = c;
        while (d != null) {
            for (Method m : Ref.getDeclaredMethods(d))
                if (m.getName().equals(name)) {
                    a = m;
                    break;
                }
            if (a != null)
                break;
            try {
                d = d.getSuperclass();
            } catch (Exception ignoredrr) {
                break;
            }
        }
        if (a != null)
            a.setAccessible(true);
        return a;
    }

    public static Method findMethod(Class<?> c, String name, Object... bricks) {
        Method a = null;
        Class<?> d = c;
        Class<?>[] param = new Class<?>[bricks.length];
        int i = 0;
        for (Object o : bricks)
            if (o != null)
                param[i++] = o instanceof Class ? (Class<?>) o : o.getClass();
        while (d != null) {
            for (Method m : Ref.getDeclaredMethods(d))
                if (m.getName().equals(name) && Ref.areSame(m.getParameterTypes(), param)) {
                    a = m;
                    break;
                }
            if (a != null)
                break;
            try {
                d = d.getSuperclass();
            } catch (Exception ignoredrr) {
                break;
            }
        }
        if (a != null)
            a.setAccessible(true);
        return a;
    }

    public static Constructor<?> findConstructor(Class<?> c, Object... bricks) {
        Constructor<?> a = null;
        Class<?>[] param = new Class<?>[bricks.length];
        int i = 0;
        for (Object o : bricks)
            if (o != null)
                param[i++] = o instanceof Class ? (Class<?>) o : o.getClass();
        for (Constructor<?> m : Ref.getDeclaredConstructors(c))
            if (Ref.areSame(m.getParameterTypes(), param)) {
                a = m;
                break;
            }
        if (a != null)
            a.setAccessible(true);
        return a;
    }

    private static boolean areSame(Class<?>[] a, Class<?>[] b) {
        return new HashSet<>(Arrays.asList(a)).containsAll(Arrays.asList(b));
    }

    public static Object newInstance(Constructor<?> constructor, Object... bricks) {
        try {
            constructor.setAccessible(true);
            return constructor.newInstance(bricks);
        } catch (Exception ignoreds) {
            return null;
        }
    }

    public static Object newInstanceByClass(String className, Object... bricks) {
        return Ref.newInstance(Ref.findConstructor(Ref.getClass(className), bricks), bricks);
    }

    public static Object newInstanceByClass(Class<?> clazz, Object... bricks) {
        return Ref.newInstance(Ref.findConstructor(clazz, bricks), bricks);
    }

    public static Class<?> nms(String modernPackageName, String name) {
        try {
            if (Ref.isNewerThan(16))
                return Class.forName("net.minecraft." + modernPackageName + "." + name);
            return Class.forName("net.minecraft.server." + Ref.serverVersion() + "." + name);
        } catch (Exception ignored) {
            return null;
        }
    }

    public static Class<?> craft(String name) {
        try {
            return Class.forName("org.bukkit.craftbukkit." + Ref.serverVersion() + "." + name);
        } catch (Exception ignored) {
            return null;
        }
    }

    public static int getInt(String fromString) {
        if (fromString == null) {
            return 0;
        } else {
            String a = fromString.replaceAll("[^+0-9E.,-]+", "").replace(",", ".");
            if (!a.contains(".")) {
                try {
                    return Integer.parseInt(a);
                } catch (NumberFormatException var5) {
                    try {
                        return (int)Long.parseLong(a);
                    } catch (NumberFormatException ignored) {
                    }
                }
            }

            try {
                return (int)Double.parseDouble(a);
            } catch (NumberFormatException var3) {
                return 0;
            }
        }
    }
}