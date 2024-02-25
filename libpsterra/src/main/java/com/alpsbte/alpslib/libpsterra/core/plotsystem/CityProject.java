package com.alpsbte.alpslib.libpsterra.core.plotsystem;

import com.alpsbte.alpslib.utils.item.LegacyItemBuilder;
import com.alpsbte.alpslib.utils.item.LegacyLoreBuilder;
import com.alpsbte.alpslib.libpsterra.utils.Utils;
import org.bukkit.inventory.ItemStack;

public class CityProject {

    public final int id;
    public final String name;
    public final int country_id;
    
    //use Connection.getCityProject(ID) instead of this constructor
    public CityProject(int id, int country_id, String name) {
        this.id = id;
        this.country_id = country_id;
        this.name = name;
      
    }



    public ItemStack getItem(String countryHeadID) {
        return new LegacyItemBuilder(Utils.getItemHead(countryHeadID))
                .setName("§b§l" + name)
                .setLore(new LegacyLoreBuilder()
                        .addLine("§bID: §7" + id)
                        .build())
                .build();
    }
}
