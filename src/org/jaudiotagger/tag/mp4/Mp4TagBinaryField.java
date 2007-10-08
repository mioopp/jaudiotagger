/*
 * Entagged Audio Tag library
 * Copyright (c) 2003-2005 Rapha�l Slinckx <raphael@slinckx.net>
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *  
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package org.jaudiotagger.tag.mp4;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

import org.jaudiotagger.tag.TagField;
import org.jaudiotagger.audio.generic.Utils;
import org.jaudiotagger.audio.mp4.util.Mp4BoxHeader;

/**
 * Represents binary data
 *
 * This is denoted with an identifier of '----' ?
 */
public class Mp4TagBinaryField extends Mp4TagField
{
    protected int    dataSize;
    protected byte[] dataBytes;
    protected boolean isBinary = false;

    public Mp4TagBinaryField(String id)
    {
        super(id);
    }

    public Mp4TagBinaryField(String id, ByteBuffer raw) throws UnsupportedEncodingException
    {
        super(id, raw);
    }

    public byte[] getRawContent()
    {
        byte[] data = dataBytes;
        byte[] b = new byte[4 + 4 + 4 + 4 + 4 + 4 + data.length];

        int offset = 0;
        Utils.copy(Utils.getSizeBigEndian(b.length), b, offset);
        offset += 4;

        Utils.copy(Utils.getDefaultBytes(getId()), b, offset);
        offset += 4;

        Utils.copy(Utils.getSizeBigEndian(4 + 4 + 4 + 4 + data.length), b, offset);
        offset += 4;

        Utils.copy(Utils.getDefaultBytes("data"), b, offset);
        offset += 4;

        Utils.copy(new byte[]{0, 0, 0, (byte) (isBinary() ? 0 : 1)}, b, offset);
        offset += 4;

        Utils.copy(new byte[]{0, 0, 0, 0}, b, offset);
        offset += 4;

        Utils.copy(data, b, offset);
        offset += data.length;

        return b;
    }

    protected void build(ByteBuffer raw)
    {
        Mp4BoxHeader header  = new Mp4BoxHeader(raw);
        dataSize = header.getDataLength();

        //Skip the version and length fields
        raw.position(raw.position() + Mp4DataBox.PRE_DATA_LENGTH);

        //Read the raw data into byte array
        this.dataBytes = new byte[dataSize - Mp4DataBox.PRE_DATA_LENGTH];
        for (int i = 0; i < dataBytes.length; i++)
        {
            this.dataBytes[i] = raw.get();
        }

        //After returning buffers position will be after the end of this atom
    }

    public boolean isBinary()
    {
        return isBinary;
    }

    public boolean isEmpty()
    {
        return this.dataBytes.length == 0;
    }

    public byte[] getData()
    {
        return this.dataBytes;
    }

    public void setData(byte[] d)
    {
        this.dataBytes = d;
    }

    public void copyContent(TagField field)
    {
        if (field instanceof Mp4TagBinaryField)
        {
            this.dataBytes = ((Mp4TagBinaryField) field).getData();
            this.isBinary = ((Mp4TagBinaryField) field).isBinary();
        }
    }
}