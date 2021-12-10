package com.watayouxiang.demo.androidhook;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void jump1(View view) {
        Intent intent = new Intent(this, TestActivityOne.class);
        startActivity(intent);
    }

    public void jump2(View view) {
        Intent intent = new Intent(this, TestActivityTwo.class);
        startActivity(intent);
    }

    public void logout(View view) {
        SharedPreferences share = this.getSharedPreferences("test", MODE_PRIVATE);
        SharedPreferences.Editor editor = share.edit();
        editor.putBoolean("login",false);
        editor.commit();
        Toast.makeText(this, "退出登录成功", Toast.LENGTH_SHORT).show();
    }
}