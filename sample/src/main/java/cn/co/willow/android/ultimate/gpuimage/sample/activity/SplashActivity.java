package cn.co.willow.android.ultimate.gpuimage.sample.activity;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;


import cn.co.willow.android.ultimate.gpuimage.sample.R;

/**
 * @author willow.li
 */
public class SplashActivity extends AppCompatActivity implements View.OnClickListener {

    private ViewGroup mCPVirtualCont;
    private Button mBtnJump2Video;
    private Button mBtnJump2Faces;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        initView();

        // set page animator
        initPageAnimator();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }


    /*init page UI & Logic==========================================================================*/
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void initView() {
        mCPVirtualCont = findViewById(R.id.control_pannel);
        mBtnJump2Video = findViewById(R.id.btn_video_recorder);
        mBtnJump2Faces = findViewById(R.id.btn_face_detector);
        mBtnJump2Video.setOnClickListener(this);
        mBtnJump2Faces.setOnClickListener(this);
    }

    private void initPageAnimator() {
        Transition shareTrans = TransitionInflater.from(this).inflateTransition(R.transition.trans_control_pannel);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_video_recorder:
                Intent intentRec = new Intent(this, SampleVideoActivity.class);
                ActivityOptionsCompat optionsCompatRec = ActivityOptionsCompat.makeSceneTransitionAnimation(this,
                        new Pair<View, String>(mBtnJump2Video, getString(R.string.trans_control_pannel)));
                startActivity(intentRec, optionsCompatRec.toBundle());
                break;
            case R.id.btn_face_detector:
                Intent intentDec = new Intent(this, FaceDetectorDemoActivity.class);
                ActivityOptionsCompat optionsCompatDec = ActivityOptionsCompat.makeSceneTransitionAnimation(this,
                        new Pair<View, String>(mBtnJump2Faces, getString(R.string.trans_control_pannel)));
                startActivity(intentDec, optionsCompatDec.toBundle());
                break;
        }
    }
}
