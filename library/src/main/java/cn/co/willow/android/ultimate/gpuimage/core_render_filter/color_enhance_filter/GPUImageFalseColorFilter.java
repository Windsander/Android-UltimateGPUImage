package cn.co.willow.android.ultimate.gpuimage.core_render_filter.color_enhance_filter;

import android.opengl.GLES30;

import cn.co.willow.android.ultimate.gpuimage.core_render_filter.GPUImageFilter;

/**
 * 色泽增强滤镜：色彩替换（替换亮部和暗部色彩）
 * replace color in the light and dark areas
 */
public class GPUImageFalseColorFilter extends GPUImageFilter {
    public static final String FALSECOLOR_FRAGMENT_SHADER = "" +
            "precision lowp float;\n" +
            "\n" +
            "varying highp vec2 textureCoordinate;\n" +
            "\n" +
            "uniform sampler2D inputImageTexture;\n" +
            "uniform float intensity;\n" +
            "uniform vec3 firstColor;\n" +
            "uniform vec3 secondColor;\n" +
            "\n" +
            "const mediump vec3 luminanceWeighting = vec3(0.2125, 0.7154, 0.0721);\n" +
            "\n" +
            "void main()\n" +
            "{\n" +
            "lowp vec4 textureColor = texture2D(inputImageTexture, textureCoordinate);\n" +
            "float luminance = dot(textureColor.rgb, luminanceWeighting);\n" +
            "\n" +
            "gl_FragColor = vec4( mix(firstColor.rgb, secondColor.rgb, luminance), textureColor.a);\n" +
            "}\n";

    private float[] mFirstColor;
    private int mFirstColorLocation;
    private float[] mSecondColor;
    private int mSecondColorLocation;

    public GPUImageFalseColorFilter() {
        this(0f, 0f, 0.5f, 1f, 0f, 0f);
    }

    public GPUImageFalseColorFilter(float firstRed, float firstGreen, float firstBlue, float secondRed, float secondGreen, float secondBlue) {
        this(new float[]{firstRed, firstGreen, firstBlue}, new float[]{secondRed, secondGreen, secondBlue});
    }

    public GPUImageFalseColorFilter(float[] firstColor, float[] secondColor) {
        super(NO_FILTER_VERTEX_SHADER, FALSECOLOR_FRAGMENT_SHADER);
        mFirstColor = firstColor;
        mSecondColor = secondColor;
    }

    @Override
    public void onInit() {
        super.onInit();
        mFirstColorLocation = GLES30.glGetUniformLocation(getProgram(), "firstColor");
        mSecondColorLocation = GLES30.glGetUniformLocation(getProgram(), "secondColor");
    }

    @Override
    public void onInitialized() {
        super.onInitialized();
        setFirstColor(mFirstColor);
        setSecondColor(mSecondColor);
    }

    public void setFirstColor(final float[] firstColor) {
        mFirstColor = firstColor;
        setFloatVec3(mFirstColorLocation, firstColor);
    }

    public void setSecondColor(final float[] secondColor) {
        mSecondColor = secondColor;
        setFloatVec3(mSecondColorLocation, secondColor);
    }
}
