package com.sixsimplex.phantom.Phantom1.products;

import android.content.Context;

import com.sixsimplex.phantom.Phantom1.model.DeliveryDataModel;
import com.sixsimplex.phantom.revelocore.data.Feature;

import java.util.ArrayList;
import java.util.List;

public class Products {
    public static List<Feature> getProducts(Context context) {
        List<Feature> productList = new ArrayList<>();
        try {
            productList = DeliveryDataModel.getInstance().getProductEntity().getFeatureTable().getallFeaturesList(context, null, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return productList;
    }
}
