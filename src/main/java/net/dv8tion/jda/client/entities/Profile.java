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

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.utils.Checks;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public interface Profile
{
    List<Badge> getBadges();

    List<ConnectedAccount> getConnectedAccounts();

    Badge getDisplayedBadge();

    List<Guild> getMutualGuilds();

    OffsetDateTime getNitroSince();

    enum Badge
    {
        DISCORD_PARTNER(3, 1),
        DISCORD_STAFF(4, 0),
        HYPESQUAD(2, 2),
        NITRO(1, -1),

        UNKNOWN(0, -1);

        public static List<Badge> getBadges(final int flags, final boolean nitro)
        {
            final ArrayList<Badge> badges = new ArrayList<>(1);

            if (nitro)
                badges.add(NITRO);

            for (final Badge badge : Badge.values())
            {
                if ((flags & badge.offset) == badge.offset)
                    badges.add(badge);
            }

            return badges;
        }

        public static byte getFlags(final Badge... badges)
        {
            Checks.notNull(badges, "Badges");
            byte flags = 0;
            for (final Badge badge : badges)
            {
                Checks.notNull(badge, "Badges");
                if (badge.offset != -1)
                    flags |= badge.value;
            }
            return flags;
        }

        public static byte getFlags(final Collection<Badge> badges)
        {
            Checks.notNull(badges, "Badges");
            byte flags = 0;
            for (final Badge badge : badges)
            {
                Checks.notNull(badge, "Badges");
                if (badge.offset != -1)
                    flags |= badge.value;
            }
            return flags;
        }

        public static final byte ALL_FLAGS = getFlags(DISCORD_PARTNER, DISCORD_STAFF, HYPESQUAD);

        private final byte offset;
        private final byte priority;
        private final byte value;

        Badge(final int priority, final int offset)
        {
            this.priority = (byte) priority;
            this.offset = (byte) offset;
            this.value = (byte) (1 << offset);
        }

        public byte getValue()
        {
            return value;
        }

        public byte getOffset()
        {
            return offset;
        }

        public byte getPriority()
        {
            return this.priority;
        }
    }
}
