package cn.co.willow.android.ultimate.gpuimage.core_render_filter.base_filter;

import android.opengl.GLES30;

import cn.co.willow.android.ultimate.gpuimage.core_render_filter.GPUImageFilter;

/**
 * 基础滤镜：色调分离，形成噪点效果，值范围 1~256，10为正常值
 * Reduces the color range of the image. <br>
 * <br>
 * colorLevels: ranges from 1 to 256, with a default of 10
 */
public class GPUImagePosterizeFilter extends GPUImageFilter {
    public static final String POSTERIZE_FRAGMENT_SHADER = "" +
            "varying highp vec2 textureCoordinate;\n" +
            "\n" +
            "uniform sampler2D inputImageTexture;\n" +
            "uniform highp float colorLevels;\n" +
            "\n" +
            "void main()\n" +
            "{\n" +
            "   highp vec4 textureColor = texture2D(inputImageTexture, textureCoordinate);\n" +
            "   \n" +
            "   gl_FragColor = floor((textureColor * colorLevels) + vec4(0.5)) / colorLevels;\n" +
            "}";

    private int mGLUniformColorLevels;
    private int mColorLevels;

    public GPUImagePosterizeFilter() {
        this(10);
    }

    public GPUImagePosterizeFilter(final int colorLevels) {
        super(GPUImageFilter.NO_FILTER_VERTEX_SHADER, POSTERIZE_FRAGMENT_SHADER);
        mColorLevels = colorLevels;
    }

    @Override
    public void onInit() {
        super.onInit();
        mGLUniformColorLevels = GLES30.glGetUniformLocation(getProgram(), "colorLevels");
        setColorLevels(mColorLevels);
    }

    public void setColorLevels(final int colorLevels) {
        mColorLevels = colorLevels;
        setFloat(mGLUniformColorLevels, colorLevels);
    }
}
