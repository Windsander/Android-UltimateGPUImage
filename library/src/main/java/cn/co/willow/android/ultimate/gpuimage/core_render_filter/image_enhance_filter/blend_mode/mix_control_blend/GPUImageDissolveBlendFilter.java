package cn.co.willow.android.ultimate.gpuimage.core_render_filter.image_enhance_filter.blend_mode.mix_control_blend;

/**
 * 混合控制滤镜：溶解混合，值范围 0.0~1.0，0.5为正常值（溶解一半）
 * Mix ranges from 0.0 (only image 1) to 1.0 (only image 2), with 0.5 (half of either) as the normal level
 */
public class GPUImageDissolveBlendFilter extends GPUImageMixBlendFilter {

    public static final String DISSOLVE_BLEND_FRAGMENT_SHADER = "varying highp vec2 textureCoordinate;\n" +
            " varying highp vec2 textureCoordinate2;\n" +
            "\n" +
            " uniform sampler2D inputImageTexture;\n" +
            " uniform sampler2D inputImageTexture2;\n" +
            " uniform lowp float mixturePercent;\n" +
            " \n" +
            " void main()\n" +
            " {\n" +
            "    lowp vec4 textureColor = texture2D(inputImageTexture, textureCoordinate);\n" +
            "    lowp vec4 textureColor2 = texture2D(inputImageTexture2, textureCoordinate2);\n" +
            "    \n" +
            "    gl_FragColor = mix(textureColor, textureColor2, mixturePercent);\n" +
            " }";

    public GPUImageDissolveBlendFilter() {
        super(DISSOLVE_BLEND_FRAGMENT_SHADER);
    }

    public GPUImageDissolveBlendFilter(float mix) {
        super(DISSOLVE_BLEND_FRAGMENT_SHADER, mix);
    }
}
