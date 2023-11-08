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

import com.alpsbte.alpslib.utils.AlpsUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

import java.util.ArrayList;
import java.util.List;

import static net.kyori.adventure.text.Component.empty;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.TextDecoration.ITALIC;

public class LoreBuilder {
    public static final Component LORE_COMPONENT = empty().decoration(ITALIC, TextDecoration.State.FALSE);
    public static final int MAX_LORE_LINE_LENGTH = 40;
    private final ArrayList<Component> lore = new ArrayList<>();

    public LoreBuilder addLine(String line) {
        List<String> lines = AlpsUtils.createMultilineFromString(line, MAX_LORE_LINE_LENGTH, AlpsUtils.LINE_BREAKER);
        for (String l : lines) addLine(text(l));
        return this;
    }

    public LoreBuilder addLine(Component line) {
        lore.add(line.hasStyling() ? LORE_COMPONENT.append(line) : LORE_COMPONENT.append(line).color(NamedTextColor.GRAY));
        return this;
    }

    public LoreBuilder addLines(String... lines) {
        for (String line : lines) addLine(line);
        return this;
    }

    public LoreBuilder addLines(Component... lines) {
        for (Component line : lines) addLine(line);
        return this;
    }

    public LoreBuilder addLines(List<Component> lines) {
        for (Component line : lines) {
            addLine(line);
        }
        return this;
    }

    public LoreBuilder emptyLine() {
        lore.add(text(""));
        return this;
    }

    public ArrayList<Component> build() {
        return lore;
    }
}
