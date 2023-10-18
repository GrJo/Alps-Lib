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

package com.alpsbte.alpslib.io.lang;

import com.alpsbte.alpslib.io.YamlFileFactory;
import com.alpsbte.alpslib.io.YamlFile;
import com.alpsbte.alpslib.utils.head.AlpsHeadUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class LanguageUtil extends YamlFileFactory {
    private static final String CONFIG_VERSION_PATH = "config-version";
    private final static String LANG_NAME_PATH = "lang.name";
    private final static String LANG_HEAD_ID_PATH = "lang.head-id";

    public LanguageFile[] languageFiles;

    public LanguageUtil(LanguageFile[] langFiles) {
        super(langFiles);
        languageFiles = langFiles;

        Arrays.stream(langFiles).forEach(lang -> {
            if (!lang.getFile().exists()) {
                createFile(lang);
            } else if (reloadFile(lang) && lang.getDouble(CONFIG_VERSION_PATH) != lang.getVersion()) {
                updateFile(lang);
            }
            reloadFile(lang);
        });
    }

    public String get(CommandSender sender, String key) {
        return getLanguageFileByLocale(sender instanceof Player ? getLocaleTagByPlayer((Player) sender) : languageFiles[0].tag).getTranslation(key);
    }

    public String get(CommandSender sender, String key, String... args) {
        return getLanguageFileByLocale(sender instanceof Player ? getLocaleTagByPlayer((Player) sender) : languageFiles[0].tag).getTranslation(key, args);
    }

    public List<String> getList(CommandSender sender, String key) {
        return getLanguageFileByLocale(sender instanceof Player ? getLocaleTagByPlayer((Player) sender) : languageFiles[0].tag).getTranslations(key);
    }

    public List<String> getList(CommandSender sender, String key, String... args) {
        return getLanguageFileByLocale(sender instanceof Player ? getLocaleTagByPlayer((Player) sender) : languageFiles[0].tag).getTranslations(key, args);
    }

    public LanguageFile getLanguageFileByLocale(String locale) {
        return Arrays.stream(languageFiles)
                .filter(lang -> lang.tag.equalsIgnoreCase(locale))
                .findFirst()
                .orElseGet(() -> Arrays.stream(languageFiles)
                        .filter(lang -> lang.additionalLang != null && Arrays.stream(lang.additionalLang).anyMatch(l -> l.equalsIgnoreCase(locale)))
                        .findFirst()
                        .orElse(languageFiles[0]));
    }

    public String getLocaleTagByPlayer(Player player) {
        return player != null ? player.getLocale() : null;
    }

    public static class LanguageFile extends YamlFile {
        private final String tag;
        private String[] additionalLang;

        public LanguageFile(String lang, double version) {
            super(Paths.get("lang", lang + ".yml"), version);

            this.tag = lang;
        }

        public LanguageFile(String lang, double version, String... additionalLang) {
            this(lang, version);
            this.additionalLang = additionalLang;
        }

        public String getTranslation(String key) {
            String translation = getString(key);
            return translation != null ? translation : "undefined";
        }

        public String getTranslation(String key, String... args) {
            String translation = getTranslation(key);
            for (int i = 0; i < args.length; i++) {
                translation = translation.replace("{" + i + "}", args[i]);
            }
            return translation;
        }

        public List<String> getTranslations(String key) {
            return getStringList(key);
        }

        public List<String> getTranslations(String key, String... args) {
            String[] translations = getTranslations(key).toArray(new String[0]);
            for (int i = 0; i < args.length; i++) {
                for (int k = 0; k < translations.length; k++) {
                    translations[k] = translations[k].replace("{" + i + "}", args[i]);
                }
            }
            return Arrays.asList(translations);
        }

        public String getTag() {
            return tag;
        }

        public String getLangName() {
            return getString(LANG_NAME_PATH);
        }

        public ItemStack getHead() {
            return AlpsHeadUtils.getCustomHead(getString(LANG_HEAD_ID_PATH));
        }

        @Override
        public InputStream getDefaultFileStream() {
            return YamlFileFactory.yamlPlugin.getResource("lang/" + getFile().getName());
        }

        @Override
        public int getMaxConfigWidth() {
            return 400;
        }
    }
}
