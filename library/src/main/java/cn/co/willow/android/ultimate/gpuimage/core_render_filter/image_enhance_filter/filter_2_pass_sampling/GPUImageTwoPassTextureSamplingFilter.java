package cn.co.willow.android.ultimate.gpuimage.core_render_filter.image_enhance_filter.filter_2_pass_sampling;

import android.opengl.GLES20;

import cn.co.willow.android.ultimate.gpuimage.core_render_filter.GPUImageFilter;
import cn.co.willow.android.ultimate.gpuimage.core_render_filter.GPUImageFilterGroup;

/**
 * 双通纹理采样滤镜，双通滤波常用于高斯模糊等需要横纵向双重过滤的滤镜
 * two-pass filter, whitch is always use in some filter that needs x and y two way to deal in a single task.
 */
public class GPUImageTwoPassTextureSamplingFilter extends GPUImageFilterGroup {
    public GPUImageTwoPassTextureSamplingFilter(String firstVertexShader, String firstFragmentShader,
                                                String secondVertexShader, String secondFragmentShader) {
        super(null);
        addFilter(new GPUImageFilter(firstVertexShader, firstFragmentShader));
        addFilter(new GPUImageFilter(secondVertexShader, secondFragmentShader));
    }

    @Override
    public void onInit() {
        super.onInit();
        initTexelOffsets();
    }

    @Override
    public void onOutputSizeChanged(int width, int height) {
        super.onOutputSizeChanged(width, height);
        initTexelOffsets();
    }

    protected void initTexelOffsets() {
        float ratio = getHorizontalTexelOffsetRatio();
        GPUImageFilter filter = mFilters.get(0);
        int texelWidthOffsetLocation = GLES20.glGetUniformLocation(filter.getProgram(), "texelWidthOffset");
        int texelHeightOffsetLocation = GLES20.glGetUniformLocation(filter.getProgram(), "texelHeightOffset");
        filter.setFloat(texelWidthOffsetLocation, ratio / mOutputWidth);
        filter.setFloat(texelHeightOffsetLocation, 0);

        ratio = getVerticalTexelOffsetRatio();
        filter = mFilters.get(1);
        texelWidthOffsetLocation = GLES20.glGetUniformLocation(filter.getProgram(), "texelWidthOffset");
        texelHeightOffsetLocation = GLES20.glGetUniformLocation(filter.getProgram(), "texelHeightOffset");
        filter.setFloat(texelWidthOffsetLocation, 0);
        filter.setFloat(texelHeightOffsetLocation, ratio / mOutputHeight);
    }

    public float getVerticalTexelOffsetRatio() {
        return 1f;
    }

    public float getHorizontalTexelOffsetRatio() {
        return 1f;
    }
}
