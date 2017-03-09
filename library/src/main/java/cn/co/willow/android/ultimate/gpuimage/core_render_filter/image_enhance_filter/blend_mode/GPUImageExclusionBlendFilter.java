package cn.co.willow.android.ultimate.gpuimage.core_render_filter.image_enhance_filter.blend_mode;

/**
 * 排除混合
 */
public class GPUImageExclusionBlendFilter extends GPUImageTwoInputFilter {

    public static final String EXCLUSION_BLEND_FRAGMENT_SHADER = "" +
            "varying highp vec2 textureCoordinate;\n" +
            " varying highp vec2 textureCoordinate2;\n" +
            "\n" +
            " uniform sampler2D inputImageTexture;\n" +
            " uniform sampler2D inputImageTexture2;\n" +
            " \n" +
            " void main()\n" +
            " {\n" +
            "     mediump vec4 base = texture2D(inputImageTexture, textureCoordinate);\n" +
            "     mediump vec4 overlay = texture2D(inputImageTexture2, textureCoordinate2);\n" +
            "     \n" +
            "     //     Dca = (Sca.Da + Dca.Sa - 2.Sca.Dca) + Sca.(1 - Da) + Dca.(1 - Sa)\n" +
            "     \n" +
            "     gl_FragColor = vec4((overlay.rgb * base.a + base.rgb * overlay.a - 2.0 * overlay.rgb * base.rgb) + overlay.rgb * (1.0 - base.a) + base.rgb * (1.0 - overlay.a), base.a);\n" +
            " }";

    public GPUImageExclusionBlendFilter() {
        super(EXCLUSION_BLEND_FRAGMENT_SHADER);
    }
}
