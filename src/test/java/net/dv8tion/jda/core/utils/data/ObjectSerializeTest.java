package net.dv8tion.jda.core.utils.data;

import org.junit.Assert;
import org.junit.Test;

import java.math.BigInteger;

public class ObjectSerializeTest
{
    @Test
    public void testSerialization()
    {
        DataObject obj = new DataObject()
            .put("int", 1)
            .put("long", 2L)
            .put("double", 0.5D)
            .put("string", "String")
            .put("bool", true)
            .put("bigInt", new BigInteger("50"))
            .put("intS", "1")
            .put("longS", "2")
            .put("doubleS", "0.5")
            .put("boolS", "false")
            .put("bigIntS", "50")
            .put("map", new DataObject().put("test", "yey"))
            .put("array", new DataArray().put("f"))
            .put("null", null);
        String expected = "{\"string\":\"String\"," +
            "\"bool\":true," +
            "\"double\":0.5," +
            "\"int\":1," +
            "\"long\":2," +
            "\"boolS\":\"false\"," +
            "\"longS\":\"2\"," +
            "\"null\":null," +
            "\"intS\":\"1\"," +
            "\"doubleS\":\"0.5\"," +
            "\"array\":[\"f\"]," +
            "\"bigIntS\":\"50\"," +
            "\"bigInt\":50," +
            "\"map\":{\"test\":\"yey\"}}";
        Assert.assertEquals(expected, obj.toString());
    }
}
