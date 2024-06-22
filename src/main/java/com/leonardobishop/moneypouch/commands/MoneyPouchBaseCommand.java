package com.leonardobishop.moneypouch.commands;

import com.leonardobishop.moneypouch.MoneyPouch;
import com.leonardobishop.moneypouch.Pouch;
import cz.basicland.blibs.shared.utils.StringUtils;
import cz.basicland.blibs.spigot.commands.LCommand;
import cz.basicland.blibs.spigot.hooks.Check;
import net.kyori.adventure.text.minimessage.MiniMessage;
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
                    sender.sendRichMessage("<light_purple>Invalid integer");
                    return true;
                }
                if (requested > 64) {
                    sender.sendRichMessage("<light_purple>Warning: The amount requested is above 64. This may result in strange behaviour.");
                }
                amount = requested;
            }

            if (target == null) {
                sender.sendRichMessage("<light_purple>The specified player could not be found.");
                return true;
            }

            Pouch pouch = null;
            for (Pouch p : plugin.getPouches()) {
                if (p.id().equals(args[0])) {
                    pouch = p;
                    break;
                }
            }
            if (pouch == null) {
                sender.sendRichMessage("<light_purple>The pouch </light_purple><dark_red>" + args[0] + "</dark_red><light_purple> could not be found.");
                return true;
            }
            if (target.getInventory().firstEmpty() == -1) {
                sender.sendRichMessage(plugin.getMessage(MoneyPouch.Message.FULL_INV));
                return true;
            }

            for (int i = 0; i < amount; i++) {
                target.getInventory().addItem(Check.addStack(new ItemStack(pouch.itemStack().getStack())));
            }

            sender.sendRichMessage(plugin.getMessage(MoneyPouch.Message.GIVE_ITEM).replace("%player%",
                    target.getName()).replace("%item%", MiniMessage.miniMessage().serialize(pouch.itemStack().getTitle())));
            if (plugin.getCfg().getBoolean("options.show-receive-message", true)) {
                target.sendRichMessage(plugin.getMessage(MoneyPouch.Message.RECEIVE_ITEM).replace("%player%",
                        target.getName()).replace("%item%", MiniMessage.miniMessage().serialize(pouch.itemStack().getTitle())));
            }
            return true;
        }

        sender.sendRichMessage("<bold><dark_purple> Money Pouch (ver " + plugin.getDescription().getVersion() + ")");
        sender.sendRichMessage("<gray><> = required, [] = optional");
        sender.sendRichMessage("<light_purple>/mp :</light_purple><gray> view this menu");
        sender.sendRichMessage("<light_purple>/mp <tier> [player] [amount] :</light_purple><gray> give <item> to [player] (or self if blank)");
        sender.sendRichMessage("<light_purple>/mpshop :</light_purple><gray> open the shop");
        sender.sendRichMessage("<light_purple>/mpa list :</light_purple><gray> list all pouches");
        sender.sendRichMessage("<light_purple>/mpa economies :</light_purple><gray> list all economies");
        sender.sendRichMessage("<light_purple>/mpa reload :</light_purple><gray> reload the config");
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (args.length == 1) {
            List<String> pouchNames = new ArrayList<>();
            for (Pouch pouch : plugin.getPouches()) {
                pouchNames.add(pouch.id());
            }
            return StringUtils.copyMatches(args[0], pouchNames);
        }

        return Collections.emptyList();
    }
}
