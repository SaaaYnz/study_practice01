package com.example.sp01;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Base64;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent i = hasValidToken()
                        ? new Intent(MainActivity.this, SplashActivity.class)
                        : new Intent(MainActivity.this, RegistartionActivity.class);
                startActivity(i);
                finish();
            }
        }, 2000);
    }

    private boolean hasValidToken() {
        String token = getSharedPreferences("auth", MODE_PRIVATE)
                .getString("access_token", null);
        if (TextUtils.isEmpty(token)) {
            return false;
        }

        String[] parts = token.split("\\.");
        if (parts.length < 2) {
            return false;
        }

        try {
            byte[] decoded = Base64.decode(parts[1], Base64.URL_SAFE | Base64.NO_WRAP | Base64.NO_PADDING);
            String payload = new String(decoded, StandardCharsets.UTF_8);
            JSONObject json = new JSONObject(payload);

            if (!json.has("exp")) {
                return true;
            }

            long expSeconds = json.getLong("exp");
            long nowSeconds = System.currentTimeMillis() / 1000L;
            return expSeconds > nowSeconds;
        } catch (IllegalArgumentException | JSONException e) {
            return false;
        }
    }
}
