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
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public abstract class HolographicPagedDisplay extends HolographicDisplay {
    protected String sortByPage;
    private BukkitTask changeSortTask = null;
    private int changeState = 0;
    private long changeDelay = 0;
    private final Plugin plugin;

    public HolographicPagedDisplay(@NotNull String id, @NotNull Plugin plugin) {
        super(id);
        this.plugin = plugin;
    }

    @Override
    public void create(Position position) {
        changeDelay = getInterval() / HolographicDisplay.contentSeparator.length();
        List<String> pages = getPages();
        sortByPage = pages.get(0);

        super.create(position);
        changeSortTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (changeState >= changeDelay) {
                    String next = getNextListItem(pages, sortByPage);
                    if (next == null) {
                        sortByPage = pages.get(0);
                    } else {
                        sortByPage = next;
                    }
                    changeState = 0;
                    reload();
                } else {
                    changeState++;
                    updateDataLines(getHologram().getLines().size() - 1, getFooter());
                }
            }
        }.runTaskTimer(plugin, changeDelay, changeDelay);
    }

    @Override
    public List<DataLine<?>> getFooter() {
        int footerLength = HolographicDisplay.contentSeparator.length();
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
    public void remove() {
        if (changeSortTask != null) {
            changeSortTask.cancel();
            changeSortTask = null;
        }
        super.remove();
    }

    public abstract long getInterval();

    public abstract List<String> getPages();

    private static <T> T getNextListItem(List<T> haystack, T needle) {
        if(!haystack.contains(needle) || haystack.indexOf(needle) + 1 >= haystack.size()) {
            return null;
        }
        return haystack.get(haystack.indexOf(needle) + 1);
    }
}
