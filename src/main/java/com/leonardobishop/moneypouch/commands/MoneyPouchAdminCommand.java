package com.leonardobishop.moneypouch.commands;

import com.leonardobishop.moneypouch.MoneyPouch;
import com.leonardobishop.moneypouch.Pouch;
import com.leonardobishop.moneypouch.economytype.EconomyType;
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
                case "list":
                    for (Pouch pouch : plugin.getPouches()) {
                        sender.sendMessage("§5" + pouch.getId() + " §d(min: " +
                                pouch.getMinRange() + ", max: " + pouch.getMaxRange() + ", economy: " +
                                pouch.getEconomyType().toString() + " [" + pouch.getEconomyType().getPrefix() +
                                "§8/&d" + pouch.getEconomyType().getSuffix() + "])");
                    }
                    return true;
                case "economy":
                case "economies":
                    for (Map.Entry<String, EconomyType> economyTypeEntry : plugin.getEconomyTypes().entrySet()) {
                        sender.sendMessage("§5" + economyTypeEntry.getKey() + " §d" + economyTypeEntry.getValue().toString() +
                                " [" + economyTypeEntry.getValue().getPrefix() +
                                "§8/§d" + economyTypeEntry.getValue().getSuffix() + "])");
                    }
                    return true;
                case "reload":
                    plugin.reload();
                    sender.sendMessage("§8MoneyPouch has been reloaded");
                    return true;
            }
        }

        sender.sendMessage("§5§lMoney Pouch (ver " + plugin.getDescription().getVersion() + ")");
        sender.sendMessage("§7<> = required, [] = optional");
        sender.sendMessage("§d/mpa :§7 view this menu");
        sender.sendMessage("§d/mp <tier> [player] [amount] :§7 give <item> to [player] (or self if blank)");
        sender.sendMessage("§d/mpshop :§7 open the shop");
        sender.sendMessage("§d/mpa list :§7 list all pouches");
        sender.sendMessage("§d/mpa economies :§7 list all economies");
        sender.sendMessage("§d/mpa reload :§7 reload the config");
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, String[] args) {
        if (sender instanceof Player) {
            if (args.length == 1) {
                List<String> options = Arrays.asList("list", "economies", "reload");
                List<String> completions = new ArrayList<>();
                StringUtil.copyPartialMatches(args[0], options, completions);
                Collections.sort(completions);
                return completions;
            }
        }
        return null;
    }
}
