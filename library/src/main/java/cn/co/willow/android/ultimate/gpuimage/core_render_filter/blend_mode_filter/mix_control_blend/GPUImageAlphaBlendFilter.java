package cn.co.willow.android.ultimate.gpuimage.core_render_filter.blend_mode_filter.mix_control_blend;

/**
 * 混合控制滤镜：透明混合,通常用于在背景上应用前景的透明度，值范围 0.0~1.0，0.5为正常值
 * Mix ranges from 0.0 (only image 1) to 1.0 (only image 2), with 0.5 (half of either) as the normal level
 */
public class GPUImageAlphaBlendFilter extends GPUImageMixBlendFilter {

    public static final String ALPHA_BLEND_FRAGMENT_SHADER = "varying highp vec2 textureCoordinate;\n" +
            " varying highp vec2 textureCoordinate2;\n" +
            "\n" +
            " uniform sampler2D inputImageTexture;\n" +
            " uniform sampler2D inputImageTexture2;\n" +
            " \n" +
            " uniform lowp float mixturePercent;\n" +
            "\n" +
            " void main()\n" +
            " {\n" +
            "   lowp vec4 textureColor = texture2D(inputImageTexture, textureCoordinate);\n" +
            "   lowp vec4 textureColor2 = texture2D(inputImageTexture2, textureCoordinate2);\n" +
            "\n" +
            "   gl_FragColor = vec4(mix(textureColor.rgb, textureColor2.rgb, textureColor2.a * mixturePercent), textureColor.a);\n" +
            " }";

    public GPUImageAlphaBlendFilter() {
        super(ALPHA_BLEND_FRAGMENT_SHADER);
    }

    public GPUImageAlphaBlendFilter(float mix) {
        super(ALPHA_BLEND_FRAGMENT_SHADER, mix);
    }
}
