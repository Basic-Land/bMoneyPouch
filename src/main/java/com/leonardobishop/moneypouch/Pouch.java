package com.leonardobishop.moneypouch;

import com.leonardobishop.moneypouch.economytype.EconomyType;
import cz.basicland.blibs.spigot.utils.item.CustomItemStack;
import org.bukkit.inventory.ItemStack;

public class Pouch {

    private final String id;
    private final long minRange;
    private final long maxRange;
    private final CustomItemStack itemStack;
    private final EconomyType economyType;
    private final boolean permissionRequired;

    public Pouch(String id, long minRange, long maxRange, CustomItemStack itemStack, EconomyType economyType, boolean permissionRequired) {
        this.id = id;
        if (minRange >= maxRange) {
            this.minRange = maxRange - 1;
        } else {
            this.minRange = minRange;
        }
        this.maxRange = maxRange;
        this.itemStack = itemStack;
        this.economyType = economyType;
        this.permissionRequired = permissionRequired;
    }

    public boolean isPermissionRequired() {
        return permissionRequired;
    }

    public String getId() {
        return id;
    }

    public long getMinRange() {
        return minRange;
    }

    public long getMaxRange() {
        return maxRange;
    }

    public CustomItemStack getItemStack() {
        return itemStack;
    }

    public EconomyType getEconomyType() {
        return economyType;
    }
}
