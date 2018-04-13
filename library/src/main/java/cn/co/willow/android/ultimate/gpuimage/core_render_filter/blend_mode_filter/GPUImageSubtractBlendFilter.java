package cn.co.willow.android.ultimate.gpuimage.core_render_filter.blend_mode_filter;

/**
 * 差值混合,通常用于创建两个图像之间的动画变暗模糊效果
 */
public class GPUImageSubtractBlendFilter extends GPUImageTwoInputFilter {

    public static final String SUBTRACT_BLEND_FRAGMENT_SHADER = "" +
            "varying highp vec2 textureCoordinate;\n" +
            " varying highp vec2 textureCoordinate2;\n" +
            "\n" +
            " uniform sampler2D inputImageTexture;\n" +
            " uniform sampler2D inputImageTexture2;\n" +
            " \n" +
            " void main()\n" +
            " {\n" +
            "   lowp vec4 textureColor = texture2D(inputImageTexture, textureCoordinate);\n" +
            "   lowp vec4 textureColor2 = texture2D(inputImageTexture2, textureCoordinate2);\n" +
            "\n" +
            "   gl_FragColor = vec4(textureColor.rgb - textureColor2.rgb, textureColor.a);\n" +
            " }";

    public GPUImageSubtractBlendFilter() {
        super(SUBTRACT_BLEND_FRAGMENT_SHADER);
    }
}
