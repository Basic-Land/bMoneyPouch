package com.leonardobishop.moneypouch.economytype;

import com.leonardobishop.moneypouch.MoneyPouch;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class VaultEconomyType extends EconomyType {

    private static Economy economy = null;
    private final MoneyPouch plugin;
    private boolean fail = false;

    public VaultEconomyType(MoneyPouch plugin, String prefix, String suffix) {
        super(prefix, suffix);
        this.plugin = plugin;

        if (Bukkit.getServer().getPluginManager().getPlugin("Vault") == null) {
            fail = true;
            plugin.getLogger().severe("Failed to hook Vault!");
            return;
        }
        RegisteredServiceProvider<Economy> rsp = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            fail = true;
            plugin.getLogger().severe("Failed to hook Vault!");
            return;
        }
        economy = rsp.getProvider();
    }

    @Override
    public void processPayment(Player player, long amount) {
        if (fail) {
            throw new RuntimeException("Failed to hook into Vault!");
        }

        try {
            economy.depositPlayer(player, amount);
        } catch (Throwable t) {
            throw new RuntimeException("An unknown exception occurred", t);
        }
    }

    @Override
    public boolean doTransaction(Player player, long amount) {
        if (fail) {
            plugin.getLogger().severe("Failed to complete transaction in shop: failed to hook into Vault");
            return false;
        }

        if (economy.getBalance(player) < amount) {
            return false;
        }
        economy.withdrawPlayer(player, amount);
        return true;
    }

    @Override
    public String toString() {
        return "Vault";
    }

}
