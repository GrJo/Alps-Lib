import java.util.List;
import java.util.Properties;
import java.util.Arrays;
import java.util.ArrayList;

import com.alpsbte.alpslib.libpsterra.core.DatabaseConnection;
import com.alpsbte.alpslib.libpsterra.core.NetworkAPIConnection;
import com.alpsbte.alpslib.libpsterra.core.api.PlotSystemAPI;
import com.alpsbte.alpslib.libpsterra.core.config.ConfigPaths;
import com.alpsbte.alpslib.libpsterra.core.plotsystem.CityProject;
import com.alpsbte.alpslib.libpsterra.core.plotsystem.Country;
import com.alpsbte.alpslib.libpsterra.core.plotsystem.Difficulty;
import com.alpsbte.alpslib.libpsterra.core.plotsystem.FTPConfiguration;
import com.alpsbte.alpslib.libpsterra.core.plotsystem.Plot;
import com.alpsbte.alpslib.libpsterra.core.plotsystem.Server;
import com.alpsbte.alpslib.libpsterra.utils.FTPManager;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.sk89q.worldedit.Vector;

import org.apache.commons.vfs2.impl.StandardFileSystemManager;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;
import java.io.File;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.hamcrest.Matchers.*;

public class TestConnections {
    private DatabaseConnection createDBConnection() throws Exception{
        File configFile = new File("src/test/resources", "config.yml"); //The file (you can name it what you want)
        YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile); //Take the file and basically turning it into a .yml file
        
