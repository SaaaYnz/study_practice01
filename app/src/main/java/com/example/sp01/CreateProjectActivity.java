package com.example.sp01;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class CreateProjectActivity extends AppCompatActivity {

    private AutoCompleteTextView typeInput;
    private AutoCompleteTextView forInput;
    private AutoCompleteTextView categoryInput;
    private EditText nameInput;
    private EditText startDateInput;
    private EditText endDateInput;
    private EditText sourceInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_project);

        ImageButton backButton = findViewById(R.id.btnBackCreateProject);
        Button confirmButton = findViewById(R.id.btnConfirmProject);

        typeInput = findViewById(R.id.etProjectType);
        forInput = findViewById(R.id.etProjectFor);
        categoryInput = findViewById(R.id.etProjectCategory);
        nameInput = findViewById(R.id.etProjectName);
        startDateInput = findViewById(R.id.etProjectStartDate);
        endDateInput = findViewById(R.id.etProjectEndDate);
        sourceInput = findViewById(R.id.etProjectSource);

        setupDropdown(typeInput, R.array.project_type_array);
        setupDropdown(forInput, R.array.project_for_array);
        setupDropdown(categoryInput, R.array.project_category_array);

        backButton.setOnClickListener(v -> finish());
        confirmButton.setOnClickListener(v -> saveProject());
    }

    private void setupDropdown(AutoCompleteTextView input, int arrayRes) {
        String[] items = getResources().getStringArray(arrayRes);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, items);
        input.setAdapter(adapter);
        input.setOnClickListener(v -> input.showDropDown());
        input.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                input.showDropDown();
            }
        });
    }

    private void saveProject() {
        String type = getTrimmed(typeInput);
        String name = getTrimmed(nameInput);
        String startDate = getTrimmed(startDateInput);
        String endDate = getTrimmed(endDateInput);
        String projectFor = getTrimmed(forInput);
        String source = getTrimmed(sourceInput);
        String category = getTrimmed(categoryInput);

        if (TextUtils.isEmpty(type) || TextUtils.isEmpty(name)) {
            Toast.makeText(this, "Заполни хотя бы тип и название проекта", Toast.LENGTH_SHORT).show();
            return;
        }

        ProjectStorage.saveProject(this, type, name, startDate, endDate, projectFor, source, category);
        Toast.makeText(this, "Проект создан", Toast.LENGTH_SHORT).show();
        finish();
    }

    private String getTrimmed(EditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString().trim();
    }
}
