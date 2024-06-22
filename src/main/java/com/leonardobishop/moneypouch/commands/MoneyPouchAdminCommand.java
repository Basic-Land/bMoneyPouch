package com.leonardobishop.moneypouch.commands;

import com.leonardobishop.moneypouch.MoneyPouch;
import com.leonardobishop.moneypouch.Pouch;
import com.leonardobishop.moneypouch.economytype.EconomyType;
import cz.basicland.blibs.shared.utils.StringUtils;
import cz.basicland.blibs.spigot.commands.LCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class MoneyPouchAdminCommand implements LCommand {

    private final MoneyPouch plugin;

    public MoneyPouchAdminCommand(MoneyPouch plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getLabel() {
        return "moneypouchadmin";
    }

    @Override
    public String getUsage() {
        return "/moneypouchshop";
    }

    @Override
    public String getPermission() {
        return "moneypouch.admin";
    }

    @Override
    public String[] aliases() {
        return new String[]{"mpa"};
    }

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (args.length > 0) {
            switch (args[0]) {
                case "list" -> {
                    for (Pouch pouch : plugin.getPouches()) {
                        sender.sendRichMessage("<dark_purple>" + pouch.id() + " </dark_purple><light_purple>(min: " +
                                pouch.minRange() + ", max: " + pouch.maxRange() + ", economy: " +
                                pouch.economyType().toString() + " [" + pouch.economyType().getPrefix() +
                                "<dark_gray>/</dark_gray><light_purple>" + pouch.economyType().getSuffix() + "])");
                    }
                    return true;
                }
                case "economy", "economies" -> {
                    for (Map.Entry<String, EconomyType> economyTypeEntry : plugin.getEconomyTypes().entrySet()) {
                        sender.sendRichMessage("<dark_purple>" + economyTypeEntry.getKey() + " </dark_purple><light_purple>" + economyTypeEntry.getValue().toString() +
                                " [" + economyTypeEntry.getValue().getPrefix() +
                                "<dark_gray>/</dark_gray><light_purple>" + economyTypeEntry.getValue().getSuffix() + "])");
                    }
                    return true;
                }
                case "reload" -> {
                    plugin.reload();
                    sender.sendRichMessage("<dark_gray>MoneyPouch has been reloaded");
                    return true;
                }
            }
        }

        sender.sendRichMessage("<bold><dark_purple>Money Pouch (ver " + plugin.getDescription().getVersion() + ")");
        sender.sendRichMessage("<gray><> = required, [] = optional");
        sender.sendRichMessage("<light_purple>/mpa :</light_purple><gray> view this menu");
        sender.sendRichMessage("<light_purple>/mp <tier> [player] [amount] :</light_purple><gray> give <item> to [player] (or self if blank)");
        sender.sendRichMessage("<light_purple>/mpshop :</light_purple><gray> open the shop");
        sender.sendRichMessage("<light_purple>/mpa list :</light_purple><gray> list all pouches");
        sender.sendRichMessage("<light_purple>/mpa economies :</light_purple><gray> list all economies");
        sender.sendRichMessage("<light_purple>/mpa reload :</light_purple><gray> reload the config");
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, String[] args) {
        if (args.length == 1) {
            List<String> options = Arrays.asList("list", "economies", "reload");
            return StringUtils.copyMatches(args[0], options);
        }

        return Collections.emptyList();
    }
}
