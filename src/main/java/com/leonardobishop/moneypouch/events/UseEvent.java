package com.leonardobishop.moneypouch.events;

import com.leonardobishop.moneypouch.MoneyPouch;
import com.leonardobishop.moneypouch.Pouch;
import cz.basicland.blibs.spigot.hooks.Check;
import cz.basicland.blibs.spigot.utils.item.NBT;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.util.Ticks;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;

public class UseEvent implements Listener {

    private final MoneyPouch plugin;
    private final ArrayList<UUID> opening = new ArrayList<>();

    final String prefixColour;
    final String suffixColour;
    final String revealColour;
    final String obfuscateColour;
    final String obfuscateDigitChar;
    final String obfuscateDelimiterChar;
    final boolean delimiter;
    final boolean revealComma;
    final boolean reversePouchReveal;


    public UseEvent(MoneyPouch plugin) {
        this.plugin = plugin;

        prefixColour = plugin.getCfg().getString("pouches.title.prefix-colour");
        suffixColour = plugin.getCfg().getString("pouches.title.suffix-colour");
        revealColour = plugin.getCfg().getString("pouches.title.reveal-colour");
        obfuscateColour = plugin.getCfg().getString("pouches.title.obfuscate-colour");
        obfuscateDigitChar = plugin.getCfg().getString("pouches.title.obfuscate-digit-char", "#");
        obfuscateDelimiterChar = ",";
        delimiter = plugin.getCfg().getBoolean("pouches.title.format.enabled", false);
        revealComma = plugin.getCfg().getBoolean("pouches.title.format.reveal-comma", false);
        reversePouchReveal = plugin.getCfg().getBoolean("reverse-pouch-reveal");
    }

    @EventHandler
    public void onPlayerUse(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_AIR) {
            return;
        }
        ItemStack hand = player.getInventory().getItemInMainHand();
        if (hand.getType() == Material.AIR) return;

        NBT nbt = new NBT(hand);
        String id = nbt.getString("moneyPouch");
        if (id == null) return;

