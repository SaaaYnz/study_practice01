package com.example.sp01;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class CaptchaActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_captcha);
    }

    public void onContinue (View view) {
        Intent intent = new Intent(CaptchaActivity.this, SplashActivity.class);
        startActivity(intent);
    }
}