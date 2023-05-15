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

package com.alpsbte.alpslib.hologram;

import me.filoghost.holographicdisplays.api.HolographicDisplaysAPI;
import me.filoghost.holographicdisplays.api.Position;
import me.filoghost.holographicdisplays.api.hologram.Hologram;
import me.filoghost.holographicdisplays.api.hologram.VisibilitySettings;
import me.filoghost.holographicdisplays.api.hologram.line.HologramLine;
import me.filoghost.holographicdisplays.api.hologram.line.ItemHologramLine;
import me.filoghost.holographicdisplays.api.hologram.line.TextHologramLine;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class HolographicDisplay implements HolographicContent {
    public static HolographicDisplaysAPI hologramAPI;
    public static void registerPlugin(Plugin plugin) {
        hologramAPI = HolographicDisplaysAPI.get(plugin);
    }
    public static String contentSeparator = "§7---------------";

    private final String id;
    private Hologram hologram;

    public HolographicDisplay(@NotNull String id) {
        this.id = id;
    }

    public void create(Position position) {
        if (hologram != null && !hologram.isDeleted()) remove();
        hologram = hologramAPI.createHologram(position);
        reload();
    }

    public boolean isVisible() {
        return hologram != null && hologram.getVisibilitySettings().getGlobalVisibility() == VisibilitySettings.Visibility.VISIBLE;
    }

    @Override
    public List<DataLine<?>> getHeader() {
        return Arrays.asList(
                new ItemLine(getItem()),
                new TextLine(getTitle()),
                new TextLine(contentSeparator)
        );
    }

    @Override
    public List<DataLine<?>> getFooter() {
        return Collections.singletonList(new TextLine(contentSeparator));
    }

    public void reload() {
        if (hologram == null) return;

        List<DataLine<?>> dataLines = Stream.of(getHeader(), getContent(), getFooter()).flatMap(Collection::stream).collect(Collectors.toList());
        updateDataLines(0, dataLines);
    }

    protected void updateDataLines(int startIndex, List<DataLine<?>> dataLines) {
        int index = startIndex;
        for (DataLine<?> data : dataLines) {
            if (data instanceof TextLine) replaceLine(index, ((TextLine) data).getLine());
            else if (data instanceof ItemLine) replaceLine(index, ((ItemLine) data).getLine());
            index++;
        }
    }

    protected void replaceLine(int line, ItemStack item) {
        if (getHologram().getLines().size() < line + 1) {
            getHologram().getLines().insertItem(line, item);
        } else {
            HologramLine hologramLine = getHologram().getLines().get(line);
            if (hologramLine instanceof TextHologramLine) {
                // we're replacing the line with a different type, so we will have to destroy old line to replace with new type
                getHologram().getLines().insertItem(line, item);
                getHologram().getLines().remove(line + 1);
            } else {
                ((ItemHologramLine) hologramLine).setItemStack(item);
            }
        }
    }

    protected void replaceLine(int line, String text) {
        if (getHologram().getLines().size() < line + 1) {
            getHologram().getLines().insertText(line, text);
        } else {
            HologramLine hologramLine = getHologram().getLines().get(line);
            if (hologramLine instanceof ItemHologramLine) {
                // we're replacing the line with a different type, so we will have to destroy old line to replace with new type
                getHologram().getLines().insertText(line, text);
                getHologram().getLines().remove(line + 1);
            } else {
                ((TextHologramLine) hologramLine).setText(text);
            }
        }
    }

    public void remove() {
        hologram.delete();
    }

    public String getId() {
        return id;
    }

    public Hologram getHologram() {
        return hologram;
    }

    public interface DataLine<T> {
        T getLine();
    }

    public static class TextLine implements DataLine<String> {
        private final String line;
        public TextLine(String line) {
            this.line = line;
        }
        @Override
        public String getLine() {
            return line;
        }
    }

    public static class ItemLine implements DataLine<ItemStack> {
        private final ItemStack line;
        public ItemLine(ItemStack line) {
            this.line = line;
        }
        @Override
        public ItemStack getLine() {
            return line;
        }
    }
}
