package cn.co.willow.android.ultimate.gpuimage.sample.interaction_logic;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;

import cn.co.willow.android.ultimate.gpuimage.sample.function_holder.VideoPlayerDialog;
import cn.co.willow.android.ultimate.gpuimage.sample.util.FileUtil;
import cn.co.willow.android.ultimate.gpuimage.utils.CameraUtil;
import cn.co.willow.android.ultimate.gpuimage.utils.LogUtil;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by willow.li on 2017/3/13.
 */
public class SamplePresenter {

    private static SamplePresenter mInstance;

    private SamplePresenter(onCoverListener mOnCoverListener) {
        this.mOnCoverListener = mOnCoverListener;
    }

    public static SamplePresenter init(onCoverListener mOnCoverListener) {
        if (mInstance == null) {
            synchronized (SamplePresenter.class) {
                if (mInstance == null) {
                    mInstance = new SamplePresenter(mOnCoverListener);
                }
            }
        }
        return mInstance;
    }


    /*play video logic==============================================================================*/

    /**
     * play the lastest video
     */
    public void doPlayerVideo(Context context) {
        if (mLastVideoFile == null) return;
        VideoPlayerDialog.getDefault(context)
                .setVideoUrl(Uri.fromFile(mLastVideoFile).toString())
                .show();
    }

    /*record finish logic===========================================================================*/
    private File mLastVideoFile;

    public File getLastVideoFile() {
        return mLastVideoFile;
    }

    /**
     * do update action
     */
    public void doUpdateVideoData(final File mOutputRecFile) {
        if (mOutputRecFile == null) return;
        mLastVideoFile = mOutputRecFile;
        Observable
                .create(new Observable.OnSubscribe<String>() {
                    @Override
                    public void call(final Subscriber<? super String> subscriber) {
                        String videoPath = mOutputRecFile.getAbsolutePath();
                        File imgFile = createCoverImg(videoPath);
                        if (imgFile != null) {
                            subscriber.onNext(Uri.fromFile(imgFile).toString());
                        }
                        File gifFile = createGifFile(videoPath);
                        if (gifFile != null) {
                            subscriber.onNext(Uri.fromFile(gifFile).toString());
                        }
                    }
                })
                .delay(10, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String gifUrl) {
                        LogUtil.w("mOnCoverListener is null?  " + (mOnCoverListener == null));
                        if (mOnCoverListener != null) {
                            mOnCoverListener.onCoverFinish(gifUrl);
                        }
                    }
                });
    }

    /**
     * create last video image
     */
    private File createCoverImg(String videoPath) {
        Bitmap bitmap = CameraUtil.getVideoThumbnailFF(videoPath);
        if (bitmap == null) return null;
        File gifFile = FileUtil.computeMD5ForImageFile(FileUtil.getCoverSavePath(), bitmap);
        bitmap.recycle();
        return gifFile;
    }

    /**
     * create gif
     */
    private File createGifFile(String videoPath) {
        List<Bitmap> bitmaps = CameraUtil.getVideoThumbnailFF(videoPath, 8);
        File gifFile = FileUtil.computeMD5ForCoverFile(FileUtil.getCoverSavePath());
        gifFile = CameraUtil.createGif(bitmaps, gifFile);
        bitmaps.clear();
        return gifFile;
    }

    /*listener======================================================================================*/
    private onCoverListener mOnCoverListener;

    public interface onCoverListener {
        void onCoverFinish(String gifUrl);
    }

    public void setOnCoverListener(onCoverListener mOnCoverListener) {
        if (mOnCoverListener != null) {
            this.mOnCoverListener = mOnCoverListener;
        }
    }
}
