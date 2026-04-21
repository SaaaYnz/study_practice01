package com.example.sp01;

import android.content.Intent;
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

public class CatalogFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_catalog, container, false);
        List<CatalogProduct> products = CatalogFakeDataSource.getProducts();

        bindProductCard(root, products.get(0), R.id.cardProduct1, R.id.tvTitle1, R.id.tvCategory1, R.id.tvPrice1);
        bindProductCard(root, products.get(1), R.id.cardProduct2, R.id.tvTitle2, R.id.tvCategory2, R.id.tvPrice2);
        root.findViewById(R.id.btnAdd1).setOnClickListener(v -> showProductSheet(products.get(0)));
        root.findViewById(R.id.btnAdd2).setOnClickListener(v -> showProductSheet(products.get(1)));
        root.findViewById(R.id.cartBar).setOnClickListener(v ->
                startActivity(new Intent(requireContext(), CartActivity.class))
        );

        return root;
    }

    private void bindProductCard(View root, CatalogProduct product, int cardId, int titleId, int categoryId, int priceId) {
        View card = root.findViewById(cardId);
        TextView title = root.findViewById(titleId);
        TextView category = root.findViewById(categoryId);
        TextView price = root.findViewById(priceId);

        title.setText(product.getTitle());
        category.setText(product.getCategory());
        price.setText(product.getPrice());

        card.setOnClickListener(v -> showProductSheet(product));
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
