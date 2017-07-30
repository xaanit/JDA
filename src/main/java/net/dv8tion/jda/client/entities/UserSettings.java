/*
 *     Copyright 2015-2017 Austin Keener & Michael Ritter & Florian Spieß
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.dv8tion.jda.client.entities;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.Guild;

import java.util.List;
import java.util.Map;

public interface UserSettings
{
    int getAfkTimeout();

    boolean isConvertEmoticons();

    boolean isDefaultGuildsRestricted();

    boolean isDetectPlatformAccounts();

    boolean isDeveloperMode();

    boolean isEnableTTSCommand();

    ExplicitContentLevel getExplicitContentFilter();

    Map<String, Boolean> getFriendSourceFlags(); // TODO: what are these?

    List<Guild> getGuildPositions();

    boolean isInlineAttachmentMedia();

    boolean isInlineEmbedMedia();

    JDA getJDA();

    Locale getLocale();

    boolean isMessageDisplayCompact();

    boolean isRenderEmbeds();

    boolean isRenderReactions();

    boolean isShowCurrentGame();

    List<Guild> getRestrictedGuilds();

    OnlineStatus getStatus();

    Theme getTheme();

    int getTimezoneOffset();

    enum ExplicitContentLevel
    {
        I_LIVE_ON_THE_EDGE(0),
        MY_FRIENDS_ARE_NICE(1),
        KEEP_ME_SAFE(2),

        UNKNOWN(-1);

        public static ExplicitContentLevel fromKey(final int key)
        {
            ExplicitContentLevel[] values = values();
            return key >= 0 && key < 3 ? values[key] : UNKNOWN;
        }

        private final int key;

        ExplicitContentLevel(final int key)
        {
            this.key = key;
        }

        public int getKey()
        {
            return this.key;
        }
    }

    enum Locale
    {
        BULGARIAN("bg"),
        CZECH("cs"),
        DANISH("da"),
        DUTCH("nl"),
        ENGLISH_UK("en-GB"),
        ENGLISH_US("en-US"),
        FINNISH("fi"),
        FRENCH("fr"),
        GERMAN("de"),
        ITALIAN("it"),
        JAPANESE("ja"),
        KOREAN("ko"),
        NORWEGIAN("no"),
        POLISH("pl"),
        PORTUGUESE_BRAZILIAN("pt-BR"),
        RUSSIAN("ru"),
        SPANISH("es-ES"),
        SWEDISH("sv-SE"),
        TRADITIONAL_CHINESE("zh-TW"),
        TURKISH("tr"),
        UKRAINIAN("uk"),

        UNKNOWN("");

        public static Locale fromKey(final String key)
        {
            if (key == null || key.isEmpty())
                return UNKNOWN;

            switch (key.toLowerCase())
            {
                case "bg": return BULGARIAN;
                case "cs": return CZECH;
                case "da": return DANISH;
                case "nl": return DUTCH;
                case "en-us": return ENGLISH_US;
                case "en-uk": return ENGLISH_UK;
                case "fi": return FINNISH;
                case "fr": return FRENCH;
                case "de": return GERMAN;
                case "it": return ITALIAN;
                case "ja": return JAPANESE;
                case "ko": return KOREAN;
                case "no": return NORWEGIAN;
                case "pl": return POLISH;
                case "pr-br": return PORTUGUESE_BRAZILIAN;
                case "ru": return RUSSIAN;
                case "es-es": return SPANISH;
                case "sv-se": return SWEDISH;
                case "zh-tw": return TRADITIONAL_CHINESE;
                case "tr": return TURKISH;
                case "uk": return UKRAINIAN;

                default: return UNKNOWN;
            }
        }

        private final String key;

        Locale(final String key)
        {
            this.key = key;
        }

        public String getKey()
        {
            return this.key;
        }
    }

    enum Theme
    {
        DARK("dark"),
        LIGHT("light"),

        UNKNOWN("");

        public static Theme fromKey(final String key)
        {
            if (key == null || key.isEmpty())
                return UNKNOWN;
            switch (key)
            {
                case "dark":
                    return DARK;
                case "light":
                    return LIGHT;
                default:
                    return UNKNOWN;
            }
        }

        private final String key;

        Theme(final String key)
        {
            this.key = key;
        }

        public String getKey()
        {
            return this.key;
        }
    }
}
