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

package com.alpsbte.alpslib.utils.item;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ItemBuilder {
    private final ItemStack item;
    protected final ItemMeta itemMeta;

    public ItemBuilder(ItemStack item) {
        itemMeta = item.getItemMeta();
        if (itemMeta != null) itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        this.item = item;
    }

    @Deprecated
    public ItemBuilder(Material material, int amount, byte color) {
        item = new ItemStack(material, amount, color);
        itemMeta = item.getItemMeta();
        if (itemMeta != null) itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
    }

    public ItemBuilder(Material material) {
        this(material, 1, (byte) 0);
    }

    public ItemBuilder(Material material, int amount) {
        this(material, amount, (byte) 0);
    }

    @Deprecated
    public ItemBuilder setName(String name) {
        itemMeta.displayName(LegacyComponentSerializer.legacySection().deserialize(name));
        return this;
    }

    public ItemBuilder setName(Component component) {
        itemMeta.displayName(component);
        return this;
    }

    @Deprecated
    public ItemBuilder setLore(ArrayList<String> lore) {
        List<Component> components = new ArrayList<>();
        for (String loreStr : lore) {
            components.add(LegacyComponentSerializer.legacySection().deserialize(loreStr));
        }
        itemMeta.lore(components);
        return this;
    }

    public ItemBuilder setLore(List<Component> components) {
        itemMeta.lore(components);
        return this;
    }

    public ItemBuilder setEnchanted(boolean setEnchanted) {
        if(setEnchanted) {
            itemMeta.addEnchant(Enchantment.ARROW_DAMAGE,1,true);
            itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        } else {
            itemMeta.removeEnchant(Enchantment.ARROW_DAMAGE);
            itemMeta.removeItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        return this;
    }

    public ItemBuilder setItemModel(int model) {
        itemMeta.setCustomModelData(model);
        return this;
    }

    public ItemStack build() {
        item.setItemMeta(itemMeta);
        return item;
    }
}
