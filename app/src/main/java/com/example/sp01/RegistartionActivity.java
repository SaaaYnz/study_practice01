package com.example.sp01;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Patterns;
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

public class RegistartionActivity extends AppCompatActivity {

    private EditText etEmail;
    private EditText etPassword;
    private View btnContinue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registartion);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnContinue = findViewById(R.id.btnContinue);

        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                updateContinueState();
            }
        };

        etEmail.addTextChangedListener(watcher);
        etPassword.addTextChangedListener(watcher);
        updateContinueState();
    }

    public void onContinue(View view) {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Введите корректный email", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 8) {
            Toast.makeText(this, "Пароль должен быть не короче 8 символов", Toast.LENGTH_SHORT).show();
            return;
        }

        JsonObject body = new JsonObject();
        body.addProperty("email", email);

        ApiClient.getApiService().authOtp(body).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (!response.isSuccessful()) {
                    String message = "Ошибка запроса OTP: " + response.code();
                    try {
                        if (response.errorBody() != null) {
                            message += "\n" + response.errorBody().string();
                        }
                    } catch (IOException ignored) {
                    }
                    Toast.makeText(RegistartionActivity.this, message, Toast.LENGTH_LONG).show();
                    return;
                }

                JsonObject json = response.body();
                if (json == null) {
                    Toast.makeText(RegistartionActivity.this, "Пустой ответ сервера", Toast.LENGTH_SHORT).show();
                    return;
                }

                String otpId = null;
                if (json.has("otpId") && !json.get("otpId").isJsonNull()) {
                    otpId = json.get("otpId").getAsString();
                } else if (json.has("data") && json.get("data").isJsonObject()) {
                    JsonObject data = json.getAsJsonObject("data");
                    if (data.has("otpId") && !data.get("otpId").isJsonNull()) {
                        otpId = data.get("otpId").getAsString();
                    }
                }

                if (TextUtils.isEmpty(otpId)) {
                    Toast.makeText(RegistartionActivity.this, "Сервер не вернул otpId", Toast.LENGTH_LONG).show();
                    return;
                }

                Intent intent = new Intent(RegistartionActivity.this, CaptchaActivity.class);
                intent.putExtra("otp_id", otpId);
                intent.putExtra("email", email);
                startActivity(intent);
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Toast.makeText(RegistartionActivity.this, "Ошибка сети: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void onRegistration(View view) {
        Intent intent = new Intent(RegistartionActivity.this, CardActivity.class);
        startActivity(intent);
    }

    private void updateContinueState() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        boolean isEnabled = Patterns.EMAIL_ADDRESS.matcher(email).matches() && password.length() >= 8;
        btnContinue.setEnabled(isEnabled);
    }
}