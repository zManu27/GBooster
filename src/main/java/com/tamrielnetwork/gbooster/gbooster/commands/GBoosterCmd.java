package com.tamrielnetwork.gbooster.gbooster.commands;

import com.google.common.collect.ImmutableMap;
import com.tamrielnetwork.gbooster.gbooster.GBooster;
import com.tamrielnetwork.gbooster.gbooster.boosters.Booster;
import com.tamrielnetwork.gbooster.gbooster.player.BoosterPlayer;
import com.tamrielnetwork.gbooster.gbooster.utils.Utils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class GBoosterCmd implements CommandExecutor {

    private final GBooster main = JavaPlugin.getPlugin(GBooster.class);

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (args.length == 0){
            Utils.sendMessage(sender, "no-args");
            return true;
        }

        switch (args[0]){
            case "give":
                executeGive(sender, args);
                break;
            case "use":
                executeUse(sender, args);
                break;
            default:
                Utils.sendMessage(sender, "invalid-option");
        }

        return true;
    }

    private void executeGive(CommandSender sender, String[] args){
        //Check permissions
        if (!sender.hasPermission("gbooster.give")){
            Utils.sendMessage(sender, "no-perms");
            return;
        }

        //Check args length
        if (args.length != 4){
            Utils.sendMessage(sender, "invalid-id");
            return;
        }

        //Get booster by id
        Booster booster = main.getBoostersManager().getBoosterById(args[2]);

        //Check if the given booster id is valid
        if (booster == null){
            Utils.sendMessage(sender, "invalid-id");
            return;
        }

        //Get booster player by name
        BoosterPlayer boosterPlayer = main.getPlayerStorage().getBoosterPlayerByName(args[1]);

        //Check if the given player is registered
        if (boosterPlayer == null){
            Utils.sendMessage(sender, "invalid-player");
            return;
        }

        //Check if the 4th arg is a number
        if (!StringUtils.isNumeric(args[3])){
            Utils.sendMessage(sender, "invalid-amount");
            return;
        }

        int amount = Integer.parseInt(args[3]);

        //Check if the amount is greater than 0
        if (amount <= 0){
            Utils.sendMessage(sender, "invalid-amount");
            return;
        }

        boosterPlayer.addBooster(booster.getId(), amount);

        //Send messages
        Player onlineBoosterPlayer = Bukkit.getPlayer(boosterPlayer.getUuid());

        if (onlineBoosterPlayer != null){
            Utils.sendMessage(onlineBoosterPlayer, ImmutableMap.of(
                    "%amount%", String.valueOf(amount),
                    "%booster%", booster.getId()),
                    "receive-boosters");
        }

        Utils.sendMessage(sender, ImmutableMap.of(
                "%amount%", String.valueOf(amount),
                "%booster%", booster.getId(),
                "%player%", boosterPlayer.getName()),
                "give-boosters");
    }

    private void executeUse(CommandSender sender, String[] args){

        //Check if command sender is a player
        if (!(sender instanceof Player)){
            Utils.sendMessage(sender, "player-only");
            return;
        }

        BoosterPlayer boosterPlayer = main.getPlayerStorage().getBoosterPlayerByUUID(((Player) sender).getUniqueId());

        //Check args length
        if (args.length != 2){
            Utils.sendMessage(sender, "no-args");
            return;
        }

        //Get booster by id
        Booster booster = main.getBoostersManager().getBoosterById(args[1]);

        //Check if the given booster id is valid
        if (booster == null){
            Utils.sendMessage(sender, "invalid-id");
            return;
        }

        //Check if there is an active countdown
        if (!boosterPlayer.canUseBooster(booster)){
            Utils.sendMessage(sender, "countdown-active");
            return;
        }

        //Check if the max limit is reached
        if (!main.getActiveBoostersManager().canUseBooster(booster)){
            Utils.sendMessage(sender, "max-limit");
            return;
        }

        //Try to remove a booster from a player
        if (!boosterPlayer.takeBooster(booster)){
            Utils.sendMessage(sender, "no-booster");
            return;
        }

        main.getActiveBoostersManager().activateBooster(booster);
        Utils.sendMessage(sender, "active-booster");
    }
}