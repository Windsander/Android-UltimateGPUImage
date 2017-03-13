package cn.co.willow.android.ultimate.gpuimage.sample.function_holder;

import android.app.Activity;
import android.view.View;

/**
 * 模块基类
 * <p/>
 * Created by willow.li on 16/7/24.
 */
public abstract class BaseHolder<T> {
    /*关键变量=======================================================================================*/
    protected Activity context;                     // 上下文
    protected View view;                                // 视图
    protected T data;                                   // 数据

    public BaseHolder(Activity context) {
        this.context = context;
    }


    /*初始化方法======================================================================================*/

    /** 构造函数通用部分 */
    protected void initWhenConstruct() {
        init();
        view = initView();
    }

    /** 初始化视图之前的准备 */
    public void init() {
    }

    /** 初始化视图 */
    public abstract View initView();

    /** 销毁时清空资源 */
    protected abstract void clearAllResource();

    /*数据加载=======================================================================================*/

    /** 设置数据 */
    public void setData(T data) {
        if (context instanceof Activity && ((Activity) context).isFinishing()) return;
        this.data = data;
        refreshView();
    }

    /** 刷新视图 */
    public void refreshView() {

    }


    /*对外暴露方法====================================================================================*/

    /** 获得所属的持有者View */
    public View getRootView() {
        return view;
    }
}
