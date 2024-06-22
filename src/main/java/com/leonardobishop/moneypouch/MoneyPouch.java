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
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;

public class MoneyPouch extends JavaPlugin {

    @Getter
    private final HashMap<String, EconomyType> economyTypes = new HashMap<>();
    @Getter
    private final ArrayList<Pouch> pouches = new ArrayList<>();
    private Config config;

    public EconomyType getEconomyType(String id) {
        if (id == null) {
            return null;
        }
        return economyTypes.get(id.toLowerCase());
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
        return config.getString("messages." + message.getId(), message.getDef());
    }

    public void reload() {
        config.reload();

        pouches.clear();
        for (String s : config.getKeys("pouches.tier")) {
            CustomItemStack is = getItemStack("pouches.tier." + s, s);
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

    public CustomItemStack getItemStack(String path, String name) {
        CustomItemStack stack = ItemUtils.get(config, path);
        stack.setString("moneyPouch", name);
        return stack;
    }

    @Getter
    public enum Message {
        FULL_INV("full-inv", "<red>%player%'s inventory is full!"),
        GIVE_ITEM("give-item", "<gold>Given </gold><yellow>%player% %item%</yellow><gold>."),
        RECEIVE_ITEM("receive-item", "<gold>You have been given %item%</gold><gold>."),
        PRIZE_MESSAGE("prize-message", "<gold>You have received </gold><red>%prefix%%prize%%suffix%</red><gold>!"),
        ALREADY_OPENING("already-opening", "<bold><red>Server </red></bold><bold><dark_gray>» </dark_gray></bold><gray>Jiz oteviras balicek, vyckej!"),
        INVALID_POUCH("invalid-pouch", "<red>This pouch is invalid and cannot be opened."),
        INVENTORY_FULL("inventory-full", "<red>Your inventory is full."),
        REWARD_ERROR("reward-error", "<red>Your reward of %prefix%%prize%%suffix% has failed to process. Contact an admin, this has been logged."),
        PURCHASE_SUCCESS("purchase-success", "<gold>You have purchased %item%</gold><gold> for </gold><red>%prefix%%price%%suffix%</red><gold>."),
        PURCHASE_FAIL("purchase-fail", "<red>You do not have </red><red>%prefix%%price%%suffix%</red><gold>."),
        PURCHASE_ERROR("purchase-ERROR", "<red>Could not complete transaction for %item%</red><red>."),
        SHOP_DISABLED("shop-disabled", "<red>The pouch shop is disabled."),
        NO_PERMISSION("no-permission", "<red>You cannot open this pouch.");

        private final String id;
        private final String def; // (default message if undefined)

        Message(String id, String def) {
            this.id = id;
            this.def = def;
        }
    }
}
