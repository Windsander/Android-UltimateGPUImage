package cn.co.willow.android.ultimate.gpuimage.core_render_filter.recommend_effect_filter_group;

import cn.co.willow.android.ultimate.gpuimage.core_render_filter.GPUImageFilterGroup;
import cn.co.willow.android.ultimate.gpuimage.core_render_filter.base_filter.GPUImageGrayscaleFilter;
import cn.co.willow.android.ultimate.gpuimage.core_render_filter.image_enhance_filter.filter_3x3_sampling.GPUImageSobelThresholdFilter;

/**
 * 边缘检测滤镜，本滤镜为三重索贝尔算子算法结合灰度滤镜组成
 * 提供较普通索贝尔来说更严格的边缘检测
 * Applies sobel edge detection on the image.
 */
public class GPUImageThresholdEdgeDetection extends GPUImageFilterGroup {
    public GPUImageThresholdEdgeDetection() {
        super();
        addFilter(new GPUImageGrayscaleFilter());
        addFilter(new GPUImageSobelThresholdFilter());
    }

    public void setLineSize(final float size) {
        ((GPUImageSobelThresholdFilter) getFilters().get(1)).setLineSize(size);
    }

    public void setThreshold(final float threshold) {
        ((GPUImageSobelThresholdFilter) getFilters().get(1)).setThreshold(threshold);
    }
}
