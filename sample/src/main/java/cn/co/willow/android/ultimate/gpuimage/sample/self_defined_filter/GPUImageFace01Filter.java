package cn.co.willow.android.ultimate.gpuimage.sample.self_defined_filter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import cn.co.willow.android.ultimate.gpuimage.core_render_filter.blend_mode_filter.color_mapping_blend.GPUImageLookupFilter;
import cn.co.willow.android.ultimate.gpuimage.sample.R;
import cn.co.willow.android.ultimate.gpuimage.sample.SampleApplication;

/**
 * Created by willow.li on 16/11/9.
 */
public class GPUImageFace01Filter extends GPUImageLookupFilter {

    public GPUImageFace01Filter() {
        super();
        Bitmap bitmap = BitmapFactory.decodeResource(SampleApplication.getApplication().getResources(), R.drawable.filter_face01);
        setBitmap(bitmap);
    }

}
