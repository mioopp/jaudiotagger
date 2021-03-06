package org.jaudiotagger.issues;

import org.jaudiotagger.AbstractTestCase;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.TagField;
import org.jaudiotagger.tag.id3.AbstractID3v2Frame;
import org.jaudiotagger.tag.id3.framebody.FrameBodyTIPL;

import java.io.File;
import java.util.List;

/**
 * Test reading of TIPL frame where the 2nd field of last pairing is not null terminated
 */
public class Issue390Test extends AbstractTestCase
{
    public void testIssue() throws Exception
    {
        Exception caught = null;
        try
        {
            File orig = new File("testdata", "test101.mp3");
            if (!orig.isFile())
            {
                System.err.println("Unable to test file - not available");
                return;
            }

            File testFile = AbstractTestCase.copyAudioToTmp("test101.mp3");
            AudioFile af = AudioFileIO.read(testFile);
            MP3File mp3 = (MP3File)af;
            assertNotNull(mp3.getID3v2Tag());
            assertNotNull(mp3.getID3v2Tag().getFrame("TIPL"));

            List<TagField> frames = mp3.getID3v2Tag().getFrame("TIPL");
            FrameBodyTIPL body = ((FrameBodyTIPL)((AbstractID3v2Frame)(frames.get(0))).getBody());
            assertEquals(4,body.getNumberOfPairs());
            assertEquals(body.getKeyAtIndex(3),"producer");
            assertEquals(body.getValueAtIndex(3),"producer");

            frames = mp3.getID3v2Tag().getFrame("TIPL");
            body = ((FrameBodyTIPL)((AbstractID3v2Frame)(frames.get(0))).getBody());
            assertEquals(4,body.getNumberOfPairs());
            assertEquals(body.getKeyAtIndex(3),"producer");
            assertEquals(body.getValueAtIndex(3),"producer");

        }
        catch(Exception e)
        {
            caught=e;
            e.printStackTrace();
        }
        assertNull(caught);
    }
}