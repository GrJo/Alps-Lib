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

package com.alpsbte.alpslib.npc;

import com.alpsbte.alpslib.hologram.HolographicDisplay;
import me.filoghost.holographicdisplays.api.Position;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class NpcHologram extends HolographicDisplay {
    private static final double NPC_HOLOGRAM_Y = 2.3;
    private static final double NPC_HOLOGRAM_Y_WITH_ACTION_TITLE = 2.6;

    private final AbstractNpc npc;
    private final Map<UUID, Boolean> isActionTitleVisible = new HashMap<>();

    private Position basePosition;

    public NpcHologram(@NotNull String id, Position position, AbstractNpc npc) {
        super(id, position.add(0, NPC_HOLOGRAM_Y, 0), false);
        this.npc = npc;
        this.basePosition = position;
    }

    @Override
    public boolean hasViewPermission(UUID uuid) {
        return npc.getNpc().getIsVisibleForPlayer().containsKey(uuid) && npc.getNpc().getIsVisibleForPlayer().get(uuid);
    }

    @Override
    public ItemStack getItem() {
        return null;
    }

    @Override
    public String getTitle(UUID playerUUID) {
        return npc.getDisplayName(playerUUID);
    }

    @Override
    public List<DataLine<?>> getHeader(UUID playerUUID) {
        return Collections.singletonList(new TextLine(this.getTitle(playerUUID)));
    }

    @Override
    public List<DataLine<?>> getContent(UUID playerUUID) {
        return new ArrayList<>();
    }

    @Override
    public List<DataLine<?>> getFooter(UUID playerUUID) {
        return isActionTitleVisible(playerUUID) ? Collections.singletonList(new TextLine(npc.getActionTitle(playerUUID))) : new ArrayList<>();
    }

    @Override
    public void setPosition(Position newPosition) {
        this.basePosition = newPosition;
        for (UUID playerUUID : getHolograms().keySet()) getHolograms().get(playerUUID)
                .setPosition(newPosition.add(0, isActionTitleVisible(playerUUID) ? NPC_HOLOGRAM_Y_WITH_ACTION_TITLE : NPC_HOLOGRAM_Y, 0));
    }

    public void setActionTitleVisibility(UUID playerUUID, boolean isVisible) {
        isActionTitleVisible.put(playerUUID, isVisible);
        getHologram(playerUUID).setPosition(basePosition.add(0, isActionTitleVisible(playerUUID) ? NPC_HOLOGRAM_Y_WITH_ACTION_TITLE : NPC_HOLOGRAM_Y, 0));
        reload(playerUUID);
    }

    public boolean isActionTitleVisible(UUID playerUUID) {
        return isActionTitleVisible.getOrDefault(playerUUID, false);
    }
}
