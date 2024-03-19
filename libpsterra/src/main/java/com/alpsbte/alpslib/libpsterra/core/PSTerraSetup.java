package com.alpsbte.alpslib.libpsterra.core;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.ipvp.canvas.MenuFunctionListener;
import com.alpsbte.alpslib.libpsterra.commands.CMD_CreatePlot;
import com.alpsbte.alpslib.libpsterra.commands.CMD_PastePlot;
import com.alpsbte.alpslib.libpsterra.core.config.ConfigManager;
import com.alpsbte.alpslib.libpsterra.core.config.ConfigPaths;
import com.alpsbte.alpslib.libpsterra.core.config.DataMode;
import com.alpsbte.alpslib.libpsterra.core.plotsystem.PlotCreator;
import com.alpsbte.alpslib.libpsterra.core.plotsystem.PlotPaster;
import com.alpsbte.alpslib.libpsterra.utils.FTPManager;
import com.alpsbte.alpslib.utils.head.AlpsHeadEventListener;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;

public class PSTerraSetup {
    public PlotCreator plotCreator;
    public PlotPaster plotPaster;
    public Connection connection;
    public ConfigManager configManager;

    /**
     * creates/instantiates all the neccessary classes and event listeners required for plugin operation
     * @param plugin
     * @throws Exception
     */
    public static PSTerraSetup setupPlugin(JavaPlugin plugin, String version) throws Exception{
        PSTerraSetup result = new PSTerraSetup();

        

        String successPrefix = ChatColor.DARK_GRAY + "[" + ChatColor.DARK_GREEN + "✔" + ChatColor.DARK_GRAY + "] " + ChatColor.GRAY;
        String errorPrefix = ChatColor.DARK_GRAY + "[" + ChatColor.RED + "X" + ChatColor.DARK_GRAY + "] " + ChatColor.GRAY;

        Bukkit.getConsoleSender().sendMessage(ChatColor.GOLD + "--------------- Plot-System-Terra V" + version + " ----------------");
        Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_GREEN + "Starting plugin...");
        Bukkit.getConsoleSender().sendMessage(" ");

        // Check for required dependencies, if it returns false disable plugin
        if (!DependencyManager.checkForRequiredDependencies(plugin)) {
            Bukkit.getConsoleSender().sendMessage(errorPrefix + "Could not load required dependencies.");
            Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "Missing Dependencies:");
            DependencyManager.missingDependencies.forEach(dependency -> Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + " - " + dependency));
            throw new RuntimeException("Could not load required dependencies");
        }
        Bukkit.getConsoleSender().sendMessage(successPrefix + "Successfully loaded required dependencies.");

        // Load config
        result.configManager = new ConfigManager(plugin);
        Bukkit.getConsoleSender().sendMessage(successPrefix + "Successfully loaded configuration file.");
        result.configManager.reloadConfigs();
        FileConfiguration configFile = result.configManager.getConfig();

        // Initialize connection


        Bukkit.getConsoleSender().sendMessage(successPrefix + "datamode:"+ configFile.getString(ConfigPaths.DATA_MODE));
        if(configFile.getString(ConfigPaths.DATA_MODE).equalsIgnoreCase(DataMode.DATABASE.toString())){

            String URL = configFile.getString(ConfigPaths.DATABASE_URL);
            String name = configFile.getString(ConfigPaths.DATABASE_NAME);
            String username = configFile.getString(ConfigPaths.DATABASE_USERNAME);
            String password = configFile.getString(ConfigPaths.DATABASE_PASSWORD);
            String teamApiKey = configFile.getString(ConfigPaths.API_KEY);
            
            result.connection = new DatabaseConnection(URL, name, username, password, teamApiKey);// DatabaseConnection.InitializeDatabase();
            Bukkit.getConsoleSender().sendMessage(successPrefix + "Successfully initialized database connection.");
        }else{
            String teamApiKey = configFile.getString(ConfigPaths.API_KEY);
            String apiHost = configFile.getString(ConfigPaths.API_URL);
            
            int apiPort = configFile.getInt(ConfigPaths.API_KEY);

            result.connection = new NetworkAPIConnection(apiHost, apiPort, teamApiKey);
            // String name = configFile.getString(ConfigPaths.DATABASE_NAME);
            // String username = configFile.getString(ConfigPaths.DATABASE_USERNAME);
            // String password = configFile.getString(ConfigPaths.DATABASE_PASSWORD);
            Bukkit.getConsoleSender().sendMessage(successPrefix + "Successfully initialized API connection.");
        

        }
        if (result.connection == null)
            throw new RuntimeException("Connection initialization failed");


        //check FTP
        FTPManager.testSFTPConnection_VFS2(result.connection);
        Bukkit.getConsoleSender().sendMessage(successPrefix + "Successfully tested FTP connection.");
            
        // Register event listeners
        plugin.getServer().getPluginManager().registerEvents(new AlpsHeadEventListener(), plugin);
        plugin.getServer().getPluginManager().registerEvents(new MenuFunctionListener(), plugin);
        Bukkit.getConsoleSender().sendMessage(successPrefix + "Successfully registered event listeners.");

        result.plotCreator = new PlotCreator(plugin, configFile, result.connection);

        // Start checking for plots to paste
        result.plotPaster = new PlotPaster(plugin, configFile, result.connection);
        result.plotPaster.start();



        // Register commands

        plugin.getCommand("createplot").setExecutor(new CMD_CreatePlot(result.plotCreator, result.connection, configFile));
        plugin.getCommand("pasteplot").setExecutor(new CMD_PastePlot(result.plotPaster, result.connection, configFile));

        Bukkit.getConsoleSender().sendMessage(successPrefix + "Successfully registered commands.");


        return result;
    }

    public static class DependencyManager {

        // List with all missing dependencies
        private final static List<String> missingDependencies = new ArrayList<>();

        /**
         * Check for all required dependencies and inform in console about missing dependencies
         * @return True if all dependencies are present
         */
        private static boolean checkForRequiredDependencies(Plugin plugin) {
            PluginManager pluginManager = plugin.getServer().getPluginManager();

            if (!pluginManager.isPluginEnabled("WorldEdit")) {
                missingDependencies.add("WorldEdit (V6.1.9)");
            }

            if (!pluginManager.isPluginEnabled("HeadDatabase")) {
                missingDependencies.add("HeadDatabase");
            }

            return missingDependencies.isEmpty();
        }

        /**
         * @return World Edit instance
         */
        public static WorldEdit getWorldEdit() {
            return WorldEdit.getInstance();
        }

        /**
         * @return World Edit Plugin
         */
        public static WorldEditPlugin getWorldEditPlugin() {
            return (WorldEditPlugin) Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");
        }
    }
};
