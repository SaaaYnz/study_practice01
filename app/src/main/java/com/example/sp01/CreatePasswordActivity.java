package com.example.sp01;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
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

public class CreatePasswordActivity extends AppCompatActivity {

    private EditText etPassword;
    private EditText etPasswordConfirm;
    private String registerEmail;
    private View btnSavePassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_password);
        etPassword = findViewById(R.id.etRegisterPassword);
        etPasswordConfirm = findViewById(R.id.etRegisterPasswordConfirm);
        btnSavePassword = findViewById(R.id.btnSavePassword);
        registerEmail = getIntent().getStringExtra("register_email");

        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                updateSaveButtonState();
            }
        };

        etPassword.addTextChangedListener(watcher);
        etPasswordConfirm.addTextChangedListener(watcher);
        updateSaveButtonState();
    }

    public void onClickNext(View view) {
        String password = etPassword.getText().toString();
        String passwordConfirm = etPasswordConfirm.getText().toString();

        if (TextUtils.isEmpty(registerEmail)) {
            Toast.makeText(this, "Не найден email регистрации", Toast.LENGTH_LONG).show();
            return;
        }

        if (password.length() < 8 || passwordConfirm.length() < 8) {
            Toast.makeText(this, "Пароль должен быть не короче 8 символов", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(passwordConfirm)) {
            Toast.makeText(this, "Пароли не совпадают", Toast.LENGTH_SHORT).show();
            return;
        }

        String defaultUserName = buildDefaultName(registerEmail);

        JsonObject registerBody = new JsonObject();
        registerBody.addProperty("email", registerEmail);
        registerBody.addProperty("password", password);
        registerBody.addProperty("passwordConfirm", passwordConfirm);
        registerBody.addProperty("name", defaultUserName);

        ApiClient.getApiService().registerUser(registerBody).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (!response.isSuccessful()) {
                    showError("Ошибка регистрации: " + response.code(), response);
                    return;
                }

                JsonObject authBody = new JsonObject();
                authBody.addProperty("identity", registerEmail);
                authBody.addProperty("password", password);

                ApiClient.getApiService().authWithPassword(authBody).enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                        if (!response.isSuccessful() || response.body() == null) {
                            showError("Регистрация прошла, но вход не выполнен: " + response.code(), response);
                            return;
                        }

                        JsonObject json = response.body();
                        String token = null;
                        if (json.has("token") && !json.get("token").isJsonNull()) {
                            token = json.get("token").getAsString();
                        }

                        if (TextUtils.isEmpty(token)) {
                            Toast.makeText(CreatePasswordActivity.this, "JWT не получен", Toast.LENGTH_LONG).show();
                            return;
                        }

                        String userName = defaultUserName;
                        if (json.has("record") && json.get("record").isJsonObject()) {
                            JsonObject record = json.getAsJsonObject("record");
                            if (record.has("name") && !record.get("name").isJsonNull()) {
                                String nameFromServer = record.get("name").getAsString();
                                if (!TextUtils.isEmpty(nameFromServer)) {
                                    userName = nameFromServer;
                                }
                            }
                        }

                        getSharedPreferences("auth", MODE_PRIVATE)
                                .edit()
                                .putString("access_token", token)
                                .putString("user_email", registerEmail)
                                .putString("user_name", userName)
                                .apply();

                        Intent intent = new Intent(CreatePasswordActivity.this, SplashActivity.class);
                        startActivity(intent);
                        finishAffinity();
                    }

                    @Override
                    public void onFailure(Call<JsonObject> call, Throwable t) {
                        Toast.makeText(CreatePasswordActivity.this, "Ошибка сети при входе: " + t.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Toast.makeText(CreatePasswordActivity.this, "Ошибка сети при регистрации: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showError(String prefix, Response<JsonObject> response) {
        String message = prefix;
        try {
            if (response != null && response.errorBody() != null) {
                message += "\n" + response.errorBody().string();
            }
        } catch (IOException ignored) {
        }
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private String buildDefaultName(String email) {
        if (TextUtils.isEmpty(email)) {
            return "Пользователь";
        }
        int atIndex = email.indexOf('@');
        if (atIndex <= 0) {
            return "Пользователь";
        }
        String localPart = email.substring(0, atIndex).trim();
        return localPart.isEmpty() ? "Пользователь" : localPart;
    }

    private void updateSaveButtonState() {
        String password = etPassword.getText().toString().trim();
        String passwordConfirm = etPasswordConfirm.getText().toString().trim();
        boolean isEnabled = password.length() >= 8
                && passwordConfirm.length() >= 8
                && password.equals(passwordConfirm);
        btnSavePassword.setEnabled(isEnabled);
    }
}