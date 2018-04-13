package cn.co.willow.android.ultimate.gpuimage.core_render_filter.recommend_effect_filter_group;

import cn.co.willow.android.ultimate.gpuimage.core_render_filter.GPUImageFilterGroup;
import cn.co.willow.android.ultimate.gpuimage.core_render_filter.filter_dual_sampling.GPUImageGaussianBlurFilter;
import cn.co.willow.android.ultimate.gpuimage.core_render_filter.recommend_effect_filter.GPUImageCartoonFilter;

/**
 * 柔化的卡通效果滤镜，本滤镜为卡通滤镜结合高斯模糊滤镜组成
 * This uses a similar process as the GPUImageCartoonFilter, only it precedes the toon effect
 * with a Gaussian blur to smooth out noise.
 */
public class GPUImageSmoothCartoonFilter extends GPUImageFilterGroup {
    GPUImageGaussianBlurFilter blurFilter;
    GPUImageCartoonFilter toonFilter;

    /**
     * Setup and Tear down
     */
    public GPUImageSmoothCartoonFilter() {
        // First pass: apply a variable Gaussian blur
        blurFilter = new GPUImageGaussianBlurFilter();
        addFilter(blurFilter);

        // Second pass: run the Sobel edge detection on this blurred image, along with a posterization effect
        toonFilter = new GPUImageCartoonFilter();
        addFilter(toonFilter);

        getFilters().add(blurFilter);

        setBlurSize(0.5f);
        setThreshold(0.2f);
        setQuantizationLevels(10.0f);
    }

    /**
     * Accessors
     */
    public void setTexelWidth(float value) {
        toonFilter.setTexelWidth(value);
    }

    public void setTexelHeight(float value) {
        toonFilter.setTexelHeight(value);
    }

    public void setBlurSize(float value) {
        blurFilter.setBlurSize(value);
    }

    public void setThreshold(float value) {
        toonFilter.setThreshold(value);
    }

    public void setQuantizationLevels(float value) {
        toonFilter.setQuantizationLevels(value);
    }

}
