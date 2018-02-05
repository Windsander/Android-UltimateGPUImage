package cn.co.willow.android.ultimate.gpuimage.utils;

public class GPUImageNativeLibrary {
    static {
        LogUtil.i("GPUImageNativeLibrary::", "yuv-decoder loading start");
        System.loadLibrary("yuv-decoder");
        LogUtil.i("GPUImageNativeLibrary::", "yuv-decoder loading finish");
    }

    public static native void YUVtoRBGA(byte[] yuv, int width, int height, int[] out);

    public static native void YUVtoARBG(byte[] yuv, int width, int height, int[] out);
}
