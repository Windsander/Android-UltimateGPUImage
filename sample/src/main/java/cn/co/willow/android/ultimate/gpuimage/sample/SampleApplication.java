package cn.co.willow.android.ultimate.gpuimage.sample;

import android.content.Context;
import android.os.Handler;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;

import cn.co.willow.android.face.FaceDetectorManager;

/**
 * App实际对象类
 */
public class SampleApplication extends MultiDexApplication {

    private static SampleApplication mApplicationContext;               // 主线程的上下文
    private static Handler mMainThreadsHandler;               // 主线程的Handler
    private static Thread mMainThread;                       // 主线程
    private static int mMainThreadId;                     // 主线程id

    /*变量获取======================================================================================*/

    /**
     * 获得应用上下文
     */
    public static SampleApplication getApplication() {
        return mApplicationContext;
    }

    /**
     * 获得主线程Handler
     */
    public static Handler getMainThreadHandler() {
        return mMainThreadsHandler;
    }

    /**
     * 获得主线程
     */
    public static Thread getMainThread() {
        return mMainThread;
    }

    /**
     * 获得主线程id
     */
    public static int getMainThreadId() {
        return mMainThreadId;
    }


    /*生命周期======================================================================================*/
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        // 堆栈分析
      /*  if (LeakCanary.isInAnalyzerProcess(this)) return;
        LeakCanary.install(this);*/
        // 基本数据初始化
        this.mApplicationContext = this;
        this.mMainThreadsHandler = new Handler();
        this.mMainThread = Thread.currentThread();
        this.mMainThreadId = android.os.Process.myTid();
        super.onCreate();
        // 网络请求提供者

        FaceDetectorManager.register(this);
    }

}
