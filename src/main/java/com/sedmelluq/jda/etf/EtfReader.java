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

import com.sedmelluq.jda.etf.utils.BoundedDataInput;
import com.sedmelluq.jda.etf.utils.ByteArrayDataInput;
import java.io.Closeable;
import java.io.DataInput;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;
import net.dv8tion.jda.core.utils.data.DataArray;
import net.dv8tion.jda.core.utils.data.DataObject;

/**
 * The ETF reader/writer and all their components were created by <a href="https://github.com/sedmelluq">sedmelluq</a> 
 * and only slightly modified by the JDA team. All credit goes to the original author.
 * 
 * @author sedmelluq
 */
public class EtfReader implements Closeable
{
    private static final ByteSequence FALSE_ATOM = new ByteSequence("false".getBytes(StandardCharsets.UTF_8));
    private static final ByteSequence NULL_ATOM = new ByteSequence("nil".getBytes(StandardCharsets.UTF_8));
    private static final ByteSequence TRUE_ATOM = new ByteSequence("true".getBytes(StandardCharsets.UTF_8));

    private final Map<ByteSequence, String> mapKeyMap;
    private final byte[] packingByteBuffer;
    private final Inflater permanentInflater;
    private final ByteSequence sequence;
    private final byte[] textBuffer;

    public EtfReader(final boolean useKeyCache)
    {
        this.textBuffer = new byte[512];
        this.packingByteBuffer = new byte[4096];
        this.permanentInflater = new Inflater();
        this.mapKeyMap = useKeyCache ? new HashMap<>() : null;
        this.sequence = new ByteSequence();
    }

    @Override
    public void close() throws IOException
    {
        this.permanentInflater.end();
    }

    public DataObject readMessage(final BoundedDataInput input)
    {
        try
        {
            return this.readTerm(input);
        }
        catch (final Exception e)
        {
            throw new RuntimeException("An unknown error occurred while reading an etf message", e);
        }
    }

    private String handleStringBytes(final boolean useKeyCache)
    {
        if (useKeyCache && this.mapKeyMap != null)
        {
            String element = this.mapKeyMap.get(this.sequence);

            if (element == null)
            {
                final ByteSequence key = new ByteSequence(this.sequence);
                element = key.toString();
                this.mapKeyMap.put(key, element);
            }

            return element;
        }
        else
            return this.sequence.toString();
    }

    private byte[] packingByteBuffer(final int size)
    {
        return this.packingByteBuffer.length >= size ? this.packingByteBuffer : new byte[size];
    }

    private Object readAtomElement(final DataInput input, final boolean useKeyCache) throws IOException
    {
        final int length = input.readUnsignedShort();

        final byte[] bytes = this.textByteBuffer(length);
        input.readFully(bytes, 0, length);

        this.sequence.update(bytes, 0, length);

        if (this.sequence.equals(EtfReader.NULL_ATOM))
            return null;
        else if (this.sequence.equals(EtfReader.TRUE_ATOM))
            return true;
        else if (this.sequence.equals(EtfReader.FALSE_ATOM))
            return false;
        else
            return this.handleStringBytes(useKeyCache);
    }

    private BigInteger readBigInteger(final DataInput input, final int sign, final long length) throws IOException
    {
        throw new UnsupportedOperationException();
    }

    private String readBinaryStringElement(final DataInput input, final boolean useKeyCache) throws IOException
    {
        final int length = input.readInt();

        final byte[] bytes = this.textByteBuffer(length);
        input.readFully(bytes, 0, length);

        this.sequence.update(bytes, 0, length);
        return this.handleStringBytes(useKeyCache);
    }

    private DataArray readByteListElement(final DataInput input) throws IOException
    {
        final int length = input.readUnsignedShort();
        final DataArray list = new DataArray(length);

        for (int i = 0; i < length; i++)
            list.add(input.readUnsignedByte());

        return list;
    }

    private Object readCompressedObject(final BoundedDataInput input) throws IOException
    {
        final int uncompressedSize = input.readInt();
        final int compressedSize = input.remaining();

        final Inflater inflater = this.permanentInflater;

        try
        {
            final byte[] buffer = this.textByteBuffer(compressedSize);
            input.readFully(buffer, 0, compressedSize);
            inflater.setInput(buffer, 0, compressedSize);

            final byte[] uncompressed = this.packingByteBuffer(uncompressedSize);
            final int length = inflater.inflate(uncompressed, 0, uncompressedSize);

            if (length != uncompressedSize || !inflater.finished())
                throw new IllegalStateException("Should have processed everything.");

            return this.readObject(new ByteArrayDataInput(uncompressed, uncompressedSize), false);
        }
        catch (final DataFormatException e)
        {
            throw new IOException(e);
        }
        finally
        {
            inflater.reset();
        }
    }

