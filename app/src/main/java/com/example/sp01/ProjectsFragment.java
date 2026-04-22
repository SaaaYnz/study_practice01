package com.example.sp01;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.sp01.entity.ProjectItem;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class ProjectsFragment extends Fragment {

    private View rootView;
    private LinearLayout projectsContainer;
    private TextView emptyProjectsText;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_projects, container, false);
        projectsContainer = rootView.findViewById(R.id.projectsContainer);
        emptyProjectsText = rootView.findViewById(R.id.tvEmptyProjects);

        rootView.findViewById(R.id.btnAddProject).setOnClickListener(v ->
                startActivity(new Intent(requireContext(), CreateProjectActivity.class))
        );

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        bindProjects();
    }

    private void bindProjects() {
        if (projectsContainer == null) {
            return;
        }

        projectsContainer.removeAllViews();
        emptyProjectsText.setVisibility(View.VISIBLE);
        emptyProjectsText.setText("Загрузка проектов...");

        ProjectStorage.getProjects(requireContext(), (projects, errorMessage) -> {
            if (!isAdded()) {
                return;
            }

            requireActivity().runOnUiThread(() -> renderProjects(projects, errorMessage));
        });
    }

    private void renderProjects(List<ProjectItem> projects, String errorMessage) {
        projectsContainer.removeAllViews();

        if (projects == null || projects.isEmpty()) {
            emptyProjectsText.setVisibility(View.VISIBLE);
            emptyProjectsText.setText(errorMessage == null ? "Пока нет проектов" : errorMessage);
            return;
        }

        emptyProjectsText.setVisibility(View.GONE);
        LayoutInflater inflater = LayoutInflater.from(requireContext());

        for (ProjectItem project : projects) {
            View card = inflater.inflate(R.layout.item_project, projectsContainer, false);
            TextView name = card.findViewById(R.id.tvProjectName);
            TextView type = card.findViewById(R.id.tvProjectType);
            TextView meta = card.findViewById(R.id.tvProjectMeta);
            Button open = card.findViewById(R.id.btnOpenProject);

            name.setText(project.getName());
            type.setText(project.getType());
            meta.setText(buildMeta(project));

            View.OnClickListener listener = v -> showProjectBottomSheet(project);
            card.setOnClickListener(listener);
            open.setOnClickListener(listener);

            projectsContainer.addView(card);
        }
    }

    private String buildMeta(ProjectItem project) {
        long elapsedDays = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - project.getCreatedAt());
        if (elapsedDays <= 0) {
            return "Создан сегодня";
        }
        if (elapsedDays == 1) {
            return "Создан 1 день назад";
        }
        return "Создан " + elapsedDays + " дн. назад";
    }

    private void showProjectBottomSheet(ProjectItem project) {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View sheet = LayoutInflater.from(requireContext()).inflate(R.layout.bottom_sheet_project, null);

        ((TextView) sheet.findViewById(R.id.tvProjectSheetTitle)).setText(project.getName());
        ((TextView) sheet.findViewById(R.id.tvProjectSheetType)).setText("Тип: " + valueOrDash(project.getType()));
        ((TextView) sheet.findViewById(R.id.tvProjectSheetStart)).setText("Дата начала: " + valueOrDash(project.getStartDate()));
        ((TextView) sheet.findViewById(R.id.tvProjectSheetEnd)).setText("Дата окончания: " + valueOrDash(project.getEndDate()));
        ((TextView) sheet.findViewById(R.id.tvProjectSheetFor)).setText("Размер: " + valueOrDash(project.getSize()));
        ((TextView) sheet.findViewById(R.id.tvProjectSheetSource)).setText("Источник: " + valueOrDash(project.getSource()));
        ((TextView) sheet.findViewById(R.id.tvProjectSheetCategory)).setText("Чертеж: " + valueOrDash(project.getTechnicalDrawing()));

        dialog.setContentView(sheet);
        dialog.show();
    }

    private String valueOrDash(String value) {
        return value == null || value.trim().isEmpty() ? "—" : value;
    }
}
