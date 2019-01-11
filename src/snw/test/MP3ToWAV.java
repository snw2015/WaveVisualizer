package snw.test;

import javax.sound.sampled.*;
import javax.swing.*;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class MP3ToWAV extends JFrame {
    public static class Mp3Info {
        public byte[] bytes;

        public long sampleNum;

        public int sampleRate;
        public int channelNum;
        public int layer;
        public int bitrate;
    }

    static {
        System.loadLibrary("mp3dec");
    }

    public MP3ToWAV() throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        setVisible(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        Mp3Info info = new Mp3Info();
        decode_mp3("file/audio/test.mp3", info);
        System.out.println(info.layer);

        AudioInputStream in = new AudioInputStream(
                new ByteArrayInputStream(info.bytes),
                new AudioFormat(
                        AudioFormat.Encoding.PCM_SIGNED,
                        info.sampleRate,
                        16,
                        info.channelNum,
                        info.channelNum * 2,
                        44100,
                        false
                ),
                info.sampleNum
        );

        Clip clip = AudioSystem.getClip();
        clip.open(in);
   //     clip.start();
    }

    public static native boolean decode_mp3(String fileName, Mp3Info info);

    public static void main(String[] args) throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        new MP3ToWAV();
    }
}
