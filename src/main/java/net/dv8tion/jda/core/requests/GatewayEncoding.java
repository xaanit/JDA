package net.dv8tion.jda.core.requests;

public enum GatewayEncoding
{
    /**
     * Communication using JSON (JavaScript Object Notation). 
     * 
     * @see    <a href="http://json.org/">json.org</a>
     */
    JSON("json"),

    /**
     * Communication using Erlang's ETF (External Term Format)
     * 
     * @see    <a href="http://erlang.org/doc/apps/erts/erl_ext_dist.html">erlang.org</a>
     */
    ETF("etf");

    private final String key;

    GatewayEncoding(String key)
    {
        this.key = key;
    }

    public String getKey()
    {
        return key;
    }
}