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

package com.alpsbte.alpslib.utils;

import com.alpsbte.alpslib.utils.heads.CustomHead;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public final class AlpsUtils {

    // Get custom head
    public static ItemStack getItemHead(CustomHead head) {
        return head.getAsItemStack();
    }

    // Get player head by UUID
    public static ItemStack getPlayerHead(UUID playerUUID) {
        ItemStack skull = null;
        if (CustomHead.isDeprecatedSkullMaterial())
            skull = new ItemStack(Material.valueOf("SKULL_ITEM"), 1, (short) SkullType.PLAYER.ordinal());
        else skull = new ItemStack(Material.PLAYER_HEAD, 1);

        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        meta.setOwningPlayer(Bukkit.getOfflinePlayer(playerUUID));
        skull.setItemMeta(meta);

        return skull;
    }

    // Integer Try Parser
    public static Integer TryParseInt(String someText) {
        try {
            return Integer.parseInt(someText);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    // Double Try Parser
    public static Double tryParseDouble(String value) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    /**
     * Get next enum in order of the enum
     *
     * @param <T>      The enum type
     * @param haystack The enum class (YourEnum.class)
     * @param needle   Current value that you want to get the next one of
     * @return Next enum in line
     */
    public static <T extends Enum<T>> Enum<T> getNextEnum(Class<? extends Enum<T>> haystack, T needle) {
        List<Enum<T>> enums = Arrays.asList(haystack.getEnumConstants());
        return getNextListItem(enums, needle);
    }

    public static <T> T getNextListItem(List<T> haystack, T needle) {
        if(!haystack.contains(needle) || haystack.indexOf(needle) + 1 >= haystack.size()) {
            return null;
        }
        return haystack.get(haystack.indexOf(needle) + 1);
    }


    /** This function creates a list of lines from one long string.
     *  Given a max value of characters per line it will iterate through the string till the maximum chars value and then back until the start of the word (until a space symbol is reached).
     *  Then it will cut that string into an extra line.
     *  This way the function will never cut a word in half and still keep the max char value (e.g. line breaks in word)
     *
     * @param maxCharsPerLine: max characters per line
     * @param lineBreaker characters which creates a new line (e.g. \n)
     */
    public static ArrayList<String> createMultilineFromString(String text, int maxCharsPerLine, String lineBreaker){
        ArrayList<String> list = new ArrayList<>();

        if (text == null) return list;
        // Split text at line breaker symbol, iterate through all subtexts and create all lists together to one large list.
        String[] texts = text.split(lineBreaker);

        for(String subText : texts)
            list.addAll(createMultilineFromString(subText, maxCharsPerLine));

        return list;
    }

    public static ArrayList<String> createMultilineFromString(String text, int maxCharsPerLine){
        int i = 0;
        ArrayList<String> list = new ArrayList<>();
        String currentText = text;
        boolean findSpace = false;


        // Create infinite loop with termination condition.
        while (true){

            // If current text is smaller than maxCharsPerLine, then add the rest of the text and return the list.
            if(currentText == null || currentText.length() < maxCharsPerLine) {
                if(currentText != null)
                    list.add(currentText);
                return list;
            }

            // If it should iterate through the word, increase i until it hits maxCharsPerLine
            if(!findSpace && i < maxCharsPerLine - 1){
                i++;

                // If it hit the maxCharsPerLine value, go back until it finds a space.
            }else{
                findSpace = true;

                // If it goes back to the start without finding a space symbol, return everything.
                if(i == 0){
                    list.add(currentText);
                    return list;
                }

                char currentSymbol = currentText.charAt(i);

                // If it reaches a space symbol, split the text from start till i and add it to the list
                if(currentSymbol == ' '){
                    String firstPart = currentText.substring(0 , i);
                    String lastPart = currentText.substring(i+1);

                    list.add(firstPart);
                    currentText = lastPart;
                    findSpace = false;
                }

                i--;
            }
        }
    }
}
