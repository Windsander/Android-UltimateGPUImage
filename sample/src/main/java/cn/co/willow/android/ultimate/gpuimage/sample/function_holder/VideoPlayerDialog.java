package cn.co.willow.android.ultimate.gpuimage.sample.function_holder;

import android.app.AlertDialog;
import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.view.View;
import android.view.WindowManager;

import cn.co.willow.android.ultimate.gpuimage.sample.R;
import cn.co.willow.android.ultimate.gpuimage.sample.util.UIUtils;
import cn.co.willow.android.ultimate.gpuimage.ui.ScalePlayerView;

/**
 * 对话框：确认对话框-标准式（包括单纯确认、确认取消）
 * <p>
 * Created by willow.li on 17/1/22.
 */
public class VideoPlayerDialog extends AlertDialog {

    /*关键参数======================================================================================*/
    private Context mContext;
    private View mDialogsView;
    private ScalePlayerView mVideoPlayer;

    public VideoPlayerDialog(Context context) {
        super(context);
        mContext = context;
        init();
    }


    /*初始化========================================================================================*/
    private void init() {
        mDialogsView = View.inflate(mContext, R.layout.holder_video_player, null);
        mVideoPlayer = (ScalePlayerView) mDialogsView.findViewById(R.id.spv_video_player);
    }

    public VideoPlayerDialog setIsCancelable(boolean cancelable) {
        setCancelable(cancelable);
        return this;
    }

    public VideoPlayerDialog setVideoUrl(String url) {
        startsPlay(url);
        return this;
    }


     /*视频播放逻辑====================================================================================*/
    /** 初始化播放器 */
    private void initPlayer() {
        mVideoPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.start();
            }
        });
        mVideoPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                onPlayFinish();
            }
        });
        mVideoPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                onPlayFinish();
                return true;
            }
        });
    }

    /** 播放视频 */
    private void startsPlay(String url) {
        mVideoPlayer.setVideoURI(Uri.parse(url));
    }

    private void onPlayFinish() {
        dismiss();
    }


    /*对外暴露方法==================================================================================*/
    @Override
    public void show() {
        super.show();
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.height = (int) (UIUtils.getScreenHeight() * 0.6f);
        params.width = (int) (UIUtils.getScreenWidth() * 0.8f);
        getWindow().setAttributes(params);
        getWindow().setBackgroundDrawable(UIUtils.getDrawable(R.drawable.rectangle_transparent));
        setContentView(mDialogsView);
        mVideoPlayer.setZOrderOnTop(true);
        mVideoPlayer.start();
    }

    public static VideoPlayerDialog getDefault(Context context) {
        return new VideoPlayerDialog(context)
                .setIsCancelable(true);
    }
}
