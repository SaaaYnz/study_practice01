package com.example.sp01;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.sp01.api.ApiClient;
import com.google.gson.JsonObject;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CaptchaActivity extends AppCompatActivity {

    private EditText etOtpCode;
    private String otpId;
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_captcha);

        etOtpCode = findViewById(R.id.etOtpCode);
        otpId = getIntent().getStringExtra("otp_id");
        email = getIntent().getStringExtra("email");
    }

    public void onContinue(View view) {
        String enteredOtpCode = etOtpCode.getText().toString().trim();

        if (TextUtils.isEmpty(enteredOtpCode)) {
            Toast.makeText(this, "Введите OTP-код", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(otpId)) {
            Toast.makeText(this, "otpId не найден. Запросите OTP заново", Toast.LENGTH_LONG).show();
            return;
        }

        JsonObject body = new JsonObject();
        body.addProperty("otpId", otpId);
        body.addProperty("password", enteredOtpCode);

        ApiClient.getApiService().verifyOtp(body).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (!response.isSuccessful()) {
                    String message = "Неверный OTP: " + response.code();
                    try {
                        if (response.errorBody() != null) {
                            message += "\n" + response.errorBody().string();
                        }
                    } catch (IOException ignored) {
                    }
                    Toast.makeText(CaptchaActivity.this, message, Toast.LENGTH_LONG).show();
                    return;
                }

                JsonObject json = response.body();
                if (json == null) {
                    Toast.makeText(CaptchaActivity.this, "Пустой ответ сервера", Toast.LENGTH_SHORT).show();
                    return;
                }

                String accessToken = null;
                if (json.has("token") && !json.get("token").isJsonNull()) {
                    accessToken = json.get("token").getAsString();
                } else if (json.has("accessToken") && !json.get("accessToken").isJsonNull()) {
                    accessToken = json.get("accessToken").getAsString();
                }

                if (!TextUtils.isEmpty(accessToken)) {
                    String userName = "Пользователь";
                    String userId = null;
                    if (json.has("record") && json.get("record").isJsonObject()) {
                        JsonObject record = json.getAsJsonObject("record");
                        if (record.has("id") && !record.get("id").isJsonNull()) {
                            userId = record.get("id").getAsString();
                        }
                        if (record.has("name") && !record.get("name").isJsonNull()) {
                            String nameFromServer = record.get("name").getAsString();
                            if (!TextUtils.isEmpty(nameFromServer)) {
                                userName = nameFromServer;
                            }
                        }
                    }

                    AuthSession.saveAuth(CaptchaActivity.this, accessToken, email, userName, userId);
                }

                startActivity(new Intent(CaptchaActivity.this, SplashActivity.class));
                finishAffinity();
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Toast.makeText(CaptchaActivity.this, "Ошибка сети: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
