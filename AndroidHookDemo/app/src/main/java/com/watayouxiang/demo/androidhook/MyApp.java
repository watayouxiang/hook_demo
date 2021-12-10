package com.watayouxiang.demo.androidhook;

import android.app.Application;

import com.watayouxiang.demo.androidhook.hook.HookUtils;

/**
 * <pre>
 *     author : TaoWang
 *     e-mail : watayouxiang@qq.com
 *     time   : 2021/12/10
 *     desc   :
 * </pre>
 */
public class MyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        HookUtils hookUtils = new HookUtils(this, ProxyActivity.class);
        try {
            hookUtils.hookAms();
            hookUtils.hookSystemHandler();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
