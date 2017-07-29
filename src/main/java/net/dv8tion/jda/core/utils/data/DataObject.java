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

package net.dv8tion.jda.core.utils.data;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.*;
import java.util.function.Function;

public class DataObject implements Map<String, Object>
{
    private static final ObjectMapper jsonMapper = new ObjectMapper();
    private static final MapType jacksonType = jsonMapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class);

    private final Map<String, Object> data;

    public DataObject()
    {
        this.data = new HashMap<>();
    }

    public DataObject(Map<String, Object> data)
    {
        this.data = data;
    }

    public DataObject(int size)
    {
        this.data = new HashMap<>(size);
    }

    public boolean isNull(String key)
    {
        return get(key) == null;
    }

    public int getInt(String key)
    {
        return get(Integer.class, key, Integer::parseInt, Number::intValue);
    }

    public long getLong(String key)
    {
        return get(Long.class, key, Long::parseLong, Number::longValue);
    }

    public long getUnsignedLong(String key)
    {
        return get(Long.class, key, Long::parseUnsignedLong, Number::longValue);
    }

    public double getDouble(String key)
    {
        return get(Double.class, key, Double::parseDouble, Number::doubleValue);
    }

    public boolean getBoolean(String key)
    {
        return get(Boolean.class, key, Boolean::parseBoolean, null);
    }

    public String getString(String key)
    {
        return get(String.class, key);
    }

    @SuppressWarnings("unchecked")
    public DataObject getObject(String key)
    {
        try
        {
            return new DataObject((Map<String, Object>) get(Map.class, key));
        }
        catch (ClassCastException ex)
        {
            ex.printStackTrace();
        }
        throw new DataReadException(String.format("Map with key %s inside of DataObject couldn't properly be assigned to DataObject", key));
    }

    @SuppressWarnings("unchecked")
    public DataArray getArray(String key)
    {
        try
        {
            return new DataArray((List<Object>) get(List.class, key));
        }
        catch (ClassCastException ex)
        {
            ex.printStackTrace();
        }
        throw new DataReadException(String.format("Array with key %s inside of DataObject couldn't properly be assigned to DataArray", key));
    }

    /**
     * Returns the JSON representation for this object
     */
    @Override
    public String toString()
    {
        try
        {
            return jsonMapper.writeValueAsString(data);
        }
        catch (JsonProcessingException e)
        {
            e.printStackTrace();
        }
        return "{}";
    }

    public static DataObject fromJson(String json) throws IOException
    {
        Map<String, Object> map = jsonMapper.readValue(json, jacksonType);
        return new DataObject(map);
    }

    public static DataObject fromJson(InputStream is) throws IOException
    {
        Map<String, Object> map = jsonMapper.readValue(is, jacksonType);
        return new DataObject(map);
    }

    public static DataObject fromJson(Reader reader) throws IOException
    {
        Map<String, Object> map = jsonMapper.readValue(reader, jacksonType);
        return new DataObject(map);
    }

    //START MAP

    /**
     * Puts the given object into the DataObject using a key.
     * It then returns the DataObject for chaining.
     * <b>Warning: This does not return the previous value as described in the Map interface!</b>
     *
     * @param key the storage key
     * @param o the object that should be stored
     * @return the DataObject instance for chaining
     */
    @Override
    public DataObject put(String key, Object o)
    {
        data.put(key, o);
        return this;
    }

    @Override
    public int size()
    {
        return data.size();
    }

    @Override
    public boolean isEmpty()
    {
        return data.isEmpty();
    }

    @Override
    public boolean containsKey(Object key)
    {
        return data.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value)
    {
        return data.containsValue(value);
    }

    @Override
    public Object get(Object key)
    {
        return data.get(key);
    }

    @Override
    public Object remove(Object key)
    {
        return data.remove(key);
    }

    @Override
    public void putAll(Map<? extends String, ?> m)
    {
        data.putAll(m);
    }

    @Override
    public void clear()
    {
        data.clear();
    }

    @Override
    public Set<String> keySet()
    {
        return data.keySet();
    }

    @Override
    public Collection<Object> values()
    {
        return data.values();
    }

    @Override
    public Set<Entry<String, Object>> entrySet()
    {
        return data.entrySet();
    }

    //END MAP

    private <T> T get(Class<T> type, String key)
    {
        return get(type, key, null, null);
    }

    private <T> T get(Class<T> type, String key, Function<String, T> stringParse, Function<Number, T> numberTransform)
    {
        checkKey(key);
        Object val = data.get(key);
        if(val == null)
            return null;
        if (!type.isAssignableFrom(val.getClass()))
        {
            if (stringParse != null && val instanceof String)
            {
                try
                {
                    return stringParse.apply((String) val);
                }
                catch (Throwable parseEx)
                {
                    throw new DataReadException(String.format("Key %s in DataObject could not be parsed to %s",
                        key, type.getName()));
                }
            }
            else if (numberTransform != null && Number.class.isAssignableFrom(type) && val instanceof Number)
            {
                try
                {
                    return numberTransform.apply((Number) val);
                }
                catch (Throwable parseEx)
                {
                    throw new DataReadException(String.format("Key %s in DataObject could not be parsed to %s",
                        key, type.getName()));
                }
            }
            throw new DataReadException(String.format("Key %s in DataObject of type %s does not match type %s!",
                key, val.getClass().getName(), type.getName()));
        }
        return type.cast(val);
    }

    private void checkKey(String key)
    {
        if (!data.containsKey(key))
        {
            throw new DataReadException("DataObject doesn't contain key " + key + "!");
        }
    }
}
