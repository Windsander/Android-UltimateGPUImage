package cn.co.willow.android.ultimate.gpuimage.core_render_filter.blend_mode_filter;

/**
 * 色彩加深混合滤镜，效果是第一张图将会以类似蒙皮的形式覆盖在第二张图纸上，形成组合
 * the second image will be covered by first image, looks like a skin.
 * can use to develop some text effect
 */
public class GPUImageColorBurnBlendFilter extends GPUImageTwoInputFilter {

    public static final String COLOR_BURN_BLEND_FRAGMENT_SHADER = "" +
            "varying highp vec2 textureCoordinate;\n" +
            " varying highp vec2 textureCoordinate2;\n" +
            "\n" +
            " uniform sampler2D inputImageTexture;\n" +
            " uniform sampler2D inputImageTexture2;\n" +
            " \n" +
            " void main()\n" +
            " {\n" +
            "    mediump vec4 textureColor = texture2D(inputImageTexture, textureCoordinate);\n" +
            "    mediump vec4 textureColor2 = texture2D(inputImageTexture2, textureCoordinate2);\n" +
            "    mediump vec4 whiteColor = vec4(1.0);\n" +
            "    gl_FragColor = whiteColor - (whiteColor - textureColor) / textureColor2;\n" +
            " }";

    public GPUImageColorBurnBlendFilter() {
        super(COLOR_BURN_BLEND_FRAGMENT_SHADER);
    }
}
