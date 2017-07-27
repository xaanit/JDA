package net.dv8tion.jda.core.utils.data;

import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.math.BigInteger;

import static org.junit.Assert.*;

public class ObjectParseTest
{
    private static DataObject obj;

    @BeforeClass
    public static void init()
    {
        try
        {
            obj = DataObject.fromJson(
                "{\"string\":\"String\"," +
                    "\"bool\":true," +
                    "\"double\":0.5," +
                    "\"int\":1," +
                    "\"long\":2," +
                    "\"boolS\":\"false\"," +
                    "\"longS\":\"2\"," +
                    "\"intS\":\"1\"," +
                    "\"doubleS\":\"0.5\"," +
                    "\"array\":[\"f\"]," +
                    "\"bigIntS\":\"50\"," +
                    "\"bigInt\":50," +
                    "\"map\":{\"test\":\"yey\"}," +
                    "\"null\":null}"
            );
        }
        catch (IOException ex)
        {
            fail("Should parse correctly");
        }
    }

    @Test
    public void readInt()
    {
        assertEquals("Should be able to read integer", 1, obj.getInt("int"));
    }

    @Test
    public void readIntS()
    {
        assertEquals("Should be able to read integer from string", 1, obj.getInt("intS"));
    }

    @Test
    public void readLong()
    {
        assertEquals("Should be able to read long", 2L, obj.getLong("long"));
    }

    @Test
    public void readLongS()
    {
        assertEquals("Should be able to read long from string", 2L, obj.getLong("longS"));
    }

    @Test
    public void readDouble()
    {
        assertEquals("Should be able to read double", 0.5D, obj.getDouble("double"), 1e-20);
    }

    @Test
    public void readDoubleS()
    {
        assertEquals("Should be able to read double from string", 0.5D, obj.getDouble("doubleS"), 1e-20);
    }

    @Test
    public void readString()
    {
        assertEquals("Should be able to read string", "String", obj.getString("string"));
    }

    @Test
    public void readBool()
    {
        assertEquals("Should be able to read boolean", true, obj.getBoolean("bool"));
    }

    @Test
    public void readBoolS()
    {
        assertEquals("Should be able to read boolean from string", false, obj.getBoolean("boolS"));
    }

    @Test
    public void readBigInt()
    {
        assertEquals("Should be able to read bigInt", new BigInteger("50"), obj.getBigInt("bigInt"));
    }

    @Test
    public void readBigIntS()
    {
        assertEquals("Should be able to read bigInt from string", new BigInteger("50"), obj.getBigInt("bigIntS"));
    }

    @Test
    public void readObject()
    {
        DataObject map = obj.getObject("map");
        assertNotNull("Read map should not be null", map);
        assertEquals("Read map should have size of 1", 1, map.size());
        assertEquals("Read map should have element 'test'", "yey", map.getString("test"));
    }

    @Test
    public void readArray()
    {
        DataArray arr = obj.getArray("array");
        assertNotNull("Read array should not be null", arr);
        assertEquals("Read array should have size of 1", 1, arr.size());
        assertEquals("Read array should have element", "f", arr.getString(0));
    }

    @Test
    public void readNull()
    {
        assertNull("null should be returned as null", obj.getString("null"));
    }

    @Test(expected = DataReadException.class)
    public void readMissingProperty()
    {
        obj.getString("lol");
    }

    @Test(expected = DataReadException.class)
    public void readWrongType()
    {
        obj.getString("int");
    }
}
