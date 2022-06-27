package com.sixsimplex.phantom.Phantom1.appfragment.formsheet;

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
import android.widget.RadioGroup;
import android.widget.Spinner;

import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.sixsimplex.phantom.R;
import com.sixsimplex.phantom.revelocore.data.Feature;
import com.sixsimplex.phantom.Phantom1.app.IdeliveryActivityView;
import com.sixsimplex.phantom.Phantom1.model.DeliveryDataModel;
import com.sixsimplex.phantom.Phantom1.products.ProductSpinnerAdapter;
import com.sixsimplex.phantom.Phantom1.CURD.upload.UploadInterface;
import com.sixsimplex.phantom.revelocore.util.sharedPreference.UserInfoPreferenceUtility;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class FormSheetFragment extends BottomSheetDialogFragment {

    Context context;
    private IdeliveryActivityView ideliveryActivityView;
    private Button cancelBtn, updateBtn, addProductBtn;
    private RadioGroup isDelivered;
    private LinearLayout productItemContainer;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    Feature targetFeature;
    String mode;

    public FormSheetFragment(Context context, IdeliveryActivityView activity,Feature targetFeature,String mode) {
        this.mode=mode;
        this.context = context;
        this.ideliveryActivityView = activity;
        this.targetFeature=targetFeature;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void setupDialog(Dialog dialog, int style) {
        View contentView = View.inflate(getContext(), R.layout.form_sheet_dialog, null);
        dialog.setContentView(contentView);
        FormSheetPresenter formSheetPresenter = new FormSheetPresenter();

        cancelBtn = contentView.findViewById(R.id.form_cancel);
        updateBtn = contentView.findViewById(R.id.form_update);
        isDelivered = (RadioGroup) contentView.findViewById(R.id.is_delivered);
        productItemContainer = contentView.findViewById(R.id.container_view_in_delivery_form);
        addProductBtn = contentView.findViewById(R.id.add_product_btn_delivery_form);

        List<Feature> deliveryItemsList = formSheetPresenter.getDeliveryItemForTargetFeature(context,targetFeature);
        List<Feature> productList = formSheetPresenter.getProducts(context);


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

                    AddUpdateDropOffItems(formSheetPresenter,deliveryItemsList,targetFeature);
                    updateDelivery(formSheetPresenter,targetFeature);
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

    private void updateDelivery(FormSheetPresenter formSheetPresenter, Feature targetFeature) {
        int isDeliveredVal = ((isDelivered.getCheckedRadioButtonId() == R.id.yes_isDelivered) ? 1 : 0);
        Map<String, Object> attributeValueMap = new HashMap<>();
        Date dateCurrent = new Date();
        String currentDate = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(dateCurrent);

        attributeValueMap.put("dropdate", currentDate);
        if(isDeliveredVal == 1){
            attributeValueMap.put("isdelivered", isDeliveredVal);
            attributeValueMap.put("skipped", 0);
        }else{
            attributeValueMap.put("isdelivered", isDeliveredVal);
            attributeValueMap.put("skipped", 1);
        }
        attributeValueMap.put("isvisited", 1);
        attributeValueMap.put("riderid", UserInfoPreferenceUtility.getUserName());
        attributeValueMap.put("w9entityclassname", DeliveryDataModel.traversalEntityName);
        formSheetPresenter.updateDataToServer(context, ideliveryActivityView, attributeValueMap,targetFeature,mode);
    }


    private void AddNewExtraProduct(List<Feature> productList) {
        @SuppressLint("InflateParams") View productRowView = getLayoutInflater().inflate(R.layout.prodcut_selection_layout_row, null, false);
        Spinner productItems = productRowView.findViewById(R.id.productItemsSpinner);

        ProductSpinnerAdapter productSpinnerAdapter = new ProductSpinnerAdapter(context, productList);
        productItems.setAdapter(productSpinnerAdapter);
        ImageView removeProduct = productRowView.findViewById(R.id.remove_item);
        removeProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeProductFromForm(productRowView);
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
                    @SuppressLint("InflateParams") View productRowView = getLayoutInflater().inflate(R.layout.prodcut_selection_layout_row, null, false);
                    Spinner productItems = productRowView.findViewById(R.id.productItemsSpinner);
                    EditText productCount = productRowView.findViewById(R.id.product_count_et);

                    List<Feature> productNameList = new ArrayList<>();
                    for (Feature product : productList) {
                        if (feature.getAttributes().get("productid").equals(product.getFeatureId())) {
                            productNameList.add(product);
                            break;
                        }
                    }
                    ProductSpinnerAdapter productSpinnerAdapter = new ProductSpinnerAdapter(context, productNameList);
                    productItems.setAdapter(productSpinnerAdapter);
                    productCount.setText(Integer.toString((Integer) feature.getAttributes().get("quantity")));

                    ImageView removeProduct = productRowView.findViewById(R.id.remove_item);
                    removeProduct.setVisibility(View.GONE);
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


    private void AddUpdateDropOffItems(FormSheetPresenter formSheetPresenter, List<Feature> deliveryItemsList, Feature targetFeature) {
        try {
            UploadInterface addInterFace = new UploadInterface() {
                @Override
                public void OnUploadStarted() {

                }

                @Override
                public void OnUploadFinished(boolean isSuccessfull, JSONObject uploadResult) {

                }
            };
            UploadInterface editInterFace = new UploadInterface() {
                @Override
                public void OnUploadStarted() {

                }

                @Override
                public void OnUploadFinished(boolean isSuccessfull, JSONObject uploadResult) {

                }
            };

            for (int i = 0; i < productItemContainer.getChildCount(); i++) {
                View productView = productItemContainer.getChildAt(i);
                Spinner spinner = productView.findViewById(R.id.productItemsSpinner);
                EditText productQntEt = (EditText) productView.findViewById(R.id.product_count_et);
                Feature product = (Feature) spinner.getSelectedItem();
                int productQntVal = 0;
                if (productQntEt.getText() != null && !productQntEt.getText().equals("")) {
                    productQntVal = Integer.valueOf(String.valueOf(productQntEt.getText()));
                    Log.d("productval", "AddUpdateDropOffItems: "+String.valueOf(productQntVal));
                }
                if (productView.getTag().equals("NEW")) {
                    formSheetPresenter.addExtraDropItem(context, product, productQntVal,targetFeature);
                } else {
                    String featureId=String.valueOf(productView.getTag());
                    for (Feature preProd:deliveryItemsList){
                        if(preProd.getFeatureId().equals(featureId)){
                            if(!String.valueOf(productQntVal).equals(String.valueOf(preProd.getAttributes().get("quantity")))){
                                formSheetPresenter.editQuantityOfExistingDropItem(context, featureId, productQntVal);
                            }
                            break;
                        }
                    }
                }
            }
//            JSONObject graphResult = CMUtils.getCMGraph(context);
//                if (graphResult.has("status") && graphResult.getString("status").equalsIgnoreCase("success")) {
//                    CMGraph cmGraph = (CMGraph) graphResult.get("result");
//                    new UploadEditedFeature(context, editInterFace, DeliveryDataModel.getInstance().getDropOffItemEntity().getName(), cmGraph, false, false);
//                }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void removeProductFromForm(View view) {
        productItemContainer.removeView(view);
    }
}
