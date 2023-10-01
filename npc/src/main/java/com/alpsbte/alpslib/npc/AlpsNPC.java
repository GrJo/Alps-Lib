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
import net.citizensnpcs.trait.LookClose;
import net.citizensnpcs.trait.SkinTrait;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.logging.Level;

import static net.md_5.bungee.api.ChatColor.BOLD;
import static net.md_5.bungee.api.ChatColor.GOLD;

public class AlpsNPC {
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
    private final String description;
    private final String skinTexture;
    private final String skinSignature;

    private NPC npc;
    private NPCHologram hologram;

    public AlpsNPC(String name, String description, String skinTexture, String skinSignature) {
        this.name = name;
        this.description = description;
        this.skinTexture = skinTexture;
        this.skinSignature = skinSignature;
    }

    public void create(Location spawnLoc) {
        if (npc != null) remove();

        npc = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, "");

        // Set NPC Skin
        npc.getOrAddTrait(SkinTrait.class).setSkinPersistent("npc_skin_" + npc.getId(), skinSignature, skinTexture);
        npc.getOrAddTrait(LookClose.class).lookClose(true);

        // Spawn NPC
        npc.spawn(spawnLoc);
        activeNPCs.add(npc.getMinecraftUniqueId());

        // Set NPC Hologram
        hologram = new NPCHologram("npc_" + npc.getId(), name, description);
        hologram.create(Position.of(spawnLoc));

        // Hide default NPC name tag
        /*
        npcTeam.addEntry(npc.getUniqueId().toString());
        ((Player) npc.getEntity()).setScoreboard(npcScoreboard);
        Bukkit.getLogger().log(Level.INFO, "Updated Name Tag");
        **/

        try {
            if (dependencyPlugin != null) {
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
        if (npc != null) npc.teleport(teleportLoc, PlayerTeleportEvent.TeleportCause.PLUGIN);
        else create(teleportLoc);
    }

    public void remove() {
        if (npc != null) {
            activeNPCs.remove(npc.getMinecraftUniqueId());
            // npcTeam.removeEntry(npc.getUniqueId().toString());
            npc.destroy();
        }
        if (hologram != null) hologram.remove();
    }

    public void setHologramFooterVisibility(boolean isVisible, boolean enableGlow) {
        if (hologram != null && hologram.getHologram() != null && !hologram.getHologram().isDeleted()) hologram.setFooterVisibility(isVisible);
        if (npc != null && npc.getEntity() != null) npc.getEntity().setGlowing(enableGlow);
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

        private final String npcName;
        private final String holoFooter;
        private boolean isFooterVisible = false;

        private Position position;

        public NPCHologram(@NotNull String id, String npcName, String holoFooter) {
            super(id);
            this.npcName = npcName;
            this.holoFooter = holoFooter;
        }

        @Override
        public void create(Position position) {
            this.position = position;
            super.create(position.add(0, NPC_HOLOGRAM_Y, 0));
        }

        @Override
        public ItemStack getItem() {
            return null;
        }

        @Override
        public String getTitle() {
            return GOLD + BOLD.toString() + npcName;
        }

        @Override
        public List<DataLine<?>> getHeader() {
            return Collections.singletonList(new TextLine(this.getTitle()));
        }

        @Override
        public List<DataLine<?>> getContent() {
            return new ArrayList<>();
        }

        @Override
        public List<DataLine<?>> getFooter() {
            return isFooterVisible ? Collections.singletonList(new TextLine(holoFooter)) : new ArrayList<>();
        }

        public void setFooterVisibility(boolean isVisible) {
            isFooterVisible = isVisible;
            getHologram().setPosition(position.add(0, isFooterVisible ? NPC_HOLOGRAM_Y_WITH_FOOTER : NPC_HOLOGRAM_Y, 0));
            reload();
        }

        public boolean isFooterVisible() {
            return isFooterVisible;
        }
    }
}
