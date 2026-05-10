package core.com.rylinaux.plugman.util;

/*
 * #%L
 * PlugMan
 * %%
 * Copyright (C) 2010 - 2014 PlugMan
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

import com.google.common.base.Preconditions;
import lombok.experimental.UtilityClass;

import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Utilities for String manipulation.
 *
 * @author rylinaux
 */
@UtilityClass
public class StringUtil {

    private static final Pattern HEX_PATTERN = Pattern.compile("(?i)<#([0-9a-f]{6})>");
    private static final Pattern CLOSING_TAG_PATTERN = Pattern.compile("(?i)</(bold|b|italic|i|underlined|u|strikethrough|st|obfuscated|obf)>");
    private static final Pattern OPENING_TAG_PATTERN = Pattern.compile("(?i)<(bold|b|italic|i|underlined|u|strikethrough|st|obfuscated|obf|reset)>");
    private static final Map<String, Character> LEGACY_TAG_CODES = Map.ofEntries(
        Map.entry("bold", 'l'),
        Map.entry("b", 'l'),
        Map.entry("italic", 'o'),
        Map.entry("i", 'o'),
        Map.entry("underlined", 'n'),
        Map.entry("u", 'n'),
        Map.entry("strikethrough", 'm'),
        Map.entry("st", 'm'),
        Map.entry("obfuscated", 'k'),
        Map.entry("obf", 'k'),
        Map.entry("reset", 'r')
    );

    /**
     * Returns an array of Strings as a single String.
     *
     * @param args  the array
     * @param start the index to start at
     * @return the array as a String
     */
    public static String consolidateStrings(String[] args, int start) {
        if (start < 0 || start > args.length) throw new IllegalArgumentException("Argument index out of bounds: " + start + "/" + args.length);

        return Stream.of(args).skip(start).collect(Collectors.joining(" "));
    }

    public static <T extends Collection<? super String>> T copyPartialMatches(String token, Iterable<String> originals, T collection) throws UnsupportedOperationException, IllegalArgumentException {
        Preconditions.checkArgument(token != null, "Search token cannot be null");
        Preconditions.checkArgument(collection != null, "Collection cannot be null");
        Preconditions.checkArgument(originals != null, "Originals cannot be null");

        for (var string : originals) {
            if (!startsWithIgnoreCase(string, token)) continue;
            collection.add(string);
        }

        return collection;
    }

    public static boolean startsWithIgnoreCase(String string, String prefix) throws IllegalArgumentException, NullPointerException {
        Preconditions.checkArgument(string != null, "Cannot check a null string for a match");
        return string.length() >= prefix.length() && string.regionMatches(true, 0, prefix, 0, prefix.length());
    }

    public static String convertMiniMessageToLegacy(String input, char colorChar) {
        if (input == null) return null;

        var withHex = HEX_PATTERN.matcher(input).replaceAll(match -> {
            var hex = match.group(1);
            var builder = new StringBuilder().append(colorChar).append('x');
            for (char c : hex.toCharArray()) {
                builder.append(colorChar).append(c);
            }
            return builder.toString();
        });

        var withoutClosing = CLOSING_TAG_PATTERN.matcher(withHex).replaceAll(String.valueOf(colorChar) + 'r');

        return OPENING_TAG_PATTERN.matcher(withoutClosing).replaceAll(match -> {
            var code = LEGACY_TAG_CODES.get(match.group(1).toLowerCase(Locale.ROOT));
            return code == null ? match.group() : String.valueOf(colorChar) + code;
        });
    }
}
