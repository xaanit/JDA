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

package com.sedmelluq.jda.etf.utils;

import java.io.IOException;

/**
 * The ETF reader/writer and all their components were created by <a href="https://github.com/sedmelluq">sedmelluq</a> 
 * and only slightly modified by the JDA devs. All credit goes to the original author.
 * 
 * @author sedmelluq
 */
public class ByteArrayDataInput implements BoundedDataInput
{
    private final byte[] data;
    private final int length;
    private int position;

    public ByteArrayDataInput(final byte[] data, final int length)
    {
        this.data = data;
        this.length = length;
    }

    @Override
    public boolean readBoolean() throws IOException
    {
        return this.data[this.position++] != 0;
    }

    @Override
    public byte readByte() throws IOException
    {
        return this.data[this.position++];
    }

    @Override
    public char readChar() throws IOException
    {
        return (char) this.readUnsignedShort();
    }

    @Override
    public double readDouble() throws IOException
    {
        return Double.longBitsToDouble(this.readLong());
    }

    @Override
    public float readFloat() throws IOException
    {
        return Float.intBitsToFloat(this.readInt());
    }

    @Override
    public void readFully(final byte[] buffer) throws IOException
    {
        System.arraycopy(this.data, this.position, buffer, 0, buffer.length);
        this.position += buffer.length;
    }

    @Override
    public void readFully(final byte[] buffer, final int offset, final int length) throws IOException
    {
        System.arraycopy(this.data, this.position, buffer, offset, length);
        this.position += length;
    }

    @Override
    public int readInt() throws IOException
    {
        return this.data[this.position++] << 24 | this.data[this.position++] << 16 | this.data[this.position++] << 8 | this.data[this.position++] << 0;
    }

    @Override
    public String readLine() throws IOException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public long readLong() throws IOException
    {
        return (long) this.data[this.position++] << 56 | (long) this.data[this.position++] << 48 | (long) this.data[this.position++] << 40 | (long) this.data[this.position++] << 32 | (long) this.data[this.position++] << 24 | (long) this.data[this.position++] << 16 | (long) this.data[this.position++] << 8 | (long) this.data[this.position++] << 0;
    }

    @Override
    public short readShort() throws IOException
    {
        return (short) this.readUnsignedShort();
    }

    @Override
    public int readUnsignedByte() throws IOException
    {
        return this.data[this.position++] & 0xFF;
    }

    @Override
    public int readUnsignedShort() throws IOException
    {
        return this.data[this.position++] << 8 | this.data[this.position++] << 0;
    }

    @Override
    public String readUTF() throws IOException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public int remaining()
    {
        return this.length - this.position;
    }

    @Override
    public int skipBytes(final int count) throws IOException
    {
        this.position += count;
        return count;
    }
}
