package com.sixsimplex.phantom.Phantom1.app.presenter;

import static android.content.Context.ACTIVITY_SERVICE;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.sixsimplex.phantom.Phantom1.mode.ModeUtility;
import com.sixsimplex.phantom.Phantom1.trail.LocationReceiver;
import com.sixsimplex.phantom.revelocore.conceptModel.CMEntity;
import com.sixsimplex.phantom.revelocore.conceptModel.CMUtils;
import com.sixsimplex.phantom.Phantom1.app.IdeliveryActivityView;
import com.sixsimplex.phantom.Phantom1.app.view.DeliveryMainActivity;
import com.sixsimplex.phantom.Phantom1.deliveryservice.DeliveryService;
import com.sixsimplex.phantom.Phantom1.model.DeliveryDataModel;
import com.sixsimplex.phantom.Phantom1.trip.ITripCallback;
import com.sixsimplex.phantom.Phantom1.trip.Trip;
import com.sixsimplex.phantom.revelocore.data.Feature;
import com.sixsimplex.phantom.revelocore.geopackage.geopackage.DbRelatedConstants;
import com.sixsimplex.phantom.revelocore.geopackage.geopackage.GeoPackageManagerAgent;
import com.sixsimplex.phantom.revelocore.geopackage.tableUtil.EditMetaDataTable;
import com.sixsimplex.phantom.revelocore.geopackage.tableUtil.ReDbTable;
import com.sixsimplex.phantom.revelocore.graph.concepmodelgraph.CMGraph;
import com.sixsimplex.phantom.revelocore.util.constants.AppConstants;
import com.sixsimplex.phantom.revelocore.util.sharedPreference.UserInfoPreferenceUtility;
import com.sixsimplex.phantom.revelocore.util.sort.ReveloFeatureComparator;
import com.sixsimplex.trail.LocationTacker;
import com.sixsimplex.trail.TrailLatLng;
import com.sixsimplex.trail.TrailLocationRecorderService;

