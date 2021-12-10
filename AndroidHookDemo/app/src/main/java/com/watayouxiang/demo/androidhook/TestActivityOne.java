package com.watayouxiang.demo.androidhook;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.Nullable;

/**
 * <pre>
 *     author : TaoWang
 *     e-mail : watayouxiang@qq.com
 *     time   : 2021/12/10
 *     desc   :
 * </pre>
 */
public class TestActivityOne extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_activity_one);
    }
}
