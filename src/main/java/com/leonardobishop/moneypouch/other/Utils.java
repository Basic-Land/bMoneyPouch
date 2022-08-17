package com.leonardobishop.moneypouch.other;

import org.bukkit.Bukkit;

public class Utils {

    public static String getServerVersion() {
        String version = null;

        try {
            version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        } catch (Exception var4) {
            try {
                version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[1];
            } catch (Exception var3) {
            }
        }

        return version;
    }

    public static Integer getServerVersionID() {
        return Integer.parseInt(getServerVersion().split("_")[1]);
    }

    public static Integer getServerVersionIdAndSubId() {
        String var10000 = getServerVersion().split("_")[1];
        return Integer.parseInt(var10000 + (getServerVersion().split("_").length >= 3 ? getServerVersion().split("_")[2].replace("R", "") : "0"));
    }

}
