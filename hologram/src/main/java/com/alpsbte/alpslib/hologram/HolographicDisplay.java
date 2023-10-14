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
import me.filoghost.holographicdisplays.api.hologram.PlaceholderSetting;
import me.filoghost.holographicdisplays.api.hologram.VisibilitySettings;
import me.filoghost.holographicdisplays.api.hologram.line.HologramLine;
import me.filoghost.holographicdisplays.api.hologram.line.ItemHologramLine;
import me.filoghost.holographicdisplays.api.hologram.line.TextHologramLine;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class HolographicDisplay implements HolographicContent {
    public static List<HolographicDisplay> activeDisplays = new ArrayList<>();
    public static HolographicDisplaysAPI hologramAPI;
    public static void registerPlugin(Plugin plugin) {
        hologramAPI = HolographicDisplaysAPI.get(plugin);
        plugin.getServer().getPluginManager().registerEvents(new HolographicEventListener(), plugin);
    }
    public static String contentSeparator = "§7---------------";
    public static final String EMPTY_TAG = "{empty}";

    private final String id;
    private Position position;
    private final boolean isPlaceholdersEnabled;

    private final HashMap<UUID, Hologram> holograms = new HashMap<>();

    public HolographicDisplay(@NotNull String id, Position position, boolean enablePlaceholders) {
        this.id = id;
        this.position = position;
        this.isPlaceholdersEnabled = enablePlaceholders;
        activeDisplays.add(this);
    }

    public void create(Player player) {
        if (!hasViewPermission(player.getUniqueId())) return;
        if (holograms.containsKey(player.getUniqueId())) {
            reload(player.getUniqueId());
            return;
        }

        Hologram hologram = hologramAPI.createHologram(position);
        hologram.getVisibilitySettings().setGlobalVisibility(VisibilitySettings.Visibility.HIDDEN);
        hologram.getVisibilitySettings().setIndividualVisibility(player, VisibilitySettings.Visibility.VISIBLE);
        if (isPlaceholdersEnabled) hologram.setPlaceholderSetting(PlaceholderSetting.ENABLE_ALL);
        holograms.put(player.getUniqueId(), hologram);
        reload(player.getUniqueId());
    }

    public abstract boolean hasViewPermission(UUID playerUUID);

    public boolean isVisible(UUID playerUUID) {
        return holograms.containsKey(playerUUID);
    }

    @Override
    public List<DataLine<?>> getHeader(UUID playerUUID) {
        return Arrays.asList(
                new ItemLine(getItem()),
                new TextLine(getTitle(playerUUID)),
                new TextLine(contentSeparator)
        );
    }

    @Override
    public List<DataLine<?>> getFooter(UUID playerUUID) {
        return Collections.singletonList(new TextLine(contentSeparator));
    }

    public void reload(UUID playerUUID) {
        if (!holograms.containsKey(playerUUID)) return;

        List<DataLine<?>> dataLines = Stream.of(getHeader(playerUUID), getContent(playerUUID), getFooter(playerUUID)).flatMap(Collection::stream).collect(Collectors.toList());
        updateDataLines(holograms.get(playerUUID), 0, dataLines);
    }

    public void reloadAll() {
        for (UUID playerUUID : holograms.keySet()) reload(playerUUID);
    }

    public void remove(UUID playerUUID) {
        if (holograms.containsKey(playerUUID)) holograms.get(playerUUID).delete();
        holograms.remove(playerUUID);
    }

    public void removeAll() {
        List<UUID> playerUUIDs = new ArrayList<>(holograms.keySet());
        for (UUID playerUUID : playerUUIDs) remove(playerUUID);
    }

    public void delete() {
        removeAll();
        holograms.clear();
        activeDisplays.remove(this);
    }

    public String getId() {
        return id;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position newPosition) {
        this.position = newPosition;
        for (UUID playerUUID : holograms.keySet()) holograms.get(playerUUID).setPosition(newPosition);
    }

    public boolean isPlaceholdersEnabled() {
        return isPlaceholdersEnabled;
    }

    public Hologram getHologram(UUID playerUUID) {
        return holograms.get(playerUUID);
    }

    public HashMap<UUID, Hologram> getHolograms() {
        return holograms;
    }

    public interface DataLine<T> {
        T getLine();
    }

    protected static void updateDataLines(Hologram hologram, int startIndex, List<DataLine<?>> dataLines) {
        int index = startIndex;

        if (index == 0 && hologram.getLines().size() > dataLines.size()) {
            int removeCount = hologram.getLines().size() - dataLines.size();
            for (int i = 0; i < removeCount; i++) {
                int lineIndex = hologram.getLines().size() - 1;
                if (lineIndex >= 0) hologram.getLines().remove(lineIndex);
            }
        }

        for (DataLine<?> data : dataLines) {
            if (data instanceof TextLine) replaceLine(hologram, index, ((TextLine) data).getLine());
            else if (data instanceof ItemLine) replaceLine(hologram, index, ((ItemLine) data).getLine());
            index++;
        }
    }

    protected static void replaceLine(Hologram hologram, int line, ItemStack item) {
        if (hologram.getLines().size() < line + 1) {
            hologram.getLines().insertItem(line, item);
        } else {
            HologramLine hologramLine = hologram.getLines().get(line);
            if (hologramLine instanceof TextHologramLine) {
                // we're replacing the line with a different type, so we will have to destroy old line to replace with new type
                hologram.getLines().insertItem(line, item);
                hologram.getLines().remove(line + 1);
            } else {
                ((ItemHologramLine) hologramLine).setItemStack(item);
            }
        }
    }

    protected static void replaceLine(Hologram hologram, int line, String text) {
        if (hologram.getLines().size() < line + 1) {
            hologram.getLines().insertText(line, text);
        } else {
            HologramLine hologramLine = hologram.getLines().get(line);
            if (hologramLine instanceof ItemHologramLine) {
                // we're replacing the line with a different type, so we will have to destroy old line to replace with a new type
                hologram.getLines().insertText(line, text);
                hologram.getLines().remove(line + 1);
            } else {
                ((TextHologramLine) hologramLine).setText(text);
            }
        }
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

    public static HolographicDisplay getById(String id) {
        return activeDisplays.stream().filter(holo -> holo.getId().equals(id)).findFirst().orElse(null);
    }
}
