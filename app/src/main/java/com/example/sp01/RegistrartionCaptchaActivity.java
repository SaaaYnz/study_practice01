package com.example.sp01;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class RegistrartionCaptchaActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registartion_captcha);
    }

    public void onContinue(View view) {
        Intent intent = new Intent(RegistrartionCaptchaActivity.this, SplashActivity.class);
        startActivity(intent);
    }
}