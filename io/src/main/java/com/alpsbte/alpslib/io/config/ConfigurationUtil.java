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

package com.alpsbte.alpslib.io.config;

import com.alpsbte.alpslib.io.YamlFileFactory;
import com.alpsbte.alpslib.io.YamlFile;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

public class ConfigurationUtil extends YamlFileFactory {
    private static final String CONFIG_VERSION_PATH = "config-version";
    public ConfigFile[] configs;

    public ConfigurationUtil(ConfigFile[] configs) throws ConfigNotImplementedException {
        super(configs);
        this.configs = configs;

        for (ConfigFile file : configs) {
            if (!file.getFile().exists() && createFile(file) && file.isMustBeConfigured()) {
                throw new ConfigNotImplementedException("The config file must be configured!");
            } else if (reloadFile(file) && file.getDouble(CONFIG_VERSION_PATH) != file.getVersion()) {
                updateConfigFile(file);
            }
        }

        reloadFiles();
    }

    /**
     * Updates the config file to the latest version.
     * This method is used to override.
     * @param file The config file to update.
     */
    public void updateConfigFile(ConfigFile file) {
        updateFile(file);
    }

    public static class ConfigFile extends YamlFile {
        private final boolean mustBeConfigured;

        public ConfigFile(Path fileName, double version, boolean mustBeConfigured) {
            super(fileName, version);
            this.mustBeConfigured = mustBeConfigured;
        }

        @Override
        public String getString(@NotNull String path) {
            return super.getString(path);
        }

        @Override
        public int getMaxConfigWidth() {
            return 400;
        }

        public boolean isMustBeConfigured() {
            return mustBeConfigured;
        }
    }
}