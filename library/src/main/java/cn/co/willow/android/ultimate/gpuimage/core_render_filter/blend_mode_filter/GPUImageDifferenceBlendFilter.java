package cn.co.willow.android.ultimate.gpuimage.core_render_filter.blend_mode_filter;

/**
 * 差异混合,通常用于创建更多变动的颜色
 */
public class GPUImageDifferenceBlendFilter extends GPUImageTwoInputFilter {

    public static final String DIFFERENCE_BLEND_FRAGMENT_SHADER = "" +
            "varying highp vec2 textureCoordinate;\n" +
            " varying highp vec2 textureCoordinate2;\n" +
            "\n" +
            " uniform sampler2D inputImageTexture;\n" +
            " uniform sampler2D inputImageTexture2;\n" +
            " \n" +
            " void main()\n" +
            " {\n" +
            "     mediump vec4 textureColor = texture2D(inputImageTexture, textureCoordinate);\n" +
            "     mediump vec4 textureColor2 = texture2D(inputImageTexture2, textureCoordinate2);\n" +
            "     gl_FragColor = vec4(abs(textureColor2.rgb - textureColor.rgb), textureColor.a);\n" +
            " }";

    public GPUImageDifferenceBlendFilter() {
        super(DIFFERENCE_BLEND_FRAGMENT_SHADER);
    }
}
