package com.alpsbte.alpslib.libpsterra.utils;

import com.alpsbte.alpslib.libpsterra.core.config.ConfigPaths;
import me.arcaniax.hdb.api.HeadDatabaseAPI;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

public class Utils {

    // Head Database API
    public static HeadDatabaseAPI headDatabaseAPI;

    /**
     * Prefix used for all command permissions.
     */
    public static final String permissionPrefix = "plotsystem";

    public static ItemStack getItemHead(String headID) {
        return headDatabaseAPI != null && headID != null ? headDatabaseAPI.getItemHead(headID) : new ItemBuilder(Material.SKULL_ITEM, 1, (byte) 3).build();
    }

    // Player Messages
    public static String messagePrefix(FileConfiguration config){
        return config.getString(ConfigPaths.MESSAGE_PREFIX) + " ";
    }
    public static String getInfoMessageFormat(String info, FileConfiguration config) {
        return messagePrefix(config) + config.getString(ConfigPaths.MESSAGE_INFO_COLOUR) + info;
    }

    public static String getErrorMessageFormat(String error, FileConfiguration config) {
        return messagePrefix(config) + config.getString(ConfigPaths.MESSAGE_ERROR_COLOUR) + error;
    }

    public static boolean hasPermission(CommandSender sender, String permissionNode) {
        return sender.hasPermission(permissionPrefix + "." + permissionNode);
    }

    public static Integer tryParseInt(String integer) {
        try {
            return Integer.parseInt(integer);
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
