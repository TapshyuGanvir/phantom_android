package com.sixsimplex.phantom.Phantom1.appfragment.delproduct;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.sixsimplex.phantom.Phantom1.products.ProductSpinnerAdapter;
import com.sixsimplex.phantom.R;
import com.sixsimplex.phantom.revelocore.data.Feature;

import java.util.ArrayList;
import java.util.List;


public class ProductSheetFragment extends BottomSheetDialogFragment {

    Context context;
    Feature targetFeature;
    //    private IdeliveryActivityView ideliveryActivityView;
    private Button cancelBtn, updateBtn, addProductBtn;
    private LinearLayout productItemContainer;
    private Handler mHandler = new Handler(Looper.getMainLooper());

    public ProductSheetFragment(Context context, Feature targetFeature) {
        this.context = context;
        this.targetFeature = targetFeature;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void setupDialog(Dialog dialog, int style) {
        View contentView = View.inflate(getContext(), R.layout.product_sheet_dialog, null);
        dialog.setContentView(contentView);
        dialog.setCancelable(false);
        ProductSheetPresenter productSheetPresenter = new ProductSheetPresenter();

        cancelBtn = contentView.findViewById(R.id.form_cancel);
        updateBtn = contentView.findViewById(R.id.form_update);
        productItemContainer = contentView.findViewById(R.id.container_view_in_delivery_form);
        addProductBtn = contentView.findViewById(R.id.add_product_btn_delivery_form);

        List<Feature> deliveryItemsList = productSheetPresenter.getDeliveryItemForTargetFeature(context, targetFeature);
        List<Feature> productList = productSheetPresenter.getProducts(context);


        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {

                    AddUpdateDropOffItems(productSheetPresenter, deliveryItemsList, targetFeature);
//                    updateDelivery(productSheetPresenter,targetFeature);
                    dialog.dismiss();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        addProductBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AddNewExtraProduct(productList);
            }
        });
        showPreFillProductInForm(deliveryItemsList, productList);
        ((View) contentView.getParent()).setBackgroundColor(getResources().getColor(android.R.color.transparent));
    }

//    private void updateDelivery(ProductSheetPresenter productSheetPresenter, Feature targetFeature) {
////        int isDeliveredVal = ((isDelivered.getCheckedRadioButtonId() == R.id.yes_isDelivered) ? 1 : 0);
//        Map<String, Object> attributeValueMap = new HashMap<>();
//        Date dateCurrent = new Date();
//        String currentDate = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(dateCurrent);
//        attributeValueMap.put("isdelivered", isDeliveredVal);
//        attributeValueMap.put("dropdate", currentDate);
//        attributeValueMap.put("isvisited", 1);
//        attributeValueMap.put("riderid", UserInfoPreferenceUtility.getUserName());
//        attributeValueMap.put("w9entityclassname", DeliveryDataModel.traversalEntityName);
//        productSheetPresenter.updateDataToServer(context, ideliveryActivityView, attributeValueMap,targetFeature,mode);
//    }


    private void AddNewExtraProduct(List<Feature> productList) {
        @SuppressLint("InflateParams") View productRowView = getLayoutInflater().inflate(R.layout.prodcut_selection_layout_row, null, false);
        Spinner productItems = productRowView.findViewById(R.id.productItemsSpinner);
        TextView minusBtn = productRowView.findViewById(R.id.minusBtn);
        TextView plusBtn = productRowView.findViewById(R.id.plusBtn);
        EditText productCount = productRowView.findViewById(R.id.product_count_et);
        productCount.setEnabled(false);
        Count count = new Count(0);
        ProductSpinnerAdapter productSpinnerAdapter = new ProductSpinnerAdapter(context, productList);
        productItems.setAdapter(productSpinnerAdapter);
        ImageView removeProduct = productRowView.findViewById(R.id.remove_item);
        removeProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeProductFromForm(productRowView);
            }
        });
        productCount.setText(String.valueOf(count.getNumber()));

        minusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                count.minusOne();
                productCount.setText(String.valueOf(count.getNumber()));
            }
        });

        plusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                count.plusOne();
                productCount.setText(String.valueOf(count.getNumber()));
            }
        });
        productRowView.setTag("NEW");
        productItemContainer.addView(productRowView);
    }

    @SuppressLint("SetTextI18n")
    private void showPreFillProductInForm(List<Feature> deliveryProductList, List<Feature> productList) {

        try {
            if (deliveryProductList != null) {
                for (Feature feature : deliveryProductList) {
                    Count count = new Count(Integer.parseInt(String.valueOf(feature.getAttributes().get("quantity"))));

                    @SuppressLint("InflateParams") View productRowView = getLayoutInflater().inflate(R.layout.prodcut_selection_layout_row, null, false);
                    Spinner productItems = productRowView.findViewById(R.id.productItemsSpinner);
                    TextView minusBtn = productRowView.findViewById(R.id.minusBtn);
                    TextView plusBtn = productRowView.findViewById(R.id.plusBtn);
                    EditText productCount = productRowView.findViewById(R.id.product_count_et);
                    productCount.setEnabled(false);

                    List<Feature> productNameList = new ArrayList<>();
                    for (Feature product : productList) {
                        if (feature.getAttributes().get("productid").equals(product.getFeatureId())) {
                            productNameList.add(product);
                            break;
                        }
                    }
                    ProductSpinnerAdapter productSpinnerAdapter = new ProductSpinnerAdapter(context, productNameList);
                    productItems.setAdapter(productSpinnerAdapter);
                    productCount.setText(String.valueOf(count.getNumber()));

                    minusBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            count.minusOne();
                            productCount.setText(String.valueOf(count.getNumber()));
                        }
                    });

                    plusBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            count.plusOne();
                            productCount.setText(String.valueOf(count.getNumber()));
                        }
                    });

                    ImageView removeProduct = productRowView.findViewById(R.id.remove_item);
                    removeProduct.setVisibility(View.INVISIBLE);
                    removeProduct.setEnabled(false);
                    removeProduct.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            removeProductFromForm(productRowView);
                        }
                    });
                    productRowView.setTag(String.valueOf(feature.getFeatureId()));
                    productItemContainer.addView(productRowView);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    private void AddUpdateDropOffItems(ProductSheetPresenter productSheetPresenter, List<Feature> deliveryItemsList, Feature targetFeature) {
        try {
            for (int i = 0; i < productItemContainer.getChildCount(); i++) {
                View productView = productItemContainer.getChildAt(i);
                Spinner spinner = productView.findViewById(R.id.productItemsSpinner);
                EditText productQntEt = (EditText) productView.findViewById(R.id.product_count_et);
                Feature product = (Feature) spinner.getSelectedItem();
                int productQntVal = 0;
                if (productQntEt.getText() != null && !productQntEt.getText().equals("")) {
                    productQntVal = Integer.valueOf(String.valueOf(productQntEt.getText()));
                    Log.d("productval", "AddUpdateDropOffItems: " + String.valueOf(productQntVal));
                }
                if (productView.getTag().equals("NEW")) {
                    productSheetPresenter.addExtraDropItem(context, product, productQntVal, targetFeature);
                } else {
                    String featureId = String.valueOf(productView.getTag());
                    for (Feature preProd : deliveryItemsList) {
                        if (preProd.getFeatureId().equals(featureId)) {
                            if (!String.valueOf(productQntVal).equals(String.valueOf(preProd.getAttributes().get("quantity")))) {
                                productSheetPresenter.editQuantityOfExistingDropItem(context, featureId, productQntVal);
                            }
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void removeProductFromForm(View view) {
        productItemContainer.removeView(view);
    }
}
