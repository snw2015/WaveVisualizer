package snw.test;

import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

import static javax.sound.sampled.AudioSystem.getSourceDataLine;

public class RawWaveStreamTest {
    public static final int LEN = 1000000;
    public static final float FREQ = 2000;

    public static void main(String[] args) throws LineUnavailableException, IOException, InterruptedException, UnsupportedAudioFileException {

        byte[] a = new byte[LEN * 2];
        for (int i = 0; i < LEN; i++) {
            short v = (short) (Math.sin(i * FREQ / 44100 * 2 * Math.PI) * 32768);
            a[2 * i] = (byte) (v & 0xFF);
            a[2 * i + 1] = (byte) ((v >> 8) & 0xFF);
        }

        AudioInputStream raw = new AudioInputStream(
                new ByteArrayInputStream(a),
                new AudioFormat(
                        AudioFormat.Encoding.PCM_SIGNED,
                        44100,
                        16,
                        1,
                        2,
                        0.01f
                        , false
                ),
                LEN
        );

        /*
        raw = new AudioInputStream(
                new ByteArrayInputStream(new byte[]{(byte) 0xFF, (byte) 0x7F}),
                new AudioFormat(
                        AudioFormat.Encoding.PCM_SIGNED,
                        44100,
                        16,
                        1,
                        2,
                        40,
                        false
                ),
                1
        );
        */

        /*
        try {
            AudioSystem.write(raw, AudioFileFormat.Type.WAVE, new File("test.wav"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(raw.getFormat());
        */

        AudioInputStream s = AudioSystem.getAudioInputStream(new File("test.wav"));
        System.out.println(s.getFormat().getFrameRate());

        SourceDataLine line = AudioSystem.getSourceDataLine(raw.getFormat());
        line.open(s.getFormat());
        line.start();
        line.write(raw.readAllBytes(), 0, (int) raw.getFrameLength());
        line.drain();
        line.stop();
        line.close();

        while (true) {
            System.out.println(line.getFramePosition());
        }
    }
}
