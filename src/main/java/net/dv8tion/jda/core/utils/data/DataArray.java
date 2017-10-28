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
import com.fasterxml.jackson.databind.type.CollectionType;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DataArray implements List<Object>
{
    private static final ObjectMapper jsonMapper = new ObjectMapper();
    private static final CollectionType jacksonType = jsonMapper.getTypeFactory().constructCollectionType(List.class, Object.class);

    private final List<Object> data;

    public DataArray()
    {
        this.data = new ArrayList<>();
    }

    public DataArray(List<Object> data)
    {
        this.data = data;
    }

    public DataArray(SerializableData... data)
    {
        this.data = Arrays.stream(data)
                        .map(SerializableData::toDataObject)
                        .collect(Collectors.toList());
    }

    public DataArray(int length)
    {
        this.data = new ArrayList<>(length);
    }

    public DataArray put(Object o)
    {
        data.add(o);
        return this;
    }

    public int length()
    {
        return data.size();
    }

    public boolean isNull(int index)
    {
        return get(index) == null;
    }

    public int getInt(int index)
    {
        return get(Integer.class, index, Integer::parseInt, Number::intValue);
    }

    public long getLong(int index)
    {
        return get(Long.class, index, Long::parseLong, Number::longValue);
    }

    public long getUnsignedLong(int index)
    {
        return get(Long.class, index, Long::parseUnsignedLong, Number::longValue);
    }

    public double getDouble(int index)
    {
        return get(Double.class, index, Double::parseDouble, Number::doubleValue);
    }

    public boolean getBoolean(int index)
    {
        return get(Boolean.class, index, Boolean::parseBoolean, null);
    }

    public String getString(int index)
    {
        return get(String.class, index);
    }

    @SuppressWarnings("unchecked")
    public DataObject getObject(int index)
    {
        try
        {
            return new DataObject((Map<String, Object>) get(Map.class, index));
        }
        catch (ClassCastException ex)
        {
            ex.printStackTrace();
        }
        throw new DataReadException(String.format("Map with index %d inside of DataArray couldn't properly be assigned to DataObject", index));
    }

    @SuppressWarnings("unchecked")
    public DataArray getArray(int index)
    {
        try
        {
            return new DataArray((List<Object>) get(List.class, index));
        }
        catch (ClassCastException ex)
        {
            ex.printStackTrace();
        }
        throw new DataReadException(String.format("Array with index %d inside of DataArray couldn't properly be assigned to DataArray", index));
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
        return "[]";
    }

    public static DataArray fromJson(String json) throws IOException
    {
        List<Object> map = jsonMapper.readValue(json, jacksonType);
        return new DataArray(map);
    }

    public static DataArray fromJson(InputStream is) throws IOException
    {
        List<Object> map = jsonMapper.readValue(is, jacksonType);
        return new DataArray(map);
    }

    public static DataArray fromJson(Reader reader) throws IOException
    {
        List<Object> map = jsonMapper.readValue(reader, jacksonType);
        return new DataArray(map);
    }

    //START LIST

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
    public boolean contains(Object o)
    {
        return data.contains(o);
    }

    @Override
    public Iterator<Object> iterator()
    {
        return data.iterator();
    }

    @Override
    public Object[] toArray()
    {
        return data.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a)
    {
        return data.toArray(a);
    }

    @Override
    public boolean add(Object o)
    {
        return data.add(o);
    }

    @Override
    public boolean remove(Object o)
    {
        return data.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c)
    {
        return data.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<?> c)
    {
        return data.addAll(c);
    }

    @Override
    public boolean addAll(int index, Collection<?> c)
    {
        return data.addAll(index, c);
    }

    @Override
    public boolean removeAll(Collection<?> c)
    {
        return data.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c)
    {
        return data.retainAll(c);
    }

    @Override
    public void clear()
    {
        data.clear();
    }

    @Override
    public Object get(int index)
    {
        return data.get(index);
    }

    @Override
    public Object set(int index, Object element)
    {
        return data.set(index, element);
    }

    @Override
    public void add(int index, Object element)
    {
        data.add(index, element);
    }

    @Override
    public Object remove(int index)
    {
        return data.remove(index);
    }

    @Override
    public int indexOf(Object o)
    {
        return data.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o)
    {
        return data.lastIndexOf(o);
    }

    @Override
    public ListIterator<Object> listIterator()
    {
        return data.listIterator();
    }

    @Override
    public ListIterator<Object> listIterator(int index)
    {
        return data.listIterator(index);
    }

    @Override
    public List<Object> subList(int fromIndex, int toIndex)
    {
        return data.subList(fromIndex, toIndex);
    }

    //END LIST

    private <T> T get(Class<T> type, int index)
    {
        return get(type, index, null, null);
    }

    private <T> T get(Class<T> type, int index, Function<String, T> stringParse, Function<Number, T> numberTransform)
    {
        checkRange(index);
        Object val = data.get(index);
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
                    throw new DataReadException(String.format("Index %d in DataArray could not be parsed to %s",
                        index, type.getName()));
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
                    throw new DataReadException(String.format("Index %d in DataArray could not be parsed to %s",
                        index, type.getName()));
                }
            }
            throw new DataReadException(String.format("Index %d in DataArray of type %s does not match type %s!",
                index, val.getClass().getName(), type.getName()));
        }
        return type.cast(val);
    }

    private void checkRange(int index)
    {
        if (index < 0 || index >= length())
        {
            throw new DataReadException(String.format("Index out of range. DataArray length: %d, index: %d", length(), index));
        }
    }
}
