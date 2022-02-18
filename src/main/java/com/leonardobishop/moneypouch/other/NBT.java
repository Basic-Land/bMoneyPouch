package com.leonardobishop.moneypouch.other;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static com.leonardobishop.moneypouch.other.Ref.*;

public class NBT {
    private static final Class<?> craftItemStack = craft("inventory.CraftItemStack");
    private static final Class<?> itemStack = nms("ItemStack", "world.item");
    private static final Class<?> nbtTagCompound = nms("NBTTagCompound", "nbt");

    private static final Method asNMSCopy = method(craftItemStack, "asNMSCopy", ItemStack.class);
    private static final Method getItemMeta = method(craftItemStack, "getItemMeta", itemStack);
    private static final Method getTag = method(itemStack, "getTag", "s");
    private static final Method hasTag = method(itemStack, "hasTag", "r");
    private static final Method setTag = method(itemStack, "setTag", "c", nbtTagCompound);

    private static final Method setStringMethod = method(nbtTagCompound, "setString", "a", String.class, String.class);
    private static final Method setIntMethod = method(nbtTagCompound, "setInt", "a", String.class, int.class);
    private static final Method setBooleanMethod = method(nbtTagCompound, "setBoolean", "a", String.class, boolean.class);
    private static final Method setDoubleMethod = method(nbtTagCompound, "setDouble", "a", String.class, double.class);

    private static final Method getStringMethod = method(nbtTagCompound, "getString", "l", String.class);
    private static final Method getIntMethod = method(nbtTagCompound, "getInt", "h", String.class);
    private static final Method getBooleanMethod = method(nbtTagCompound, "getBoolean", "q", String.class);
    private static final Method getDoubleMethod = method(nbtTagCompound, "getDouble", "k", String.class);

    private final ItemStack item;

    public NBT(ItemStack item) {
        this.item = item;
    }

    public Object getNBTTags() {
        Object is = invokeStatic(asNMSCopy, item);
        Object tag = invoke(is, getTag);

        if (tag != null) {
            return tag;
        }

        try {
            return nbtTagCompound.getDeclaredConstructor().newInstance();
        } catch (IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException e) {
            System.out.println("Error while getting nbtTag of " + item.getType().name() + ":");
            e.printStackTrace();
            return null;
        }
    }

    public boolean hasNBTTags() {
        Object is = invokeStatic(asNMSCopy, item);
        return (boolean) invoke(is, hasTag);
    }

    public void setString(String tagName, String tagValue) {
        set(tagName, tagValue, setStringMethod);
    }

    public void setInt(String tagName, int tagValue) {
        set(tagName, tagValue, setIntMethod);
    }

    public void setBoolean(String tagName, boolean tagValue) {
        set(tagName, tagValue, setBooleanMethod);
    }

    public void setDouble(String tagName, double tagValue) {
        set(tagName, tagValue, setDoubleMethod);
    }

    private void set(String tagName, Object tagValue, Method method) {
        Object is = invokeStatic(asNMSCopy, item);
        Object nbtTags = getNBTTags();

        invoke(nbtTags, method, tagName, tagValue);
        invoke(is, setTag, nbtTags);
        item.setItemMeta((ItemMeta) invokeStatic(getItemMeta, is));
    }

    public String getString(String tagName) {
        return get(tagName, getStringMethod) + "";
    }

    public int getInt(String tagName) {
        return (int) get(tagName, getIntMethod);
    }

    public boolean getBoolean(String tagName) {
        return (boolean) get(tagName, getBooleanMethod);
    }

    public double getDouble(String tagName) {
        return (double) get(tagName, getDoubleMethod);
    }

    private Object get(String tagName, Method method) {
        return invoke(getNBTTags(), method, tagName);
    }

}