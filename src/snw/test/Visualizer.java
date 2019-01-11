package snw.test;

import snw.engine.animation.AnimationData;
import snw.engine.component.EasyPaintable;
import snw.engine.component.Paintable;
import snw.engine.component.demo.NormalPanel;
import snw.engine.core.Engine;
import snw.math.VectorInt;
import snw.slowfft.Complex;
import snw.slowfft.DFTable;
import snw.slowfft.Transformer;

import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import static javax.sound.sampled.AudioFormat.Encoding.PCM_FLOAT;
import static javax.sound.sampled.AudioFormat.Encoding.PCM_SIGNED;
import static javax.sound.sampled.AudioFormat.Encoding.PCM_UNSIGNED;

public class Visualizer extends NormalPanel {
    public static final Color COLOR_BACKGROUND = Color.BLACK;
    public static final Color COLOR_FOREGROUND = Color.RED;
    public static final Color COLOR_SCROLLBAR = Color.MAGENTA;
    public static final Color COLOR_SCROLLBACK = Color.WHITE;

    public static final int WINDOW_LENGTH = 1024;
    public static final int SLICE_NUM = 64;
    public static final String NAME = "pp";
    public static final String FORMAT = "mp3";
    public static final boolean IS_DESK = false;

    private Clip clip = null;
    private long flms = 0;
    private int fl = 0;
    private long fpms = 0;
    private int fp = 0;
    //NOTE: frame length could be long
    private double[] freq = null;
    private DFTable d = new Transformer();
    float[] wave = null;
    int iconNum;
    VectorInt desktopSize;
    int desktop = 0;

    public Visualizer() throws IOException, UnsupportedAudioFileException {
        super("visualizer");

        if (IS_DESK) {
            desktop = IconDance.getDesktopHWND();
            iconNum = IconDance.getIconNum(desktop);
            int[] sizeA = IconDance.getSize(desktop);
            desktopSize = new VectorInt(sizeA[0], sizeA[1]);
        }

        Engine.setDecoder("mp3", new MP3Decoder());

        Engine.storeBGM(NAME, FORMAT);

        clip = Engine.getClip(NAME);
        AudioInputStream s = Engine.getStream(NAME, FORMAT);
        fl = (int) s.getFrameLength();
        flms = clip.getMicrosecondLength();
        wave = getRawWave(s);
        s.close();

        freq = new double[WINDOW_LENGTH];

        clip.addLineListener(event -> {
            if (event.getType() == LineEvent.Type.STOP) {
                Engine.playBGM(NAME, 1);
            }
        });

        Engine.setMasterVol(0.5f);
        Engine.playBGM(NAME, 1);
    }

