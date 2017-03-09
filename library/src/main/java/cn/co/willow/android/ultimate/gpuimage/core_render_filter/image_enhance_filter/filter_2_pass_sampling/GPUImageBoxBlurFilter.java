package cn.co.willow.android.ultimate.gpuimage.core_render_filter.image_enhance_filter.filter_2_pass_sampling;

/**
 * 盒状模糊滤镜，一种经过硬件加速后的快速图片模糊算法，有可能产生马赛克，如果像素太低的话。
 * A hardware-accelerated 9-hit box blur of an image
 * <p>
 * scaling: for the size of the applied blur, default of 1.0
 */
public class GPUImageBoxBlurFilter extends GPUImageTwoPassTextureSamplingFilter {
    public static final String VERTEX_SHADER =
            "attribute vec4 position;\n" +
                    "attribute vec2 inputTextureCoordinate;\n" +
                    "\n" +
                    "uniform float texelWidthOffset; \n" +
                    "uniform float texelHeightOffset; \n" +
                    "\n" +
                    "varying vec2 centerTextureCoordinate;\n" +
                    "varying vec2 oneStepLeftTextureCoordinate;\n" +
                    "varying vec2 twoStepsLeftTextureCoordinate;\n" +
                    "varying vec2 oneStepRightTextureCoordinate;\n" +
                    "varying vec2 twoStepsRightTextureCoordinate;\n" +
                    "\n" +
                    "void main()\n" +
                    "{\n" +
                    "gl_Position = position;\n" +
                    "\n" +
                    "vec2 firstOffset = vec2(1.5 * texelWidthOffset, 1.5 * texelHeightOffset);\n" +
                    "vec2 secondOffset = vec2(3.5 * texelWidthOffset, 3.5 * texelHeightOffset);\n" +
                    "\n" +
                    "centerTextureCoordinate = inputTextureCoordinate;\n" +
                    "oneStepLeftTextureCoordinate = inputTextureCoordinate - firstOffset;\n" +
                    "twoStepsLeftTextureCoordinate = inputTextureCoordinate - secondOffset;\n" +
                    "oneStepRightTextureCoordinate = inputTextureCoordinate + firstOffset;\n" +
                    "twoStepsRightTextureCoordinate = inputTextureCoordinate + secondOffset;\n" +
                    "}\n";

    public static final String FRAGMENT_SHADER =
            "precision highp float;\n" +
                    "\n" +
                    "uniform sampler2D inputImageTexture;\n" +
                    "\n" +
                    "varying vec2 centerTextureCoordinate;\n" +
                    "varying vec2 oneStepLeftTextureCoordinate;\n" +
                    "varying vec2 twoStepsLeftTextureCoordinate;\n" +
                    "varying vec2 oneStepRightTextureCoordinate;\n" +
                    "varying vec2 twoStepsRightTextureCoordinate;\n" +
                    "\n" +
                    "void main()\n" +
                    "{\n" +
                    "lowp vec4 fragmentColor = texture2D(inputImageTexture, centerTextureCoordinate) * 0.2;\n" +
                    "fragmentColor += texture2D(inputImageTexture, oneStepLeftTextureCoordinate) * 0.2;\n" +
                    "fragmentColor += texture2D(inputImageTexture, oneStepRightTextureCoordinate) * 0.2;\n" +
                    "fragmentColor += texture2D(inputImageTexture, twoStepsLeftTextureCoordinate) * 0.2;\n" +
                    "fragmentColor += texture2D(inputImageTexture, twoStepsRightTextureCoordinate) * 0.2;\n" +
                    "\n" +
                    "gl_FragColor = fragmentColor;\n" +
                    "}\n";

    private float blurSize = 1f;

    /**
     * Construct new BoxBlurFilter with default blur size of 1.0.
     */
    public GPUImageBoxBlurFilter() {
        this(1f);
    }


    public GPUImageBoxBlurFilter(float blurSize) {
        super(VERTEX_SHADER, FRAGMENT_SHADER, VERTEX_SHADER, FRAGMENT_SHADER);
        this.blurSize = blurSize;
    }

    /**
     * A scaling for the size of the applied blur, default of 1.0
     *
     * @param blurSize
     */
    public void setBlurSize(float blurSize) {
        this.blurSize = blurSize;
        runOnDraw(new Runnable() {
            @Override
            public void run() {
                initTexelOffsets();
            }
        });
    }

    @Override
    public float getVerticalTexelOffsetRatio() {
        return blurSize;
    }

    @Override
    public float getHorizontalTexelOffsetRatio() {
        return blurSize;
    }
}