        //read db access
        String dbURL = config.getString(ConfigPaths.DATABASE_URL);
        String dbName = config.getString(ConfigPaths.DATABASE_NAME);
        String dbUusername = config.getString(ConfigPaths.DATABASE_USERNAME);
        String dbPassword = config.getString(ConfigPaths.DATABASE_PASSWORD);
        String teamApiKey = config.getString(ConfigPaths.API_KEY);
        return new DatabaseConnection(dbURL, dbName, dbUusername, dbPassword, teamApiKey);
    }

    private NetworkAPIConnection createAPIconnection() throws Exception{
        File configFile = new File("src/test/resources", "config.yml"); //The file (you can name it what you want)
        YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile); //Take the file and basically turning it into a .yml file
        
        String teamApiKey = config.getString(ConfigPaths.API_KEY);
        String apiHost = config.getString(ConfigPaths.API_URL);
        
        int apiPort = config.getInt(ConfigPaths.API_KEY);

        return new NetworkAPIConnection(apiHost, apiPort, teamApiKey);
    }

    private Plot getPlot(int plotID, PlotSystemAPI api, String teamApiKey) throws Exception {
        
        try {
            List<Plot> plots = api.getPSTeamPlots(teamApiKey);
            for (Plot p : plots){
                if (p.id == plotID)
                    return p;
        }
            return null;
        } catch (Exception ex) {
             return null;
        }
    }

    @Test
    public void testPlotSystemAPI_READ() throws Exception {
        File configFile = new File("src/test/resources", "config.yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        
        String team_apikey = config.getString(ConfigPaths.API_KEY);
        String apiHost = config.getString(ConfigPaths.API_URL);

        //Test PlostSystemAPI class 
        PlotSystemAPI api = new PlotSystemAPI(apiHost,8080);
        int builderCount = api.getPSBuilderCount();
        System.out.println("Builder Count: " + builderCount);
        assertEquals(17, builderCount);

        Thread.sleep(200);

        List<Difficulty> difficulties = api.getPSDifficulties();
        System.out.println(difficulties);

        Thread.sleep(200);        
        System.out.println("-------countries-----------");

        List<Country> countries = api.getPSTeamCountries(team_apikey);
        for (Country c : countries){
            System.out.println((c));
        }

        Thread.sleep(200);        
        System.out.println("-------cities-----------");

        List<CityProject> cities = api.getPSTeamCities(team_apikey);
        for (CityProject c : cities){
            System.out.println((c.name + ": "+ c));
        }
               
        Thread.sleep(200);        
        System.out.println("-------plots-----------");

        List<Plot> plots = api.getPSTeamPlots(team_apikey);
        for (Plot p : plots){
            System.out.println((p.mc_coordinates + ": "+ p));
        }
        
        Thread.sleep(200);
        System.out.println("-------servers-----------");

        List<Server> servers = api.getPSTeamServers(team_apikey);
        for (Server s : servers){
            System.out.println((s.name + ": "+ s));
            assert(s.ftp_configuration_id > 0);
        }

        Thread.sleep(200);
        System.out.println("-------ftp configs-----------");

        List<FTPConfiguration> configs = api.getPSTeamFTPConfigurations(team_apikey);
        for (FTPConfiguration c : configs){
            System.out.println((c.address +  ": "+ c));
        }                     

    }

    @Test
    public void testPlotSystemAPI_MODIFY() throws Exception {
        File configFile = new File("src/test/resources", "config.yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        
        String team_apikey = config.getString(ConfigPaths.API_KEY);
        String apiHost = config.getString(ConfigPaths.API_URL);

        //Test PlostSystemAPI class 
        PlotSystemAPI api = new PlotSystemAPI(apiHost,8080);
            
        //check plot 1 has "pasted: 1"
        Plot plot = getPlot(1, api, team_apikey);
        assertEquals(1, plot.id);
        assertEquals(1, plot.pasted);

        //change to "pasted: 0"
        api.updatePSPlot(1, Arrays.asList("\"pasted\": 0"), team_apikey);
        plot = getPlot(1, api, team_apikey);
        assertEquals(1, plot.id);
        assertEquals(0, plot.pasted);


        //change back
        api.updatePSPlot(1, Arrays.asList("\"pasted\": 1"), team_apikey);
        plot = getPlot(1, api, team_apikey);
        assertEquals(1, plot.id);
        assertEquals(1, plot.pasted);
    }

    @Test
    public void testPlotSystemAPI_CREATE() throws Exception {
        File configFile = new File("src/test/resources", "config.yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        
        String team_apikey = config.getString(ConfigPaths.API_KEY);
        String apiHost = config.getString(ConfigPaths.API_URL);

        //Test PlostSystemAPI class 
        PlotSystemAPI api = new PlotSystemAPI(apiHost,8080);
            
        //plot direct creation, returns plotID for potential undo/delete
        int plotID = api.createPSPlot(1,1,
            new Vector(1.1,2.2,3.3),"4078352.0,-4550687.0|4078371.0,-4550675.0|4078370.0,-4550669.0", 
            3, team_apikey );


        api.deletePSPlot(plotID, team_apikey);
    }
    @Test
    public void compareConnections_AllCities() throws Exception{
        DatabaseConnection db = createDBConnection();
        NetworkAPIConnection api = createAPIconnection();

        // ------------ cityprojects: all --------------
        List<CityProject> citiesDB = new ArrayList<>();
        boolean resultDB = db.getAllCityProjects(citiesDB);
        List<CityProject> citiesAPI = new ArrayList<>();
        boolean resultAPI = api.getAllCityProjects(citiesAPI);
        
        assertEquals(true, resultDB);
        assertEquals(true, resultAPI);
        assertEquals(citiesDB.size(), citiesAPI.size());
        //assertThat(citiesDB, containsInAnyOrder(citiesAPI));
        for (CityProject cityDB : citiesDB){
            assertThat(citiesAPI, hasItem(samePropertyValuesAs(cityDB)));
            System.out.println("checking city " + cityDB.name);
            Country cityCountry = db.getCountry(cityDB.country_id);
        }




    }

    @Test
    public void compareConnections_SingleCity() throws Exception{
        DatabaseConnection db = createDBConnection();
        NetworkAPIConnection api = createAPIconnection();

        //test both interfaces return the same values

        //--------------- cityProjects ---------------------

        CityProject city1DB = db.getCityProject(1) ;
        CityProject city1API = api.getCityProject(1) ;
        assertNotNull(city1API);
        assertThat(city1DB, samePropertyValuesAs(city1API));


    }

    
    @Test
    public void compareConnections_Plots() throws Exception{
        DatabaseConnection db = createDBConnection();
        NetworkAPIConnection api = createAPIconnection();

        //test both interfaces return the same values

        //--------------plots --------------------------

        Plot plot1DB = db.getPlot(1);
        Plot plot1API = api.getPlot(1);
        assertThat(plot1DB, samePropertyValuesAs(plot1API));

        List<Plot> plotsDB = db.getCompletedAndUnpastedPlots() ;
        List<Plot> plotsAPI = api.getCompletedAndUnpastedPlots() ;
        assertEquals(plotsDB.size(), plotsAPI.size());
        for (Plot plotDB : plotsDB){
            assertThat(plotsAPI, hasItem(samePropertyValuesAs(plotDB)));
        }
    }

    @Test
    public void compareConnections_Countries() throws Exception{
        DatabaseConnection db = createDBConnection();
        NetworkAPIConnection api = createAPIconnection();

        //test both interfaces return the same values

        //--------------plots --------------------------

        Country c1DB = db.getCountry(1);
        Country c1API = api.getCountry(1);
        assertNotNull(c1DB);
        assertThat(c1DB, samePropertyValuesAs(c1API));

    }

    @Test
    public void compareConnections_Server() throws Exception{
        DatabaseConnection db = createDBConnection();
        NetworkAPIConnection api = createAPIconnection();

        //test both interfaces return the same values

        //--------------plots --------------------------

        Server s1DB = db.getServer(1);
        Server s1API = api.getServer(1);
        assertNotNull(s1API);
        assertThat(s1DB, samePropertyValuesAs(s1API));

    }

    @Test
    public void compareConnections_FTP() throws Exception{
        DatabaseConnection db = createDBConnection();
        NetworkAPIConnection api = createAPIconnection();

        //test both interfaces return the same values

        FTPConfiguration s1DB = db.getFTPConfiguration(3);
        FTPConfiguration s1API = api.getFTPConfiguration(3);
        assertNotNull(s1API);
        assertThat(s1DB, samePropertyValuesAs(s1API));

    }

    @Test
    public void testReadFTP() throws Exception{
        NetworkAPIConnection connection = createAPIconnection();
        DatabaseConnection connectionDB = createDBConnection();

        CityProject city1 = connection.getCityProject(1);
        FTPConfiguration ftpConfiguration = connection.getFTPConfiguration(city1);
        FTPConfiguration ftpConfigurationDB = connectionDB.getFTPConfiguration(city1);
        
        System.out.println(ftpConfiguration.address + ", user " + ftpConfiguration.username);
        System.out.println(ftpConfigurationDB.address + ", user " + ftpConfigurationDB.username);
                        
    }

    @Test
    public void testSFTPConnection() throws Exception{
        DatabaseConnection db = createDBConnection();

        CityProject cityProject = db.getCityProject(1);
        FTPConfiguration ftpConfiguration = db.getFTPConfiguration(cityProject);

        //FTPManager.uploadSchematics(FTPManager.getFTPUrl(ftpConfiguration, cityProject.id), new File(plotFilePath));
        //StandardFileSystemManager fileManager = new StandardFileSystemManager();
        JSch jsch = new JSch();
        try {
            // Properties config = new Properties();
            // config.put("cipher.s2c", 
            //         "aes128-ctr,aes128-cbc,3des-ctr,3des-cbc,blowfish-cbc,aes192-ctr,aes192-cbc,aes256-ctr,aes256-cbc");
            // config.put("cipher.c2s",
            //         "aes128-ctr,aes128-cbc,3des-ctr,3des-cbc,blowfish-cbc,aes192-ctr,aes192-cbc,aes256-ctr,aes256-cbc");
            // config.put("kex", "diffie-hellman-group1-sha1,diffie-hellman-group14-sha1,diffie-hellman-group-exchange-sha1,diffie-hellman-group-exchange-sha256");
            
            Session jschSession = jsch.getSession(ftpConfiguration.username,ftpConfiguration.address,ftpConfiguration.port);

            jschSession.setConfig("StrictHostKeyChecking", "no");
            jschSession.setPassword(ftpConfiguration.password);
            //jschSession.setConfig(config);

            System.out.println("Trying to connect to " + ftpConfiguration.address + " with user " + ftpConfiguration.username);
        
            jschSession.connect();
            jschSession.disconnect();
        } catch (JSchException e) {
            e.printStackTrace();
        } 
        
    }
    
}