    double ratio = 1000;
    public static final double ALPHA = 1;
    public static final double DELTA = 20;
    public static final int SLICE_LENGTH = WINDOW_LENGTH / 2 / SLICE_NUM;
    public Paintable painter = (EasyPaintable) (g, x, y, alpha) -> {
        g.translate(-x, -y);

        g.setColor(COLOR_BACKGROUND);
        g.fillRect(0, 0, 800, 500);

        g.setColor(COLOR_FOREGROUND);
        int startX = 400 - 512 / 2 - 512 / SLICE_NUM / 2;
        double maxL = 0;
        double[] ls = new double[SLICE_NUM];
        for (int i = 0; i < SLICE_NUM; i++) {
            double l = 0;
            for (int j = 0; j < SLICE_LENGTH; j++) {
                if (i * SLICE_LENGTH + j < WINDOW_LENGTH / 2)
                    l += freq[i * SLICE_LENGTH + j] * ratio;
            }
            l /= SLICE_LENGTH;
            if (l > maxL)
                maxL = l;
            ls[i] = l;
        }
        for (int i = 0; i < SLICE_NUM; i++) {
            double l = ls[i];
            g.setColor(Color.getHSBColor((float) i / SLICE_NUM, 0.43f, 1));
            g.fillRect(startX + i * (512 / SLICE_NUM) - 2, 320 - (int) l - 2,
                    (512 / SLICE_NUM) + 4, (int) (l * 1.35) + 1 + 4);
        }
        for (int i = 0; i < SLICE_NUM; i++) {
            double l = ls[i];
            g.setColor(Color.getHSBColor((float) i / SLICE_NUM, 1, 1));
            g.fillRect(startX + i * (512 / SLICE_NUM), 320 - (int) l,
                    (512 / SLICE_NUM), (int) (l * 1.35) + 1);
        }
        if (maxL - 250 > DELTA) {
            ratio -= ALPHA;
        }

        g.setColor(COLOR_SCROLLBACK);
        g.fillRoundRect(100, 456, 600, 15, 8, 6);
        g.setColor(COLOR_SCROLLBAR);
        g.fillRoundRect(102, 458, (int) ((long) 596 * fp / fl), 11, 5, 5);

        int[] hmsl = getHmsFromMicrosecond(flms), hmsp = getHmsFromMicrosecond(fpms);
        int min = (hmsl[0] > 0) ? 3 : ((hmsl[1] > 0) ? 2 : 1);
        String timeStr = hmsToString(hmsp, min) + "/" + hmsToString(hmsl, min);

        g.setFont(new Font("", Font.PLAIN, 18));
        g.setColor(COLOR_SCROLLBACK);
        g.drawString(timeStr, 600, 452);

        g.translate(x, y);
    };

    private int[] getHmsFromMicrosecond(long mcs) {
        int[] hms = new int[]{0, 0, 0};

        mcs /= 1e6;
        hms[2] = (int) (mcs % 60);
        mcs /= 60;
        hms[1] = (int) (mcs % 60);
        mcs /= 60;
        hms[0] = (int) mcs;

        return hms;
    }

    private String hmsToString(int[] hms, int min) {
        String str = "";

        if (hms[0] > 0 || min >= 3) str += String.format("%02d:", hms[0]);
        if (hms[1] > 0 || min >= 2) str += String.format("%02d:", hms[1]);
        if (hms[2] > 0 || min >= 1) str += String.format("%02d", hms[2]);

        return str;
    }

    private int getIntFromBytes(byte[] bytes, int offset, int length, boolean isBigEndian) {
        short result = 0;

        for (int i = 0; i < length; i++) {
            byte b = bytes[offset + (isBigEndian ? length - i - 1 : i)];
            result |= (b & 0xFF) << (i * 8);
        }

        return result;
    }

    /**
     * Untested.
     * Passed case: 16bit, mono/stereo, little-endian
     *
     * @param stream
     * @return
     * @throws IOException
     */
    private float[] getRawWave(AudioInputStream stream) throws IOException {
        AudioFormat format = stream.getFormat();
        //System.out.println(format + ", " + stream.getFrameLength() + " frames");
        int length = (int) stream.getFrameLength();
        float[] wave = new float[length];
        byte[] bytes = stream.readAllBytes();
        int maxInt = 1;
        if (format.getEncoding() == PCM_SIGNED || format.getEncoding() == PCM_UNSIGNED) {
            for (int i = 0; i < format.getSampleSizeInBits() - 1; i++) {
                maxInt <<= 1;
            }
        }

        for (int i = 0; i < length; i++) {
            double valueDouble = 0;

            if (format.getEncoding() == PCM_SIGNED || format.getEncoding() == PCM_UNSIGNED) {
                long valueInt = getIntFromBytes(
                        bytes,
                        i * format.getFrameSize(),
                        format.getSampleSizeInBits() / 8,
                        format.isBigEndian()
                );
                if (format.getChannels() == 2) {
                    valueInt += getIntFromBytes(
                            bytes,
                            i * format.getFrameSize() + format.getFrameSize() / 2,
                            format.getSampleSizeInBits() / 8,
                            format.isBigEndian()
                    );
                    valueInt >>= 1;
                }
                valueDouble = (double) valueInt / maxInt;
            } else if (format.getEncoding() == PCM_FLOAT) {
                valueDouble = Float.intBitsToFloat(getIntFromBytes(
                        bytes,
                        i * format.getFrameSize(),
                        format.getSampleSizeInBits() / 8,
                        format.isBigEndian()
                ));
                if (format.getChannels() == 2) {
                    valueDouble += Float.intBitsToFloat(getIntFromBytes(
                            bytes,
                            i * format.getFrameSize() + format.getFrameSize() / 2,
                            format.getSampleSizeInBits() / 8,
                            format.isBigEndian()
                    ));
                    valueDouble /= 2;
                }
            } else {
                new Exception("Unsupported encoding format.").printStackTrace();
            }

            wave[i] = (float) valueDouble;
        }

        return wave;
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY) {
        super.mouseClicked(mouseX, mouseY);

        if (new VectorInt(mouseX, mouseY).inBound(100, 456, 600, 15)) {
            int p = mouseX;
            if (p < 102) p = 102;
            else if (p >= 596 + 102) p = 596 + 102 - 1;
            p -= 102;

            clip.setFramePosition((int) ((long) p * fl / 596));
        }
    }

