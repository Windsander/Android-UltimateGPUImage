package cn.co.willow.android.ultimate.gpuimage.sample.util;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.MainThread;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.lang.reflect.Method;
import java.util.List;

import cn.co.willow.android.ultimate.gpuimage.sample.SampleApplication;

/**
 * UI相关工具类集合
 * 功能包括：
 * 像素转换、主线程操作、资源获取、安全吐司、获取屏幕属性、小键盘操作、角标与虚拟键
 * <p>
 * Created by willow.li on 16/9/29.
 */
public class UIUtils {

    private static int screenHeight;
    private static int screenWidth;
    private static float screenDensity;
    private static int mAppVersion;
    private static String mAppVersionName;
    private static int dip10, dip1;

    /*像素转换======================================================================================*/
    public static int getDip10() {
        if (dip10 == 0) {
            dip10 = dip2px(10);
        }
        return dip10;
    }

    public static int getDip1() {
        if (dip1 == 0) {
            dip1 = dip2px(1);
        }
        return dip1;
    }

    /** dip转换px */
    public static int dip2px(int dip) {
        final float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) (dip * scale + 0.5f);
    }

    /** px转换dip */
    public static int px2dip(int px) {
        final float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) (px / scale + 0.5f);
    }

    /** sp值转换为px值 */
    public static int sp2px(float spValue) {
        final float fontScale = UIUtils.getContext().getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    /*主线程操作======================================================================================*/

    /** 获取当前App上下文（基础类的定义，直接使用Application方法） */
    public static SampleApplication getContext() {
        return SampleApplication.getApplication();
    }

    public static Thread getMainThread() {
        return SampleApplication.getMainThread();
    }

    public static long getMainThreadId() {
        return SampleApplication.getMainThreadId();
    }

    /** 判断当前主进程是否运行 */
    public static boolean isAppOnForeground() {
        ActivityManager activityManager = (ActivityManager) getContext().getSystemService(Context.ACTIVITY_SERVICE);
        String packageName = getContext().getPackageName();
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        if (appProcesses == null) return false;
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.processName.equals(packageName)
                    && appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                return true;
            }
        }
        return false;
    }

    /** 获取当前应用程序的版本号 */
    public static int getAppVersion() {
        if (mAppVersion == 0) {
            try {
                PackageInfo info = getContext().getPackageManager().getPackageInfo(
                        getContext().getPackageName(), 0);
                mAppVersion = info.versionCode;
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        return mAppVersion;
    }

    /** 获取当前应用程序的版本 */
    public static String getAppVersionName() {
        if (mAppVersionName == null || mAppVersionName.isEmpty()) {
            try {
                PackageInfo info = getContext().getPackageManager().getPackageInfo(
                        getContext().getPackageName(), 0);
                mAppVersionName = info.versionName;
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        return mAppVersionName;
    }

    /** 获取主线程的handler */
    public static Handler getHandler() {
        return SampleApplication.getMainThreadHandler();
    }

    /** 延时在主线程执行runnable */
    public static boolean postDelayed(Runnable runnable, long delayMillis) {
        if (runnable != null) {
            return getHandler().postDelayed(runnable, delayMillis);
        }
        return false;
    }

    /** 在主线程执行runnable */
    public static boolean post(Runnable runnable) {
        if (runnable != null) {
            return getHandler().post(runnable);
        }
        return false;
    }

    /** 从主线程looper里面移除runnable */
    public static void removeCallbacks(Runnable runnable) {
        if (runnable != null) {
            getHandler().removeCallbacks(runnable);
        }
    }

    /** 判断当前activity是否正在关闭 */
    public static boolean isActivityDestory(Context context) {
        if (context instanceof Activity) {
            if (((Activity) context).isFinishing() ||
                    (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && ((Activity) context).isDestroyed())) {
                return true;
            }
        }
        return false;
    }


    /*资源获取=======================================================================================*/

    /** 获取资源 */
    public static Resources getResources() {
        return getContext().getResources();
    }

    /** 获取文字 */
    public static String getString(int resId) {
        return getResources().getString(resId);
    }

    /** 获取文字 */
    public static String getString(int resId, Object... formatArg) {
        return getResources().getString(resId, formatArg);
    }

    /** 获取文字数组 */
    public static String[] getStringArray(int resId) {
        return getResources().getStringArray(resId);
    }

    /** 获取dimen */
    public static int getDimens(int resId) {
        return getResources().getDimensionPixelSize(resId);
    }

    /** 获取drawable */
    public static Drawable getDrawable(int resId) {
        return getResources().getDrawable(resId);
    }

    /** 获取颜色 */
    public static int getColor(int resId) {
        return getResources().getColor(resId);
    }

    /** 获取颜色选择器 */
    public static ColorStateList getColorStateList(int resId) {
        return getResources().getColorStateList(resId);
    }

    /** 判断当前的线程是不是在主线程 */
    public static boolean isRunInMainThread() {
        return android.os.Process.myTid() == getMainThreadId();
    }

    public static void runInMainThread(Runnable runnable) {
        if (isRunInMainThread()) {
            runnable.run();
        } else {
            post(runnable);
        }
    }

    /*安全吐司======================================================================================*/

    /** 对toast的简易封装。线程安全，可以在非UI线程调用。 */
    public static void showToastSafe(final int resId) {
        showToastSafe(getString(resId));
    }

    /** 对toast的简易封装。线程安全，可以在非UI线程调用。 */
    public static void showToastSafe(final String str) {
        if (isRunInMainThread()) {
            showToast(str);
        } else {
            post(new Runnable() {
                @Override
                public void run() {
                    showToast(str);
                }
            });
        }
    }

    private static void showToast(String str) {
        Toast.makeText(getContext(), str, Toast.LENGTH_SHORT).show();
    }

    /*获取屏幕属性==================================================================================*/

    /** 获取屏幕宽度 */
    public static int getScreenHeight() {
        if (screenHeight <= 0) {
            WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
            DisplayMetrics outMetrics = new DisplayMetrics();
            wm.getDefaultDisplay().getMetrics(outMetrics);
            screenHeight = outMetrics.heightPixels;
            screenWidth = outMetrics.widthPixels;
        }
        return screenHeight;
    }

    /** 获取屏幕高度 */
    public static int getScreenWidth() {
        if (screenWidth <= 0) {
            WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
            DisplayMetrics outMetrics = new DisplayMetrics();
            wm.getDefaultDisplay().getMetrics(outMetrics);
            screenHeight = outMetrics.heightPixels;
            screenWidth = outMetrics.widthPixels;
        }
        return screenWidth;
    }

    /** 获取屏幕像素密度 */
    public static float getScreenDensity() {
        if (screenDensity <= 0) {
            WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
            DisplayMetrics outMetrics = new DisplayMetrics();
            wm.getDefaultDisplay().getMetrics(outMetrics);
            screenHeight = outMetrics.heightPixels;
            screenWidth = outMetrics.widthPixels;
            screenDensity = outMetrics.density;
        }
        return screenDensity;
    }

    /*小键盘操作=====================================================================================*/

    /** 显示键盘 */
    public static void showIME(View v) {
        if (v == null) return;
        InputMethodManager imm = (InputMethodManager) UIUtils.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(v, InputMethodManager.SHOW_FORCED);
    }

    /** 隐藏键盘 */
    public static void hideIME(View v) {
        if (v == null) return;
        InputMethodManager imm = (InputMethodManager) UIUtils.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive()) imm.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
    }


    /*零碎操作======================================================================================*/

    /** 数组角标越界避免算法 */
    public static int checkPositionBound(int position, int totalSize) {
        return position < 0 ? 0 : (position + 1 > totalSize ? (totalSize > 0 ? totalSize - 1 : 0) : position);
    }

    /** 获取状态栏高度 */
    public static int getStatusBarHeight() {
        int resId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        int statusBarHeight = 0;
        if (resId > 0) {
            statusBarHeight = getResources().getDimensionPixelSize(resId);
        }
        if (statusBarHeight == 0) {
            try {
                Class<?> clazz = Class.forName("com.android.internal.R$dimen");
                Object object = clazz.newInstance();
                int height = Integer.parseInt(clazz.getField("status_bar_height")
                        .get(object).toString());
                statusBarHeight = getResources().getDimensionPixelSize(height);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return statusBarHeight;
    }

    /** 判断是否有虚拟按键 */
    public static boolean hasPermanentMenuKey(Context context) {
        return ViewConfiguration.get(context).hasPermanentMenuKey();
    }

    /** 判断是否有虚拟按键 */
    public static boolean checkDeviceHasNavigationBar() {
        boolean hasNavigationBar = false;
        boolean hasMenuKey = ViewConfiguration.get(getContext())
                .hasPermanentMenuKey();
        boolean hasBackKey = KeyCharacterMap
                .deviceHasKey(KeyEvent.KEYCODE_BACK);
        hasNavigationBar = !hasMenuKey && !hasBackKey;
        if (hasNavigationBar) return true;
        try {
            @SuppressWarnings("rawtypes")
            Class c = Class.forName("android.os.SystemProperties");
            @SuppressWarnings("unchecked")
            Method m = c.getMethod("get", String.class);
            String navBarOverride = (String) m.invoke(c, "qemu.hw.mainkeys");
            hasNavigationBar = "1".equals(navBarOverride);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (hasNavigationBar) return true;
        hasNavigationBar = (getVirtualBarHeigh() == 0);
        return hasNavigationBar;
    }

    /** 数据获取：获取虚拟功能键高度 */
    public static int getVirtualBarHeigh() {
        int vh = 0;
        WindowManager windowManager = (WindowManager) UIUtils.getContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        DisplayMetrics dm = new DisplayMetrics();
        try {
            @SuppressWarnings("rawtypes")
            Class c = Class.forName("android.view.Display");
            @SuppressWarnings("unchecked")
            Method method = c.getMethod("getRealMetrics", DisplayMetrics.class);
            method.invoke(display, dm);
            vh = dm.heightPixels - windowManager.getDefaultDisplay().getHeight();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return vh;
    }


    /*小键盘adjustResize系统bug的解决方案===========================================================*/
    /**
     * 原因：
     * AndroidManifest.xml的Activity设置属性：android:windowSoftInputMode = "adjustResize" ，
     * 软键盘弹出时，要对主窗口布局重新进行布局，并调用onSizeChanged方法，切记一点当我们设置
     * 为“adjustResize”时，我们的界面不要设置为全屏模式，否则设置了这个属性也不会有什么效
     * 果。而当我们设置android: windowSoftInputMode = "adjustPan"时，主窗口就不会调用
     * onSizeChanged方法，界面的一部分就会被软键盘覆盖住，就不会被挤到软键盘之上了。
     * <p>
     * 效果：
     * 手动解决布局重绘
     */
    private View mChildOfContent;
    private int usableHeightPrevious;
    private FrameLayout.LayoutParams frameLayoutParams;

    @MainThread
    protected void adjustResizeIME(Activity activity) {
        FrameLayout content = (FrameLayout) activity.findViewById(android.R.id.content);
        mChildOfContent = content.getChildAt(0);
        mChildOfContent.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            public void onGlobalLayout() {
                possiblyResizeChildOfContent();
            }
        });
        frameLayoutParams = (FrameLayout.LayoutParams) mChildOfContent.getLayoutParams();
    }

    private void possiblyResizeChildOfContent() {
        int usableHeightNow = computeUsableHeight();
        if (usableHeightNow != usableHeightPrevious) {
            int usableHeightSansKeyboard = mChildOfContent.getRootView().getHeight();
            int heightDifference = usableHeightSansKeyboard - usableHeightNow;
            if (heightDifference > (usableHeightSansKeyboard / 4)) {
                // keyboard probably just became visible
                frameLayoutParams.height = usableHeightSansKeyboard - heightDifference;
            } else {
                // keyboard probably just became hidden
                frameLayoutParams.height = usableHeightSansKeyboard;
            }
            mChildOfContent.requestLayout();
            usableHeightPrevious = usableHeightNow;
        }
    }

    private int computeUsableHeight() {
        Rect r = new Rect();
        mChildOfContent.getWindowVisibleDisplayFrame(r);
        if (r.top == 0) {
            r.top = 0;//状态栏目的高度
        }
        return (r.bottom - r.top);
    }


}





