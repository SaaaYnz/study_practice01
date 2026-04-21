package com.example.sp01;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import java.util.List;

public class HomeFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        List<CatalogProduct> products = CatalogFakeDataSource.getProducts();

        View card1 = root.findViewById(R.id.homeCardProduct1);
        View card2 = root.findViewById(R.id.homeCardProduct2);
        View btnAdd1 = root.findViewById(R.id.homeBtnAdd1);
        View btnAdd2 = root.findViewById(R.id.homeBtnAdd2);

        card1.setOnClickListener(v -> showProductSheet(products.get(0)));
        card2.setOnClickListener(v -> showProductSheet(products.get(1)));
        btnAdd1.setOnClickListener(v -> showProductSheet(products.get(0)));
        btnAdd2.setOnClickListener(v -> showProductSheet(products.get(1)));

        return root;
    }

    private void showProductSheet(CatalogProduct product) {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View sheet = LayoutInflater.from(requireContext()).inflate(R.layout.bottom_sheet_product, null);

        TextView title = sheet.findViewById(R.id.tvSheetTitle);
        TextView description = sheet.findViewById(R.id.tvSheetDescription);
        TextView consumption = sheet.findViewById(R.id.tvSheetConsumption);
        TextView addButton = sheet.findViewById(R.id.btnSheetAdd);
        ImageView close = sheet.findViewById(R.id.ivClose);

        title.setText(product.getTitle());
        description.setText(product.getDescription());
        consumption.setText(product.getConsumption());
        addButton.setText("Добавить за " + product.getPrice());
        close.setOnClickListener(v -> dialog.dismiss());

        dialog.setContentView(sheet);
        dialog.show();
    }
}
