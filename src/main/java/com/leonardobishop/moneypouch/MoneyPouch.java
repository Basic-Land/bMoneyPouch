package com.leonardobishop.moneypouch;

import com.leonardobishop.moneypouch.commands.MoneyPouchAdminCommand;
import com.leonardobishop.moneypouch.commands.MoneyPouchBaseCommand;
import com.leonardobishop.moneypouch.economytype.EconomyType;
import com.leonardobishop.moneypouch.economytype.VaultEconomyType;
import com.leonardobishop.moneypouch.economytype.XPEconomyType;
import com.leonardobishop.moneypouch.events.UseEvent;
import cz.basicland.blibs.shared.dataholder.Config;
import cz.basicland.blibs.spigot.BLibs;
import cz.basicland.blibs.spigot.commands.CommandHandler;
import cz.basicland.blibs.spigot.listeners.ListenerHandler;
import cz.basicland.blibs.spigot.utils.Version;
import cz.basicland.blibs.spigot.utils.item.CustomItemStack;
import cz.basicland.blibs.spigot.utils.item.ItemUtils;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;

public class MoneyPouch extends JavaPlugin {

    private final HashMap<String, EconomyType> economyTypes = new HashMap<>();
    private final ArrayList<Pouch> pouches = new ArrayList<>();
    private Config config;

    public EconomyType getEconomyType(String id) {
        if (id == null) {
            return null;
        }
        return economyTypes.get(id.toLowerCase());
    }

    public HashMap<String, EconomyType> getEconomyTypes() {
        return economyTypes;
    }

    public void registerEconomyType(String id, EconomyType type) {
        if (economyTypes.containsKey(id)) {
            super.getLogger().warning("Economy type registration " + type.toString() + " ignored due to conflicting ID '" + id + "' with economy type " + economyTypes.get(id).toString());
            return;
        }
        economyTypes.put(id, type);
    }

    @Override
    public void onEnable() {
        CommandHandler commandHandler = BLibs.getApi().getCommandHandler();
        ListenerHandler listenerHandler = BLibs.getApi().getListenerHandler();
        config = Config.loadFromPlugin(this, "config.yml", "config.yml").saveYaml();
        getLogger().info("Your server is running version " + Version.CURRENT.getName() + ".");

        registerEconomyType("xp", new XPEconomyType(   // vv for legacy purposes
                config.getString("economy.xp.prefix", config.getString("economy.prefixes.xp", "")),
                config.getString("economy.xp.suffix", config.getString("economy.suffixes.xp", " XP"))));


        if (Bukkit.getServer().getPluginManager().getPlugin("Vault") != null) {
            registerEconomyType("vault", new VaultEconomyType(this,
                    config.getString("economy.vault.prefix", config.getString("economy.prefixes.vault", "$")),
                    config.getString("economy.vault.suffix", config.getString("economy.suffixes.vault", ""))));
        }

        commandHandler.addCommand(this, new MoneyPouchBaseCommand(this));
        commandHandler.addCommand(this, new MoneyPouchAdminCommand(this));
        listenerHandler.addListener(this, new UseEvent(this));

        Bukkit.getScheduler().runTask(this, this::reload);
    }

    @NotNull
    public Config getCfg() {
        return config;
    }

    public String getMessage(Message message) {
        return config.getStringCC("messages." + message.getId(), message.getDef());
    }

    public ArrayList<Pouch> getPouches() {
        return pouches;
    }

    public void reload() {
        config.reload();

        pouches.clear();
        for (String s : config.getKeys("pouches.tier")) {
            CustomItemStack is = getItemStack("pouches.tier." + s);
            String economyTypeId = config.getString("pouches.tier." + s + ".options.economytype", "VAULT");
            long priceMin = config.getLong("pouches.tier." + s + ".pricerange.from", 0);
            long priceMax = config.getLong("pouches.tier." + s + ".pricerange.to", 0);
            boolean permissionRequired = config.getBoolean("pouches.tier." + s + ".options.permission-required", false);

            EconomyType economyType = getEconomyType(economyTypeId);
            if (economyType == null) {
                economyType = getEconomyType("invalid");
                super.getLogger().warning("Pouch with ID " + s + " tried to use an invalid economy type '" + economyTypeId + "'.");
            }

            pouches.add(new Pouch(s.replace(" ", "_"), priceMin, priceMax, is, economyType, permissionRequired));
        }
    }

    public CustomItemStack getItemStack(String path) {
        CustomItemStack stack = ItemUtils.get(config, path);
        stack.setString("moneyPouch", path.split("\\.")[2]);
        return stack;
    }

    public enum Message {

        FULL_INV("full-inv", "&c%player%'s inventory is full!"),
        GIVE_ITEM("give-item", "&6Given &e%player% %item%&6."),
        RECEIVE_ITEM("receive-item", "&6You have been given %item%&6."),
        PRIZE_MESSAGE("prize-message", "&6You have received &c%prefix%%prize%%suffix%&6!"),
        ALREADY_OPENING("already-opening", "&c&lServer &8&l» &7Jiz oteviras balicek, vyckej!"),
        INVALID_POUCH("invalid-pouch", "&cThis pouch is invalid and cannot be opened."),
        INVENTORY_FULL("inventory-full", "&cYour inventory is full."),
        REWARD_ERROR("reward-error", "&cYour reward of %prefix%%prize%%suffix% has failed to process. Contact an admin, this has been logged."),
        PURCHASE_SUCCESS("purchase-success", "&6You have purchased %item%&6 for &c%prefix%%price%%suffix%&6."),
        PURCHASE_FAIL("purchase-fail", "&cYou do not have &c%prefix%%price%%suffix%&6."),
        PURCHASE_ERROR("purchase-ERROR", "&cCould not complete transaction for %item%&c."),
        SHOP_DISABLED("shop-disabled", "&cThe pouch shop is disabled."),
        NO_PERMISSION("no-permission", "&cYou cannot open this pouch.");

        private final String id;
        private final String def; // (default message if undefined)

        Message(String id, String def) {
            this.id = id;
            this.def = def;
        }

        public String getId() {
            return id;
        }

        public String getDef() {
            return def;
        }
    }
}
