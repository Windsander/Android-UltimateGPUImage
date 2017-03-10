package cn.co.willow.android.ultimate.gpuimage.sample.self_defined_filter;

import cn.co.willow.android.ultimate.gpuimage.core_render_filter.GPUImageFilter;
import cn.co.willow.android.ultimate.gpuimage.core_render_filter.color_enhance_filter.GPUImageColorInvertFilter;
import cn.co.willow.android.ultimate.gpuimage.core_render_filter.image_enhance_filter.filter_3x3_sampling.GPUImageSobelThresholdFilter;
import cn.co.willow.android.ultimate.gpuimage.core_render_filter.recommend_effect_filter.GPUImageHalftoneFilter;
import cn.co.willow.android.ultimate.gpuimage.core_render_filter.recommend_effect_filter.GPUImageHazeFilter;
import cn.co.willow.android.ultimate.gpuimage.core_render_filter.recommend_effect_filter.GPUImageKuwaharaFilter;
import cn.co.willow.android.ultimate.gpuimage.core_render_filter.recommend_effect_filter.GPUImagePixelationFilter;
import cn.co.willow.android.ultimate.gpuimage.core_render_filter.recommend_effect_filter.GPUImageSepiaFilter;
import cn.co.willow.android.ultimate.gpuimage.core_render_filter.recommend_effect_filter_group.GPUImageSketchFilter;
import cn.co.willow.android.ultimate.gpuimage.core_render_filter.recommend_effect_filter_group.GPUImageSmoothCartoonFilter;
import cn.co.willow.android.ultimate.gpuimage.core_render_filter.recommend_effect_filter_group.GPUImageSobelEdgeDetection;
import cn.co.willow.android.ultimate.gpuimage.core_render_filter.recommend_effect_filter_group.GPUImageThresholdEdgeDetection;

public class FilterType {
    private static GPUImageFilter lookupFilter1 = new GPUImageColorInvertFilter();          // single
    private static GPUImageFilter lookupFilter2 = new GPUImageSketchFilter();               // group    bug     3x3
    private static GPUImageFilter lookupFilter3 = new GPUImageSmoothCartoonFilter();        // group    bug     3x3
    private static GPUImageFilter lookupFilter4 = new GPUImageSobelThresholdFilter();       // single   bug     3x3
    private static GPUImageFilter lookupFilter5 = new GPUImageSobelEdgeDetection();         // group    bug     3x3
    private static GPUImageFilter lookupFilter6 = new GPUImageThresholdEdgeDetection();     // group    bug     3x3
    private static GPUImageFilter lookupFilter7 = new GPUImageHalftoneFilter();             // single
    private static GPUImageFilter lookupFilter8 = new GPUImageHazeFilter();                 // single
    private static GPUImageFilter lookupFilter9 = new GPUImageKuwaharaFilter();             // single
    private static GPUImageFilter lookupFilter10 = new GPUImagePixelationFilter();          // single
    private static GPUImageFilter lookupFilter11 = new GPUImageSepiaFilter();               // single
    private static GPUImageFilter lookupFilter12 = new GPUImageFace01Filter();              // TwoInput bug     2input
    private static GPUImageFilter lookupFilter13 = new GPUImageFace02Filter();              // TwoInput bug     2input


    public enum Type {
        Original(0, "Origin 原图", "Original", new GPUImageFilter()),
        f1(1, "Hollow", "GaussianBlur", lookupFilter1),
        f2(2, "Sketch", "Sketch", lookupFilter2),
        f3(3, "Smooth Cartoon", "SmoothCartoon", lookupFilter3),
        f4(4, "Sobel Threshold", "SobelThreshold", lookupFilter4),
        f5(5, "Sobel Edge Detection", "SobelEdgeDetection", lookupFilter5),
        f6(6, "Threshold Edge Detection", "ThresholdEdgeDetection", lookupFilter6),
        f7(7, "Halftone", "Halftone", lookupFilter7),
        f8(8, "Haze", "Haze", lookupFilter8),
        f9(9, "Kuwahara", "Kuwahara", lookupFilter9),
        f10(10, "Pixelation", "Pixelation", lookupFilter10),
        f11(11, "Sepia", "Sepia", lookupFilter11),
        f12(12, "lookup-postcard", "Face01", lookupFilter12),
        f13(13, "lookup-classical", "Face02", lookupFilter13);
        int index;
        String name;
        String tag;
        GPUImageFilter filter;

        public static Type getFilter(int index) {
            Type[] values = values();
            for (Type f : values) {
                if (f.index == index) {
                    return f;
                }
            }
            return Original;
        }

        Type(int index, String name, String tag, GPUImageFilter filter) {
            this.name = name;
            this.tag = tag;
            this.index = index;
            this.filter = filter;
        }

        public GPUImageFilter getFilter() {
            return filter;
        }

        public int getIndex() {
            return index;
        }

        public String getName() {
            return name;
        }

        public static int getTypeSize() {
            return values().length - 1;
        }
    }
}