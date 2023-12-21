package com.alpsbte.alpslib.libpsterra.core.config;

import org.bukkit.configuration.file.YamlConfiguration;
import org.yaml.snakeyaml.DumperOptions;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Config extends YamlConfiguration {

    public static final double VERSION = 1.6;

    private final File file;
    private final String fileName;
    private InputStream defaultFileStream;

    protected Config(String fileName, String absolutePluginDataPath, InputStream defaultFileStream) {
        this.fileName = fileName;
        this.defaultFileStream = defaultFileStream;
        //this.file = Paths.get(PlotSystemTerra.getPlugin().getDataFolder().getAbsolutePath(), fileName).toFile();
        this.file = Paths.get(absolutePluginDataPath, fileName).toFile();
    }

    @Override
    public String saveToString() {
        try {
            // Increase config width to avoid line breaks
            Field op;
            op = YamlConfiguration.class.getDeclaredField("yamlOptions");
            op.setAccessible(true);
            final DumperOptions options = (DumperOptions) op.get(this);
            options.setWidth(250);
        } catch (final Exception ignored) {
        }

        return super.saveToString();
    }

    public List<String> readDefaultConfig() {
        try (InputStream in = getDefaultFileStream()) {
            BufferedReader input = new BufferedReader(new InputStreamReader(in));
            return input.lines().collect(Collectors.toList());
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public static String getDefaultFileStreamName(String fileName) {
        return "default" + fileName.substring(0, 1).toUpperCase() + fileName.substring(1);
    }
    public InputStream getDefaultFileStream() {
        //return PlotSystemTerra.getPlugin().getResource("default" + fileName.substring(0, 1).toUpperCase() + fileName.substring(1));
        return defaultFileStream;
    }

    public String getFileName() {
        return fileName;
    }

    public File getFile() {
        return file;
    }
}
