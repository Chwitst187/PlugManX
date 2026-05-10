package core.com.rylinaux.plugman.file.messaging;

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

import core.com.rylinaux.plugman.config.YamlConfigurationProvider;
import core.com.rylinaux.plugman.messaging.ColorFormatter;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.nio.file.Path;
import java.util.regex.Pattern;

/**
 * Manages custom messages.
 *
 * @author rylinaux
 */
@Getter
@RequiredArgsConstructor
public class MessageFormatter {
    private static final Pattern MINI_HEX_PATTERN = Pattern.compile("<#[A-Fa-f0-9]{6}>");

    private final MessageFile messageFile;
    private final ColorFormatter colorFormatter;

    /**
     * Construct our object.
     *
     * @param yamlProvider   the YAML configuration provider
     * @param colorFormatter the color formatter
     */
    public MessageFormatter(YamlConfigurationProvider yamlProvider, ColorFormatter colorFormatter) {
        messageFile = new MessageFile(Path.of("plugins", "PlugManX", "messages.yml").toFile(), yamlProvider);
        this.colorFormatter = colorFormatter;
    }

    /**
     * Returns the formatted version of the message.
     *
     * @param key  the key
     * @param args the args to replace
     * @return the formatted String
     */
    public String formatMessage(String key, Object... args) {
        return formatMessage(true, key, args);
    }

    /**
     * Returns the formatted version of the message.
     *
     * @param prefix whether to prepend with the plugin's prefix
     * @param key    the key
     * @param args   the args to replace
     * @return the formatted String
     */
    public String formatMessage(boolean prefix, String key, Object... args) {
        var rawMessage = messageFile.getString(key);
        if (rawMessage == null) return "Error: '" + key + "' not found in messages.yml";

        var message = prefix? messageFile.getString("prefix") + rawMessage : rawMessage;

        for (var i = 0; i < args.length; i++) message = message.replace("{" + i + "}", String.valueOf(args[i]));
        return colorFormatter.translateAlternateColorCodes('&', convertMiniMessageHexToLegacyAmpersand(message));
    }

    /**
     * Add the prefix to a message.
     *
     * @param msg the message.
     * @return the message with the prefix.
     */
    public String prefix(String msg) {
        return colorFormatter.translateAlternateColorCodes('&', convertMiniMessageHexToLegacyAmpersand(messageFile.getString("prefix") + msg));
    }

    private static String convertMiniMessageHexToLegacyAmpersand(String message) {
        var matcher = MINI_HEX_PATTERN.matcher(message);
        var result = new StringBuilder();

        while (matcher.find()) {
            var hex = matcher.group().substring(2, 8);
            var replacement = new StringBuilder("&x");
            for (var i = 0; i < hex.length(); i++) replacement.append('&').append(hex.charAt(i));
            matcher.appendReplacement(result, replacement.toString());
        }

        matcher.appendTail(result);
        return result.toString();
    }
}
