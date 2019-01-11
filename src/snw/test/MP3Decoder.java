package snw.test;

import snw.engine.audio.Decoder;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import java.io.ByteArrayInputStream;

public class MP3Decoder implements Decoder{
    @Override
    public AudioInputStream decode(String fileName) {
        PureMP3Decoder.Mp3Info info = new PureMP3Decoder.Mp3Info();
        PureMP3Decoder.decodeMp3(fileName, info);

        AudioInputStream wav = new AudioInputStream(
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
                info.sampleNum / info.channelNum
        );

        return wav;
    }
}
