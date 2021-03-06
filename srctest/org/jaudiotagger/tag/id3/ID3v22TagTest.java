package org.jaudiotagger.tag.id3;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.jaudiotagger.AbstractTestCase;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.audio.mp3.MP3AudioHeader;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.TagField;
import org.jaudiotagger.tag.TagOptionSingleton;
import org.jaudiotagger.tag.id3.framebody.*;
import org.jaudiotagger.tag.images.Artwork;
import org.jaudiotagger.tag.images.StandardArtwork;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 *
 */
public class ID3v22TagTest extends TestCase
{
    /**
     * Constructor
     *
     * @param arg0
     */
    public ID3v22TagTest(String arg0)
    {
        super(arg0);
    }

    /**
     * Command line entrance.
     *
     * @param args
     */
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(ID3v22TagTest.suite());
    }

    /////////////////////////////////////////////////////////////////////////
    // TestCase classes to override
    /////////////////////////////////////////////////////////////////////////

    /**
        *
        */
       protected void setUp()
       {
           TagOptionSingleton.getInstance().setToDefault();
       }

       /**
        *
        */
       protected void tearDown()
       {
       }


    /**
     *
     */
//    protected void runTest()
//    {
//    }

    /**
     * Builds the Test Suite.
     *
     * @return the Test Suite.
     */
    public static Test suite()
    {
        return new TestSuite(ID3v22TagTest.class);
    }

    /////////////////////////////////////////////////////////////////////////
    // Tests
    /////////////////////////////////////////////////////////////////////////


    public void testCreateIDv22Tag()
    {
        ID3v22Tag v2Tag = new ID3v22Tag();
        assertEquals((byte) 2, v2Tag.getRelease());
        assertEquals((byte) 2, v2Tag.getMajorVersion());
        assertEquals((byte) 0, v2Tag.getRevision());
    }

    public void testCreateID3v22FromID3v11()
    {
        ID3v11Tag v11Tag = ID3v11TagTest.getInitialisedTag();
        ID3v22Tag v2Tag = new ID3v22Tag(v11Tag);
        assertNotNull(v11Tag);
        assertNotNull(v2Tag);
        assertEquals(ID3v11TagTest.ARTIST, ((FrameBodyTPE1) ((ID3v22Frame) v2Tag.getFrame(ID3v22Frames.FRAME_ID_V2_ARTIST).get(0)).getBody()).getText());
        assertEquals(ID3v11TagTest.ALBUM, ((FrameBodyTALB) ((ID3v22Frame) v2Tag.getFrame(ID3v22Frames.FRAME_ID_V2_ALBUM).get(0)).getBody()).getText());
        assertEquals(ID3v11TagTest.COMMENT, ((FrameBodyCOMM) ((ID3v22Frame) v2Tag.getFrame(ID3v22Frames.FRAME_ID_V2_COMMENT).get(0)).getBody()).getText());
        assertEquals(ID3v11TagTest.TITLE, ((FrameBodyTIT2) ((ID3v22Frame) v2Tag.getFrame(ID3v22Frames.FRAME_ID_V2_TITLE).get(0)).getBody()).getText());
        assertEquals(ID3v11TagTest.TRACK_VALUE, String.valueOf(((FrameBodyTRCK) ((ID3v22Frame) v2Tag.getFrame(ID3v22Frames.FRAME_ID_V2_TRACK).get(0)).getBody()).getTrackNo()));
        assertTrue(((FrameBodyTCON) ((ID3v22Frame) v2Tag.getFrame(ID3v22Frames.FRAME_ID_V2_GENRE).get(0)).getBody()).getText().endsWith(ID3v11TagTest.GENRE_VAL));

        //TODO:Note confusingly V22 YEAR Frame shave v2 identifier but use TDRC behind the scenes, is confusing
        assertEquals(ID3v11TagTest.YEAR, ((FrameBodyTDRC) ((ID3v22Frame) v2Tag.getFrame(ID3v22Frames.FRAME_ID_V2_TYER).get(0)).getBody()).getText());

        assertEquals((byte) 2, v2Tag.getRelease());
        assertEquals((byte) 2, v2Tag.getMajorVersion());
        assertEquals((byte) 0, v2Tag.getRevision());
    }

    public void testCreateIDv22TagAndSave()
    {
        Exception exception = null;
        try
        {
            File testFile = AbstractTestCase.copyAudioToTmp("testV1.mp3");
            MP3File mp3File = new MP3File(testFile);
            ID3v22Tag v2Tag = new ID3v22Tag();
            v2Tag.setField(FieldKey.TITLE,"fred");
            v2Tag.setField(FieldKey.ARTIST,"artist");
            v2Tag.setField(FieldKey.ALBUM,"album");

            assertEquals((byte) 2, v2Tag.getRelease());
            assertEquals((byte) 2, v2Tag.getMajorVersion());
            assertEquals((byte) 0, v2Tag.getRevision());
            mp3File.setID3v2Tag(v2Tag);
            mp3File.save();

            //Read using new Interface
            AudioFile v22File = AudioFileIO.read(testFile);
            assertEquals("fred", v22File.getTag().getFirst(FieldKey.TITLE));
            assertEquals("artist", v22File.getTag().getFirst(FieldKey.ARTIST));
            assertEquals("album", v22File.getTag().getFirst(FieldKey.ALBUM));

            //Read using old Interface
            mp3File = new MP3File(testFile);
            v2Tag = (ID3v22Tag) mp3File.getID3v2Tag();
            ID3v22Frame frame = (ID3v22Frame) v2Tag.getFrame(ID3v22Frames.FRAME_ID_V2_TITLE).get(0);
            assertEquals("fred", ((AbstractFrameBodyTextInfo) frame.getBody()).getText());

        }
        catch (Exception e)
        {
            exception = e;
        }
        assertNull(exception);
    }

    public void testCreateExtraLargeIDv22TagAndSave() throws Exception {
        TagOptionSingleton.getInstance().setPreserveFileIdentity(false);
        checkPadding();
    }

    public void testCreateExtraLargeIDv22TagAndSavePreserveIdentity() throws Exception {
        TagOptionSingleton.getInstance().setPreserveFileIdentity(true);
        checkPadding();
    }

    private void checkPadding() throws IOException, TagException, ReadOnlyFileException, CannotReadException, InvalidAudioFrameException {
        final File modifiedFile = AbstractTestCase.copyAudioToTmp("testV1.mp3");
        // keep a copy with different name
        final File origFile = new File(modifiedFile.getParentFile(), "testV1_copy.mp3");
        AbstractTestCase.copy(modifiedFile, origFile);

        final MP3File mp3File = new MP3File(modifiedFile);
        final ID3v22Tag v2Tag = new ID3v22Tag();

        // add extra large artwork to force shift of audio part
        final StandardArtwork artwork = new StandardArtwork();
        artwork.setMimeType("image/jpeg");
        artwork.setPictureType(0);
        final byte[] binaryData = new byte[1024 * 1024];
        artwork.setBinaryData(binaryData);
        v2Tag.setField(artwork);
        v2Tag.setField(FieldKey.TITLE,"fred");
        mp3File.setID3v2Tag(v2Tag);
        mp3File.save();

        final AudioFile v22File = AudioFileIO.read(modifiedFile);
        final Artwork firstArtwork = v22File.getTag().getFirstArtwork();
        assertTrue(Arrays.equals(firstArtwork.getBinaryData(), binaryData));
        assertEquals("fred", v22File.getTag().getFirst(FieldKey.TITLE));

        // make sure the audio portion of the file is still identical
        final AudioFile mp3OrigFile = AudioFileIO.read(origFile);
        final MP3AudioHeader origAudioHeader = (MP3AudioHeader)mp3OrigFile.getAudioHeader();
        final MP3AudioHeader modifiedAudioHeader = (MP3AudioHeader)v22File.getAudioHeader();

        // confirm shift
        assertTrue(origAudioHeader.getMp3StartByte() < modifiedAudioHeader.getMp3StartByte());

        // compare audio parts
        final byte[] origAudio = new byte[(int) (origFile.length() - origAudioHeader.getMp3StartByte())];
        final byte[] modifiedAudio = new byte[(int) (modifiedFile.length() - modifiedAudioHeader.getMp3StartByte())];
        assertTrue(Arrays.equals(origAudio, modifiedAudio));
    }

    public void testv22TagWithUnneccessaryTrailingNulls()
    {
        File orig = new File("testdata", "test24.mp3");
        if (!orig.isFile())
        {
            return;
        }

        Exception exception = null;
        try
        {
            File testFile = AbstractTestCase.copyAudioToTmp("test24.mp3");
            AudioFile af = AudioFileIO.read(testFile);
            MP3File m = (MP3File) af;

            //Read using new Interface getFirst method with key
            assertEquals("*Listen to images:*", "*"+af.getTag().getFirst(FieldKey.TITLE) + ":*");
            assertEquals("Clean:", af.getTag().getFirst(FieldKey.ALBUM) + ":");
            assertEquals("Cosmo Vitelli:", af.getTag().getFirst(FieldKey.ARTIST) + ":");
            assertEquals("Electronica/Dance:", af.getTag().getFirst(FieldKey.GENRE) + ":");
            assertEquals("2003:", af.getTag().getFirst(FieldKey.YEAR) + ":");


            //Read using new Interface getFirst method with String
            assertEquals("Listen to images:", af.getTag().getFirst(ID3v22Frames.FRAME_ID_V2_TITLE) + ":");
            assertEquals("Clean:", af.getTag().getFirst(ID3v22Frames.FRAME_ID_V2_ALBUM) + ":");
            assertEquals("Cosmo Vitelli:", af.getTag().getFirst(ID3v22Frames.FRAME_ID_V2_ARTIST) + ":");
            assertEquals("Electronica/Dance:", af.getTag().getFirst(ID3v22Frames.FRAME_ID_V2_GENRE) + ":");
            assertEquals("2003:", af.getTag().getFirst(ID3v22Frames.FRAME_ID_V2_TYER) + ":");
            assertEquals("1:", af.getTag().getFirst(ID3v22Frames.FRAME_ID_V2_TRACK) + ":");

            //Read using new Interface getFirst methods for common fields
            assertEquals("Listen to images:", af.getTag().getFirst(FieldKey.TITLE) + ":");
            assertEquals("Cosmo Vitelli:", af.getTag().getFirst(FieldKey.ARTIST) + ":");
            assertEquals("Clean:", af.getTag().getFirst(FieldKey.ALBUM) + ":");
            assertEquals("Electronica/Dance:", af.getTag().getFirst(FieldKey.GENRE) + ":");
            assertEquals("2003:", af.getTag().getFirst(FieldKey.YEAR) + ":");

            //Read using old Interface
            ID3v22Tag v2Tag = (ID3v22Tag) m.getID3v2Tag();
            ID3v22Frame frame = (ID3v22Frame) v2Tag.getFrame(ID3v22Frames.FRAME_ID_V2_TITLE);
            assertEquals("Listen to images:", ((AbstractFrameBodyTextInfo) frame.getBody()).getText() + ":");
            frame = (ID3v22Frame) v2Tag.getFrame(ID3v22Frames.FRAME_ID_V2_ARTIST);
            assertEquals("Cosmo Vitelli:", ((AbstractFrameBodyTextInfo) frame.getBody()).getText() + ":");
            frame = (ID3v22Frame) v2Tag.getFrame(ID3v22Frames.FRAME_ID_V2_ALBUM);
            assertEquals("Clean:", ((AbstractFrameBodyTextInfo) frame.getBody()).getText() + ":");
            frame = (ID3v22Frame) v2Tag.getFrame(ID3v22Frames.FRAME_ID_V2_GENRE);
            assertEquals("Electronica/Dance:", ((AbstractFrameBodyTextInfo) frame.getBody()).getText() + ":");
            frame = (ID3v22Frame) v2Tag.getFrame(ID3v22Frames.FRAME_ID_V2_TYER);
            assertEquals("2003:", ((AbstractFrameBodyTextInfo) frame.getBody()).getText() + ":");
            frame = (ID3v22Frame) v2Tag.getFrame(ID3v22Frames.FRAME_ID_V2_TRACK);
            assertEquals("01/11:", ((FrameBodyTRCK) frame.getBody()).getText() + ":");

        }
        catch (Exception e)
        {
            e.printStackTrace();
            exception = e;
        }
        assertNull(exception);
    }

     public void testDeleteFields() throws Exception
    {
        File testFile = AbstractTestCase.copyAudioToTmp("testV1.mp3");
        MP3File mp3File = new MP3File(testFile);
        ID3v22Tag v2Tag = new ID3v22Tag();
        mp3File.setID3v2Tag(v2Tag);
        mp3File.save();

        //Delete using generic key
        AudioFile f = AudioFileIO.read(testFile);
        List<TagField> tagFields = f.getTag().getFields(FieldKey.ALBUM_ARTIST_SORT);
        assertEquals(0,tagFields.size());
        f.getTag().addField(FieldKey.ALBUM_ARTIST_SORT,"artist1");
        tagFields = f.getTag().getFields(FieldKey.ALBUM_ARTIST_SORT);
        assertEquals(1,tagFields.size());
        f.getTag().deleteField(FieldKey.ALBUM_ARTIST_SORT);
        f.commit();

        //Delete using flac id
        f = AudioFileIO.read(testFile);
        tagFields = f.getTag().getFields(FieldKey.ALBUM_ARTIST_SORT);
        assertEquals(0,tagFields.size());
        f.getTag().addField(FieldKey.ALBUM_ARTIST_SORT,"artist1");
        tagFields = f.getTag().getFields(FieldKey.ALBUM_ARTIST_SORT);
        assertEquals(1,tagFields.size());
        f.getTag().deleteField("TS2");
        tagFields = f.getTag().getFields(FieldKey.ALBUM_ARTIST_SORT);
        assertEquals(0,tagFields.size());
        f.commit();

        f = AudioFileIO.read(testFile);
        tagFields = f.getTag().getFields(FieldKey.ALBUM_ARTIST_SORT);
        assertEquals(0,tagFields.size());
    }

    public void testWriteMultipleGenresToID3v22TagUsingDefault() throws Exception
    {
        File testFile = AbstractTestCase.copyAudioToTmp("testV1.mp3");
        MP3File file = null;
        file = new MP3File(testFile);
        assertNull(file.getID3v1Tag());
        file.setTag(new ID3v22Tag());
        assertNotNull(file.getTag());
        file.getTag().deleteField(FieldKey.GENRE);
        file.getTag().addField(FieldKey.GENRE,"Genre1");
        file.getTag().addField(FieldKey.GENRE,"Genre2");
        file.commit();
        file = new MP3File(testFile);
        assertEquals("Genre1",file.getTag().getFirst(FieldKey.GENRE));
        assertEquals("Genre1",file.getTag().getValue(FieldKey.GENRE, 0));
        assertEquals("Genre2",file.getTag().getValue(FieldKey.GENRE, 1));

        TagOptionSingleton.getInstance().setWriteMp3GenresAsText(false);
        file.getTag().deleteField(FieldKey.GENRE);
        file.getTag().addField(FieldKey.GENRE,"Death Metal");
        file.getTag().addField(FieldKey.GENRE,"(23)");
        assertEquals("Death Metal",file.getTag().getFirst(FieldKey.GENRE));
        assertEquals("Death Metal",file.getTag().getValue(FieldKey.GENRE, 0));
        assertEquals("Pranks",file.getTag().getValue(FieldKey.GENRE, 1));
        file.commit();
        file = new MP3File(testFile);
        assertEquals("Death Metal",file.getTag().getFirst(FieldKey.GENRE));
        assertEquals("Death Metal",file.getTag().getValue(FieldKey.GENRE, 0));
        assertEquals("Pranks",file.getTag().getValue(FieldKey.GENRE, 1));

        TagOptionSingleton.getInstance().setWriteMp3GenresAsText(true);
        file.getTag().deleteField(FieldKey.GENRE);
        file.getTag().addField(FieldKey.GENRE,"Death Metal");
        file.getTag().addField(FieldKey.GENRE,"23");
        assertEquals("Death Metal",file.getTag().getFirst(FieldKey.GENRE));
        assertEquals("Death Metal",file.getTag().getValue(FieldKey.GENRE, 0));
        assertEquals("Pranks",file.getTag().getValue(FieldKey.GENRE, 1));
        file.commit();
        file = new MP3File(testFile);
        assertEquals("Death Metal",file.getTag().getFirst(FieldKey.GENRE));
        assertEquals("Death Metal",file.getTag().getValue(FieldKey.GENRE, 0));
        assertEquals("Pranks",file.getTag().getValue(FieldKey.GENRE, 1));
    }

    public void testWriteMultipleGenresToID3v22TagUsingCreateField() throws Exception
    {
        File testFile = AbstractTestCase.copyAudioToTmp("testV1.mp3");
        MP3File file = null;
        file = new MP3File(testFile);
        assertNull(file.getID3v1Tag());
        file.setTag(new ID3v22Tag());
        assertNotNull(file.getTag());
        ID3v22Tag v22Tag = (ID3v22Tag)file.getTag();
        TagField genreField = v22Tag.createField(FieldKey.GENRE,"Genre1");
        v22Tag.addField(genreField);
        genreField = v22Tag.createField(FieldKey.GENRE,"Genre2");
        v22Tag.addField(genreField);
        file.commit();
        file = new MP3File(testFile);
        assertEquals("Genre1",file.getTag().getFirst(FieldKey.GENRE));
        assertEquals("Genre1",file.getTag().getValue(FieldKey.GENRE, 0));
        assertEquals("Genre2",file.getTag().getValue(FieldKey.GENRE, 1));

        TagOptionSingleton.getInstance().setWriteMp3GenresAsText(false);
        file.getTag().deleteField(FieldKey.GENRE);
        v22Tag = (ID3v22Tag)file.getTag();
        genreField = v22Tag.createField(FieldKey.GENRE,"Death Metal");
        v22Tag.addField(genreField);
        genreField = v22Tag.createField(FieldKey.GENRE,"(23)");
        v22Tag.addField(genreField);
        assertEquals("Death Metal",file.getTag().getFirst(FieldKey.GENRE));
        assertEquals("Death Metal",file.getTag().getValue(FieldKey.GENRE, 0));
        assertEquals("Pranks",file.getTag().getValue(FieldKey.GENRE, 1));
        file.commit();
        file = new MP3File(testFile);
        assertEquals("Death Metal",file.getTag().getFirst(FieldKey.GENRE));
        assertEquals("Death Metal",file.getTag().getValue(FieldKey.GENRE, 0));
        assertEquals("Pranks",file.getTag().getValue(FieldKey.GENRE, 1));

        TagOptionSingleton.getInstance().setWriteMp3GenresAsText(true);
        file.getTag().deleteField(FieldKey.GENRE);
        v22Tag = (ID3v22Tag)file.getTag();
        genreField = v22Tag.createField(FieldKey.GENRE,"Death Metal");
        v22Tag.addField(genreField);
        genreField = v22Tag.createField(FieldKey.GENRE,"23");
        v22Tag.addField(genreField);
        assertEquals("Death Metal",file.getTag().getFirst(FieldKey.GENRE));
        assertEquals("Death Metal",file.getTag().getValue(FieldKey.GENRE, 0));
        assertEquals("Pranks",file.getTag().getValue(FieldKey.GENRE, 1));
        file.commit();
        file = new MP3File(testFile);
        assertEquals("Death Metal",file.getTag().getFirst(FieldKey.GENRE));
        assertEquals("Death Metal",file.getTag().getValue(FieldKey.GENRE, 0));
        assertEquals("Pranks",file.getTag().getValue(FieldKey.GENRE, 1));
    }

    public void testWriteMultipleGenresToID3v22TagUsingV22CreateField() throws Exception
    {
        File testFile = AbstractTestCase.copyAudioToTmp("testV1.mp3");
        MP3File file = null;
        file = new MP3File(testFile);
        assertNull(file.getID3v1Tag());
        file.setTag(new ID3v22Tag());
        assertNotNull(file.getTag());
        ID3v22Tag v22Tag = (ID3v22Tag)file.getTag();
        TagField genreField = v22Tag.createField(ID3v22FieldKey.GENRE,"Genre1");
        v22Tag.addField(genreField);
        genreField = v22Tag.createField(ID3v22FieldKey.GENRE,"Genre2");
        v22Tag.addField(genreField);
        file.commit();
        file = new MP3File(testFile);
        assertEquals("Genre1",file.getTag().getFirst(FieldKey.GENRE));
        assertEquals("Genre1",file.getTag().getValue(FieldKey.GENRE, 0));
        assertEquals("Genre2",file.getTag().getValue(FieldKey.GENRE, 1));

        TagOptionSingleton.getInstance().setWriteMp3GenresAsText(false);
        file.getTag().deleteField(FieldKey.GENRE);
        v22Tag = (ID3v22Tag)file.getTag();
        genreField = v22Tag.createField(ID3v22FieldKey.GENRE,"Death Metal");
        v22Tag.addField(genreField);
        genreField = v22Tag.createField(ID3v22FieldKey.GENRE,"(23)");
        v22Tag.addField(genreField);
        assertEquals("Death Metal",file.getTag().getFirst(FieldKey.GENRE));
        assertEquals("Death Metal",file.getTag().getValue(FieldKey.GENRE, 0));
        assertEquals("Pranks",file.getTag().getValue(FieldKey.GENRE, 1));
        file.commit();
        file = new MP3File(testFile);
        assertEquals("Death Metal",file.getTag().getFirst(FieldKey.GENRE));
        assertEquals("Death Metal",file.getTag().getValue(FieldKey.GENRE, 0));
        assertEquals("Pranks",file.getTag().getValue(FieldKey.GENRE, 1));

        TagOptionSingleton.getInstance().setWriteMp3GenresAsText(true);
        file.getTag().deleteField(FieldKey.GENRE);
        v22Tag = (ID3v22Tag)file.getTag();
        genreField = v22Tag.createField(ID3v22FieldKey.GENRE,"Death Metal");
        v22Tag.addField(genreField);
        genreField = v22Tag.createField(ID3v22FieldKey.GENRE,"23");
        v22Tag.addField(genreField);
        assertEquals("Death Metal",file.getTag().getFirst(FieldKey.GENRE));
        assertEquals("Death Metal",file.getTag().getValue(FieldKey.GENRE, 0));
        assertEquals("Pranks",file.getTag().getValue(FieldKey.GENRE, 1));
        file.commit();
        file = new MP3File(testFile);
        assertEquals("Death Metal",file.getTag().getFirst(FieldKey.GENRE));
        assertEquals("Death Metal",file.getTag().getValue(FieldKey.GENRE, 0));
        assertEquals("Pranks",file.getTag().getValue(FieldKey.GENRE, 1));
    }

}
