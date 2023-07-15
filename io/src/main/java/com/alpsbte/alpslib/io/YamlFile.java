/*
 * The MIT License (MIT)
 *
 *  Copyright © 2023, Alps BTE <bte.atchli@gmail.com>
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package com.alpsbte.alpslib.io;

import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.DumperOptions;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class YamlFile extends YamlConfiguration {
    private final double version;
    private final File file;
    private final String fileName;

    protected YamlFile(Path fileName, double version) {
        this.version = version;
        this.file = new File(YamlFileFactory.yamlPlugin.getDataFolder().getAbsolutePath() + File.separator + fileName.toString());
        this.fileName = file.getName();
    }

    @Override
    public @NotNull String saveToString() {
        try {
            // Increase config width to avoid line breaks
            Field op;
            try {
                // Legacy support for older versions of SnakeYAML
                op = YamlConfiguration.class.getDeclaredField("yamlOptions");
            } catch (NoSuchFieldException e) {
                op = YamlConfiguration.class.getDeclaredField("yamlDumperOptions");
            }
            op.setAccessible(true);
            final DumperOptions options = (DumperOptions) op.get(this);
            options.setWidth(getMaxConfigWidth());
            options.setSplitLines(false); // throws NoSuchMethodError on Legacy versions of SnakeYAML
        } catch (final Exception ignored) {}

        return super.saveToString();
    }

    public List<String> readDefaultFile() {
        try (InputStream in = getDefaultFileStream()) {
            BufferedReader input = new BufferedReader(new InputStreamReader(in));
            List<String> lines = input.lines().collect(Collectors.toList());
            input.close();
            return lines;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public InputStream getDefaultFileStream() {
        return YamlFileFactory.yamlPlugin.getResource(fileName);
    }

    public double getVersion() {
        return version;
    }

    public String getFileName() {
        return fileName;
    }

    public File getFile() {
        return file;
    }

    public int getMaxConfigWidth() {
        return 250;
    }
}
