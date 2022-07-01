package com.watayouxiang.demo.androidhook.hook;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.watayouxiang.demo.androidhook.LoginActivity;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;

/**
 * <pre>
 *     author : TaoWang
 *     e-mail : watayouxiang@qq.com
 *     time   : 2021/12/10
 *     desc   : 只有运行在9.0的系统才有效
 * </pre>
 */
public class HookUtils {
    private final Context context;
    private final Class<?> proxyActivity;

    public HookUtils(Context context, Class<?> proxyActivity) {
        this.context = context;
        this.proxyActivity = proxyActivity;
    }

    // ====================================================================================
    // hook 修改 startActivity
    // ====================================================================================

    public void hookAms() throws Exception {
        // 1、拿到 Singleton<IActivityManager> IActivityManagerSingleton
        Class ActivityManagerClz = Class.forName("android.app.ActivityManager");
        Field IActivityManagerSingletonFiled = ActivityManagerClz.getDeclaredField("IActivityManagerSingleton");
        IActivityManagerSingletonFiled.setAccessible(true);
        Object IActivityManagerSingletonObj = IActivityManagerSingletonFiled.get(null);

        // 2、拿到 IActivityManager
        // /frameworks/base/core/java/android/app/IActivityManager.aidl
        Class SingletonClz = Class.forName("android.util.Singleton");
        Field mInstanceField = SingletonClz.getDeclaredField("mInstance");
        mInstanceField.setAccessible(true);
        Object IActivityManagerObj = mInstanceField.get(IActivityManagerSingletonObj);
        Class IActivityManagerClz = Class.forName("android.app.IActivityManager");

        // 3、动态代理 IActivityManager
        Object proxyIActivityManager = Proxy.newProxyInstance(
                // 类加载器
                Thread.currentThread().getContextClassLoader(),
                // 需要被代理的类
                new Class[]{IActivityManagerClz},
                // 代理
                new AmsInvocationHandler(IActivityManagerObj)
        );
        mInstanceField.set(IActivityManagerSingletonObj, proxyIActivityManager);
    }

    private class AmsInvocationHandler implements InvocationHandler {
        private final Object iActivityManagerObject;

        public AmsInvocationHandler(Object iActivityManagerObject) {
            this.iActivityManagerObject = iActivityManagerObject;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if ("startActivity".contains(method.getName())) {
                Intent intent = null;
                int index = 0;
                for (int i = 0; i < args.length; i++) {
                    Object arg = args[i];
                    if (arg instanceof Intent) {
                        intent = (Intent) args[i];
                        index = i;
                        break;
                    }
                }
                Intent proxyIntent = new Intent();
                ComponentName componentName = new ComponentName(context, proxyActivity);
                proxyIntent.setComponent(componentName);
                proxyIntent.putExtra("oldIntent", intent);
                args[index] = proxyIntent;
            }
            return method.invoke(iActivityManagerObject, args);
        }
    }

    // ====================================================================================
    // hook 修改 Handler
    // ====================================================================================

    public void hookSystemHandler() throws Exception {
        // 1、拿到 ActivityThread
        Class ActivityThreadClz = Class.forName("android.app.ActivityThread");
        Field field = ActivityThreadClz.getDeclaredField("sCurrentActivityThread");
        field.setAccessible(true);
        Object ActivityThreadObj = field.get(null);

        // 2、拿到系统 Handler
        Field mHField = ActivityThreadClz.getDeclaredField("mH");
        mHField.setAccessible(true);
        Handler mHObj = (Handler) mHField.get(ActivityThreadObj);

        // 3、修改 Handler 的 mCallback
        Field mCallbackField = Handler.class.getDeclaredField("mCallback");
        mCallbackField.setAccessible(true);
        ProxyHandlerCallback proxyMHCallback = new ProxyHandlerCallback();
        mCallbackField.set(mHObj, proxyMHCallback);

    }

    private class ProxyHandlerCallback implements Handler.Callback {
        private int EXECUTE_TRANSACTION = 159;

        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == EXECUTE_TRANSACTION) {
                try {
                    // ClientTransaction
                    Class ClientTransactionClz = Class.forName("android.app.servertransaction.ClientTransaction");
                    if (!ClientTransactionClz.isInstance(msg.obj)) return false;

                    // ClientTransaction # List<ClientTransactionItem> mActivityCallbacks;
                    Field mActivityCallbacksField = ClientTransactionClz.getDeclaredField("mActivityCallbacks");
                    mActivityCallbacksField.setAccessible(true);
                    Object mActivityCallbacksObj = mActivityCallbacksField.get(msg.obj);

                    List list = (List) mActivityCallbacksObj;
                    if (list.size() == 0) return false;
                    Object LaunchActivityItemObj = list.get(0);

                    // LaunchActivityItem
                    Class LaunchActivityItemClz = Class.forName("android.app.servertransaction.LaunchActivityItem");
                    if (!LaunchActivityItemClz.isInstance(LaunchActivityItemObj)) return false;

                    Field mIntentField = LaunchActivityItemClz.getDeclaredField("mIntent");
                    mIntentField.setAccessible(true);
                    Intent mIntent = (Intent) mIntentField.get(LaunchActivityItemObj);
                    Intent realIntent = mIntent.getParcelableExtra("oldIntent");
                    if (realIntent != null) {
                        SharedPreferences preferences = context.getSharedPreferences("test", Context.MODE_PRIVATE);
                        if (preferences.getBoolean("login", false)) {
                            mIntent.setComponent(realIntent.getComponent());
                        } else {
                            mIntent.putExtra("extraIntent", realIntent.getComponent().getClassName());
                            ComponentName componentName = new ComponentName(context, LoginActivity.class);
                            mIntent.setComponent(componentName);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return false;
        }
    }
}
