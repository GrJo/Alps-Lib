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

import me.filoghost.holographicdisplays.api.Position;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public abstract class HolographicPagedDisplay extends HolographicDisplay {
    protected String sortByPage;
    public BukkitTask changePageTask = null;
    private int changeState = 0;
    private long changeDelay = 0;
    private final Plugin plugin;
    private static String contentSeparator = "§7---------------";
    protected boolean automaticallySkipPage = true;

    public HolographicPagedDisplay(@NotNull String id, Position position, boolean enablePlaceholders, @NotNull Plugin plugin) {
        super(id, position, enablePlaceholders);
        this.plugin = plugin;
    }

    @Override
    public void create(Player player) {
        if (getPages() != null && getPages().size() > 0) sortByPage = getPages().get(0);
        super.create(player);
        if (automaticallySkipPage) startChangePageTask();
    }

    @Override
    public void reload(UUID playerUUID) {
        if (!holograms.containsKey(playerUUID)) return;
        List<DataLine<?>> dataLines = new ArrayList<>();

        List<DataLine<?>> header = getHeader(playerUUID);
        if (header != null) dataLines.addAll(header);

        List<DataLine<?>> content = getContent(playerUUID);
        if (content != null) dataLines.addAll(content);

        List<DataLine<?>> footer = getFooter(playerUUID);
        if (footer != null) dataLines.addAll(footer);

        updateDataLines(holograms.get(playerUUID), 0, dataLines);
    }

    private void startChangePageTask() {
        final long interval = getInterval();
        changeState = 0;
        changeDelay = interval / contentSeparator.length();

        if (changePageTask != null) changePageTask.cancel();
        changePageTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (changeState == 0) getHolograms().keySet().forEach(uuid -> reload(uuid));
                if (interval == 0) return;
                if (changeState >= changeDelay) {
                    if (automaticallySkipPage) nextPage();
                    else changePageTask.cancel();
                } else {
                    changeState++;
                    getHolograms().forEach((uuid, holo) -> updateDataLines(holo,holo.getLines().size() - 1, getFooter(uuid)));
                }
            }
        }.runTaskTimer(plugin, 0, changeDelay);
    }


    public List<DataLine<?>> getFooter(UUID playerUUID) {
        int footerLength = contentSeparator.length();
        int highlightCount = (int) (((float) changeState / changeDelay) * footerLength);

        StringBuilder highlighted = new StringBuilder();
        for (int i = 0; i < highlightCount; i++) {
            highlighted.append("-");
        }
        StringBuilder notH = new StringBuilder();
        for (int i = 0; i < footerLength - highlightCount; i++) {
            notH.append("-");
        }

        return Collections.singletonList(new TextLine("§e" + highlighted + "§7" + notH));
    }

    @Override
    public void remove(UUID playerUUID) {
        super.remove(playerUUID);
    }

    public abstract long getInterval();

    public abstract List<String> getPages();

    public void nextPage() {
        String next = getNextListItem(getPages(), sortByPage);
        sortByPage = next == null ? getPages().get(0) : next;
        startChangePageTask();
    }

    // TODO: UNTESTED
    public void previousPage() {
        int index = getPages().indexOf(sortByPage);
        if (index == 0) {
            sortByPage = getPages().get(getPages().size() - 1); // Wrap around to the last page
        } else {
            sortByPage = getPages().get(index - 1); // Go to the previous page
        }
        startChangePageTask();
    }

    private static <T> T getNextListItem(List<T> haystack, T needle) {
        if(!haystack.contains(needle) || haystack.indexOf(needle) + 1 >= haystack.size()) {
            return null;
        }
        return haystack.get(haystack.indexOf(needle) + 1);
    }
}
