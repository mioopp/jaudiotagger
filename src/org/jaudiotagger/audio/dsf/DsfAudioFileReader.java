/*
 * Created on 03.05.2015
 * Author: Veselin Markov.
 */
package org.jaudiotagger.audio.dsf;

import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.generic.AudioFileReader;
import org.jaudiotagger.audio.generic.GenericAudioHeader;
import org.jaudiotagger.audio.generic.Utils;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.id3.AbstractID3v2Tag;
import org.jaudiotagger.tag.id3.ID3v22Tag;
import org.jaudiotagger.tag.id3.ID3v23Tag;
import org.jaudiotagger.tag.id3.ID3v24Tag;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.logging.Level;

/**
 * Reads the ID3 Tags as specified by <a href=
 * "http://dsd-guide.com/sites/default/files/white-papers/DSFFileFormatSpec_E.pdf"
 * /> DSFFileFormatSpec_E.pdf </a>.
 *
 * @author Veselin Markov (veselin_m84 a_t yahoo.com)
 */
public class DsfAudioFileReader extends AudioFileReader
{
    private static final String DSD_SIGNATURE = "DSD ";
    private static final String FMT_SIGNATURE = "fmt ";

    @Override
    protected GenericAudioHeader getEncodingInfo(RandomAccessFile raf) throws CannotReadException, IOException
    {
        String type = Utils.readFourBytesAsChars(raf);
        if (DSD_SIGNATURE.equals(type))
        {
            raf.readLong();
            raf.readLong();
            raf.readLong();
            String fmt = Utils.readFourBytesAsChars(raf);
            if (FMT_SIGNATURE.equals(fmt))
            {
                long size = Long.reverseBytes(raf.readLong());
                size -= (FMT_SIGNATURE.length() + 8);
                byte[] audiodataBytes = new byte[(int) size];
                raf.read(audiodataBytes);
                return readAudioInfo(ByteBuffer.wrap(audiodataBytes));
            }
            else
            {
                logger.log(Level.WARNING, "Not a valid dsf file. Content does not start with 'fmt '.");
            }
        }
        else
        {
            logger.log(Level.WARNING, "Not a valid dsf file. Content does not start with 'DSD '.");
        }
        return new GenericAudioHeader();
    }

    /**
     * @param audioInfoChunk contains the bytes from "format version" up to "reserved"
     *                       fields
     * @return an empty {@link GenericAudioHeader} if audioInfoChunk has less
     * than 40 bytes, the read data otherwise. Never <code>null</code>.
     */
    @SuppressWarnings("unused")
    private GenericAudioHeader readAudioInfo(ByteBuffer audioInfoChunk)
    {
        GenericAudioHeader audioHeader = new GenericAudioHeader();
        if (audioInfoChunk.limit() < 40)
        {
            logger.log(Level.WARNING, "Not enough bytes supplied for Generic audio header. Returning an empty one.");
            return audioHeader;
        }

        int version = Integer.reverseBytes(audioInfoChunk.getInt());
        int formatId = Integer.reverseBytes(audioInfoChunk.getInt());
        int channelType = Integer.reverseBytes(audioInfoChunk.getInt());
        int channelNumber = Integer.reverseBytes(audioInfoChunk.getInt());
        int samplingFreqency = Integer.reverseBytes(audioInfoChunk.getInt());
        int bitsPerSample = Integer.reverseBytes(audioInfoChunk.getInt());
        long sampleCount = Long.reverseBytes(audioInfoChunk.getLong());
        int blocksPerSample = Integer.reverseBytes(audioInfoChunk.getInt());

        audioHeader.setBitrate(bitsPerSample * samplingFreqency * channelNumber);
        audioHeader.setBitsPerSample(bitsPerSample);
        audioHeader.setChannelNumber(channelNumber);
        audioHeader.setSamplingRate(samplingFreqency);
        audioHeader.setLength((int) (sampleCount / samplingFreqency));
        audioHeader.setChannelNumber(channelNumber);
        audioHeader.setPreciseLength((float) sampleCount / samplingFreqency);
        audioHeader.setVariableBitRate(false);

        logger.log(Level.FINE, "Created audio header: " + audioHeader);

        return audioHeader;
    }

    @Override
    protected Tag getTag(RandomAccessFile raf) throws CannotReadException, IOException
    {
        String type = Utils.readFourBytesAsChars(raf);
        if (DSD_SIGNATURE.equals(type))
        {
            raf.readLong();
            raf.readLong();
            long offset = Long.reverseBytes(raf.readLong());
            return readTag(raf, offset);
        }
        else
        {
            logger.log(Level.WARNING, "Not a valid dsf file. Content does not start with 'DSD '.");
        }

        return null;
    }

    /**
     * Reads the ID3v2 tag starting at the {@code tagOffset} position in the
     * supplied file.
     *
     * @param file      the file from which to read
     * @param tagOffset the offset where it ID3v2 tag begins
     * @return the read tag or an empty tag if something went wrong. Never
     * <code>null</code>.
     * @throws IOException if cannot read file.
     */
    private Tag readTag(RandomAccessFile file, long tagOffset) throws IOException
    {
        file.seek(tagOffset);
        file.skipBytes(3);
        int majorVersion = file.readByte();
        file.skipBytes(2);

        ByteBuffer tagBuffer = getTagBuffer(file, tagOffset);

        try
        {
            logger.log(Level.FINE, "Start creating ID3v2 Tag for version: " + majorVersion);

            switch (majorVersion)
            {
                case ID3v22Tag.MAJOR_VERSION:
                    return new ID3v22Tag(tagBuffer, "");
                case ID3v23Tag.MAJOR_VERSION:
                    return new ID3v23Tag(tagBuffer, "");
                case ID3v24Tag.MAJOR_VERSION:
                    return new ID3v24Tag(tagBuffer, "");
                default:
                    logger.log(Level.WARNING, "Unknown major ID3v2 version " + majorVersion + ". Returning an empty ID3v2 Tag.");
                    break;
            }
        }
        catch (TagException e)
        {
            logger.log(Level.WARNING, "Could not create ID3v2 Tag. Returning an empty one.", e);
        }
        return new ID3v24Tag();
    }

    private ByteBuffer getTagBuffer(RandomAccessFile file, long tagOffset) throws IOException
    {
        byte[] sizeBytes = new byte[4];
        file.read(sizeBytes);
        int size = ((sizeBytes[0] & 0xff) << 21) + ((sizeBytes[1] & 0xff) << 14) + ((sizeBytes[2] & 0xff) << 7) + ((sizeBytes[3]) & 0xff);
        size += AbstractID3v2Tag.TAG_HEADER_LENGTH;

        file.seek(tagOffset);
        byte[] tagBytes = new byte[size];
        file.read(tagBytes);
        ByteBuffer tagBuffer = ByteBuffer.wrap(tagBytes);
        return tagBuffer;
    }
}
