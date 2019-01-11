package snw.test;

import snw.engine.animation.AnimationData;
import snw.engine.component.demo.NormalPanel;
import snw.engine.core.Engine;

import javax.sound.sampled.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;

public class Mp3BGMTest extends NormalPanel {
    public Mp3BGMTest() throws IOException, LineUnavailableException, InterruptedException {
        super("test");
        Engine.setDecoder("mp3", new MP3Decoder());
        Engine.storeBGM("test", "mp3");
        //Engine.playBGM("test");

        AudioInputStream s = Engine.getStream("test", "mp3");
        AudioInputStream signed = new AudioInputStream(
                s,
                new AudioFormat(
                        AudioFormat.Encoding.PCM_SIGNED,
                        s.getFormat().getSampleRate(),
                        s.getFormat().getSampleSizeInBits(),
                        s.getFormat().getChannels(),
                        s.getFormat().getFrameSize(),
                        s.getFormat().getFrameRate(),
                        s.getFormat().isBigEndian()
                ),
                s.getFrameLength()
        );
        Clip c = AudioSystem.getClip();
        c.open(signed);
        c.start();
        Thread.sleep(10000);
        c.stop();
        c.close();
        s.close();
        signed.close();

        s = Engine.getStream("test", "mp3");
        AudioInputStream unsigned = new AudioInputStream(
                s,
                new AudioFormat(
                        AudioFormat.Encoding.PCM_UNSIGNED,
                        s.getFormat().getSampleRate(),
                        s.getFormat().getSampleSizeInBits(),
                        s.getFormat().getChannels(),
                        s.getFormat().getFrameSize(),
                        s.getFormat().getFrameRate(),
                        s.getFormat().isBigEndian()
                ),
                s.getFrameLength()
        );
        c = AudioSystem.getClip();
        c.open(unsigned);
        c.start();
        Thread.sleep(10000);
        c.stop();
        c.close();
        s.close();
        unsigned.close();
    }

    @Override
    public void paint(Graphics2D g, AnimationData appliedData) {
        super.paint(g, appliedData);

        g.drawString("" + Engine.getClip("test").getFramePosition()
                + "/" + Engine.getClip("test").getFrameLength(), 10, 20);
    }

    public static void main(String[] args) {
        Engine.initialize();

        Engine.setTitle("Mp3 BGM Test");
        Engine.setSize(200, 200);

        Engine.addState("1", Mp3BGMTest.class);

        Engine.loadState("1");

        Engine.start();
        Engine.showState("1");
    }
}
