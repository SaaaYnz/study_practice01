package com.example.sp01;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class ProfileFragment extends Fragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SharedPreferences prefs = requireContext().getSharedPreferences("auth", 0);

        String name = prefs.getString("user_name", null);
        String email = prefs.getString("user_email", null);

        TextView tvProfileName = view.findViewById(R.id.tvProfileName);
        TextView tvProfileEmail = view.findViewById(R.id.tvProfileEmail);
        TextView tvLogout = view.findViewById(R.id.tvLogout);

        tvProfileName.setText(name == null || name.trim().isEmpty() ? "Пользователь" : name);
        tvProfileEmail.setText(email == null || email.trim().isEmpty() ? "Почта не указана" : email);

        tvLogout.setOnClickListener(v -> {
            prefs.edit()
                    .remove("access_token")
                    .remove("user_email")
                    .remove("user_name")
                    .apply();

            Intent intent = new Intent(requireContext(), RegistartionActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }
}
