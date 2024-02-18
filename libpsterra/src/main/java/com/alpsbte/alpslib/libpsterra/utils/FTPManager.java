package com.alpsbte.alpslib.libpsterra.utils;

import com.alpsbte.alpslib.libpsterra.core.Connection;
import com.alpsbte.alpslib.libpsterra.core.plotsystem.CityProject;
import com.alpsbte.alpslib.libpsterra.core.plotsystem.Country;
import com.alpsbte.alpslib.libpsterra.core.plotsystem.FTPConfiguration;
import com.alpsbte.alpslib.libpsterra.core.plotsystem.Server;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Logger;
import com.jcraft.jsch.Session;

import org.apache.commons.vfs2.*;
import org.apache.commons.vfs2.impl.StandardFileSystemManager;
import org.apache.commons.vfs2.provider.ftp.FtpFileSystemConfigBuilder;
import org.apache.commons.vfs2.provider.sftp.SftpFileSystemConfigBuilder;
import org.bukkit.Bukkit;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;

public class FTPManager {

    private static FileSystemOptions fileOptions;

    private final static String DEFAULT_SCHEMATIC_PATH_LINUX = "/var/lib/Plot-System/schematics";

    static {
        try {
            fileOptions = new FileSystemOptions();
            SftpFileSystemConfigBuilder.getInstance().setStrictHostKeyChecking(fileOptions, "no");
            SftpFileSystemConfigBuilder.getInstance().setPreferredAuthentications(fileOptions, "password");
            SftpFileSystemConfigBuilder.getInstance().setUserDirIsRoot(fileOptions, false);
            SftpFileSystemConfigBuilder.getInstance().setKeyExchangeAlgorithm(fileOptions, "diffie-hellman-group1-sha1,diffie-hellman-group14-sha1,diffie-hellman-group-exchange-sha1,diffie-hellman-group-exchange-sha256");
            
            FtpFileSystemConfigBuilder.getInstance().setPassiveMode(fileOptions, true);
            FtpFileSystemConfigBuilder.getInstance().setUserDirIsRoot(fileOptions, false);
        } catch (FileSystemException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "Exception found with FileSystemManager!", ex);
        }
    }

    public static String getFTPUrl(FTPConfiguration ftpConfiguration, Server server, CityProject city) throws URISyntaxException {
        String schematicsPath = ftpConfiguration.schematics_path;
        return new URI(ftpConfiguration.isSFTP ? "sftp" : "ftp",
                ftpConfiguration.username + ":" + ftpConfiguration.password,
                ftpConfiguration.address,
                ftpConfiguration.port,
                String.format("/%s/%s/%s/%s/", schematicsPath == null ? DEFAULT_SCHEMATIC_PATH_LINUX : schematicsPath, server.id, "finishedSchematics", city.id),
                null,
                null).toString();
    }

    public static void uploadSchematics(String ftpURL, File... schematics) throws FileSystemException {
        try (StandardFileSystemManager fileManager = new StandardFileSystemManager()) {
            fileManager.init();

            for (File schematic : schematics) {
                
                // Get local schematic
                FileObject localSchematic = fileManager.toFileObject(schematic);

                // Get remote path and create missing directories
                FileObject remote = fileManager.resolveFile(ftpURL.replace("finishedSchematics/", ""), fileOptions);
                Bukkit.getConsoleSender().sendMessage("FTPManager: uploading Schematic "+ schematic.getName()+" to " + remote.getPublicURIString());
        
                remote.createFolder();

                // Create remote schematic and write to it
                FileObject remoteSchematic = remote.resolveFile(schematic.getName());
                remoteSchematic.copyFrom(localSchematic, Selectors.SELECT_SELF);

                localSchematic.close();
                remoteSchematic.close();
            }
        }
    }

    public static void downloadSchematic(String ftpURL, File schematic) throws FileSystemException {
        try (StandardFileSystemManager fileManager = new StandardFileSystemManager()) {
            fileManager.init();

            // Get local schematic
            FileObject localSchematic = fileManager.toFileObject(schematic);

            // Get remote path
            FileObject remote = fileManager.resolveFile(ftpURL, fileOptions);
            Bukkit.getConsoleSender().sendMessage("FTPManager: downloading Schematic "+ schematic.getName()+" from " + remote.getPublicURIString());
        
            // Get remote schematic and write it to local file
            FileObject remoteSchematic = remote.resolveFile(schematic.getName());
            localSchematic.copyFrom(remoteSchematic, Selectors.SELECT_SELF);

            localSchematic.close();
            remoteSchematic.close();
        }
    }

    public static void testSFTPConnection_JSCH(Connection connection)  throws Exception {

        //get ftpconfig from first country/server associated with the buildteam/apikey
        List<Country> teamCountries = connection.getTeamCountries();
        if (teamCountries.isEmpty()){
            System.out.println("Cannot test FTP config - no countries/servers associated with API key");
            return;
        }

        Server server = connection.getServer(teamCountries.get(0).server_id);
        FTPConfiguration ftpConfiguration = connection.getFTPConfiguration(server.ftp_configuration_id);

        JSch jsch = new JSch();
    
        Properties config = new Properties();
        config.put("cipher.s2c", 
                "aes128-ctr,aes128-cbc,3des-ctr,3des-cbc,blowfish-cbc,aes192-ctr,aes192-cbc,aes256-ctr,aes256-cbc");
        config.put("cipher.c2s",
                "aes128-ctr,aes128-cbc,3des-ctr,3des-cbc,blowfish-cbc,aes192-ctr,aes192-cbc,aes256-ctr,aes256-cbc");
        config.put("kex", "diffie-hellman-group1-sha1,diffie-hellman-group14-sha1,diffie-hellman-group-exchange-sha1,diffie-hellman-group-exchange-sha256");
        
        Session jschSession = jsch.getSession(ftpConfiguration.username,ftpConfiguration.address,ftpConfiguration.port);
        //FOR DEBUGGING ONLY: STRICTHOSTKEY = no
        //TODO : add target to known-hosts and include public key, see
        //https://stackoverflow.com/questions/2003419/com-jcraft-jsch-jschexception-unknownhostkey
        config.put("StrictHostKeyChecking", "no");
        jschSession.setPassword(ftpConfiguration.password);
        jschSession.setConfig(config);

        System.out.println("Testing JSCH sftp connect (ignoring host fingerprint, strong crpyto) to " + ftpConfiguration.address + " with user " + ftpConfiguration.username);
        Logger jschLogger = new Logger() {
            @Override
            public boolean isEnabled(int arg0){return true;}

            @Override
            public void log(int arg0, String arg1){
                System.out.println("JSCH log: " + Integer.toString(arg0) + ": " +arg1);
            }
        };

        JSch.setLogger(jschLogger);
        jschSession.connect();
        jschSession.disconnect();
        System.out.println("JSCH Success!");

        
    }

    public static void testSFTPConnection_VFS2(Connection connection) throws Exception {
        //get ftpconfig from first country/server associated with the buildteam/apikey
        List<Country> teamCountries = connection.getTeamCountries();
        if (teamCountries.isEmpty()){
            System.out.println("Cannot test FTP config - no countries/servers associated with API key");
            return;
        }

        Server server = connection.getServer(teamCountries.get(0).server_id);
        //--- SSHJ --------------------
        StandardFileSystemManager fileManager = new StandardFileSystemManager();


        FTPConfiguration ftpConfiguration = connection.getFTPConfiguration(server.ftp_configuration_id);

        System.out.println("Testing VFS2 sftp connect connect to " + ftpConfiguration.address + " with user " + ftpConfiguration.username);
        

        fileManager.init();

        FileSystemOptions fileOptions = new FileSystemOptions();
        SftpFileSystemConfigBuilder.getInstance().setStrictHostKeyChecking(fileOptions, "no");
        SftpFileSystemConfigBuilder.getInstance().setPreferredAuthentications(fileOptions, "password");
        SftpFileSystemConfigBuilder.getInstance().setUserDirIsRoot(fileOptions, false);
        SftpFileSystemConfigBuilder.getInstance().setKeyExchangeAlgorithm(fileOptions, "diffie-hellman-group1-sha1,diffie-hellman-group14-sha1,diffie-hellman-group-exchange-sha1,diffie-hellman-group-exchange-sha256");
        //SftpFileSystemConfigBuilder.getInstance().set "aes128-ctr,aes128-cbc,3des-ctr,3des-cbc,blowfish-cbc,aes192-ctr,aes192-cbc,aes256-ctr,aes256-cbc");
        //we need to configure the ssh options to use stronger crypto
        
        FtpFileSystemConfigBuilder.getInstance().setPassiveMode(fileOptions, true);
        FtpFileSystemConfigBuilder.getInstance().setUserDirIsRoot(fileOptions, false);

        URI ftpURI = new URI("sftp",
            ftpConfiguration.username + ":" + ftpConfiguration.password,
            ftpConfiguration.address,
            ftpConfiguration.port,
            "/plugins/PlotSystem-Terra/schematics/test.txt",
            null,
            null);
        // Get remote path
        FileObject remote = fileManager.resolveFile(ftpURI.toString(), fileOptions);
        // BufferedInputStream inputStreamReader = new BufferedInputStream(remote.getContent().getInputStream());
        // String content = "";
        // while (inputStreamReader.available() > 0) {
        //     char c = (char) inputStreamReader.read();
        //     content = content.concat(String.valueOf(c));
        // }
        fileManager.close();
        System.out.println("FTP testfile exists: " +Boolean.toString(remote.exists()));
        
        
    }
}
