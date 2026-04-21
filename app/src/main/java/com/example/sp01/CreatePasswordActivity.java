package com.example.sp01;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class CreatePasswordActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_password);
    }

    public void onClickNext(View view) {
        Intent intent = new Intent(CreatePasswordActivity.this, RegistrartionCaptchaActivity.class);
        startActivity(intent);
    }
}