    private List<Object> readListElement(final BoundedDataInput input) throws IOException
    {
        final int length = input.readInt();
        final List<Object> list = new ArrayList<>(length);

        for (int i = 0; i < length; i++)
            list.add(this.readObject(input, false));

        this.readObject(input, false);
        return list;
    }

    private long readLong(final DataInput input, final int sign, final long length) throws IOException
    {
        long value = 0;

        // sign is lost
        for (int i = 0; i < length; i++)
            value |= (long) input.readUnsignedByte() << (i << 3);

        return sign == 0 ? value : -value;
    }

    private DataObject readMapElement(final BoundedDataInput input) throws IOException
    {
        final int arity = input.readInt();
        final DataObject map = new DataObject(arity);

        for (int i = 0; i < arity; i++)
        {
            final Object key = this.readObject(input, true);
            final Object value = this.readObject(input, false);

            if (!(key instanceof String))
                throw new IllegalStateException("Map key must be a string.");

            map.put((String) key, value);
        }

        return map;
    }

    private Object readObject(final BoundedDataInput input, final boolean useKeyCache) throws IOException
    {
        switch (input.readUnsignedByte())
        {
            case EtfTag.SMALL_INTEGER_EXT:
                return input.readUnsignedByte();
            case EtfTag.COMPRESSED:
                return this.readCompressedObject(input);
            case EtfTag.INTEGER_EXT:
                return input.readInt();
            case EtfTag.NEW_FLOAT_EXT:
                return input.readDouble();
            case EtfTag.ATOM_EXT:
                return this.readAtomElement(input, useKeyCache);
            case EtfTag.NIL_EXT:
                return Collections.emptyList();
            case EtfTag.STRING_EXT:
                return this.readByteListElement(input);
            case EtfTag.LIST_EXT:
                return this.readListElement(input);
            case EtfTag.BINARY_EXT:
                return this.readBinaryStringElement(input, useKeyCache);
            case EtfTag.SMALL_BIG_EXT:
                return this.readSmallBigInteger(input);
            case EtfTag.MAP_EXT:
                return this.readMapElement(input);
            default:
                throw new RuntimeException("Unsupported tag.");
        }
    }

    private Number readSmallBigInteger(final DataInput input) throws IOException
    {
        final int length = input.readUnsignedByte();
        final int sign = input.readByte();

        if (length <= 8)
            return this.readLong(input, sign, length);
        else
            return this.readBigInteger(input, sign, length);
    }

    private DataObject readTerm(final BoundedDataInput input) throws IOException
    {
        final int termHeader = input.readUnsignedByte();

        if (termHeader != 131)
            throw new IllegalStateException("Should start with a term header.");

        final Object element = this.readObject(input, false);

        if (!(element instanceof DataObject))
            throw new IllegalStateException("Root element must be a map.");

        return (DataObject) element;
    }

    private byte[] textByteBuffer(final int size)
    {
        return this.textBuffer.length >= size ? this.textBuffer : new byte[size];
    }

    private static class ByteSequence
    {
        private static int calculateHashCode(final byte[] bytes, final int offset, final int length)
        {
            int hash = 0;
            final int endOffset = offset + length;

            for (int i = offset; i < endOffset; i++)
                hash = 31 * hash * bytes[i];

            if (hash == 0)
                hash = 1;

            return hash;
        }

        private byte[] bytes;
        private int hash;
        private int length;

        private int offset;

        private ByteSequence()
        {

        }

        private ByteSequence(final byte[] bytes)
        {
            this.update(bytes, 0, bytes.length);
        }

        private ByteSequence(final ByteSequence sequence)
        {
            this.update(sequence.bytes, sequence.offset, sequence.length);
        }

        @Override
        public boolean equals(final Object object)
        {
            if (!(object instanceof ByteSequence))
                return false;

            final ByteSequence other = (ByteSequence) object;

            if (other.hash != this.hash || this.length != other.length)
                return false;

            final byte[] otherBytes = other.bytes;
            final int otherOffset = other.offset;

            for (int i = 0; i < this.length; i++)
                if (this.bytes[this.offset + i] != otherBytes[otherOffset + i])
                    return false;

            return true;
        }

        @Override
        public int hashCode()
        {
            return this.hash;
        }

        @Override
        public String toString()
        {
            return new String(this.bytes, this.offset, this.length, StandardCharsets.UTF_8);
        }

        public void update(final byte[] bytes, final int offset, final int length)
        {
            this.bytes = bytes;
            this.offset = offset;
            this.length = length;
            this.hash = ByteSequence.calculateHashCode(bytes, offset, length);
        }
    }
}
