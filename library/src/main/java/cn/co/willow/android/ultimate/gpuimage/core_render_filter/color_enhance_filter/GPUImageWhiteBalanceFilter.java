package cn.co.willow.android.ultimate.gpuimage.core_render_filter.color_enhance_filter;

import android.opengl.GLES30;

import cn.co.willow.android.ultimate.gpuimage.core_render_filter.GPUImageFilter;

/**
 * 增强滤镜：白平衡滤镜，保证其他景物的影像就会接近人眼的色彩视觉习惯的一种处理滤镜
 * Adjusts the white balance of incoming image. To insure image adjust humans vision<br>
 * <br>
 * temperature:
 * tint:
 */
public class GPUImageWhiteBalanceFilter extends GPUImageFilter {
    public static final String WHITE_BALANCE_FRAGMENT_SHADER = "" +
            "uniform sampler2D inputImageTexture;\n" +
            "varying highp vec2 textureCoordinate;\n" +
            " \n" +
            "uniform lowp float temperature;\n" +
            "uniform lowp float tint;\n" +
            "\n" +
            "const lowp vec3 warmFilter = vec3(0.93, 0.54, 0.0);\n" +
            "\n" +
            "const mediump mat3 RGBtoYIQ = mat3(0.299, 0.587, 0.114, 0.596, -0.274, -0.322, 0.212, -0.523, 0.311);\n" +
            "const mediump mat3 YIQtoRGB = mat3(1.0, 0.956, 0.621, 1.0, -0.272, -0.647, 1.0, -1.105, 1.702);\n" +
            "\n" +
            "void main()\n" +
            "{\n" +
            "	lowp vec4 source = texture2D(inputImageTexture, textureCoordinate);\n" +
            "	\n" +
            "	mediump vec3 yiq = RGBtoYIQ * source.rgb; //adjusting tint\n" +
            "	yiq.b = clamp(yiq.b + tint*0.5226*0.1, -0.5226, 0.5226);\n" +
            "	lowp vec3 rgb = YIQtoRGB * yiq;\n" +
            "\n" +
            "	lowp vec3 processed = vec3(\n" +
            "		(rgb.r < 0.5 ? (2.0 * rgb.r * warmFilter.r) : (1.0 - 2.0 * (1.0 - rgb.r) * (1.0 - warmFilter.r))), //adjusting temperature\n" +
            "		(rgb.g < 0.5 ? (2.0 * rgb.g * warmFilter.g) : (1.0 - 2.0 * (1.0 - rgb.g) * (1.0 - warmFilter.g))), \n" +
            "		(rgb.b < 0.5 ? (2.0 * rgb.b * warmFilter.b) : (1.0 - 2.0 * (1.0 - rgb.b) * (1.0 - warmFilter.b))));\n" +
            "\n" +
            "	gl_FragColor = vec4(mix(rgb, processed, temperature), source.a);\n" +
            "}";

    private int mTemperatureLocation;
    private float mTemperature;
    private int mTintLocation;
    private float mTint;

    public GPUImageWhiteBalanceFilter() {
        this(5000.0f, 0.0f);
    }

    public GPUImageWhiteBalanceFilter(final float temperature, final float tint) {
        super(NO_FILTER_VERTEX_SHADER, WHITE_BALANCE_FRAGMENT_SHADER);
        mTemperature = temperature;
        mTint = tint;
    }

    @Override
    public void onInit() {
        super.onInit();
        mTemperatureLocation = GLES30.glGetUniformLocation(getProgram(), "temperature");
        mTintLocation = GLES30.glGetUniformLocation(getProgram(), "tint");

        setTemperature(mTemperature);
        setTint(mTint);
    }


    public void setTemperature(final float temperature) {
        mTemperature = temperature;
        setFloat(mTemperatureLocation, mTemperature < 5000 ? (float) (0.0004 * (mTemperature - 5000.0)) : (float) (0.00006 * (mTemperature - 5000.0)));
    }

    public void setTint(final float tint) {
        mTint = tint;
        setFloat(mTintLocation, (float) (mTint / 100.0));
    }
}