        for (Pouch p : plugin.getPouches()) {
            if (!id.equalsIgnoreCase(p.id())) continue;

            event.setCancelled(true);

            if (opening.contains(player.getUniqueId())) {
                player.sendRichMessage(plugin.getMessage(MoneyPouch.Message.ALREADY_OPENING));
                return;
            }

            if (Check.dupe(hand, player, false, true)) {
                event.setCancelled(true);
                return;
            }

            String permission = "moneypouch.pouches." + p.id();
            if (p.permissionRequired() && !player.hasPermission(permission)) {
                player.sendRichMessage(plugin.getMessage(MoneyPouch.Message.NO_PERMISSION));
                return;
            }

            if (hand.getAmount() == 1) {
                player.getInventory().setItemInMainHand(null);
            } else {
                player.getInventory().getItemInMainHand().setAmount(hand.getAmount() - 1);
                player.updateInventory();
            }

            usePouch(player, p);
            return;
        }
    }

    private void playSound(Player player, String name) {
        try {
            player.playSound(player.getLocation(), Sound.valueOf(name), 3, 1);
        } catch (Exception ignored) {
        }
    }

    private void usePouch(Player player, Pouch p) {
        opening.add(player.getUniqueId());
        long random = ThreadLocalRandom.current().nextLong(p.minRange(), p.maxRange());
        playSound(player, plugin.getCfg().getString("pouches.sound.opensound"));
        new BukkitRunnable() {
            final String number = (delimiter ? (new DecimalFormat("#,###").format(random)) : String.valueOf(random));
            int position = 0;
            boolean complete = false;

            @Override
            public void run() {
                if (player.isOnline()) {
                    playSound(player, plugin.getCfg().getString("pouches.sound.revealsound"));
                    String prefix = prefixColour + p.economyType().getPrefix();
                    StringBuilder viewedTitle = new StringBuilder();
                    String suffix = suffixColour + p.economyType().getSuffix();
                    for (int i = 0; i < position; i++) {
                        if (reversePouchReveal) {
                            viewedTitle.insert(0, number.charAt(number.length() - i - 1)).insert(0, revealColour);
                        } else {
                            viewedTitle.append(revealColour).append(number.charAt(i));
                        }
                        if ((i == (position - 1)) && (position != number.length())
                                && (reversePouchReveal
                                ? (revealComma && (number.charAt(number.length() - i - 1)) == ',')
                                : (revealComma && (number.charAt(i + 1)) == ','))) {
                            position++;
                        }
                    }
                    for (int i = position; i < number.length(); i++) {
                        if (reversePouchReveal) {
                            char at = number.charAt(number.length() - i - 1);
                            if (at == ',') {
                                if (revealComma) {
                                    viewedTitle.insert(0, at).insert(0, revealColour);
                                } else
                                    viewedTitle.insert(0, "</obfuscated>").insert(0, obfuscateDelimiterChar).insert(0, "<obfuscated>").insert(0, obfuscateColour);
                            } else
                                viewedTitle.insert(0, "</obfuscated>").insert(0, obfuscateDigitChar).insert(0, "<obfuscated>").insert(0, obfuscateColour);
                        } else {
                            char at = number.charAt(i);
                            if (at == ',') {
                                if (revealComma) viewedTitle.append(revealColour).append(at);
                                else
                                    viewedTitle.append(obfuscateColour).append("<obfuscated>").append(obfuscateDelimiterChar).append("</obfuscated>");
                            } else
                                viewedTitle.append(obfuscateColour).append("<obfuscated>").append(obfuscateDigitChar).append("</obfuscated>");
                        }
                    }

                    Component main = MiniMessage.miniMessage().deserialize(prefix + viewedTitle + suffix);
                    Component sub = MiniMessage.miniMessage().deserialize(plugin.getCfg().getString("pouches.title.subtitle"));
                    Title.Times times = Title.Times.times(Ticks.duration(0), Ticks.duration(50), Ticks.duration(20));
                    player.showTitle(Title.title(main, sub, times));
                } else {
                    position = number.length();
                }
                if (position == number.length()) {
                    if (complete) {    // prevent running twice
                        return;
                    }
                    complete = true;

                    opening.remove(player.getUniqueId());
                    this.cancel();
                    try {
                        p.economyType().processPayment(player, random);
                        if (player.isOnline()) {
                            playSound(player, plugin.getCfg().getString("pouches.sound.endsound"));
                            player.sendRichMessage(plugin.getMessage(MoneyPouch.Message.PRIZE_MESSAGE)
                                    .replace("%prefix%", p.economyType().getPrefix())
                                    .replace("%suffix%", p.economyType().getSuffix())
                                    .replace("%prize%", NumberFormat.getInstance().format(random)));
                        }
                    } catch (Throwable t) {
                        if (plugin.getCfg().getBoolean("error-handling.log-failed-transactions", true)) {
                            plugin.getLogger().log(Level.SEVERE, "Failed to process payment from pouch with ID '" + p.id() + "' for player '" + player.getName()
                                    + "' of amount " + random + " of economy " + p.economyType().toString() + ": " + t.getMessage());
                        }
                        if (player.isOnline()) {
                            if (plugin.getCfg().getBoolean("error-handling.refund-pouch", false)) {
                                player.getInventory().addItem(p.itemStack().getStack());
                            }
                            player.sendRichMessage(plugin.getMessage(MoneyPouch.Message.REWARD_ERROR)
                                    .replace("%prefix%", p.economyType().getPrefix())
                                    .replace("%suffix%", p.economyType().getSuffix())
                                    .replace("%prize%", NumberFormat.getInstance().format(random)));
                        }
                        t.printStackTrace();
                    }
                    return;
                }
                position++;
            }
        }.runTaskTimer(plugin, 10, plugin.getCfg().getInt("pouches.title.speed-in-tick"));
    }

}
