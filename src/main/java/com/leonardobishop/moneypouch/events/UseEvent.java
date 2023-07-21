package com.leonardobishop.moneypouch.events;

import com.leonardobishop.moneypouch.MoneyPouch;
import com.leonardobishop.moneypouch.Pouch;
import cz.basicland.blibs.spigot.hooks.Check;
import cz.basicland.blibs.spigot.utils.item.NBT;
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

    public UseEvent(MoneyPouch plugin) {
        this.plugin = plugin;
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
        if (!nbt.hasNBTTags()) return;

        String id = nbt.getString("moneyPouch");
        if (id == null) return;

        for (Pouch p : plugin.getPouches()) {
            if (!id.equalsIgnoreCase(p.getId())) continue;

            event.setCancelled(true);

            if (opening.contains(player.getUniqueId())) {
                player.sendMessage(plugin.getMessage(MoneyPouch.Message.ALREADY_OPENING));
                return;
            }

            if (Check.dupe(hand, player, false, true)) {
                event.setCancelled(true);
                return;
            }

            String permission = "moneypouch.pouches." + p.getId();
            if (p.isPermissionRequired() && !player.hasPermission(permission)) {
                player.sendMessage(plugin.getMessage(MoneyPouch.Message.NO_PERMISSION));
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
        long random = ThreadLocalRandom.current().nextLong(p.getMinRange(), p.getMaxRange());
        playSound(player, plugin.getCfg().getString("pouches.sound.opensound"));
        new BukkitRunnable() {
            final String prefixColour = plugin.getCfg().getStringCC("pouches.title.prefix-colour");
            final String suffixColour = plugin.getCfg().getStringCC("pouches.title.suffix-colour");
            final String revealColour = plugin.getCfg().getStringCC("pouches.title.reveal-colour");
            final String obfuscateColour = plugin.getCfg().getStringCC("pouches.title.obfuscate-colour");
            final String obfuscateDigitChar = plugin.getCfg().getString("pouches.title.obfuscate-digit-char", "#");
            final String obfuscateDelimiterChar = ",";
            final boolean delimiter = plugin.getCfg().getBoolean("pouches.title.format.enabled", false);
            final boolean revealComma = plugin.getCfg().getBoolean("pouches.title.format.reveal-comma", false);
            final String number = (delimiter ? (new DecimalFormat("#,###").format(random)) : String.valueOf(random));
            final boolean reversePouchReveal = plugin.getCfg().getBoolean("reverse-pouch-reveal");

            int position = 0;
            boolean complete = false;

            @Override
            public void run() {
                if (player.isOnline()) {
                    playSound(player, plugin.getCfg().getString("pouches.sound.revealsound"));
                    String prefix = prefixColour + p.getEconomyType().getPrefix();
                    StringBuilder viewedTitle = new StringBuilder();
                    String suffix = suffixColour + p.getEconomyType().getSuffix();
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
                                    viewedTitle.insert(0, obfuscateDelimiterChar).insert(0, "§k").insert(0, obfuscateColour);
                            } else
                                viewedTitle.insert(0, obfuscateDigitChar).insert(0, "§k").insert(0, obfuscateColour);
                        } else {
                            char at = number.charAt(i);
                            if (at == ',') {
                                if (revealComma) viewedTitle.append(revealColour).append(at);
                                else
                                    viewedTitle.append(obfuscateColour).append("§k").append(obfuscateDelimiterChar);
                            } else
                                viewedTitle.append(obfuscateColour).append("§k").append(obfuscateDigitChar);
                        }
                    }
                    player.sendTitle(prefix + viewedTitle + suffix, plugin.getCfg().getStringCC("pouches.title.subtitle"), 0, 50, 20);
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
                        p.getEconomyType().processPayment(player, random);
                        if (player.isOnline()) {
                            playSound(player, plugin.getCfg().getString("pouches.sound.endsound"));
                            player.sendMessage(plugin.getMessage(MoneyPouch.Message.PRIZE_MESSAGE)
                                    .replace("%prefix%", p.getEconomyType().getPrefix())
                                    .replace("%suffix%", p.getEconomyType().getSuffix())
                                    .replace("%prize%", NumberFormat.getInstance().format(random)));
                        }
                    } catch (Throwable t) {
                        if (plugin.getCfg().getBoolean("error-handling.log-failed-transactions", true)) {
                            plugin.getLogger().log(Level.SEVERE, "Failed to process payment from pouch with ID '" + p.getId() + "' for player '" + player.getName()
                                    + "' of amount " + random + " of economy " + p.getEconomyType().toString() + ": " + t.getMessage());
                        }
                        if (player.isOnline()) {
                            if (plugin.getCfg().getBoolean("error-handling.refund-pouch", false)) {
                                player.getInventory().addItem(p.getItemStack().getStack());
                            }
                            player.sendMessage(plugin.getMessage(MoneyPouch.Message.REWARD_ERROR)
                                    .replace("%prefix%", p.getEconomyType().getPrefix())
                                    .replace("%suffix%", p.getEconomyType().getSuffix())
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
