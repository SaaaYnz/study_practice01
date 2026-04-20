package com.example.sp01;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class RegistartionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registartion);
    }

    public void onContinue(View view) {
        Intent intent = new Intent(RegistartionActivity.this, CaptchaActivity.class);
        startActivity(intent);
    }

    public void onRegistration(View view) {
        Intent intent = new Intent(RegistartionActivity.this, CardActivity.class);
        startActivity(intent);
    }
}