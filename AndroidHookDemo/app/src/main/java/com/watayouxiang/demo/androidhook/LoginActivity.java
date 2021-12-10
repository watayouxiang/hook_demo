package com.watayouxiang.demo.androidhook;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

/**
 * Created by Administrator on 2018/2/26 0026.
 */
public class LoginActivity extends Activity {
    EditText name;
    EditText password;
    private String className;
    SharedPreferences share;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        name = (EditText) findViewById(R.id.name);
        password = (EditText) findViewById(R.id.password);

        share = this.getSharedPreferences("test", MODE_PRIVATE);

        className = getIntent().getStringExtra("extraIntent");
        if (className != null) {
            ((TextView) findViewById(R.id.text)).setText(" 跳转界面：" + className);
        }
    }

    public void login(View view) {
        if ((name.getText() == null || password.getText() == null)) {
            Toast.makeText(this, "请填写用户名或密码", Toast.LENGTH_SHORT).show();
            return;
        }
        if ("test".equals(name.getText().toString()) && "123456".equals(password.getText()
                .toString())) {
            SharedPreferences.Editor editor = share.edit();
            editor.putString("name", name.getText().toString());
            editor.putString("sex", password.getText().toString());
            editor.putBoolean("login", true);
            editor.commit();

            Toast.makeText(this, "登录成功", Toast.LENGTH_SHORT).show();

            if (className != null) {
                ComponentName componentName = new ComponentName(this, className);
                Intent intent = new Intent();
                intent.setComponent(componentName);
                startActivity(intent);
            }
            finish();
        } else {
            SharedPreferences.Editor editor = share.edit();
            editor.putBoolean("login", false);   //设置保存的数据
            editor.commit();

            Toast.makeText(this, "登录失败", Toast.LENGTH_SHORT).show();
        }
    }
}
