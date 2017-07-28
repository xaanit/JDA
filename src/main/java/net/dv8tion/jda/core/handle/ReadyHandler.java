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

package net.dv8tion.jda.core.handle;

import gnu.trove.iterator.TLongIterator;
import gnu.trove.set.TLongSet;
import gnu.trove.set.hash.TLongHashSet;
import net.dv8tion.jda.client.entities.Relationship;
import net.dv8tion.jda.client.entities.impl.FriendImpl;
import net.dv8tion.jda.client.entities.impl.UserSettingsImpl;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.WebSocketCode;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.EntityBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.managers.impl.PresenceImpl;
import net.dv8tion.jda.core.requests.WebSocketClient;
import net.dv8tion.jda.core.utils.data.DataArray;
import net.dv8tion.jda.core.utils.data.DataObject;

public class ReadyHandler extends SocketHandler
{
    private final TLongSet incompleteGuilds = new TLongHashSet();
    private final TLongSet acknowledgedGuilds = new TLongHashSet();
    private final TLongSet unavailableGuilds = new TLongHashSet();
    private final TLongSet guildsRequiringChunking = new TLongHashSet();
    private final TLongSet guildsRequiringSyncing = new TLongHashSet();

    public ReadyHandler(JDAImpl api)
    {
        super(api);
    }

    @Override
    protected Long handleInternally(DataObject content)
    {
        EntityBuilder builder = api.getEntityBuilder();

        //Core
        DataArray guilds = content.getArray("guilds");
        DataObject selfJson = content.getObject("user");

        builder.createSelfUser(selfJson);

        if (api.getAccountType() == AccountType.CLIENT && !content.isNull("user_settings"))
        {
            // handle user settings
            DataObject userSettingsJson = content.getObject("user_settings");
            UserSettingsImpl userSettingsObj = (UserSettingsImpl) api.asClient().getSettings();
            userSettingsObj
                    // TODO: set all information and handle updates
                    .setStatus(userSettingsJson.isNull("status") ? OnlineStatus.ONLINE : OnlineStatus.fromKey(userSettingsJson.getString("status")));
            // update presence information unless the status is ONLINE
            if (userSettingsObj.getStatus() != OnlineStatus.ONLINE)
                ((PresenceImpl) api.getPresence()).setCacheStatus(userSettingsObj.getStatus());
        }

        //Keep a list of all guilds in incompleteGuilds that need to be setup (GuildMemberChunk / GuildSync)
        //Send all guilds to the EntityBuilder's first pass to setup caching for when GUILD_CREATE comes
        // or, for Client accounts, to start the setup process (since we already have guild info)
        //Callback points to guildSetupComplete so that when MemberChunking and GuildSync processes are done, we can
        // "check off" the completed guild from the set of guilds in incompleteGuilds.

        for (int i = 0; i < guilds.length(); i++)
        {
            DataObject guild = guilds.getObject(i);
            incompleteGuilds.add(guild.getLong("id"));
        }

        //We use two different for-loops here so that we cache all of the ids before sending them off to the EntityBuilder
        //  due to the checks in checkIfReadyToSendRequests and guildSetupComplete triggering too soon otherwise.
        // Specifically: incompleteGuilds.size() == acknowledgedGuilds.size() and
        //  incompleteGuilds.size() == unavailableGuilds.size() respectively.

        for (int i = 0; i < guilds.length(); i++)
        {
            DataObject guild = guilds.getObject(i);

            //If a Guild isn't unavailable, then it is possible that we were given all information
            // needed to fully load the guild. In this case, we provide the method `guildSetupComplete`
            // as the secondPassCallback so it can immediately be called to signify that the provided guild
            // is loaded and ready to go.
            //If a Guild is unavailable it won't have the information needed, so we pass null as the secondPassCallback
            // for now and wait for the GUILD_CREATE event to give us the required information.
            if (guild.containsKey("unavailable") && guild.getBoolean("unavailable"))
                builder.createGuildFirstPass(guild, null);
            else
                builder.createGuildFirstPass(guild, this::guildSetupComplete);
        }

        if (guilds.length() == 0)
            guildLoadComplete(content);

        return null;
    }

