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
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import me.filoghost.holographicdisplays.api.Position;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.trait.SkinTrait;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.logging.Level;

import static net.md_5.bungee.api.ChatColor.BOLD;
import static net.md_5.bungee.api.ChatColor.GOLD;

public abstract class AbstractNPC {
    public static List<UUID> activeNPCs = new ArrayList<>();

    private static Plugin dependencyPlugin;
    public static void registerPlugin(Plugin plugin) {
        dependencyPlugin = plugin;
    }

    /*
    private final static Scoreboard npcScoreboard;
    private final static Team npcTeam;

    static {
        npcScoreboard = Objects.requireNonNull(Bukkit.getScoreboardManager()).getNewScoreboard();
        npcTeam = npcScoreboard.registerNewTeam("npc");
        npcTeam.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);
    }
    **/

    private final String name;
    private final String skinTexture;
    private final String skinSignature;

    private NPC npc;
    private NPCHologram hologram;

    public AbstractNPC(String name, String skinTexture, String skinSignature) {
        this.name = name;
        this.skinTexture = skinTexture;
        this.skinSignature = skinSignature;
    }

    public AbstractNPC(NPC npc) {
        this(npc.getName(), null, null);
        finalizeNpc(npc.getStoredLocation());
    }

    public abstract String getDescription(UUID playerUUID);

    public void create(Location spawnLoc) {
        if (npc != null) remove();

        npc = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, "");

        // Set NPC Skin
        npc.getOrAddTrait(SkinTrait.class).setSkinPersistent("npc_skin_" + npc.getId(), skinSignature, skinTexture);

        // Spawn NPC
        npc.spawn(spawnLoc);
        activeNPCs.add(npc.getMinecraftUniqueId());

        finalizeNpc(spawnLoc);
    }

    private void finalizeNpc(Location hologramLoc) {
        // Set NPC Hologram
        hologram = new NPCHologram("npc_" + npc.getId(), Position.of(hologramLoc), this);
        Bukkit.getOnlinePlayers().forEach(player -> hologram.create(player));

        // Hide default NPC name tag
        /*
        npcTeam.addEntry(npc.getUniqueId().toString());
        ((Player) npc.getEntity()).setScoreboard(npcScoreboard);
        Bukkit.getLogger().log(Level.INFO, "Updated Name Tag");
        **/

        try {
            if (dependencyPlugin != null && npc.getEntity() != null) {
                ProtocolManager pm = ProtocolLibrary.getProtocolManager();
                PacketContainer packet = pm.createPacket(PacketType.Play.Server.ENTITY_METADATA);
                packet.getIntegers().write(0, npc.getEntity().getEntityId());

                WrappedDataWatcher dataWatcher = new WrappedDataWatcher(packet.getWatchableCollectionModifier().read(0));
                dataWatcher.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(3, WrappedDataWatcher.Registry.get(Boolean.class)), false);
                packet.getWatchableCollectionModifier().write(0, dataWatcher.getWatchableObjects());

                Bukkit.getScheduler().runTaskLater(dependencyPlugin, () -> {
                    pm.sendServerPacket((Player) npc.getEntity(), packet);
                    Bukkit.getLogger().log(Level.INFO, "Sent packet");
                }, 4 * 20);
                return;
            }
        } catch (Exception ex) {
            Bukkit.getLogger().log(Level.SEVERE, "An error occurred while hiding npc name tag.", ex);
        }
        Bukkit.getLogger().log(Level.WARNING, "Could not hide default NPC name tag.");
    }

    public void teleport(Location teleportLoc) {
        if (npc != null) {
            npc.teleport(teleportLoc, PlayerTeleportEvent.TeleportCause.PLUGIN);
            hologram.setPosition(Position.of(teleportLoc));
        }
        else create(teleportLoc);
    }

    public void remove() {
        if (npc != null) {
            activeNPCs.remove(npc.getMinecraftUniqueId());
            // npcTeam.removeEntry(npc.getUniqueId().toString());
            npc.destroy();
        }
        if (hologram != null) hologram.delete();
    }

    public void setHologramFooterVisibility(UUID playerUUID, boolean isVisible, boolean enableGlow) {
        if (hologram != null && hologram.getHologram(playerUUID) != null && !hologram.getHologram(playerUUID).isDeleted())
            hologram.setFooterVisibility(playerUUID, isVisible);
        if (npc != null && npc.getEntity() != null) npc.getEntity().setGlowing(enableGlow);
    }

    public String getName() {
        return name;
    }

    public String getSkinSignature() {
        return skinSignature;
    }

    public String getSkinTexture() {
        return skinTexture;
    }

    public NPC getNPC() {
        return npc;
    }

    public NPCHologram getHologram() {
        return hologram;
    }



    public static class NPCHologram extends HolographicDisplay {
        private static final double NPC_HOLOGRAM_Y = 2.3;
        private static final double NPC_HOLOGRAM_Y_WITH_FOOTER = 2.6;

        private final AbstractNPC npc;
        private boolean isFooterVisible = false;

        private Position position;

        public NPCHologram(@NotNull String id, Position position, AbstractNPC npc) {
            super(id, position.add(0, NPC_HOLOGRAM_Y, 0), false);
            this.npc = npc;
            this.position = position;
        }

        @Override
        public ItemStack getItem() {
            return null;
        }

        @Override
        public String getTitle(UUID playerUUID) {
            return GOLD + BOLD.toString() + npc.name;
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
            return isFooterVisible ? Collections.singletonList(new TextLine(npc.getDescription(playerUUID))) : new ArrayList<>();
        }

        public void setPosition(Position position) {
            this.position = position;
            getHolograms().values().forEach(holo -> holo.setPosition(position.add(0, isFooterVisible ? NPC_HOLOGRAM_Y_WITH_FOOTER : NPC_HOLOGRAM_Y, 0)));
        }

        public void setFooterVisibility(UUID playerUUID, boolean isVisible) {
            isFooterVisible = isVisible;
            getHologram(playerUUID).setPosition(position.add(0, isFooterVisible ? NPC_HOLOGRAM_Y_WITH_FOOTER : NPC_HOLOGRAM_Y, 0));
            reload(playerUUID);
        }

        public boolean isFooterVisible() {
            return isFooterVisible;
        }
    }
}
