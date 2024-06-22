package com.leonardobishop.moneypouch;

import com.leonardobishop.moneypouch.economytype.EconomyType;
import cz.basicland.blibs.spigot.utils.item.CustomItemStack;

public record Pouch(String id, long minRange, long maxRange, CustomItemStack itemStack, EconomyType economyType,
                    boolean permissionRequired) {

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
}
