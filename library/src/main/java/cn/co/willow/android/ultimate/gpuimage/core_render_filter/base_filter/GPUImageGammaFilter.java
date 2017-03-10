package cn.co.willow.android.ultimate.gpuimage.core_render_filter.base_filter;

import android.opengl.GLES30;

import cn.co.willow.android.ultimate.gpuimage.core_render_filter.GPUImageFilter;

/**
 * 基础滤镜：伽马滤镜，值范围 0.0~3，1.0为正常值
 * gamma value ranges from 0.0 to 3.0, with 1.0 as the normal level
 */
public class GPUImageGammaFilter extends GPUImageFilter {

    public static final String GAMMA_FRAGMENT_SHADER = "" +
            "varying highp vec2 textureCoordinate;\n" +
            " \n" +
            " uniform sampler2D inputImageTexture;\n" +
            " uniform lowp float gamma;\n" +
            " \n" +
            " void main()\n" +
            " {\n" +
            "     lowp vec4 textureColor = texture2D(inputImageTexture, textureCoordinate);\n" +
            "     \n" +
            "     gl_FragColor = vec4(pow(textureColor.rgb, vec3(gamma)), textureColor.w);\n" +
            " }";

    private int mGammaLocation;
    private float mGamma;

    public GPUImageGammaFilter() {
        this(1.2f);
    }

    public GPUImageGammaFilter(final float gamma) {
        super(NO_FILTER_VERTEX_SHADER, GAMMA_FRAGMENT_SHADER);
        mGamma = gamma;
    }

    @Override
    public void onInit() {
        super.onInit();
        mGammaLocation = GLES30.glGetUniformLocation(getProgram(), "gamma");
    }

    @Override
    public void onInitialized() {
        super.onInitialized();
        setGamma(mGamma);
    }

    public void setGamma(final float gamma) {
        mGamma = gamma;
        setFloat(mGammaLocation, mGamma);
    }
}
