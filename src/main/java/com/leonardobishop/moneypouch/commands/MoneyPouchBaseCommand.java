package com.leonardobishop.moneypouch.commands;

import com.leonardobishop.moneypouch.MoneyPouch;
import com.leonardobishop.moneypouch.Pouch;
import com.leonardobishop.moneypouch.economytype.EconomyType;
import cz.devfire.bantidupe.AntiDupe;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class MoneyPouchBaseCommand implements CommandExecutor, TabCompleter {

    private MoneyPouch plugin;

    public MoneyPouchBaseCommand(MoneyPouch plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length > 0) {
            Player target = null;

            if (args.length >= 2) {
                for (Player p : Bukkit.getServer().getOnlinePlayers()) {
                    if (!p.getName().equalsIgnoreCase(args[1]))
                        continue;
                    target = p;
                    break;
                }
            } else if (sender instanceof Player) {
                target = ((Player) sender);
            }
            int amount = 1;
            if (args.length >= 3) {
                int requested;
                try {
                    requested = Integer.parseInt(args[2]);
                } catch (Exception e) {
                    sender.sendMessage(ChatColor.RED + "Invalid integer");
                    return true;
                }
                if (requested > 64) {
                    sender.sendMessage(ChatColor.RED + "Warning: The amount requested is above 64. This may result in strange behaviour.");
                }
                amount = requested;
            }

            if (target == null) {
                sender.sendMessage(ChatColor.RED + "The specified player could not be found.");
                return true;
            }

            Pouch pouch = null;
            for (Pouch p: plugin.getPouches()) {
                if (p.getId().equals(args[0])) {
                    pouch = p;
                    break;
                }
            }
            if (pouch == null) {
                sender.sendMessage(ChatColor.RED + "The pouch " + ChatColor.DARK_RED + args[0] + ChatColor.RED + " could not be found.");
                return true;
            }
            if (target.getInventory().firstEmpty() == -1) {
                sender.sendMessage(plugin.getMessage(MoneyPouch.Message.FULL_INV));
                return true;
            }

            for (int i = 0; i < amount; i++) {
                if (Bukkit.getPluginManager().isPluginEnabled("bAntiDupe")) {
                    target.getInventory().addItem(AntiDupe.getInstance().getApi().saveItem(pouch.getItemStack().clone()));
                } else {
                    target.getInventory().addItem(pouch.getItemStack());
                }
            }

            sender.sendMessage(plugin.getMessage(MoneyPouch.Message.GIVE_ITEM).replace("%player%",
                    target.getName()).replace("%item%", pouch.getItemStack().getItemMeta().getDisplayName()));
            if (plugin.getConfig().getBoolean("options.show-receive-message", true)) {
                target.sendMessage(plugin.getMessage(MoneyPouch.Message.RECEIVE_ITEM).replace("%player%",
                        target.getName()).replace("%item%", pouch.getItemStack().getItemMeta().getDisplayName()));
            }
            return true;
        }

        sender.sendMessage(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "Money Pouch (ver " + plugin.getDescription().getVersion() + ")");
        sender.sendMessage(ChatColor.GRAY + "<> = required, [] = optional");
        sender.sendMessage(ChatColor.LIGHT_PURPLE + "/mp :" + ChatColor.GRAY + " view this menu");
        sender.sendMessage(ChatColor.LIGHT_PURPLE + "/mp <tier> [player] [amount] :" + ChatColor.GRAY + " give <item> to [player] (or self if blank)");
        sender.sendMessage(ChatColor.LIGHT_PURPLE + "/mpshop :" + ChatColor.GRAY + " open the shop");
        sender.sendMessage(ChatColor.LIGHT_PURPLE + "/mpa list :" + ChatColor.GRAY + " list all pouches");
        sender.sendMessage(ChatColor.LIGHT_PURPLE + "/mpa economies :" + ChatColor.GRAY + " list all economies");
        sender.sendMessage(ChatColor.LIGHT_PURPLE + "/mpa reload :" + ChatColor.GRAY + " reload the config");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String s, String[] args) {
        if (sender instanceof Player) {
            if (args.length == 1) {
                List<String> pouchNames = new ArrayList<>();
                for (Pouch pouch : plugin.getPouches()) {
                    pouchNames.add(pouch.getId());
                }
                List<String> completions = new ArrayList<>();
                StringUtil.copyPartialMatches(args[0], pouchNames, completions);
                Collections.sort(completions);
                return completions;
            }
        }
        return null;
    }
}
