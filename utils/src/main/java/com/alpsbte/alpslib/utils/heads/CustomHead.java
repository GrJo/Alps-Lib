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

package com.alpsbte.alpslib.utils.heads;

import com.alpsbte.alpslib.utils.AlpsUtils;
import com.alpsbte.alpslib.utils.item.ItemBuilder;
import com.google.common.base.Enums;
import me.arcaniax.hdb.api.HeadDatabaseAPI;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public final class CustomHead {
    public static HeadDatabaseAPI headDatabaseAPI;
    private ItemStack headItem;

    public CustomHead(String headID) {
        headItem = headDatabaseAPI != null && headID != null && AlpsUtils.TryParseInt(headID) != null
                ? headDatabaseAPI.getItemHead(headID) : null;

        if (headItem == null) {
            if (isDeprecatedSkullMaterial())
            headItem = new ItemBuilder(Material.valueOf("SKULL_ITEM"), 1, (byte) 3).build();
            else this.headItem = new ItemBuilder(Material.SKELETON_SKULL, 1).build();
        }
    }

    public ItemStack getAsItemStack() {
        return headItem;
    }

    public static boolean isDeprecatedSkullMaterial() { // Backwards compatibility
        return Enums.getIfPresent(Material.class, "SKULL_ITEM").orNull() != null;
    }
}
