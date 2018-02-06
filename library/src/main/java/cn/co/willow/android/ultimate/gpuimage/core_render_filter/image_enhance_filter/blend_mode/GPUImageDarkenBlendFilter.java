package cn.co.willow.android.ultimate.gpuimage.core_render_filter.image_enhance_filter.blend_mode;

/**
 * 加深混合,通常用于重叠类型,可以类推Proter&Duff 的 darken 渲染模式
 */
public class GPUImageDarkenBlendFilter extends GPUImageTwoInputFilter {

    public static final String DARKEN_BLEND_FRAGMENT_SHADER = "" +
            "varying highp vec2 textureCoordinate;\n" +
            " varying highp vec2 textureCoordinate2;\n" +
            "\n" +
            " uniform sampler2D inputImageTexture;\n" +
            " uniform sampler2D inputImageTexture2;\n" +
            " \n" +
            " void main()\n" +
            " {\n" +
            "    lowp vec4 base = texture2D(inputImageTexture, textureCoordinate);\n" +
            "    lowp vec4 overlayer = texture2D(inputImageTexture2, textureCoordinate2);\n" +
            "    \n" +
            "    gl_FragColor = vec4(min(overlayer.rgb * base.a, base.rgb * overlayer.a) + overlayer.rgb * (1.0 - base.a) + base.rgb * (1.0 - overlayer.a), 1.0);\n" +
            " }";

    public GPUImageDarkenBlendFilter() {
        super(DARKEN_BLEND_FRAGMENT_SHADER);
    }
}
