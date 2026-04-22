package com.example.sp01;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.List;

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
        View rowMyOrders = view.findViewById(R.id.rowMyOrders);

        tvProfileName.setText(name == null || name.trim().isEmpty() ? "Пользователь" : name);
        tvProfileEmail.setText(email == null || email.trim().isEmpty() ? "Почта не указана" : email);

        rowMyOrders.setOnClickListener(v -> showOrdersBottomSheet());

        tvLogout.setOnClickListener(v -> {
            prefs.edit()
                    .remove("access_token")
                    .remove("user_email")
                    .remove("user_name")
                    .remove("user_id")
                    .apply();

            Intent intent = new Intent(requireContext(), RegistartionActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

    private void showOrdersBottomSheet() {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View sheet = LayoutInflater.from(requireContext()).inflate(R.layout.bottom_sheet_orders, null);

        LinearLayout ordersContainer = sheet.findViewById(R.id.ordersContainer);
        TextView tvOrdersEmpty = sheet.findViewById(R.id.tvOrdersEmpty);
        tvOrdersEmpty.setVisibility(View.VISIBLE);
        tvOrdersEmpty.setText("Загрузка заказов...");

        dialog.setContentView(sheet);
        dialog.show();

        OrderManager.getOrders(requireContext(), (orders, errorMessage) -> {
            if (!isAdded()) {
                return;
            }

            requireActivity().runOnUiThread(() -> renderOrders(ordersContainer, tvOrdersEmpty, orders, errorMessage));
        });
    }

    private void renderOrders(LinearLayout ordersContainer, TextView tvOrdersEmpty,
                              List<OrderManager.OrderEntry> orders, String errorMessage) {
        ordersContainer.removeAllViews();

        if (orders == null || orders.isEmpty()) {
            tvOrdersEmpty.setVisibility(View.VISIBLE);
            tvOrdersEmpty.setText(errorMessage == null ? "Заказов пока нет" : errorMessage);
            return;
        }

        tvOrdersEmpty.setVisibility(View.GONE);
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        for (int i = 0; i < orders.size(); i++) {
            OrderManager.OrderEntry order = orders.get(i);
            View orderView = inflater.inflate(R.layout.item_order, ordersContainer, false);

            TextView tvOrderNumber = orderView.findViewById(R.id.tvOrderNumber);
            TextView tvOrderDate = orderView.findViewById(R.id.tvOrderDate);
            TextView tvOrderItems = orderView.findViewById(R.id.tvOrderItems);
            TextView tvOrderTotal = orderView.findViewById(R.id.tvOrderTotal);

            tvOrderNumber.setText("Заказ " + (i + 1));
            tvOrderDate.setText(order.createdAt + " • " + order.getTotalQuantity() + " шт");
            tvOrderItems.setText(buildOrderItemsText(order));
            tvOrderTotal.setText("Сумма: " + order.getTotalPriceText());

            ordersContainer.addView(orderView);
        }
    }

    private String buildOrderItemsText(OrderManager.OrderEntry order) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < order.items.size(); i++) {
            OrderManager.OrderLine line = order.items.get(i);
            if (i > 0) {
                builder.append("\n");
            }
            builder.append(line.product.getTitle())
                    .append(" x")
                    .append(line.quantity);
        }
        return builder.toString();
    }
}