    @Override
    public void refresh() {
        super.refresh();

        if (clip == null) return;
        fp = clip.getFramePosition();
        fpms = (long) ((double) fp / fl * flms);
        Complex[] windowedWave = new Complex[WINDOW_LENGTH];
        for (int i = 0; i < WINDOW_LENGTH; i++) {
            windowedWave[i] = new Complex((fp + i < fl ? wave[fp + i] : 0) *
                    (0.53836 - 0.46164 * Math.cos(2 * Math.PI * i / (WINDOW_LENGTH - 1))));
        }

        Complex[] dft = d.DFT(windowedWave);

        for (int i = 0; i < WINDOW_LENGTH; i++) {
            freq[i] = Math.log10(dft[i].abs() / 20 + 1);
        }

        if (IS_DESK) {
            int sliceLen = WINDOW_LENGTH / 2 / iconNum;
            for (int i = 0; i < iconNum; i++) {
                double l = 0;
                for (int j = 0; j < sliceLen; j++) {
                    l += freq[i * sliceLen + j];
                }
                l /= sliceLen;
                int posY = (int) (desktopSize.y - 100 - l * (desktopSize.y - 100) * 10);
                if (posY < 0) posY = 0;
                IconDance.setIconPos(desktop, i, 100 + i * (desktopSize.x - 200) / iconNum,
                        posY);
            }
        }
    }

    @Override
    public void paint(Graphics2D g, AnimationData appliedData) {
        super.paint(g, appliedData);

        painter.paint(g, appliedData);
    }

    public static void main(String[] args) {
        /**/
        Engine.initialize();

        Engine.setTitle("Wave Visualizer");
        Engine.setSize(800, 500);

        Engine.addState("1", Visualizer.class);
        Engine.loadState("1");

        Engine.start();
        Engine.showState("1");

        /*

        try {
            File file = new File("file/audio/test.mp3");
            AudioInputStream in = AudioSystem.getAudioInputStream(file);
            AudioInputStream din = AudioSystem.getAudioInputStream(
                    new AudioFormat(
                            AudioFormat.Encoding.PCM_SIGNED,
                            in.getFormat().getSampleRate(),
                            16,
                            1,
                            2,
                            in.getFormat().getFrameRate(),
                            false
                    ),
                    in
            );

            Clip clip = AudioSystem.getClip();

            clip.open(din);
            System.out.println(clip.getFrameLength());
            clip.addLineListener(new LineListener() {
                @Override
                public void update(LineEvent event) {
                    System.out.println(event.getType());
                }
            });
            clip.loop(-1);

            Thread.sleep(1000000);
        } catch (Exception e) {
            e.printStackTrace();
        }


        /**/
    }
}
