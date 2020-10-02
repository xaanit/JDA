/*
 * Copyright 2015-2020 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.dv8tion.jda.api.entities;

import javax.annotation.Nonnull;

public enum WebhookType
{
    /** Normal webhooks that can be used for sending messages */
    INCOMING(1),
    /** Webhook responsible for re-posting messages from another channel */
    FOLLOWER(2),
    /** Placeholder for unsupported types */
    UNKNOWN(-1);

    private final int key;

    WebhookType(int key)
    {
        this.key = key;
    }

    /**
     * The raw api key for this type
     *
     * @return The api key, or -1 for {@link #UNKNOWN}
     */
    public int getKey()
    {
        return key;
    }

    /**
     * Resolves the provided raw api key to the corresponding webhook type.
     *
     * @param  key
     *         The key
     *
     * @return The WebhookType or {@link #UNKNOWN}
     */
    @Nonnull
    public static WebhookType fromKey(int key)
    {
       WebhookType[] values = values();
       if(key > values[values.length - 1].key) return UNKNOWN;
       return values[key - 1]; // Webhook keys are 1 based while array indexes are 0 based.
    }
}
