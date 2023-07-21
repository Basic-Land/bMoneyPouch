package com.leonardobishop.moneypouch.commands;

import com.leonardobishop.moneypouch.MoneyPouch;
import com.leonardobishop.moneypouch.Pouch;
import cz.basicland.blibs.spigot.commands.LCommand;
import cz.basicland.blibs.spigot.hooks.Check;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MoneyPouchBaseCommand implements LCommand {

    private final MoneyPouch plugin;

    public MoneyPouchBaseCommand(MoneyPouch plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getLabel() {
        return "moneypouch";
    }

    @Override
    public String getUsage() {
        return "/moneypouch";
    }

    @Override
    public String getPermission() {
        return "moneypouch.admin";
    }

    @Override
    public String[] aliases() {
        return new String[]{"mp"};
    }

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
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
                    sender.sendMessage("§dInvalid integer");
                    return true;
                }
                if (requested > 64) {
                    sender.sendMessage("§dWarning: The amount requested is above 64. This may result in strange behaviour.");
                }
                amount = requested;
            }

            if (target == null) {
                sender.sendMessage("§dThe specified player could not be found.");
                return true;
            }

            Pouch pouch = null;
            for (Pouch p : plugin.getPouches()) {
                if (p.getId().equals(args[0])) {
                    pouch = p;
                    break;
                }
            }
            if (pouch == null) {
                sender.sendMessage("§dThe pouch §4" + args[0] + "§d could not be found.");
                return true;
            }
            if (target.getInventory().firstEmpty() == -1) {
                sender.sendMessage(plugin.getMessage(MoneyPouch.Message.FULL_INV));
                return true;
            }

            for (int i = 0; i < amount; i++) {
                target.getInventory().addItem(Check.addStack(new ItemStack(pouch.getItemStack().getStack())));
            }

            sender.sendMessage(plugin.getMessage(MoneyPouch.Message.GIVE_ITEM).replace("%player%",
                    target.getName()).replace("%item%", pouch.getItemStack().getTitle()));
            if (plugin.getCfg().getBoolean("options.show-receive-message", true)) {
                target.sendMessage(plugin.getMessage(MoneyPouch.Message.RECEIVE_ITEM).replace("%player%",
                        target.getName()).replace("%item%", pouch.getItemStack().getTitle()));
            }
            return true;
        }

        sender.sendMessage("§5&l Money Pouch (ver " + plugin.getDescription().getVersion() + ")");
        sender.sendMessage("§7<> = required, [] = optional");
        sender.sendMessage("§d/mp :§7 view this menu");
        sender.sendMessage("§d/mp <tier> [player] [amount] :§7 give <item> to [player] (or self if blank)");
        sender.sendMessage("§d/mpshop :§7 open the shop");
        sender.sendMessage("§d/mpa list :§7 list all pouches");
        sender.sendMessage("§d/mpa economies :§7 list all economies");
        sender.sendMessage("§d/mpa reload :§7 reload the config");
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
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
