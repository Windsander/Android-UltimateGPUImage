package cn.co.willow.android.ultimate.gpuimage.core_render_filter.image_enhance_filter.blend_mode;

/**
 * 减淡混合,通常用于重叠类型
 */
public class GPUImageLightenBlendFilter extends GPUImageTwoInputFilter {

    public static final String LIGHTEN_BLEND_FRAGMENT_SHADER = "" +
            "varying highp vec2 textureCoordinate;\n" +
            " varying highp vec2 textureCoordinate2;\n" +
            "\n" +
            " uniform sampler2D inputImageTexture;\n" +
            " uniform sampler2D inputImageTexture2;\n" +
            " \n" +
            " void main()\n" +
            " {\n" +
            "    lowp vec4 textureColor = texture2D(inputImageTexture, textureCoordinate);\n" +
            "    lowp vec4 textureColor2 = texture2D(inputImageTexture2, textureCoordinate2);\n" +
            "    \n" +
            "    gl_FragColor = max(textureColor, textureColor2);\n" +
            " }";

    public GPUImageLightenBlendFilter() {
        super(LIGHTEN_BLEND_FRAGMENT_SHADER);
    }
}
