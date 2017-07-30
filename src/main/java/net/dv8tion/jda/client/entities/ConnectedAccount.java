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

public interface ConnectedAccount
{

    String getId();

    String getName();

    Type getType();

    int getVisibility();

    boolean isFriendSync();

    boolean isRevoked();

    boolean isVerified();

    enum Type
    {
        LEAGUE_OF_LEGENDS("leagueoflegends"),
        REDDIT("reddit"),
        SKYPE("skype"),
        STEAM("steam"),
        TWITCH("twitch"),
        TWITTER("twitter"),
        YOUTUBE("youtube"),
        UNKNOWN("");

        public static Type fromName(final String name)
        {
            if (name == null || name.isEmpty())
                return UNKNOWN;

            switch (name.toLowerCase())
            {
                case "lol":
                case "leagueoflegends":
                    return LEAGUE_OF_LEGENDS;
                case "reddit": return REDDIT;
                case "skype": return SKYPE;
                case "steam": return STEAM;
                case "twitch": return TWITCH;
                case "twitter": return TWITTER;
                case "youtube": return YOUTUBE;
                default: return UNKNOWN;
            }
        }

        private final String name;

        Type(final String name)
        {
            this.name = name;
        }

        public String getName()
        {
            return this.name;
        }
    }
}