import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class DeliveryPresenter {

    IdeliveryActivityView ideliveryActivityView;
    CMGraph cmGraph;
    FusedLocationProviderClient fusedLocationProviderClient;
    Location locationL = null;
    private Handler mHandler = new Handler(Looper.getMainLooper());


    public DeliveryPresenter(IdeliveryActivityView ideliveryActivityView) {
        this.ideliveryActivityView = ideliveryActivityView;

    }

    public void createTraversalFeatureListAndStoreInDataModelAndUpdate(Context context) {
        try {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        JSONObject graphResult = CMUtils.getCMGraph(context);
                        if (graphResult.has("status") && graphResult.getString("status").equalsIgnoreCase("success")) {
                            cmGraph = (CMGraph) graphResult.get("result");
                        } else {
                            return;
                        }

                        Set<CMEntity> entities = cmGraph.getAllVertices();
                        for (CMEntity cmEntity : entities) {
                            if (cmEntity.getName().equalsIgnoreCase(DeliveryDataModel.traversalEntityName)) {
                                JSONObject sortJsonObject = new JSONObject();
                                try {
                                    sortJsonObject.put("sortBy", ReveloFeatureComparator.TraversalSort);
                                    sortJsonObject.put("sortByProperty", cmEntity.getW9IdProperty());
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    sortJsonObject = null;
                                }
//                            List<Feature> featureList = cmEntity.getSortedFeaturesByQuery(context, true,
//                                    null, null, null, "OR",
//                                    true, false, true, 0,
//                                    -1, false, true, true, sortJsonObject);
                                DeliveryDataModel.getInstance().setFeatureList(
                                        cmEntity.getSortedFeaturesByQuery(context, true,
                                                null, null, null, "OR",
                                                true, false, true, 0,
                                                -1, false, true, true, sortJsonObject)
                                );
                                DeliveryDataModel.getInstance().setTraversalEntity(cmEntity);
                            }
                            if (cmEntity.getName().equalsIgnoreCase(DeliveryDataModel.tripentityname)) {
                                DeliveryDataModel.getInstance().setTripEntity(cmEntity);
                            }
                            if (cmEntity.getName().equalsIgnoreCase(DeliveryDataModel.tripItemEntityname)) {
                                DeliveryDataModel.getInstance().setTripItemEntity(cmEntity);
                            }
                            if (cmEntity.getName().equalsIgnoreCase(DeliveryDataModel.productEntityName)) {
                                DeliveryDataModel.getInstance().setProductEntity(cmEntity);
                            }
                            if (cmEntity.getName().equalsIgnoreCase(DeliveryDataModel.dropOffItemEntityName)) {
                                DeliveryDataModel.getInstance().setDropOffItemEntity(cmEntity);
                            }
                            if(cmEntity.getName().equalsIgnoreCase(DeliveryDataModel.consumerEntityName)){

                                List<String> requiredColumnList = new ArrayList<>();
                                requiredColumnList.add("customerid");
                                requiredColumnList.add("cusname");
                                requiredColumnList.add("mobno");
                                requiredColumnList.add("address");

                                DeliveryDataModel.getInstance().setConsumersList(

                                        cmEntity.getFeatureTable().getallFeaturesList(context,requiredColumnList,false)
                                );
                            }
                        }
                        Trip.initiateTripData(context);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            ideliveryActivityView.onTraversalDataFetchComplete();
                        }
                    });
                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }


    @SuppressLint("LongLogTag")
    public void startDeliveryBtn(DeliveryMainActivity activity, Location location, Feature feature) {
        try {
            locationL = location;
            if (locationL == null) {
                locationL = new Location("");
                locationL.setLatitude(00.00);
                locationL.setLongitude(00.00);
            }
            ITripCallback iTripCallback = new ITripCallback() {
                @Override
                public void onSuccessTripAddUpdate() {
                    try {
                        startDeliveryService(activity, feature);
                        ideliveryActivityView.hideProgressDialog();
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                activity.onStart();
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailureTripAddUpdate(String message) {
                    ideliveryActivityView.hideProgressDialog();
                    ideliveryActivityView.showError(message);
                }
            };
            Trip.addUpdateTrip(activity, Trip.ADD, locationL, iTripCallback, ideliveryActivityView);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startDeliveryService(Context context, Feature feature) {
        try {
            if (feature != null) {
                DeliveryDataModel.getInstance().setTargetFeature(feature);
                Intent intent = new Intent(context, DeliveryService.class);
                intent.putExtra(DeliveryService.ACTION, DeliveryService.ACTION_START_DELIVERY);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent);
                } else {
                    context.startService(intent);
                }
                if (!DeliveryService.isDeliveryServiceRunning((ActivityManager) context.getSystemService(ACTIVITY_SERVICE))) {
                    DeliveryDataModel.getInstance().setTargetFeature(null);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void stopDelivery(Context context, Location location) {
        try {
            locationL = location;
            if (locationL == null) {
                locationL = new Location("");
                locationL.setLatitude(00.00);
                locationL.setLongitude(00.00);
            }
            ITripCallback iTripCallback = new ITripCallback() {
                @Override
                public void onSuccessTripAddUpdate() {
                    context.stopService(new Intent(context, DeliveryService.class));
                    ideliveryActivityView.hideProgressDialog();
                }

                @Override
                public void onFailureTripAddUpdate(String message) {
                    ideliveryActivityView.hideProgressDialog();
                    ideliveryActivityView.showError("Error in TripUpdate, Please Try again letter");
                }
            };
            Trip.addUpdateTrip(context, Trip.EDIT, locationL, iTripCallback, ideliveryActivityView);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @SuppressLint("MissingPermission")
    private void getCurrentLocation(Context context) {

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);
        fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                Location location = task.getResult();
                if (location != null) {
                    locationL = location;
                }
            }
        });
    }

    private String generateAutomaticId() {
        String w9Id = "";
        w9Id = UUID.randomUUID().toString();
        return w9Id;
    }

    public void markAsDeliver(Context context, String mode) {
        Intent intent = new Intent(context, DeliveryService.class);

        if (mode.equals(ModeUtility.SINGLE)) {
            intent.putExtra(DeliveryService.ACTION, DeliveryService.ACTION_COMPLETE_DELIVERY);
        } else if (mode.equals(ModeUtility.MULTI)) {
            intent.putExtra(DeliveryService.ACTION, DeliveryService.ACTION_START_DELIVERY);
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
    }

    public void showNextDelivery(Context context) {
        if(hasNextTraversingFeature()){
            Feature feature=getCurrentlyTraversingFeature();
            if(feature != null){
                startDeliveryService(context,feature);
            }
        }
    }

    public void openInfoFillForm(DeliveryMainActivity deliveryMainActivity, String mode) {
        if (DeliveryDataModel.getInstance().getTargetFeature() != null) {
            ideliveryActivityView.performDeliveryActionForFeature(DeliveryDataModel.getInstance().getTargetFeature(), mode);
        }
    }

    public void updateTraversalFeatureList(Context context, int position) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    JSONObject graphResult = CMUtils.getCMGraph(context);
                    if (graphResult.has("status") && graphResult.getString("status").equalsIgnoreCase("success")) {
                        cmGraph = (CMGraph) graphResult.get("result");
                    } else {
                        return;
                    }
                    Set<CMEntity> entities = cmGraph.getAllVertices();
                    for (CMEntity cmEntity : entities) {
                        if (cmEntity.getName().equalsIgnoreCase(DeliveryDataModel.traversalEntityName)) {
                            JSONObject sortJsonObject = new JSONObject();
                            try {
                                sortJsonObject.put("sortBy", ReveloFeatureComparator.TraversalSort);
                                sortJsonObject.put("sortByProperty", cmEntity.getW9IdProperty());
                            } catch (Exception e) {
                                e.printStackTrace();
                                sortJsonObject = null;
                            }

                            DeliveryDataModel.getInstance().setFeatureList(cmEntity.getSortedFeaturesByQuery(context, true,
                                    null, null, null, "OR",
                                    true, false, true, 0,
                                    -1, false, true, true, sortJsonObject));
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        ideliveryActivityView.updateHomeAndMapUI(position);
                    }
                });
            }

        }).start();
    }

    public boolean hasNextTraversingFeature() {
        boolean has = false;
        try {
            if (DeliveryDataModel.getInstance().getTraversalEntity() != null) {
                if (DeliveryDataModel.getInstance().getTraversalEntity().getTraversalGraph() != null) {
                    if (DeliveryDataModel.getInstance().getTraversalEntity().getTraversalGraph().getCurrentlyTraversingFeature() != null) {
                        has = true;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return has;
    }

    public Feature getCurrentlyTraversingFeature() {
        try {
            if (hasNextTraversingFeature()) {
                JSONObject currentlyTraversingVertex = DeliveryDataModel.getInstance().getTraversalEntity().getTraversalGraph().getCurrentlyTraversingFeature();
                if (currentlyTraversingVertex != null && currentlyTraversingVertex.has("w9id")) {
                    if (currentlyTraversingVertex.getString("w9id") != null && !currentlyTraversingVertex.getString("w9id").isEmpty()) {
                        if (!DeliveryDataModel.getFeatureList().isEmpty()) {
                            for (Feature feature : DeliveryDataModel.getFeatureList()) {
                                if (feature.getFeatureId().equals(currentlyTraversingVertex.getString("w9id"))) {
                                    return feature;
                                }
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void initAndStartTrail(Context context) {
        LocationTacker locationTacker = LocationTacker.getInstance();
        CMEntity trailEntity=null;
        CMEntity trailstopsEntity=null;

        try {
            if(cmGraph != null){
                Set<CMEntity> entities = cmGraph.getAllVertices();
                for (CMEntity cmEntity : entities) {
                    if(cmEntity.getName().equalsIgnoreCase(AppConstants.TRAIL_TABLE_NAME)){
                        trailEntity=cmEntity;
                    } if(cmEntity.getName().equalsIgnoreCase(AppConstants.STOP_TABLE_NAME)){
                        trailstopsEntity=cmEntity;
                    }
                }
            }


            if (! locationTacker.isTrailStarted()) {

                JSONObject propMetaGpkg = DbRelatedConstants.getPropertiesJsonForMetdataGpkg(context);
                JSONObject dataSrcMetaGpkg = DbRelatedConstants.getDataSourceInfoForMetdataGpkg(context);

                JSONObject propDataGpkg = DbRelatedConstants.getPropertiesJsonForDataGpkg(context);

                JSONObject dataSrcDataGpkg =DbRelatedConstants.getDataSourceInfoForDataGpkg(context);

                JSONObject propRedbGpkg = DbRelatedConstants.getPropertiesJsonForREGpkg(context);

                JSONObject dataSrcRedbGpkg = DbRelatedConstants.getDataSourceInfoForREGpkg(context);

                JSONObject datasetw9obre = ReDbTable.getDatasetInfo();
                JSONObject datasetTrails=new JSONObject();
                if(trailEntity != null){
                     datasetTrails =trailEntity.getFeatureTable().getDatasetInfo(false);
                    Log.d("datasetTrails", "initAndStartTrail: "+datasetTrails);
                }
                JSONObject datasetStops = null;
                if(trailstopsEntity != null){
                    datasetStops =trailstopsEntity.getFeatureTable().getDatasetInfo(false);
                    Log.d("datasetStops", "initAndStartTrail: "+datasetStops);
                }

                JSONObject dataseEditMetadata = EditMetaDataTable.getEditMetadataDatasetInfo();

                String dataDbName = UserInfoPreferenceUtility.getDataDbName();
                String metaDbName = UserInfoPreferenceUtility.getMetatdataDbName();
                LocationReceiver locationReceiver = new LocationReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
//                        try {
//                            Toast.makeText(context, "received msg", Toast.LENGTH_SHORT).show();
//                        }
//                        catch (Exception e) {
//                            e.printStackTrace();
//                        }
                        try {
                            if (null != intent && intent.getAction()
                                    .equals(TrailLocationRecorderService.ACTION.UPDATE_LOCATION_BROADCASTACTION_RESULTCODE_LOCATION)) {
                                //send your call to api or do any things with the of location data
                                try {
                                    Bundle resultData = intent
                                            .getBundleExtra(TrailLocationRecorderService.ACTION.UPDATE_LOCATION_BROADCASTACTION_RESULTCODE_LOCATION);
                                    if (resultData.containsKey("trailState") && resultData.getString("trailState").equalsIgnoreCase("started")) {

                                        Log.i("MapActivity",
                                                "CurrentLocationReceiver - location info received for drawing trail and trail state = started.. processing further info");
                                        if (resultData.containsKey("stopTrail")) {
                                            String trailId = resultData.getString("trailId", "");

                                            if (resultData.getBoolean("stopTrail", false)) {
                                                Log.i("MapActivity",
                                                        "CurrentLocationReceiver - location info has stop trail command set to true..updating and saving trail");
                                            }
                                            else {
                                                if (resultData.getBoolean("firstLocation", false)) {
                                                    Log.i("MapActivity",
                                                            "CurrentLocationReceiver - location info has first location flag set true.. simply setting buttons");
                                                    //changeTrailProgressDialogMsg()
//                                                    Toast.makeText(context, "Your location is being recorded.", Toast.LENGTH_SHORT).show();
                                                    // trailStartBtnPos();//trail location received, first location
                                                }
                                                if (resultData.containsKey("trailLatLongs")) {
                                                    Log.i("MapActivity", "CurrentLocationReceiver - location info has trailLatLongs.. drawing trail");
                                                    List<TrailLatLng> trailLatngs = (List<TrailLatLng>) resultData.get("trailLatLongs");
                                                  //  List<List<TrailLatLng>> completetrailLatLongs = (List< List < TrailLatLng >>) resultData.get("completetrailLatLongs");
                                                    String geometryGeoJsonStr = (String) resultData.get("geometryGeoJsonStr");
                                                    //LocationReceiverService.setTrailLatLngs(trailLatngs);
                                                    //  drawTrail(trailId);//get location from broadcast receiver
                                                    //  trailStartBtnPos();//trail location received, latlongs
                                                    // stopTrailProgressDialog();//get location from broadcast receiver
                                                    ideliveryActivityView.drawTrail(geometryGeoJsonStr);
                                                }
                                                if (resultData.containsKey("stopJson")) {
                                                    Log.i("MapActivity", "CurrentLocationReceiver - location info has stopJson.. drawing stop");
                                                    String geometryGeoJsonStr = (String) resultData.get("stopJson");
                                                    ideliveryActivityView.drawStop(geometryGeoJsonStr);
                                                }
                                            }
                                        }
                                    }
                                    else {
                                        Log.e("MapActivity", "CurrentLocationReceiver - is location info received for drawing trail? " + resultData
                                                .containsKey("trailState"));
                                        if (resultData.containsKey("trailState")) {
                                            Log.e("MapActivity",
                                                    "CurrentLocationReceiver - is trail state started = ?" + resultData.getString("trailState")
                                                            .equalsIgnoreCase("started"));
                                        }
                                    }
                                }
                                catch (Exception e) {
                                    Log.e("MapActivity",
                                            "CurrentLocationReceiver - Exception while drawing trail using location.. " + e.getMessage());
                                    e.printStackTrace();
                                }
                            }
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                };
                locationTacker.initialize(context,true, true, 5, 1, false, true,true,true, locationReceiver, UserInfoPreferenceUtility.getUserName(), UserInfoPreferenceUtility.getSurveyName(),
                        "/storage/emulated/0/Android/data/com.sixsimplex.revelo/files/Revelo_3.0/milkdelivery/deliveryboy_1/afproject/Log",
                        "device name:Samsung SM-G615F .. manufacturer:samsung .. isTablet?:false .. android version:27free memory:1991app version:Build version: 3.0.13-deb_03_06_2022__17_49",
                        propDataGpkg, dataSrcDataGpkg, datasetTrails, datasetStops, dataDbName, propMetaGpkg, dataSrcMetaGpkg,
                        dataseEditMetadata, metaDbName, propRedbGpkg, dataSrcRedbGpkg, datasetw9obre);
               locationTacker.initGeoPackages(context,GeoPackageManagerAgent.getDataGeoPackage(context,DbRelatedConstants.getPropertiesJsonForDataGpkg(context)),
                                              GeoPackageManagerAgent.getMetaGeoPackage(context,DbRelatedConstants.getPropertiesJsonForMetdataGpkg(context)),
                                              GeoPackageManagerAgent.getReGeoPackage(context,DbRelatedConstants.getPropertiesJsonForREGpkg(context)));
                locationTacker.start(context);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
