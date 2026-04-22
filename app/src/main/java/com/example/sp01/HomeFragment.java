package com.example.sp01;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.sp01.api.ApiClient;
import com.example.sp01.api.ProductApiMapper;
import com.example.sp01.entity.CatalogProduct;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private enum FilterType {
        ALL,
        WOMEN,
        MEN
    }

    private View rootView;
    private ViewGroup productsContainer;
    private TextView tvEmptyProducts;

    private final List<CatalogProduct> allProducts = new ArrayList<>();
    private FilterType currentFilter = FilterType.ALL;
    private String currentQuery = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_home, container, false);

        productsContainer = rootView.findViewById(R.id.homeProductsContainer);
        tvEmptyProducts = rootView.findViewById(R.id.homeTvEmptyProducts);

        rootView.findViewById(R.id.homeCartBar).setOnClickListener(v ->
                startActivity(new Intent(requireContext(), CartActivity.class))
        );

        setupControls();
        loadProducts();
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshCartUi();
    }

    private void loadProducts() {
        String token = requireContext().getSharedPreferences("auth", 0)
                .getString("access_token", null);
        String authorization = TextUtils.isEmpty(token) ? null : "Bearer " + token;

        ApiClient.getApiService().getProducts(authorization, 30, "-created")
                .enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                        if (!isAdded()) {
                            return;
                        }

                        List<CatalogProduct> products = response.isSuccessful() && response.body() != null
                                ? ProductApiMapper.parseProducts(response.body())
                                : null;

                        allProducts.clear();
                        if (products != null) {
                            allProducts.addAll(products);
                        }
                        applyFilters();
                    }

                    @Override
                    public void onFailure(Call<JsonObject> call, Throwable t) {
                        if (!isAdded()) {
                            return;
                        }
                        allProducts.clear();
                        applyFilters();
                    }
                });
    }

    private void setupControls() {
        EditText etSearch = rootView.findViewById(R.id.etSearch);
        Button btnFilterAll = rootView.findViewById(R.id.btnFilterAll);
        Button btnFilterWomen = rootView.findViewById(R.id.btnFilterWomen);
        Button btnFilterMen = rootView.findViewById(R.id.btnFilterMen);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                currentQuery = s == null ? "" : s.toString().trim();
                applyFilters();
            }
        });

        btnFilterAll.setOnClickListener(v -> {
            currentFilter = FilterType.ALL;
            updateFilterButtons();
            applyFilters();
        });
        btnFilterWomen.setOnClickListener(v -> {
            currentFilter = FilterType.WOMEN;
            updateFilterButtons();
            applyFilters();
        });
        btnFilterMen.setOnClickListener(v -> {
            currentFilter = FilterType.MEN;
            updateFilterButtons();
            applyFilters();
        });

        updateFilterButtons();
    }

    private void applyFilters() {
        List<CatalogProduct> filtered = new ArrayList<>();
        for (CatalogProduct product : allProducts) {
            if (matchesQuery(product) && matchesFilter(product)) {
                filtered.add(product);
            }
        }
        bindProducts(filtered);
    }

    private void bindProducts(List<CatalogProduct> products) {
        if (productsContainer == null) {
            return;
        }

        productsContainer.removeAllViews();

        if (products == null || products.isEmpty()) {
            if (tvEmptyProducts != null) {
                tvEmptyProducts.setVisibility(View.VISIBLE);
            }
            refreshCartUi();
            return;
        }

        if (tvEmptyProducts != null) {
            tvEmptyProducts.setVisibility(View.GONE);
        }

        LayoutInflater inflater = LayoutInflater.from(requireContext());
        for (CatalogProduct product : products) {
            View card = inflater.inflate(R.layout.item_product_card, productsContainer, false);
            card.setTag(product);

            TextView title = card.findViewById(R.id.tvProductTitle);
            TextView category = card.findViewById(R.id.tvProductCategory);
            TextView price = card.findViewById(R.id.tvProductPrice);
            Button action = card.findViewById(R.id.btnProductAction);

            title.setText(product.getTitle());
            category.setText(product.getCategory());
            price.setText(product.getPrice());

            card.setOnClickListener(v -> showProductSheet(product));
            action.setOnClickListener(v -> {
                CartManager.toggle(requireContext(), product);
                refreshCartUi();
            });

            updateProductActionButton(action, product);
            productsContainer.addView(card);
        }

        refreshCartUi();
    }

    private boolean matchesQuery(CatalogProduct product) {
        if (TextUtils.isEmpty(currentQuery)) {
            return true;
        }
        String haystack = (product.getTitle() + " " + product.getCategory() + " " + product.getDescription())
                .toLowerCase(Locale.ROOT);
        return haystack.contains(currentQuery.toLowerCase(Locale.ROOT));
    }

    private boolean matchesFilter(CatalogProduct product) {
        if (currentFilter == HomeFragment.FilterType.ALL) {
            return true;
        }

        String typeCloses = product.getTypeCloses() == null
                ? ""
                : product.getTypeCloses().toLowerCase(Locale.ROOT);

        if (currentFilter == HomeFragment.FilterType.WOMEN) {
            return typeCloses.contains("жен");
        }

        return typeCloses.contains("муж");
    }

    private void updateFilterButtons() {
        updateFilterButton(R.id.btnFilterAll, currentFilter == FilterType.ALL);
        updateFilterButton(R.id.btnFilterWomen, currentFilter == FilterType.WOMEN);
        updateFilterButton(R.id.btnFilterMen, currentFilter == FilterType.MEN);
    }

    private void updateFilterButton(int buttonId, boolean selected) {
        Button button = rootView.findViewById(buttonId);
        if (button == null) {
            return;
        }
        if (selected) {
            button.setBackgroundResource(R.drawable.bg_button_continue);
            button.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
        } else {
            button.setBackgroundResource(R.drawable.bg_button_outline_blue);
            button.setTextColor(ContextCompat.getColor(requireContext(), R.color.blue_main));
        }
    }

    private void refreshCartUi() {
        if (rootView == null || !isAdded()) {
            return;
        }

        View cartBar = rootView.findViewById(R.id.homeCartBar);
        TextView tvCartTotal = rootView.findViewById(R.id.homeTvCartTotal);
        TextView tvCartAction = rootView.findViewById(R.id.homeTvCartAction);

        int totalCount = CartManager.getTotalQuantity(requireContext());
        if (totalCount > 0) {
            cartBar.setVisibility(View.VISIBLE);
            tvCartTotal.setText(CartManager.getTotalPriceText(requireContext()));
            tvCartAction.setText("В корзину (" + totalCount + ")");
        } else {
            cartBar.setVisibility(View.GONE);
        }

        if (productsContainer != null) {
            for (int i = 0; i < productsContainer.getChildCount(); i++) {
                View child = productsContainer.getChildAt(i);
                Object tag = child.getTag();
                if (!(tag instanceof CatalogProduct)) {
                    continue;
                }

                CatalogProduct product = (CatalogProduct) tag;
                Button action = child.findViewById(R.id.btnProductAction);
                updateProductActionButton(action, product);
            }
        }
    }

    private void updateProductActionButton(Button button, CatalogProduct product) {
        if (button == null) {
            return;
        }
        if (product == null) {
            button.setEnabled(false);
            return;
        }

        boolean inCart = CartManager.isInCart(requireContext(), product.getId());
        button.setEnabled(true);
        if (inCart) {
            button.setText("Убрать");
            button.setBackgroundResource(R.drawable.bg_button_outline_blue);
            button.setTextColor(ContextCompat.getColor(requireContext(), R.color.blue_main));
        } else {
            button.setText("Добавить");
            button.setBackgroundResource(R.drawable.bg_button_continue);
            button.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
        }
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

        boolean inCart = CartManager.isInCart(requireContext(), product.getId());
        addButton.setText(inCart ? "Убрать из корзины" : "Добавить за " + product.getPrice());
        addButton.setOnClickListener(v -> {
            CartManager.toggle(requireContext(), product);
            refreshCartUi();
            dialog.dismiss();
        });

        close.setOnClickListener(v -> dialog.dismiss());

        dialog.setContentView(sheet);
        dialog.show();
    }
}
