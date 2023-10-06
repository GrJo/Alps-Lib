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

import de.oliver.fancynpcs.api.FancyNpcsPlugin;
import de.oliver.fancynpcs.api.Npc;
import de.oliver.fancynpcs.api.NpcData;
import de.oliver.fancynpcs.api.utils.SkinFetcher;
import me.filoghost.holographicdisplays.api.Position;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;

public abstract class AbstractNpc {
    public static final List<AbstractNpc> activeNPCs = new ArrayList<>();

    public abstract String getDisplayName(UUID playerUUID);

    public abstract String getActionTitle(UUID playerUUID);

    private final String id;
    private final String skinTexture;
    private final String skinSignature;

    private Npc npc;
    private NpcHologram hologram;

    public AbstractNpc(String id, String skinTexture, String skinSignature) {
        this.id = id;
        this.skinTexture = skinTexture;
        this.skinSignature = skinSignature;
    }

    public void create(Location spawnPos, boolean turnToPlayer) {
        if (npc != null) delete();

        NpcData npcData = new NpcData(id, UUID.randomUUID(), spawnPos);
        npc = FancyNpcsPlugin.get().getNpcAdapter().apply(npcData);
        npc.getData().setSkin(new SkinFetcher("npc_" + id, skinTexture, skinSignature));
        npc.getData().setDisplayName("<empty>");
        npc.getData().setTurnToPlayer(turnToPlayer);
        npc.setSaveToFile(false);
        npc.create();
        FancyNpcsPlugin.get().getNpcManager().registerNpc(npc);
        hologram = new NpcHologram("npc_" + id, Position.of(spawnPos), this);
        activeNPCs.add(this);
    }

    public void teleport(Location teleportPos) {
        if (npc == null) return;
        npc.getData().setLocation(teleportPos);
        npc.updateForAll();
        if (hologram != null) hologram.setPosition(Position.of(teleportPos));
    }

    public void show(Player player) {
        if (npc == null) return;
        npc.spawn(player);
        if (hologram != null && player.getWorld().getName().equals(hologram.getPosition().getWorldName())) hologram.create(player);
    }

    public void showForAll() {
        if (npc == null) return;
        npc.spawnForAll();
        if (hologram != null) {
            Bukkit.getOnlinePlayers().forEach(player -> {
                if (player.getWorld().getName().equals(hologram.getPosition().getWorldName())) hologram.create(player);
            });
        }
    }

    public void hide(Player player) {
        if (npc == null || !npc.getIsVisibleForPlayer().containsKey(player.getUniqueId())) return;
        hologram.remove(player.getUniqueId());
        npc.remove(player);
    }

    public void setActionTitleVisibility(UUID playerUUID, boolean isVisible, boolean enableGlow) {
        if (hologram != null && hologram.getHologram(playerUUID) != null && !hologram.getHologram(playerUUID).isDeleted())
            hologram.setActionTitleVisibility(playerUUID, isVisible);
        if (npc != null) {
            if (npc.getData().isGlowing() != enableGlow) {
                npc.getData().setGlowing(enableGlow);
                npc.updateForAll();
            }
        }
    }

    public void delete() {
        if (npc != null) npc.removeForAll();
        if (hologram != null) hologram.delete();
        activeNPCs.remove(this);
    }

    public String getId() {
        return id;
    }

    public String getSkinTexture() {
        return skinTexture;
    }

    public String getSkinSignature() {
        return skinSignature;
    }

    public Npc getNpc() {
        return npc;
    }

    public NpcHologram getHologram() {
        return hologram;
    }
}