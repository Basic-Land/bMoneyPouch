package com.leonardobishop.moneypouch.other;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class NBT {

    private ItemStack item;

    private Object nmsItem;

    private static final Class<?> nbtTagCompound = Ref.getClass(NBTMap.NBTTagCompound.getPath());
    private static final Class<?> itemStack = Ref.getClass(NBTMap.ItemStack.getPath());

    private boolean isNms;

    public NBT(ItemStack item) {
        this.item = item;
        this.isNms = false;
    }

    public NBT(Object item) {
        this.nmsItem = item;
        this.isNms = true;
    }

    public void setTags(String stringTags) {

        Object itemstack;
        if(isNms) {
            itemstack = nmsItem;
        }
        else {
            itemstack =  Ref.invokeNulled(Ref.method(Ref.craft("inventory.CraftItemStack"), "asNMSCopy", ItemStack.class), item);
        }

        Object nbtTag = Ref.invokeNulled(Ref.method(Ref.nms("nbt", "MojangsonParser"), NBTMap.parse.getPath(), String.class), stringTags);

        Ref.invoke(itemstack, Ref.method(itemStack, NBTMap.setTag.getPath(), nbtTagCompound), nbtTag);

        if(!isNms) item.setItemMeta((ItemMeta) Ref.invokeNulled(Ref.method(Ref.craft("inventory.CraftItemStack"), "getItemMeta", itemStack), itemstack));

    }


    public Object getNBTTags() {
        Object itemstack;
        if(isNms) {
            itemstack = nmsItem;
        }
        else {
            itemstack =  Ref.invokeNulled(Ref.method(Ref.craft("inventory.CraftItemStack"), "asNMSCopy", ItemStack.class), item);
        }

        Object tag = Ref.invoke(itemstack, NBTMap.getTag.getPath());

        if(tag != null) {
            return tag;
        }

        try {
            return nbtTagCompound.newInstance();

        }
        catch (IllegalAccessException | InstantiationException e) { System.out.println("Error while getting nbtTag of " + item.getType().name() + ":"); e.printStackTrace(); return null;}
    }

    public boolean hasNBTTags() {
        Object itemstack;
        if(isNms) {
            itemstack = nmsItem;
        }
        else {
            itemstack =  Ref.invokeNulled(Ref.method(Ref.craft("inventory.CraftItemStack"), "asNMSCopy", ItemStack.class), item);
        }
        return (boolean)Ref.invoke(itemstack,NBTMap.hasTag.getPath());
    }

    public void setString(String tagName, String tagValue) { set(tagName, tagValue, String.class, "String"); }
    public void setInt(String tagName, int tagValue) { set(tagName, tagValue, int.class, "Int"); }
    public void setBoolean(String tagName, boolean tagValue) { set(tagName, tagValue, boolean.class, "Boolean"); }
    public void setDouble(String tagName, double tagValue) { set(tagName, tagValue, double.class, "Double"); }
    public void setLong(String tagName, long tagValue) { set(tagName, tagValue, long.class, "Long"); }

    private void set(String tagName, Object tagValue, Class type, String n) {
        Object itemstack;
        if(isNms) {
            itemstack = nmsItem;
        }
        else {
            itemstack =  Ref.invokeNulled(Ref.method(Ref.craft("inventory.CraftItemStack"), "asNMSCopy", ItemStack.class), item);
        }
        Object nbtTags = getNBTTags();

        Ref.invoke(nbtTags, Ref.method(nbtTagCompound, "set" + n, "a", String.class, type), tagName, tagValue);
        Ref.invoke(itemstack, Ref.method(itemStack, NBTMap.setTag.getPath(), nbtTagCompound), nbtTags);

        if(!isNms) item.setItemMeta((ItemMeta) Ref.invokeNulled(Ref.method(Ref.craft("inventory.CraftItemStack"), "getItemMeta", itemStack), itemstack));
    }

    public String getString(String tagName) { return get(tagName, "String", "l") + ""; }
    public int getInt(String tagName) { return (int)get(tagName, "Int", "h"); }
    public boolean getBoolean(String tagName) { return (boolean)get(tagName, "Boolean", "b"); }
    public double getDouble(String tagName) { return (double)get(tagName, "Double", "k"); }
    public long getLong(String tagName) { return (long)get(tagName, "Long", "i"); }

    private Object get(String tagName, String oldType, String newType) {
        return Ref.invoke(getNBTTags(), Ref.method(nbtTagCompound, "get" + oldType, newType, String.class), tagName);
    }

    public void remove(String tagName) {
        Object itemstack;
        if(isNms) {
            itemstack = nmsItem;
        }
        else {
            itemstack =  Ref.invokeNulled(Ref.method(Ref.craft("inventory.CraftItemStack"), "asNMSCopy", ItemStack.class), item);
        }

        Object nbtTags = getNBTTags();

        Ref.invoke(nbtTags, Ref.method(nbtTagCompound,NBTMap.remove.getPath(), String.class), tagName);
        Ref.invoke(itemstack, Ref.method(itemStack,NBTMap.setTag.getPath(), nbtTagCompound), nbtTags);
        if(!isNms) item.setItemMeta((ItemMeta) Ref.invokeNulled(Ref.method(Ref.craft("inventory.CraftItemStack"),"getItemMeta", itemStack), itemstack));
    }

}

enum NBTMap {

       /*

        i - 1.8+ support
       ii - 1.18+ support
      iii - 1.18.2 support
      iv - 1.19+ support

     */

    NBTTagCompound("net.minecraft.server." + Ref.serverVersionInt() + ".NBTTagCompound", "net.minecraft.nbt.NBTTagCompound", "net.minecraft.nbt.NBTTagCompound", "net.minecraft.nbt.NBTTagCompound"),
    ItemStack("net.minecraft.server." + Ref.serverVersionInt() + ".ItemStack", "net.minecraft.world.item.ItemStack", "net.minecraft.world.item.ItemStack", "net.minecraft.world.item.ItemStack"),
    parse("parse", "parse", "parse", "a"),
    getTag("getTag", "s", "t", "u"),
    hasTag("hasTag", "r", "s", "t"),
    setTag("setTag", "c", "c", "c"),
    remove("remove", "r", "r", "r");

    private final String path;

    NBTMap(String i, String ii, String iii, String iv) {
        int version = Utils.getServerVersionIdAndSubId();

        // Versions bellow 1.17
        if(version < 170) {
            path = i;
        }

        // Versions above 1.17 but bellow 1.18.2
        else if (version > 170 && version < 182) {
            path = ii;
        }

        // Versions below 1.19
        else if(version < 190) {
            path = iii;
        }

        else {
            path = iv;
        }

    }

    public String getPath() {
        return path;
    }
}