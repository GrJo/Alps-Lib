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

package com.alpsbte.alpslib.utils.head;

import com.alpsbte.alpslib.utils.AlpsUtils;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import me.arcaniax.hdb.api.HeadDatabaseAPI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AlpsHeadUtils {
    private static HeadDatabaseAPI headDatabaseAPI;

    private static final Cache<String, ItemStack> customHeads = CacheBuilder.newBuilder().build();
    private static final List<String> unregisteredCustomHeads = new ArrayList<>();

    private static final Cache<UUID, ItemStack> playerHeads = CacheBuilder.newBuilder().build();

    public static void registerCustomHead(String headDbId) {
        if (headDatabaseAPI == null) {
            unregisteredCustomHeads.add(headDbId);
        } else customHeads.put(headDbId, getCustomHead(headDbId));
    }

    public static void registerCustomHeads(List<String> headDbIds) {
        headDbIds.forEach(AlpsHeadUtils::registerCustomHead);
    }

    public static void unregisterCustomHead(String headDbId) {
        customHeads.invalidate(headDbId);
    }

    public static void unregisterCustomHeads() {
        customHeads.invalidateAll();
    }

    public static void registerPlayerHead(UUID playerUUID) {
        playerHeads.put(playerUUID, getPlayerHead(playerUUID));
    }

    public static void unregisterPlayerHead(UUID playerUUID) {
        playerHeads.invalidate(playerUUID);
    }

    public static ItemStack getCustomHead(String headDbId) {
        ItemStack customHead = customHeads.getIfPresent(headDbId);
        if (customHead != null) return customHead;
        if (headDatabaseAPI == null || AlpsUtils.tryParseInt(headDbId) == null || !headDatabaseAPI.isHead(headDbId))
            return new ItemStack(Material.SKELETON_SKULL, 1);
        return headDatabaseAPI.getItemHead(headDbId);
    }

    public static ItemStack getPlayerHead(UUID playerUUID) {
        ItemStack playerHead = customHeads.getIfPresent(playerUUID.toString());
        if (playerHead != null) return playerHead;
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD, 1);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        if (meta == null) return skull;
        meta.setOwningPlayer(Bukkit.getOfflinePlayer(playerUUID));
        skull.setItemMeta(meta);
        return skull;
    }

    public static void setHeadDatabaseAPI(HeadDatabaseAPI headDatabaseAPI) {
        AlpsHeadUtils.headDatabaseAPI = headDatabaseAPI;
        if (headDatabaseAPI == null) return;
        unregisteredCustomHeads.forEach(id -> customHeads.put(id, getCustomHead(id)));
        unregisteredCustomHeads.clear();
    }

    public static HeadDatabaseAPI getHeadDatabaseAPI() {
        return headDatabaseAPI;
    }

    public static Cache<String, ItemStack> getCustomHeads() {
        return customHeads;
    }

    public static Cache<UUID, ItemStack> getPlayerHeads() {
        return playerHeads;
    }
}
