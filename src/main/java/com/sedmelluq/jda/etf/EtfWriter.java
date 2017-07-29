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

package com.sedmelluq.jda.etf;

import java.io.ByteArrayOutputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;
import net.dv8tion.jda.core.utils.data.DataArray;
import net.dv8tion.jda.core.utils.data.DataObject;

/**
 * The ETF reader/writer and all their components were created by <a href="https://github.com/sedmelluq">sedmelluq</a> 
 * and only slightly modified by the JDA team. All credit goes to the original author.
 * 
 * @author sedmelluq
 */
public class EtfWriter
{
    private static int numberOfBytes(final long value)
    {
        if (value == 0)
            return 0;

        int bytes = 8;
        int x = (int) (value >>> 32);

        if (x == 0)
        {
            bytes -= 4;
            x = (int) value;
        }

        if (x >>> 16 == 0)
        {
            bytes -= 2;
            x <<= 16;
        }

        if (x >>> 24 == 0)
            bytes -= 1;

        return bytes;
    }

    private final byte[] smallTextBuffer;

    public EtfWriter(final boolean threadSafe)
    {
        this.smallTextBuffer = threadSafe ? null : new byte[512];
    }

    public byte[] writeMessage(final DataObject message)
    {
        try
        {
            final ByteArrayOutputStream stream = new ByteArrayOutputStream();
            this.writeMessage(message, new DataOutputStream(stream));
            stream.flush();
            return stream.toByteArray();
        }
        catch (final IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    public void writeMessage(final DataObject message, final DataOutput output)
    {
        try
        {
            output.write(131);
            this.writeObject(message, output);
        }
        catch (final IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    private byte[] byteBuffer(final int size)
    {
        return this.smallTextBuffer != null && this.smallTextBuffer.length >= size ? this.smallTextBuffer : new byte[size];
    }

    private void writeList(final DataArray list, final DataOutput output) throws IOException
    {
        output.write(EtfTag.LIST_EXT);
        output.writeInt(list.size());

        for (final Object element : list)
            this.writeObject(element, output);

        output.write(EtfTag.NIL_EXT);
    }

    private void writeLong(long value, final DataOutput output) throws IOException
    {
        output.write(EtfTag.SMALL_BIG_EXT);
        final int bytes = EtfWriter.numberOfBytes(value);

        output.write(bytes);
        output.write(0);

        for (int i = 0; i < bytes; i++)
        {
            output.write((int) (value & 0xFF));
            value >>>= 8;
        }
    }

    private void writeMap(final DataObject object, final DataOutput output) throws IOException
    {
        output.write(EtfTag.MAP_EXT);
        output.writeInt(object.size());

        for (final Map.Entry<String, Object> entry : object.entrySet())
        {
            this.writeObject(entry.getKey(), output);
            this.writeObject(entry.getValue(), output);
        }
    }

    private void writeObject(final Object object, final DataOutput output) throws IOException
    {
        if (object instanceof String)
            this.writeStringBytes((String) object, output);
        else if (object instanceof DataObject)
            this.writeMap((DataObject) object, output);
        else if (object instanceof Number)
        {
            if (object instanceof Byte)
            {
                output.write(EtfTag.SMALL_INTEGER_EXT);
                output.write((byte) object);
            }
            else if (object instanceof Long)
                this.writeLong((Long) object, output);
            else if (object instanceof Double || object instanceof Float)
            {
                output.write(EtfTag.NEW_FLOAT_EXT);
                output.writeFloat(((Number) object).floatValue());
            }
            else
            {
                output.write(EtfTag.INTEGER_EXT);
                output.writeInt(((Number) object).intValue());
            }
        }
        else if (object instanceof DataArray)
            this.writeList((DataArray) object, output);
        else if (object instanceof Boolean)
            this.writeStringAtom(object == Boolean.TRUE ? "true" : "false", output);
        else if (object == null)
            this.writeStringAtom("nil", output);
        else
            throw new IllegalArgumentException("Invalid object type to write: " + object.getClass().getName());
    }

    private void writeStringAtom(final String string, final DataOutput output) throws IOException
    {
        final int length = string.length();
        output.write(EtfTag.ATOM_EXT);
        output.writeShort(length);

        final byte[] buffer = this.byteBuffer(length);
        char character;

        for (int i = 0; i < length; i++)
        {
            character = string.charAt(i);

            if (character >= 0x0001 && character <= 0x007F)
                buffer[i] = (byte) character;
            else
                throw new IllegalStateException("Cannot have non-ascii characters in string atoms.");
        }

        output.write(buffer, 0, length);
    }

    private void writeStringBytes(final String string, final DataOutput output) throws IOException
    {
        output.write(EtfTag.BINARY_EXT);

        final int length = string.length();
        int binaryLength = 0;
        char character;

        for (int i = 0; i < length; i++)
        {
            character = string.charAt(i);
            if (character >= 0x0001 && character <= 0x007F)
                binaryLength++;
            else if (i > 0x07FF)
                binaryLength += 3;
            else
                binaryLength += 2;
        }

        output.writeInt(binaryLength);

        final byte[] buffer = this.byteBuffer(binaryLength);
        int position = 0;

        for (int i = 0; i < length; i++)
        {
            character = string.charAt(i);

            if (character >= 0x0001 && character <= 0x007F)
                buffer[position++] = (byte) character;
            else if (character > 0x07FF)
            {
                buffer[position++] = (byte) (0xE0 | character >> 12 & 0x0F);
                buffer[position++] = (byte) (0x80 | character >> 6 & 0x3F);
                buffer[position++] = (byte) (0x80 | character & 0x3F);
            }
            else
            {
                buffer[position++] = (byte) (0xC0 | character >> 6 & 0x1F);
                buffer[position++] = (byte) (0x80 | character & 0x3F);
            }
        }

        output.write(buffer, 0, binaryLength);
    }
}
