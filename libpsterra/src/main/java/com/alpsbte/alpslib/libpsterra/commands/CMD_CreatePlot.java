package com.alpsbte.alpslib.libpsterra.commands;

import com.alpsbte.alpslib.libpsterra.core.Connection;
import com.alpsbte.alpslib.libpsterra.core.plotsystem.CreatePlotMenu;
import com.alpsbte.alpslib.libpsterra.core.plotsystem.PlotCreator;
import com.alpsbte.alpslib.libpsterra.utils.Utils;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.logging.Level;

public class CMD_CreatePlot implements CommandExecutor {
    private PlotCreator plotCreator;
    private Connection connection;
    private FileConfiguration config;

    public CMD_CreatePlot(PlotCreator creator, Connection connection, FileConfiguration config){
        this.plotCreator = creator;
        this.connection = connection;
        this.config = config;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {
        if(sender instanceof Player) {
            if(Utils.hasPermission(sender, "createplot")) {
                try {
                    if (args.length > 1) {
                        if (args[0].equalsIgnoreCase("tutorial") && Utils.tryParseInt(args[1]) != null) {
                            plotCreator.createTutorialPlot(((Player) sender).getPlayer(), Integer.parseInt(args[1]));
                            return true;
                        }
                    }
                    new CreatePlotMenu(((Player) sender).getPlayer(), connection, plotCreator);
                } catch (Exception ex) {
                    Bukkit.getLogger().log(Level.SEVERE, "An error occurred while opening create plot menu!", ex);
                    sender.sendMessage(Utils.getErrorMessageFormat("An error occurred while opening create plot menu!", config));
                }
            }
        }
        return true;
    }
}
