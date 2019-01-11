package snw.test;

public class IconDance {
    static {
        System.loadLibrary("icon_dance");
    }

    public static native int getDesktopHWND();
    public static native int getIconNum(int hwnd);
    public static native void setIconPos(int hwnd, int id, int x, int y);
    public static native int[] getSize(int hwnd);
}
