package net.dv8tion.jda.core.utils.data;

import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

public class ArrayReadTest
{
    private static DataArray arr;

    @BeforeClass
    public static void setup()
    {
        arr = new DataArray()
            .put(1)
            .put("1")
            .put(2L)
            .put("2")
            .put(0.5D)
            .put("0.5")
            .put("String")
            .put(true)
            .put("false")
            .put(new DataObject().put("test", "yey"))
            .put(new DataArray().put("f"))
            .put(null);
    }

    @Test
    public void readInt()
    {
        assertEquals("Should be able to read integer", 1, arr.getInt(0));
    }

    @Test
    public void readIntS()
    {
        assertEquals("Should be able to read integer from string", 1, arr.getInt(1));
    }

    @Test
    public void readLong()
    {
        assertEquals("Should be able to read long", 2L, arr.getLong(2));
    }

    @Test
    public void readLongS()
    {
        assertEquals("Should be able to read long from string", 2L, arr.getLong(3));
    }

    @Test
    public void readDouble()
    {
        assertEquals("Should be able to read double", 0.5D, arr.getDouble(4), 1e-20);
    }

    @Test
    public void readDoubleS()
    {
        assertEquals("Should be able to read double from string", 0.5D, arr.getDouble(5), 1e-20);
    }

    @Test
    public void readString()
    {
        assertEquals("Should be able to read string", "String", arr.getString(6));
    }

    @Test
    public void readBool()
    {
        assertEquals("Should be able to read boolean", true, arr.getBoolean(7));
    }

    @Test
    public void readBoolS()
    {
        assertEquals("Should be able to read boolean from string", false, arr.getBoolean(8));
    }

    @Test
    public void readObject()
    {
        DataObject map = arr.getObject(9);
        assertNotNull("Read map should not be null", map);
        assertEquals("Read map should have size of 1", 1, map.size());
        assertEquals("Read map should have element 'test'", "yey", map.getString("test"));
    }

    @Test
    public void readArray()
    {
        DataArray array = arr.getArray(10);
        assertNotNull("Read array should not be null", array);
        assertEquals("Read array should have size of 1", 1, array.size());
        assertEquals("Read array should have element", "f", array.getString(0));
    }

    @Test
    public void readNull()
    {
        assertNull("null should be returned as null", arr.getString(11));
    }

    @Test(expected = DataReadException.class)
    public void readToHighIndex()
    {
        arr.getString(12);
    }

    @Test(expected = DataReadException.class)
    public void readWrongType()
    {
        arr.getString(0);
    }
}
