package cn.co.willow.android.ultimate.gpuimage.core_render_filter.image_enhance_filter.blend_mode.color_mapping_blend;

import android.opengl.GLES30;

import cn.co.willow.android.ultimate.gpuimage.core_render_filter.image_enhance_filter.blend_mode.GPUImageTwoInputFilter;

/**
 * 颜色映射表渲染滤镜基类，通过设置好的颜色映射表来进行图片渲染
 */
public class GPUImageLookupFilter extends GPUImageTwoInputFilter {

    public static final String LOOKUP_FRAGMENT_SHADER = "" +
            "varying highp vec2 textureCoordinate;\n" +
            " varying highp vec2 textureCoordinate2; // TODO: This is not used\n" +
            " \n" +
            " uniform sampler2D inputImageTexture;\n" +
            " uniform sampler2D inputImageTexture2; // lookup texture\n" +
            " \n" +
            " uniform lowp float intensity;\n" +
            " \n" +
            " void main()\n" +
            " {\n" +
            "     highp vec4 textureColor = texture2D(inputImageTexture, textureCoordinate);\n" +
            "     \n" +
            "     highp float blueColor = textureColor.b * 63.0;\n" +
            "     \n" +
            "     highp vec2 quad1;\n" +
            "     quad1.y = floor(floor(blueColor) / 8.0);\n" +
            "     quad1.x = floor(blueColor) - (quad1.y * 8.0);\n" +
            "     \n" +
            "     highp vec2 quad2;\n" +
            "     quad2.y = floor(ceil(blueColor) / 8.0);\n" +
            "     quad2.x = ceil(blueColor) - (quad2.y * 8.0);\n" +
            "     \n" +
            "     highp vec2 texPos1;\n" +
            "     texPos1.x = (quad1.x * 0.125) + 0.5/512.0 + ((0.125 - 1.0/512.0) * textureColor.r);\n" +
            "     texPos1.y = (quad1.y * 0.125) + 0.5/512.0 + ((0.125 - 1.0/512.0) * textureColor.g);\n" +
            "     \n" +
            "     highp vec2 texPos2;\n" +
            "     texPos2.x = (quad2.x * 0.125) + 0.5/512.0 + ((0.125 - 1.0/512.0) * textureColor.r);\n" +
            "     texPos2.y = (quad2.y * 0.125) + 0.5/512.0 + ((0.125 - 1.0/512.0) * textureColor.g);\n" +
            "     \n" +
            "     lowp vec4 newColor1 = texture2D(inputImageTexture2, texPos1);\n" +
            "     lowp vec4 newColor2 = texture2D(inputImageTexture2, texPos2);\n" +
            "     \n" +
            "     lowp vec4 newColor = mix(newColor1, newColor2, fract(blueColor));\n" +
            "     gl_FragColor = mix(textureColor, vec4(newColor.rgb, textureColor.w), intensity);\n" +
            " }";

    private int mIntensityLocation;
    private float mIntensity;

    public GPUImageLookupFilter() {
        this(1.0f);
    }

    public GPUImageLookupFilter(final float intensity) {
        super(LOOKUP_FRAGMENT_SHADER);
        mIntensity = intensity;
    }

    @Override
    public void onInit() {
        super.onInit();
        mIntensityLocation = GLES30.glGetUniformLocation(getProgram(), "intensity");
    }

    @Override
    public void onInitialized() {
        super.onInitialized();
        setIntensity(mIntensity);
    }

    public void setIntensity(final float intensity) {
        mIntensity = intensity;
        setFloat(mIntensityLocation, mIntensity);
    }
}
