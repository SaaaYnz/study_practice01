package com.example.sp01;

import java.util.ArrayList;
import java.util.List;

public final class CatalogFakeDataSource {

    private CatalogFakeDataSource() {
    }

    public static List<CatalogProduct> getProducts() {
        List<CatalogProduct> products = new ArrayList<>();

        products.add(new CatalogProduct(
                "Рубашка воскресенье для машинного вязания",
                "Мужская одежда",
                "690 ₽",
                "Мягкая база для ежедневных образов. Ткань держит форму, комфортна в носке и не теряет вид после стирки.",
                "80-90 г"
        ));

        products.add(new CatalogProduct(
                "Шорты вторник для машинного вязания",
                "Мужская одежда",
                "590 ₽",
                "Легкая модель на каждый день. Посадка свободная, материал дышит, удобно сочетать с любым верхом.",
                "70-90 г"
        ));

        return products;
    }
}
