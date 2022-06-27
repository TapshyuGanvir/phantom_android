package com.sixsimplex.phantom.Phantom1.listdelivery;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import com.sixsimplex.phantom.Phantom1.CURD.EditAndUpload;
import com.sixsimplex.phantom.Phantom1.CURD.upload.UploadInterface;
import com.sixsimplex.phantom.Phantom1.CURD.upload.UploadTrail;
import com.sixsimplex.phantom.Phantom1.app.IdeliveryActivityView;
import com.sixsimplex.phantom.Phantom1.mode.ModeUtility;
import com.sixsimplex.phantom.Phantom1.model.DeliveryDataModel;
import com.sixsimplex.phantom.Phantom1.picture.PictureActivity;
import com.sixsimplex.phantom.R;
import com.sixsimplex.phantom.revelocore.data.Feature;
import com.sixsimplex.phantom.revelocore.util.constants.AppConstants;
import com.sixsimplex.phantom.revelocore.util.sharedPreference.UserInfoPreferenceUtility;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class PerformListDelivery {

    public static final String DELIVER = "deliver";
    public static final String SKIP = "skip";

    Context context;
    IdeliveryActivityView ideliveryActivityView;
    boolean isCheckedStatus;
    Handler mHandler = new Handler(Looper.getMainLooper());
    private ListDeliveryViewAdapter.ViewHolder viewHolder;
    private int position;
    private Feature targetFeature;
    private Animation rotate_down;
    private Animation rotate_up;

    public PerformListDelivery(Context context, ListDeliveryViewAdapter.ViewHolder viewHolder, int position, Feature targetFeature, IdeliveryActivityView ideliveryActivityView) {
        this.context = context;
        this.viewHolder = viewHolder;
        this.position = position;
        this.targetFeature = targetFeature;
        this.ideliveryActivityView = ideliveryActivityView;
    }

    public boolean isCheckedStatus() {
        return isCheckedStatus;
    }

    public void setCheckedStatus(boolean checkedStatus) {
        isCheckedStatus = checkedStatus;
    }

    public void perform() {

        viewHolder.pictureButtonLl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePicture();
            }
        });

        rotate_down = AnimationUtils.loadAnimation(context, R.anim.slide_up);
        rotate_up = AnimationUtils.loadAnimation(context, R.anim.slide_up);
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (viewHolder.drop_data_ll.getVisibility() != View.VISIBLE) {
//                    viewHolder.downArrowImageView.setVisibility(View.GONE);
//                    viewHolder.upArrowImageView.setVisibility(View.VISIBLE);
                    viewHolder.drop_data_ll.startAnimation(rotate_down);
                    viewHolder.drop_data_ll.setVisibility(View.VISIBLE);
                } else {
//                    viewHolder.downArrowImageView.setVisibility(View.VISIBLE);
//                    viewHolder.upArrowImageView.setVisibility(View.GONE);
                    viewHolder.drop_data_ll.startAnimation(rotate_up);
                    viewHolder.drop_data_ll.setVisibility(View.GONE);
                }
            }
        });

        viewHolder.delivery_toggle_btn_lv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    ideliveryActivityView.showProgressDialog("Updating");
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            updateDelivery(context, targetFeature, DELIVER, viewHolder.getAdapterPosition());
                        }
                    }).start();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
        viewHolder.skip_lv_ll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ideliveryActivityView.showProgressDialog("Updating");
                updateDelivery(context, targetFeature, SKIP, viewHolder.getAdapterPosition());
            }
        });

        viewHolder.product_lv_ll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ideliveryActivityView.showProductsUpdateDialogForTargetFeature(targetFeature);
            }
        });


    }

    private void takePicture() {
        try {

            Intent intent = new Intent(context, PictureActivity.class);
            intent.putExtra("featureId", String.valueOf(targetFeature.getFeatureId()));
            intent.putExtra("entityName", String.valueOf(targetFeature.getEntityName()));
            intent.putExtra(AppConstants.attachmentType, AppConstants.photo);
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateDelivery(Context context, Feature targetFeature, String status, int position) {
        try {
            Map<String, Object> attributeValueMap = new HashMap<>();
            Date dateCurrent = new Date();
            String currentDate = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(dateCurrent);
            if (status.equals(DELIVER)) {
                attributeValueMap.put("isdelivered", 1);
                attributeValueMap.put("skipped", 0);
            } else {
                attributeValueMap.put("isdelivered", 0);
                attributeValueMap.put("skipped", 1);
            }
            attributeValueMap.put("dropdate", currentDate);
            attributeValueMap.put("isvisited", 1);
            attributeValueMap.put("riderid", UserInfoPreferenceUtility.getUserName());
            attributeValueMap.put("w9entityclassname", DeliveryDataModel.traversalEntityName);
            saveAndUpdateDataToServer(context, attributeValueMap, targetFeature, position,status);
            new UploadTrail(context, new UploadInterface() {
                @Override
                public void OnUploadStarted() {

                }

                @Override
                public void OnUploadFinished(boolean isSuccessfull, JSONObject uploadResult) {
                    Log.d("trailstat", "OnUploadFinished: " + isSuccessfull);
                }
            }, null).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveAndUpdateDataToServer(Context context, Map<String, Object> attributeValueMap, Feature targetFeature, int position, String status) {
        try {
            JSONObject jsonObject = DeliveryDataModel.getInstance().getTraversalEntity()
                    .updateFeatureTraversal(String.valueOf(targetFeature.getFeatureId()), true, context);
            JSONObject resulJson = EditAndUpload.perform(context,
                    AppConstants.EDIT,
                    DeliveryDataModel.getInstance().getTraversalEntity(),
                    attributeValueMap,
                    null,
                    DeliveryDataModel.getInstance().getTraversalEntity().getFeatureTable(),
                    null,
                    String.valueOf(targetFeature.getFeatureId()),
                    String.valueOf(targetFeature.getFeatureLabel()),
                    null,
                    null,
                    "", new UploadInterface() {
                        @Override
                        public void OnUploadStarted() {

                        }

                        @Override
                        public void OnUploadFinished(boolean isSuccessfull, JSONObject uploadResult) {

                        }
                    }, true);

            if (resulJson != null) {
                Log.d("resjson", "saveAndUpdateDataToServer: " + resulJson);
                if (resulJson.has("status")) {
                    if (resulJson.getString("status").equalsIgnoreCase("success")) {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                if(status.equals(DELIVER)){
                                    Toast.makeText(context, "Delivery Successful", Toast.LENGTH_SHORT).show();
                                }else{
                                    Toast.makeText(context, "Skipped", Toast.LENGTH_SHORT).show();
                                }

                            }
                        });
                        if (DeliveryDataModel.getInstance().getTargetFeature() != null) {
                            if (targetFeature.getFeatureId().equals(DeliveryDataModel.getInstance().getTargetFeature().getFeatureId())) {
                                ideliveryActivityView.onTargetFeatureUpdated(ModeUtility.SINGLE, position);
                            } else {
                                ideliveryActivityView.onTargetFeatureUpdated(ModeUtility.MULTI, position);
                            }
                        } else {
                            ideliveryActivityView.onTargetFeatureUpdated(ModeUtility.SINGLE, position);
                        }
                    } else {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
//                                Toast.makeText(context, "Delivery Failed", Toast.LENGTH_SHORT).show();
                                ideliveryActivityView.hideProgressDialog();
                                if(status.equals(DELIVER)){
                                    ideliveryActivityView.showError("Delivery Failed, Something went wrong Please contact Admin");
                                }else{
                                    ideliveryActivityView.showError("Skipped Failed, Something went wrong Please contact Admin");
                                }

                            }
                        });
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
