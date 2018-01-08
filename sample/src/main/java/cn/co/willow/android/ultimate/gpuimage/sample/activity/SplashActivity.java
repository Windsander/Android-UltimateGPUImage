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
    private Button    mBtnJump2Video;

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
        mCPVirtualCont = (ViewGroup) findViewById(R.id.control_pannel);
        mBtnJump2Video = (Button) findViewById(R.id.btn_video_recorder);
        mBtnJump2Video.setOnClickListener(this);
    }

    private void initPageAnimator() {
        Transition shareTrans = TransitionInflater.from(this).inflateTransition(R.transition.trans_control_pannel);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_video_recorder:
                Intent intent = new Intent(this, SampleVideoActivity.class);
                ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(this,
                        new Pair<View, String>(mBtnJump2Video, "trans_control_pannel"));
                startActivity(intent, optionsCompat.toBundle());
                break;
        }
    }
}
