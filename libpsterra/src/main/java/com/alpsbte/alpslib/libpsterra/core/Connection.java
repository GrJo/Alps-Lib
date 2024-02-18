package com.alpsbte.alpslib.libpsterra.core;

import java.util.List;
import org.bukkit.entity.Player;
import com.sk89q.worldedit.Vector;

import com.alpsbte.alpslib.libpsterra.core.plotsystem.CityProject;
import com.alpsbte.alpslib.libpsterra.core.plotsystem.Country;
import com.alpsbte.alpslib.libpsterra.core.plotsystem.FTPConfiguration;
import com.alpsbte.alpslib.libpsterra.core.plotsystem.Plot;
import com.alpsbte.alpslib.libpsterra.core.plotsystem.Server;

public interface Connection {
    //public List<Integer> getBuilders() throws Exception;
    
    public boolean getAllCityProjects(List<CityProject> resultList) throws Exception;
    public CityProject getCityProject(int cityID) throws Exception;

    public Plot getPlot(int plotID) throws Exception;
    public List<Plot> getCompletedAndUnpastedPlots() throws Exception;
    
    public FTPConfiguration getFTPConfiguration(int ftp_configuration_id) throws Exception;
    public default FTPConfiguration getFTPConfiguration(CityProject cityProject) throws Exception
    {
        Country c = getCountry(cityProject.country_id);
        Server s = getServer(c.server_id);
        return (getFTPConfiguration(s.ftp_configuration_id));
    }

    public Server getServer(int serverID) throws Exception;
    
    public default Server getServer(CityProject cityProject) throws Exception
    {
        Country c = getCountry(cityProject.country_id);
        return getServer(c.server_id);
    }

    public Country getCountry(int countryID) throws Exception;
    List<Country> getTeamCountries() throws Exception; //all countries associated with the apikey

    //plot creation and modification
    public void setPlotPasted(int plotID) throws Exception;


    public int createPlotTransaction(CityProject cityProject, int difficultyID, Vector plotCenter, String polyOutline, Player player, double plotVersion) throws Exception;
    public void commitPlot() throws Exception;
    /** aborts a transaction that created a plot
     * @param plotID the plotID of the plot created. if no plot was created in the db, use -1
     * @throws Exception
     */
    public void rollbackPlot(int plotID) throws Exception; 
}
