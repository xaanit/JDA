package net.dv8tion.jda.client.requests.restaction.pagination;

import gnu.trove.set.TLongSet;
import gnu.trove.set.hash.TLongHashSet;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.exceptions.AccountTypeException;
import net.dv8tion.jda.core.requests.Request;
import net.dv8tion.jda.core.requests.Response;
import net.dv8tion.jda.core.requests.Route;
import net.dv8tion.jda.core.requests.Route.CompiledRoute;
import net.dv8tion.jda.core.requests.restaction.pagination.PaginationAction;
import net.dv8tion.jda.core.utils.Checks;
import net.dv8tion.jda.core.utils.MiscUtil;
import org.json.JSONArray;
import org.json.JSONObject;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

//TODO: actual pagination including limit (aka offset) and handling total results change while paginating
public class SearchPaginationAction extends PaginationAction<SearchPaginationAction.SearchResult, SearchPaginationAction>
{
    protected static long toEpochMillis(final OffsetDateTime time)
    {
        return time == null ? 0 : time.toInstant().toEpochMilli();
    }

    protected static long toId(final long epochMillis)
    {
        return epochMillis == 0 ? 0 : epochMillis - MiscUtil.DISCORD_EPOCH << MiscUtil.TIMESTAMP_OFFSET;
    }

    protected final boolean isGuildSearch;
    protected long offset, author, totalResults, maxId, minId;
    protected boolean hasEmbed, hasFile, hasImage, hasLink, hasSound, hasVideo, includeNSFW;
    protected TextChannel channel;
    protected String content;
    protected TLongSet mentions;
    protected Mode mode;

    public SearchPaginationAction(final MessageChannel channel, long initialOffset)
    {
        super(channel.getJDA(), Route.Channels.SEARCH.compile(channel.getId()), 1, 25, 25);

        if (getJDA().getAccountType() != AccountType.CLIENT)
            throw new AccountTypeException(AccountType.CLIENT);
        this.channel = null;
        this.isGuildSearch = false;
        this.offset = initialOffset;
    }

    public SearchPaginationAction(final Guild guild, long initialOffset)
    {
        super(guild.getJDA(), Route.Guilds.SEARCH.compile(guild.getId()), 1, 25, 25);

        if (getJDA().getAccountType() != AccountType.CLIENT)
            throw new AccountTypeException(AccountType.CLIENT);
        this.channel = null;
        this.isGuildSearch = true;
        this.offset = initialOffset;
    }

    public SearchPaginationAction after(final long epochMillis)
    {
        return this.minId(SearchPaginationAction.toId(epochMillis));
    }

    public SearchPaginationAction after(final OffsetDateTime time)
    {
        return this.after(SearchPaginationAction.toEpochMillis(time));
    }

    public SearchPaginationAction author(final long authorId)
    {
        this.author = authorId;
        return this;
    }

    public SearchPaginationAction author(final String authorId)
    {
        return this.author(authorId == null ? 0 : MiscUtil.parseSnowflake(authorId));
    }

    public SearchPaginationAction author(final User author)
    {
        return this.author(author == null ? 0 : author.getIdLong());
    }

    public SearchPaginationAction before(final long epochMillis)
    {
        return this.maxId(SearchPaginationAction.toId(epochMillis));
    }

    public SearchPaginationAction before(final OffsetDateTime time)
    {
        return this.before(SearchPaginationAction.toEpochMillis(time));
    }

    public SearchPaginationAction channel(final TextChannel channel)
    {
        if (!this.isGuildSearch)
            throw new IllegalStateException("Channel may only be set for guild searches!");

        this.channel = channel;
        return this;
    }

    public SearchPaginationAction content(final String content)
    {
        this.content = content;
        return this;
    }

    public SearchPaginationAction hasEmbed(final boolean hasEmbed)
    {
        this.hasEmbed = hasEmbed;
        return this;
    }

    public SearchPaginationAction hasFile(final boolean hasFile)
    {
        this.hasFile = hasFile;
        return this;
    }

    public SearchPaginationAction hasImage(final boolean hasImage)
    {
        this.hasImage = hasImage;
        return this;
    }

    public SearchPaginationAction hasLink(final boolean hasLink)
    {
        this.hasLink = hasLink;
        return this;
    }

    public SearchPaginationAction hasSound(final boolean hasSound)
    {
        this.hasSound = hasSound;
        return this;
    }

    public SearchPaginationAction hasVideo(final boolean hasVideo)
    {
        this.hasVideo = hasVideo;
        return this;
    }