    public void guildLoadComplete(DataObject content)
    {
        api.getClient().setChunkingAndSyncing(false);
        EntityBuilder builder = api.getEntityBuilder();
        DataArray privateChannels = content.getArray("private_channels");

        if (api.getAccountType() == AccountType.CLIENT)
        {
            DataArray relationships = content.getArray("relationships");
            DataArray presences = content.getArray("presences");
            DataObject notes = content.getObject("notes");
            DataArray readstates = content.containsKey("read_state") ? content.getArray("read_state") : null;
            DataArray guildSettings = content.containsKey("user_guild_settings") ? content.getArray("user_guild_settings") : null;

            for (int i = 0; i < relationships.length(); i++)
            {
                DataObject relationship = relationships.getObject(i);
                Relationship r = builder.createRelationship(relationship);
                if (r == null)
                    JDAImpl.LOG.fatal("Provided relationship in READY with an unknown type! JSON: " + relationship.toString());
            }

            for (int i = 0; i < presences.length(); i++)
            {
                DataObject presence = presences.getObject(i);
                long userId = presence.getObject("user").getLong("id");
                FriendImpl friend = (FriendImpl) api.asClient().getFriendById(userId);
                if (friend == null)
                    WebSocketClient.LOG.warn("Received a presence in the Presences array in READY that did not correspond to a cached Friend! JSON: " + presence);
                else
                    builder.createPresence(friend, presence);
            }
        }

        for (int i = 0; i < privateChannels.length(); i++)
        {
            DataObject chan = privateChannels.getObject(i);
            ChannelType type = ChannelType.fromId(chan.getInt("type"));

            switch (type)
            {
                case PRIVATE:
                    builder.createPrivateChannel(chan);
                    break;
                case GROUP:
                    builder.createGroup(chan);
                    break;
                default:
                    WebSocketClient.LOG.warn("Received a Channel in the priv_channels array in READY of an unknown type! JSON: " + type);
            }

        }

        api.getClient().ready();
    }

    public void acknowledgeGuild(Guild guild, boolean available, boolean requiresChunking, boolean requiresSync)
    {
        acknowledgedGuilds.add(guild.getIdLong());
        if (available)
        {
            //We remove from unavailable guilds because it is possible that we were told it was unavailable, but
            // during a long READY load it could have become available and was sent to us.
            unavailableGuilds.remove(guild.getIdLong());
            if (requiresChunking)
                guildsRequiringChunking.add(guild.getIdLong());
            if (requiresSync)
                guildsRequiringSyncing.add(guild.getIdLong());
        }
        else
            unavailableGuilds.add(guild.getIdLong());

        checkIfReadyToSendRequests();
    }

    public void guildSetupComplete(Guild guild)
    {
        if (!incompleteGuilds.remove(guild.getIdLong()))
            WebSocketClient.LOG.fatal("Completed the setup for Guild: " + guild + " without matching id in ReadyHandler cache");
        if (incompleteGuilds.size() == unavailableGuilds.size())
            guildLoadComplete(allContent.getObject("d"));
        else
            checkIfReadyToSendRequests();
    }


    public void clearCache()
    {
        incompleteGuilds.clear();
        acknowledgedGuilds.clear();
        unavailableGuilds.clear();
        guildsRequiringChunking.clear();
        guildsRequiringSyncing.clear();
    }

    private void checkIfReadyToSendRequests()
    {
        if (acknowledgedGuilds.size() == incompleteGuilds.size())
        {
            api.getClient().setChunkingAndSyncing(true);
            if (api.getAccountType() == AccountType.CLIENT)
                sendGuildSyncRequests();
            sendMemberChunkRequests();
        }
    }

    private void sendGuildSyncRequests()
    {
        if (guildsRequiringSyncing.isEmpty())
            return;

        DataArray guildIds = new DataArray();

        for (TLongIterator it = guildsRequiringSyncing.iterator(); it.hasNext(); )
        {
            guildIds.put(it.next());

            //We can only request 50 guilds in a single request, so after we've reached 50, send them
            // and reset the
            if (guildIds.length() == 50)
            {
                api.getClient().chunkOrSyncRequest(new DataObject()
                        .put("op", WebSocketCode.GUILD_SYNC)
                        .put("d", guildIds));
                guildIds = new DataArray();
            }
        }

        //Send the remaining guilds that need to be sent
        if (guildIds.length() > 0)
        {
            api.getClient().chunkOrSyncRequest(new DataObject()
                    .put("op", WebSocketCode.GUILD_SYNC)
                    .put("d", guildIds));
        }
        guildsRequiringSyncing.clear();
    }

    private void sendMemberChunkRequests()
    {
        if (guildsRequiringChunking.isEmpty())
            return;

        DataArray guildIds = new DataArray();
        for (TLongIterator it = guildsRequiringChunking.iterator(); it.hasNext(); )
        {
            guildIds.put(it.next());

            //We can only request 50 guilds in a single request, so after we've reached 50, send them
            // and reset the
            if (guildIds.length() == 50)
            {
                api.getClient().chunkOrSyncRequest(new DataObject()
                    .put("op", 8)
                    .put("d", new DataObject()
                        .put("guild_id", guildIds)
                        .put("query", "")
                        .put("limit", 0)
                    ));
                guildIds = new DataArray();
            }
        }

        //Send the remaining guilds that need to be sent
        if (guildIds.length() > 0)
        {
            api.getClient().chunkOrSyncRequest(new DataObject()
                .put("op", 8)
                .put("d", new DataObject()
                        .put("guild_id", guildIds)
                        .put("query", "")
                        .put("limit", 0)
                ));
        }
        guildsRequiringChunking.clear();
    }
}
