package com.alpsbte.alpslib.libpsterra.commands;

import com.alpsbte.alpslib.libpsterra.core.Connection;
import com.alpsbte.alpslib.libpsterra.core.plotsystem.CityProject;
import com.alpsbte.alpslib.libpsterra.core.plotsystem.Plot;
import com.alpsbte.alpslib.libpsterra.core.plotsystem.PlotPaster;
import com.alpsbte.alpslib.libpsterra.utils.Utils;
import com.alpsbte.alpslib.utils.AlpsUtils;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.logging.Level;

public class CMD_PastePlot implements CommandExecutor {
    private PlotPaster plotPaster;
    private Connection connection;
    private FileConfiguration config;

    public CMD_PastePlot(PlotPaster plotPaster, Connection connection, FileConfiguration config){
        this.plotPaster = plotPaster;
        this.connection = connection;
        this.config = config;

    }
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {
        if(Utils.hasPermission(sender, "pasteplot")) {

            try {

                if (args.length >= 1 && AlpsUtils.tryParseInt(args[0]) != null) {
                    int plotID = Integer.parseInt(args[0]);


                    Plot plot = connection.getPlot(plotID);
                    CityProject cityProject = connection.getCityProject(plot.city_project_id);
                    if (plot.status.equals("completed")) {


                         try {
                            plotPaster.pastePlotSchematic(plotID, cityProject, plotPaster.world, plot.mc_coordinates, plot.version, plotPaster.fastMode);
                            Bukkit.broadcastMessage("§7§l>§a Pasted §61 §aplot!");
                        } catch (Exception ex) {
                            Bukkit.getLogger().log(Level.SEVERE, "An error occurred while pasting plot with the ID " + plotID + "!", ex);
                            sender.sendMessage(Utils.getErrorMessageFormat("An error occurred while pasting plot!", config));
                        }
                    } else 
                        sender.sendMessage(Utils.getErrorMessageFormat("Plot with the ID " + plotID + " is not completed!", config));

                    
                } else {
                    sender.sendMessage(Utils.getErrorMessageFormat("Incorrect Input! Try /pasteplot <ID>", config));
                }
            } catch (Exception ex) {
                Bukkit.getLogger().log(Level.SEVERE, "An error occurred while pasting plot!", ex);
                sender.sendMessage(Utils.getErrorMessageFormat("An error occurred while pasting plot!", config));
            }
        
        }//endif permission
        return true;
    }
}
