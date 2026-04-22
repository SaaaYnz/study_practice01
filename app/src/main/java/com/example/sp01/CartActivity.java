package com.example.sp01;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;
import java.util.Locale;

public class CartActivity extends AppCompatActivity {

    private LinearLayout cartItemsContainer;
    private TextView tvEmptyCart;
    private TextView tvTotalAmount;
    private View rowTotal;
    private View btnCheckout;
    private ImageView ivTrash;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        ImageButton backButton = findViewById(R.id.btnBack);
        backButton.setOnClickListener(v -> finish());

        cartItemsContainer = findViewById(R.id.cartItemsContainer);
        tvEmptyCart = findViewById(R.id.tvEmptyCart);
        tvTotalAmount = findViewById(R.id.tvTotalAmount);
        rowTotal = findViewById(R.id.rowTotal);
        btnCheckout = findViewById(R.id.btnCheckout);
        ivTrash = findViewById(R.id.ivTrash);

        ivTrash.setOnClickListener(v -> {
            CartManager.clear(this);
            renderCart();
        });

        renderCart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        renderCart();
    }

    private void renderCart() {
        List<CartManager.CartEntry> entries = CartManager.getEntries(this);
        cartItemsContainer.removeAllViews();

        if (entries.isEmpty()) {
            tvEmptyCart.setVisibility(View.VISIBLE);
            rowTotal.setVisibility(View.GONE);
            btnCheckout.setVisibility(View.GONE);
            ivTrash.setVisibility(View.GONE);
            return;
        }

        tvEmptyCart.setVisibility(View.GONE);
        rowTotal.setVisibility(View.VISIBLE);
        btnCheckout.setVisibility(View.VISIBLE);
        ivTrash.setVisibility(View.VISIBLE);

        LayoutInflater inflater = LayoutInflater.from(this);
        for (CartManager.CartEntry entry : entries) {
            View itemView = inflater.inflate(R.layout.item_cart_product, cartItemsContainer, false);

            TextView tvTitle = itemView.findViewById(R.id.tvCartItemTitle);
            TextView tvPrice = itemView.findViewById(R.id.tvCartItemPrice);
            TextView tvQty = itemView.findViewById(R.id.tvCartItemQty);
            View btnMinus = itemView.findViewById(R.id.btnQtyMinus);
            View btnPlus = itemView.findViewById(R.id.btnQtyPlus);
            View btnRemove = itemView.findViewById(R.id.ivCartItemRemove);

            tvTitle.setText(entry.product.getTitle());
            tvPrice.setText(entry.product.getPrice());
            tvQty.setText(String.format(Locale.getDefault(), "%d шт", entry.quantity));

            btnPlus.setOnClickListener(v -> {
                CartManager.increment(this, entry.product.getId());
                renderCart();
            });

            btnMinus.setOnClickListener(v -> {
                CartManager.decrement(this, entry.product.getId());
                renderCart();
            });

            btnRemove.setOnClickListener(v -> {
                CartManager.remove(this, entry.product.getId());
                renderCart();
            });

            cartItemsContainer.addView(itemView);
        }

        tvTotalAmount.setText(CartManager.getTotalPriceText(this));
    }
}