    public SearchPaginationAction includeNSFW(final boolean includeNSFW)
    {
        this.includeNSFW = includeNSFW;
        return this;
    }

    public SearchPaginationAction maxId(final long maxId)
    {
        this.maxId = maxId;
        return this;
    }

    public SearchPaginationAction mentions(final Collection<User> mentions)
    {
        if (mentions == null)
        {
            this.mentions = null;
            return this;
        }
        Checks.noneNull(mentions, "User");
        this.mentions = new TLongHashSet(mentions.size());
        for (final User user : mentions)
            this.mentions.add(user.getIdLong());
        return this;
    }

    public SearchPaginationAction minId(final long minId)
    {
        this.minId = minId;
        return this;
    }

    public SearchPaginationAction mode(final Mode mode)
    {
        this.mode = mode;
        return this;
    }

    @Override
    protected CompiledRoute finalizeRoute()
    {
        Route.CompiledRoute route = super.finalizeRoute();

        if (this.author != 0)
            route = route.withQueryParams("author_id", Long.toUnsignedString(this.author));
        if (this.content != null)
            route = route.withQueryParams("content", this.content);
        if (this.mentions != null)
        {
            final String[] params = new String[this.mentions.size() * 2];
            final long[] mentionsArray = this.mentions.toArray();
            for (int i = 0; i < this.mentions.size(); i += 2)
            {
                params[i] = "mentions";
                params[i + 1] = Long.toUnsignedString(mentionsArray[i / 2]);
            }
            route = route.withQueryParams(params);
        }
        if (this.includeNSFW)
            route = route.withQueryParams("include_nsfw", "true");
        if (this.hasEmbed)
            route = route.withQueryParams("has", "embed");
        if (this.hasFile)
            route = route.withQueryParams("has", "file");
        if (this.hasImage)
            route = route.withQueryParams("has", "image");
        if (this.hasLink)
            route = route.withQueryParams("has", "link");
        if (this.hasSound)
            route = route.withQueryParams("has", "sound");
        if (this.hasVideo)
            route = route.withQueryParams("has", "video");
        if (this.minId != 0)
            route = route.withQueryParams("min_id", Long.toUnsignedString(this.minId));
        if (this.maxId != 0)
            route = route.withQueryParams("max_id", Long.toUnsignedString(this.maxId));
        if (this.channel != null)
            route = route.withQueryParams("channel_id", this.channel.getId());
        if (this.mode == Mode.RELEVANT)
            route = route.withQueryParams("sort_by", "relevance");
        route = route.withQueryParams("limit", String.valueOf(limit.get()));

        if (offset > 0)
            route = route.withQueryParams("offset", String.valueOf(offset));

        return route;
    }

    @Override
    protected void handleResponse(final Response response, final Request<List<SearchResult>> request)
    {
        if (!response.isOk())
        {
            request.onFailure(response);
            return;
        }

        final JSONObject object = response.getObject();

        this.totalResults = object.getLong("total_results");
        final String analyticsId = object.getString("analytics_id");

        final JSONArray resultsArray = object.getJSONArray("messages");

        final List<SearchResult> results = new ArrayList<>(resultsArray.length());

        for (int i = 0; i < resultsArray.length(); i++)
        {
            final JSONArray messages = resultsArray.getJSONArray(i);

            Message[] result = new Message[messages.length()];
            int hit = 0;

            for (int j = 0; j < result.length; j++)
            {
                final JSONObject jsonObject = messages.getJSONObject(j);
                result[j] = api.getEntityBuilder().createMessage(jsonObject);
                if (jsonObject.has("hit") && jsonObject.getBoolean("hit"))
                    hit = j;
            }

            results.add(new SearchResult(analyticsId, result, hit));
        }
        offset += results.size();
        request.onSuccess(results);
    }

    public static class SearchResult
    {
        protected final String analyticsId;
        protected final Message[] messages;
        protected final int result;

        private SearchResult(final String analyticsId, final Message[] messages, int resultIndex)
        {
            this.analyticsId = analyticsId;
            this.messages = messages;
            this.result = resultIndex;
        }

        //max length = 2, min length = 0
        public Message[] getAfter()
        {
            if (messages.length - 1 == result)
                return new Message[0];
            return Arrays.copyOfRange(messages, result + 1, messages.length);
        }

        public String getAnalyticsId()
        {
            return this.analyticsId;
        }

        //max length = 2, min length = 0
        public Message[] getBefore()
        {
            return Arrays.copyOf(messages, result);
        }

        public Message getResult()
        {
            return messages[result];
        }
    }

    enum Mode
    {
        RECENT,
        RELEVANT
    }
}
