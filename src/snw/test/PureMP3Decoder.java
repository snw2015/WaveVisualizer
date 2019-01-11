package snw.test;

public class PureMP3Decoder {
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

    public static native boolean decodeMp3(String fileName, Mp3Info info);
}
