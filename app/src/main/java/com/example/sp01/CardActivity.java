package com.example.sp01;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class CardActivity extends AppCompatActivity {

    private EditText etEmail;
    private View btnNext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card);
        etEmail = findViewById(R.id.etRegEmail);
        btnNext = findViewById(R.id.btnRegistrationNext);

        etEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                updateButtonState();
            }
        });

        updateButtonState();
    }

    public void onClickNext(View view) {
        String email = etEmail.getText().toString().trim();
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Введите корректный email", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(CardActivity.this, CreatePasswordActivity.class);
        intent.putExtra("register_email", email);
        startActivity(intent);
    }

    private void updateButtonState() {
        String email = etEmail.getText().toString().trim();
        btnNext.setEnabled(Patterns.EMAIL_ADDRESS.matcher(email).matches());
    }
}