package cn.co.willow.android.ultimate.gpuimage.manager_pure_image.image_loader;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import cn.co.willow.android.ultimate.gpuimage.core_render.PureImageRenderer;

/**
 * get image with uri
 * 根据传入 uri 来获取图片
 * <p>
 * Created by willow.li on 2017/4/22.
 */
public class UriImageLoader extends BaseImageLoader {

    private final Uri mUri;

    public UriImageLoader(Context context, final PureImageRenderer mRenderer,  Uri uri) {
        super(context,mRenderer);
        mUri = uri;
    }

    @Override
    protected Bitmap decode(BitmapFactory.Options options) {
        try {
            InputStream inputStream;
            if (mUri.getScheme().startsWith("http") || mUri.getScheme().startsWith("https")) {
                inputStream = new URL(mUri.toString()).openStream();
            } else {
                inputStream = context.getContentResolver().openInputStream(mUri);
            }
            return BitmapFactory.decodeStream(inputStream, null, options);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected int getImageOrientation() throws IOException {
        Cursor cursor = context.getContentResolver().query(mUri,
                new String[]{MediaStore.Images.ImageColumns.ORIENTATION}, null, null, null);

        if (cursor == null || cursor.getCount() != 1) {
            return 0;
        }

        cursor.moveToFirst();
        int orientation = cursor.getInt(0);
        cursor.close();
        return orientation;
    }
}
