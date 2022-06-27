package com.sixsimplex.phantom.Phantom1.app.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.sixsimplex.phantom.Phantom1.appfragment.dashboard.DashBoardFragment;
import com.sixsimplex.phantom.Phantom1.appfragment.dashboard.DashBoardFragmentPresenter;
import com.sixsimplex.phantom.Phantom1.appfragment.delproduct.ProductSheetFragment;
import com.sixsimplex.phantom.Phantom1.mode.ModeUtility;
import com.sixsimplex.phantom.Phantom1.utils.ConnectivityHelper;
import com.sixsimplex.phantom.R;
import com.sixsimplex.phantom.revelocore.data.Feature;
import com.sixsimplex.phantom.Phantom1.app.IdeliveryActivityView;
import com.sixsimplex.phantom.Phantom1.app.presenter.DeliveryPresenter;
import com.sixsimplex.phantom.Phantom1.appfragment.formsheet.FormSheetFragment;
import com.sixsimplex.phantom.Phantom1.appfragment.home.IhomeFragmentView;
import com.sixsimplex.phantom.Phantom1.appfragment.profile.ProfilePresenter;
import com.sixsimplex.phantom.Phantom1.deliveryservice.DeliveryService;
import com.sixsimplex.phantom.Phantom1.model.DeliveryDataModel;
import com.sixsimplex.phantom.Phantom1.trip.ITripCallback;
import com.sixsimplex.phantom.Phantom1.trip.TripItemsForm;
import com.sixsimplex.phantom.Phantom1.appfragment.home.HomeFragment;
import com.sixsimplex.phantom.Phantom1.appfragment.map.MapFragment;
import com.sixsimplex.phantom.Phantom1.appfragment.map.MapFragmentPresenter;
import com.sixsimplex.phantom.Phantom1.appfragment.profile.ProfileFragment;
import com.sixsimplex.phantom.revelocore.liveLocationUpdate.SendLocationToServerService;
import com.sixsimplex.phantom.revelocore.util.AppFolderStructure;
import com.sixsimplex.phantom.revelocore.util.RuntimePermission;
import com.sixsimplex.phantom.revelocore.util.SystemUtils;
import com.sixsimplex.phantom.revelocore.util.bottom_sheet.InfoBottomSheet;
import com.sixsimplex.phantom.revelocore.util.bottom_sheet.InfoBottomSheetInterface;
import com.sixsimplex.phantom.revelocore.util.constants.AppConstants;
import com.sixsimplex.phantom.revelocore.util.locationModule.GetUserLocation;
import com.sixsimplex.phantom.revelocore.util.locationModule.LocationReceiverInterface;
import com.sixsimplex.phantom.revelocore.util.log.DeviceInfo;
import com.sixsimplex.phantom.revelocore.util.sharedPreference.UserInfoPreferenceUtility;
import com.sixsimplex.revelologger.ReveloLogger;
import com.vividsolutions.jts.geom.Geometry;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class DeliveryMainActivity extends AppCompatActivity implements IhomeFragmentView, IdeliveryActivityView, InfoBottomSheetInterface {

    BottomNavigationView bottomNavigationView;

    FrameLayout flFragment, sliderContainer;
    DeliveryPresenter deliveryPresenter;
    Fragment activeFragment;
    ConstraintLayout buttonsLayoutRl;
    //    Button startDeliveryBtn;
    Button pauseDeliveryBtn, deliverBtn;
    //       Button showNextBtn;
    Location locationL;
    ServiceConnection deliveryServiceConnection;
    boolean mServiceConnected = false;
    boolean mBound = false;
    GetUserLocation getUserLocation = null;
    Menu mapOptionMenu = null;
    Dialog progressDialog;
    InfoBottomSheet enableLocationBottomSheet;
    InfoBottomSheet enableInternetBottomSheet;
    private DeliveryService deliveryService;
    private ReveloLogger reveloLogger;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private LocationListener mLocationListener;
    private LocationManager mLocationManager;

    ConnectivityManager connectivityManager;
    ConnectivityManager.NetworkCallback networkCallback;

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_main);
        RuntimePermission.checkPermissions(this);

        deliveryPresenter = new DeliveryPresenter(this);
        init();
        createTraversalFeatureList();
        MapFragmentPresenter mapFragmentPresenter = new MapFragmentPresenter();
        ProfilePresenter profileFragmentPresenter = new ProfilePresenter();
//        HomeFragmentPresenter homeFragmentPresenter = new HomeFragmentPresenter(this);
        DashBoardFragmentPresenter dashBoardFragmentPresenter = new DashBoardFragmentPresenter();

        //        Hide the actionbar
        startService(new Intent(DeliveryMainActivity.this, SendLocationToServerService.class));
        configureNavigationPanel(dashBoardFragmentPresenter, mapFragmentPresenter /*homeFragmentPresenter*/, profileFragmentPresenter);
        DeliveryServiceUiConnections();
        DeliveryButtonOperation();


        getUserLocation = new GetUserLocation(this, null, null, null, null, new LocationReceiverInterface() {
            @Override
            public void onLocationChange(Location location) {
                onLocationChanged(location);

            }

            @Override
            public void onProviderDisable(String provider) {
            }

            @Override
            public void onProviderEnable(String provider) {

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }
        });
        if (getUserLocation != null) {
            locationL = getUserLocation.getUserCurrentLocation();
        }
        reveloLogger = ReveloLogger.getInstance();

        reveloLogger.initialize(this, UserInfoPreferenceUtility.getUserName(), UserInfoPreferenceUtility.getSurveyName(),
                AppFolderStructure.getLogFolderPath(this),
                DeviceInfo.getDeviceInfo(this));

        locationListener();
//        networkConnectivityCheck();
    }

    private void networkConnectivityCheck() {
        try{
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                connectivityManager= (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
                networkCallback  =new ConnectivityManager.NetworkCallback(){
                    @Override
                    public void onAvailable(@NonNull Network network) {
                        super.onAvailable(network);
                        Log.d("netcheck", "onAvailable: "+"available");
                        hideConnectivityDialog();

                    }

                    @Override
                    public void onLost(@NonNull Network network) {
                        super.onLost(network);
                        Log.d("netcheck", "onAvailable: "+"lost");
                       showConnectivityDialog();
                    }
                };
                connectivityManager.registerDefaultNetworkCallback(networkCallback);
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private void showConnectivityDialog() {
        try{
            hideConnectivityDialog();
            if (!ConnectivityHelper.isConnectedToInternet(DeliveryMainActivity.this)) {
                if (enableInternetBottomSheet != null) {
                    if (!enableInternetBottomSheet.isVisible()) {
                        enableInternetBottomSheet = InfoBottomSheet.geInstance(DeliveryMainActivity.this,
                                "Retry", null, "Connectivity Lost", "Please turn on your mobile data",
                                AppConstants.LOST_CONNECTION_RETRY, 1, "");

                        enableInternetBottomSheet.setCancelable(false);
                        enableInternetBottomSheet.show(DeliveryMainActivity.this.getSupportFragmentManager(), "Location Request");
                    }
                } else {
                    enableInternetBottomSheet = InfoBottomSheet.geInstance(DeliveryMainActivity.this,
                            "Retry", null, "Connectivity Lost", "Please turn on your mobile data",
                            AppConstants.LOST_CONNECTION_RETRY, 1, "");

                    enableInternetBottomSheet.setCancelable(false);
                    enableInternetBottomSheet.show(DeliveryMainActivity.this.getSupportFragmentManager(), "Location Request");
                }
            }else{
                hideConnectivityDialog();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    private void hideConnectivityDialog() {
        try {
            if (enableInternetBottomSheet != null) {
                enableInternetBottomSheet.dismiss();
                enableInternetBottomSheet=null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void locationListener() {
        mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {

            }

            @Override
            public void onProviderDisabled(@NonNull String provider) {
                enableLocationDialog();
            }

            @Override
            public void onProviderEnabled(@NonNull String provider) {
                disableLocationDialog();
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }


        };

        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
    }


    public DeliveryPresenter getDeliveryPresenter() {
        return deliveryPresenter;
    }


    private void onLocationChanged(Location location) {
        if (location != null) {
            locationL = location;
        }
    }


    private void init() {
//        startDeliveryBtn = findViewById(R.id.start_delivery);
        pauseDeliveryBtn = findViewById(R.id.pause_delivery_button);
        deliverBtn = findViewById(R.id.deliver_btn);
//        showNextBtn = findViewById(R.id.shownext_btn);
        buttonsLayoutRl = findViewById(R.id.buttonTabs);
        sliderContainer = findViewById(R.id.slide_button_container);

//        SlideButton slideButton = new SlideButton(DeliveryMainActivity.this);
//        slideButton.setThumb(ContextCompat.getDrawable(DeliveryMainActivity.this,R.drawable.marker_cluster));
//        slideButton.setText("Start Delivery");
//        slideButton.setBackgroundResource(R.drawable.back_slide_button);
//        sliderContainer.addView(slideButton);


    }

    @Override
    @SuppressLint("SetTextI18n")
    public void showProgressDialog(String progressText) {
        DeliveryMainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    progressDialog = new Dialog(DeliveryMainActivity.this);
                    progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                    progressDialog.setTitle(null);
                    progressDialog.setCancelable(false);
                    LayoutInflater inflater = DeliveryMainActivity.this.getLayoutInflater();
                    @SuppressLint("InflateParams") View content = inflater.inflate(R.layout.progress_dialog, null);
                    progressDialog.setContentView(content);
                    TextView progressTextView = (TextView) content.findViewById(R.id.progressText);
                    progressTextView.setText(progressText);
                    progressDialog.show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }


    @Override
    public void hideProgressDialog() {
        DeliveryMainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (progressDialog != null) {
                        if (progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }


    private void createTraversalFeatureList() {
        showProgressDialog("Fetching Today's task...");
        deliveryPresenter.createTraversalFeatureListAndStoreInDataModelAndUpdate(DeliveryMainActivity.this);
    }

    private void configureNavigationPanel(DashBoardFragmentPresenter dashBoardFragmentPresenter, MapFragmentPresenter mapFragmentPresenter/* HomeFragmentPresenter homeFragmentPresenter*/, ProfilePresenter profileFragmentPresenter) {
        flFragment = findViewById(R.id.flFragment);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);


        Fragment dashboardFragment = new DashBoardFragment(DeliveryMainActivity.this, dashBoardFragmentPresenter);
//        Fragment homeFragment = new HomeFragment(DeliveryMainActivity.this, homeFragmentPresenter, DeliveryMainActivity.this);
        Fragment mapFragment = new MapFragment(DeliveryMainActivity.this, mapFragmentPresenter, DeliveryMainActivity.this);
        Fragment profileFragment = new ProfileFragment(DeliveryMainActivity.this, profileFragmentPresenter);
        activeFragment = dashboardFragment;

        getSupportFragmentManager().beginTransaction().add(R.id.flFragment, dashboardFragment, "dashboardfragmenttag").commit();
//        getSupportFragmentManager().beginTransaction().add(R.id.flFragment, homeFragment, "homefragmenttag").hide(homeFragment).commit();
        getSupportFragmentManager().beginTransaction().add(R.id.flFragment, mapFragment, "mapfragmenttag").hide(mapFragment).commit();
        getSupportFragmentManager().beginTransaction().add(R.id.flFragment, profileFragment, "profilefragmenttag").hide(profileFragment).commit();


        //    transition between pages after clicking on the button of the bottom navigation bar
        BottomNavigationView.OnNavigationItemSelectedListener navListner =
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {


                        switch (item.getItemId()) {

                            case R.id.dashboard_d:
                                getSupportFragmentManager().beginTransaction().hide(activeFragment).show(dashboardFragment).commit();
                                activeFragment = dashboardFragment;
                                FrameLayout.LayoutParams layoutParamsD = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                                flFragment.setLayoutParams(layoutParamsD);
                                break;

//                            case R.id.home_d:
//
//                                getSupportFragmentManager().beginTransaction().hide(activeFragment).show(homeFragment).commit();
//                                activeFragment = homeFragment;
//                                FrameLayout.LayoutParams layoutParamsH = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
//                                flFragment.setLayoutParams(layoutParamsH);
//                                break;


                            case R.id.map_d:
                                getSupportFragmentManager().beginTransaction().hide(activeFragment).show(mapFragment).commit();
                                activeFragment = mapFragment;
                                FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                                flFragment.setLayoutParams(layoutParams);
                                break;


                            case R.id.profile_d:
                                getSupportFragmentManager().beginTransaction().hide(activeFragment).show(profileFragment).commit();
                                activeFragment = profileFragment;
                                break;

                        }
                        return true;
                    }
                };
        bottomNavigationView.setOnNavigationItemSelectedListener(navListner);
        bottomNavigationView.setSelectedItemId(R.id.dashboard_d);
    }


    private void DeliveryButtonOperation() {
//        startDeliveryBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                try{
//                    onStartClick();
//                }catch (Exception e){
//                    e.printStackTrace();
//                }
//            }
//        });
//        pauseDeliveryBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
////                onStopDelivery();
//
//            }
//        });
//        deliverBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                try {
//
//                    deliveryPresenter.openInfoFillForm(DeliveryMainActivity.this, ModeUtility.SINGLE);
//
//
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        });

//        showNextBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                onShowNextClick();
//            }
//        });
    }

    public void onShowNextClick() {
        try {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    deliveryPresenter.showNextDelivery(DeliveryMainActivity.this);
                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onStartClick() {
        try {
            showProgressDialog("Starting...");
            if (getUserLocation != null && locationL == null) {
                locationL = getUserLocation.getUserCurrentLocation();
            }
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (deliveryPresenter.hasNextTraversingFeature()) {
                        Feature feature = deliveryPresenter.getCurrentlyTraversingFeature();
                        if (feature != null) {
                            startDelivery(feature);
                        }
                    }
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            onStart();
                        }
                    });
                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startDelivery(Feature targetFeature) {
        try {
            deliveryPresenter.startDeliveryBtn(DeliveryMainActivity.this, locationL, targetFeature);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private void onStopDelivery() {
        try {
            showProgressDialog("Stoping...");
            if (mBound) {
                unbindService(deliveryServiceConnection);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        if (locationL == null) {
            if (getUserLocation != null) {
                locationL = getUserLocation.getUserCurrentLocation();
            }
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                deliveryPresenter.stopDelivery(DeliveryMainActivity.this, locationL);
            }
        }).start();

    }

    private void DeliveryServiceUiConnections() {
        try {
            deliveryServiceConnection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                    DeliveryService.LocalBinderD binder = (DeliveryService.LocalBinderD) iBinder;
                    deliveryService = binder.getService();


                    final androidx.lifecycle.Observer<Boolean> deliveryStateResult = new androidx.lifecycle.Observer<Boolean>() {
                        @Override
                        public void onChanged(Boolean deliveryState) {
                            MapFragment fragment = (MapFragment) getSupportFragmentManager().findFragmentByTag("mapfragmenttag");
                            DeliveryDataModel.getInstance().setDeliveryState(deliveryState);
                            if (deliveryState != null) {
                                try {
                                    configureUi(deliveryState);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } else {
                                configureUi(false);
                            }

                            if (deliveryState != null) {
                                if (fragment != null) {
                                    fragment.updateMapFragmentUIIfDeliveryIsOnGoing(deliveryState);
                                    fragment.configureStartButtonUi(deliveryState);
                                }
                            } else {
                                if (fragment != null) {
                                    fragment.updateMapFragmentUIIfDeliveryIsOnGoing(null);
                                    fragment.configureStartButtonUi(false);
                                }
                            }
                        }
                    };
                    deliveryService.getDeliveryOngoingStatus().observe(DeliveryMainActivity.this, deliveryStateResult);


                    final androidx.lifecycle.Observer<JSONObject> deliveryRouteResult = new androidx.lifecycle.Observer<JSONObject>() {
                        @Override
                        public void onChanged(JSONObject route) {
                            DeliveryDataModel.getInstance().setDeliveryRoute(route);
                            MapFragment fragment = (MapFragment) getSupportFragmentManager().findFragmentByTag("mapfragmenttag");
                            if (route != null) {
                                if (fragment != null) {
                                    fragment.showRouteOnMap(route);
                                }
                            } else {
                                if (fragment != null) {
                                    fragment.removeDirectionFromMap();
                                }
                            }
                        }
                    };
                    deliveryService.getRouteJson().observe(DeliveryMainActivity.this, deliveryRouteResult);


                    final androidx.lifecycle.Observer<JSONObject> riderLocateResult = new androidx.lifecycle.Observer<JSONObject>() {
                        @Override
                        public void onChanged(JSONObject riderLocationState) {


//                            deliverBtnConfigration(riderLocationState);

                            MapFragment mapfragment = (MapFragment) getSupportFragmentManager().findFragmentByTag("mapfragmenttag");
                            if (mapfragment != null) {
                                mapfragment.configureShowNextButton(riderLocationState);
                            }
                        }
                    };
                    deliveryService.getRiderLocationState().observe(DeliveryMainActivity.this, riderLocateResult);


                    final androidx.lifecycle.Observer<Feature> targetFeatureResult = new androidx.lifecycle.Observer<Feature>() {
                        @Override
                        public void onChanged(Feature targetFeatureData) {


                            if (targetFeatureData != null) {
                                DeliveryDataModel.getInstance().setTargetFeature(targetFeatureData);
                            } else {
                                DeliveryDataModel.getInstance().setTargetFeature(null);
                            }

                            MapFragment mapfragment = (MapFragment) getSupportFragmentManager().findFragmentByTag("mapfragmenttag");
                            if (targetFeatureData != null) {
                                if (mapfragment != null) {
                                    mapfragment.showTargetTv(targetFeatureData);
                                    mapfragment.updateTargetFeatureUiOnMap(targetFeatureData);
                                    mapfragment.highlightTargetFeature(targetFeatureData);
                                }
                            } else {
                                if (mapfragment != null) {
                                    mapfragment.showTargetTv(null);
                                    mapfragment.updateTargetFeatureUiOnMap(null);
                                    mapfragment.highlightTargetFeature(null);
                                }
                            }

                            HomeFragment homefragment = (HomeFragment) getSupportFragmentManager().findFragmentByTag("homefragmenttag");
                            if (targetFeatureData != null) {
                                if (homefragment != null) {
                                    homefragment.highlightTargetFeature(targetFeatureData);
                                }
                            } else {
                                if (homefragment != null) {
                                    homefragment.highlightTargetFeature(null);
                                }
                            }
                        }
                    };
                    deliveryService.getTargetFeatureLive().observe(DeliveryMainActivity.this, targetFeatureResult);


                    final androidx.lifecycle.Observer<List<Feature>> inRangeFeatureList = new androidx.lifecycle.Observer<List<Feature>>() {
                        @Override
                        public void onChanged(List<Feature> inRangeTargetList) {
                            DeliveryDataModel.getInstance().setInRangeFeature(inRangeTargetList);
                            MapFragment mapfragment = (MapFragment) getSupportFragmentManager().findFragmentByTag("mapfragmenttag");
                            if (inRangeTargetList != null) {
                                if (mapfragment != null) {
                                    mapfragment.setSelectableFeature(inRangeTargetList);
                                    mapfragment.performAutoSelectionFunctionalityInListView(DeliveryDataModel.getInstance().getInRangeFeature());
                                }
                            } else {
                                if (mapfragment != null) {
                                    mapfragment.setSelectableFeature(null);
                                    mapfragment.performAutoSelectionFunctionalityInListView(new ArrayList<>());
                                }
                            }

                            HomeFragment homefragment = (HomeFragment) getSupportFragmentManager().findFragmentByTag("homefragmenttag");
                            if (inRangeTargetList != null) {
                                if (homefragment != null) {
                                    homefragment.performAutoSelectionFunctionalityInListView(DeliveryDataModel.getInstance().getInRangeFeature());
                                }
                            } else {
                                if (homefragment != null) {
                                    homefragment.performAutoSelectionFunctionalityInListView(new ArrayList<>());
                                }
                            }

                        }
                    };
                    deliveryService.getInRangeFeature().observe(DeliveryMainActivity.this, inRangeFeatureList);

                    final androidx.lifecycle.Observer<Geometry> bufferGeom = new androidx.lifecycle.Observer<Geometry>() {
                        @Override
                        public void onChanged(Geometry bufferGeometry) {
                            MapFragment mapfragment = (MapFragment) getSupportFragmentManager().findFragmentByTag("mapfragmenttag");
                            if (bufferGeometry != null) {
                                if (mapfragment != null) {
                                    mapfragment.showBufferOnMap(bufferGeometry);
                                }
                            } else {
                                if (mapfragment != null) {

                                    mapfragment.removeExistingBufferOnMap();
                                }
                            }

                        }
                    };
                    deliveryService.getBufferGeom().observe(DeliveryMainActivity.this, bufferGeom);

                    mBound = true;
                }

                @Override
                public void onServiceDisconnected(ComponentName componentName) {
                    mBound = false;
                }
            };
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

  /*  private void deliverBtnConfigration(JSONObject riderLocationState) {
        try {
            if(showNextBtn != null){
                if(riderLocationState != null){
                    if (riderLocationState.has("deliverobject")) {
                        boolean isOrderDelivered = riderLocationState.getBoolean("deliverobject");
                        boolean isReachTarget = riderLocationState.getBoolean("reachTarget");
                        if (isReachTarget && !isOrderDelivered) {
//                    deliverBtn.setVisibility(View.VISIBLE);
                            showNextBtn.setVisibility(View.GONE);
                        } else if (isReachTarget && isOrderDelivered) {
                        deliverBtn.setVisibility(View.GONE);

                            if (mapFragmentPresenter.hasNextTraversingFeature()) {
                                showNextBtn.setVisibility(View.VISIBLE);
                            } else {
                                showNextBtn.setVisibility(View.GONE);
                            }
                        } else if (!isReachTarget && !isOrderDelivered) {
                        deliverBtn.setVisibility(View.GONE);
                            showNextBtn.setVisibility(View.GONE);
                        } else if (!isReachTarget && isOrderDelivered) {
                        deliverBtn.setVisibility(View.GONE);


                            if (mapFragmentPresenter.hasNextTraversingFeature()) {
                                showNextBtn.setVisibility(View.VISIBLE);
                            } else {
                                showNextBtn.setVisibility(View.GONE);
                            }
                        }
                    } else {
                        showNextBtn.setVisibility(View.GONE);
                    deliverBtn.setVisibility(View.GONE);
                    }
                }else{
                    showNextBtn.setVisibility(View.GONE);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }*/

    public void configureUi(Boolean deliveryState) {
        try {
            if (!deliveryState) {
//                if (startDeliveryBtn != null) {
//                    if (deliveryPresenter.hasNextTraversingFeature()) {
//                        if (deliveryPresenter.getCurrentlyTraversingFeature() != null) {
//                            startDeliveryBtn.setVisibility(View.VISIBLE);
//                        } else {
//                            startDeliveryBtn.setVisibility(View.GONE);
//                        }
//                    } else {
//                        startDeliveryBtn.setVisibility(View.GONE);
//                    }
//                    ((View) startDeliveryBtn.getParent()).setBackgroundColor(getResources().getColor(android.R.color.transparent));
//                }
                showHideStopItemInMenu(false);
            } else {
//                if (startDeliveryBtn != null) {
//                    startDeliveryBtn.setVisibility(View.GONE);
//                    ((View) startDeliveryBtn.getParent()).setBackgroundColor(0);
//                }
                showHideStopItemInMenu(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showHideStopItemInMenu(boolean b) {
        try {
            if (mapOptionMenu != null) {
                if (mapOptionMenu.size() > 0) {
                    MenuItem item = mapOptionMenu.getItem(0);
                    item.setVisible(b);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        try {
            mapOptionMenu = menu;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        try {
            if (item.getItemId() == R.id.stop_delivery) {
                onStopDelivery();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onStart() {
        super.onStart();
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 2, mLocationListener);
        networkConnectivityCheck();
        showConnectivityDialog();
        if (DeliveryService.isDeliveryServiceRunning((ActivityManager) getSystemService(ACTIVITY_SERVICE))) {
            mServiceConnected = bindService(new Intent(DeliveryMainActivity.this, DeliveryService.class), deliveryServiceConnection, Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    protected void onStop() {
        disableLocationDialog();
        hideConnectivityDialog();
        super.onStop();
        if (mLocationListener != null && mLocationManager != null) {
            mLocationManager.removeUpdates(mLocationListener);
        }


        try {
            if (mBound) {
                unbindService(deliveryServiceConnection);
                mBound = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();

    }

    @Override
    public void onTraversalDataFetchComplete() {
        try {
            hideProgressDialog();
            HomeFragment homeFragment = (HomeFragment) getSupportFragmentManager().findFragmentByTag("homefragmenttag");
            if (homeFragment != null) {
                homeFragment.notifyListData(-1);
            }
            MapFragment mapFragment = (MapFragment) getSupportFragmentManager().findFragmentByTag("mapfragmenttag");
            if (mapFragment != null) {
                //after fetching features complete show the all the features on map
                mapFragment.showDeliveryFeatureOnMap();
                mapFragment.notifyListData(-1);
                //below code is used to show start button after completion of fetching af all valid tasks.
                if (DeliveryDataModel.getInstance().getDeliveryState() != null) {
                    mapFragment.configureStartButtonUi(DeliveryDataModel.getInstance().getDeliveryState());
                }
            }

            DashBoardFragment dashBoardFragment = (DashBoardFragment) getSupportFragmentManager().findFragmentByTag("dashboardfragmenttag");
            if (dashBoardFragment != null) {
                dashBoardFragment.updateDashBoard();
            }
            deliveryPresenter.initAndStartTrail(DeliveryMainActivity.this);
//            if (deliveryPresenter.hasNextTraversingFeature()) {
//                if (deliveryPresenter.getCurrentlyTraversingFeature() != null) {
//                    if (startDeliveryBtn != null) {
//                        startDeliveryBtn.setVisibility(View.VISIBLE);
//                    }
//                } else {
//                    if (startDeliveryBtn != null) {
//                        startDeliveryBtn.setVisibility(View.GONE);
//                    }
//                }
//            } else {
//                if (startDeliveryBtn != null) {
//                    startDeliveryBtn.setVisibility(View.GONE);
//                }
//            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void performDeliveryActionForFeature(Feature targetFeature, String mode) {
        FormSheetFragment bottomSheetDialog = new FormSheetFragment(DeliveryMainActivity.this, DeliveryMainActivity.this, targetFeature, mode);
        bottomSheetDialog.show(getSupportFragmentManager(), "Bottom Sheet Dialog Fragment");
    }

    @Override
    public void showProductsUpdateDialogForTargetFeature(Feature targetFeature) {
        ProductSheetFragment productSheetFragment = new ProductSheetFragment(DeliveryMainActivity.this, targetFeature);
        productSheetFragment.setCancelable(false);
        productSheetFragment.show(getSupportFragmentManager(), "Product Sheet Dialog Fragment");
    }


    @Override
    public void onTargetFeatureUpdated(String mode, int position) {
        deliveryPresenter.updateTraversalFeatureList(DeliveryMainActivity.this, position);
        deliveryPresenter.markAsDeliver(DeliveryMainActivity.this, mode);
        if (mode.equals(ModeUtility.MULTI)) {
            MapFragment mapFragment = (MapFragment) getSupportFragmentManager().findFragmentByTag("mapfragmenttag");
            if (mapFragment != null) {
                mapFragment.deSelectTheSelectingFeature();
            }
        }
    }

    @Override
    public void updateHomeAndMapUI(int position) {
        try {
            HomeFragment homeFragment = (HomeFragment) getSupportFragmentManager().findFragmentByTag("homefragmenttag");
            if (homeFragment != null) {
                homeFragment.notifyListData(position);
            }
            MapFragment mapFragment = (MapFragment) getSupportFragmentManager().findFragmentByTag("mapfragmenttag");
            if (mapFragment != null) {
                mapFragment.showDeliveryFeatureOnMap();
                mapFragment.notifyListData(position);
            }
            DashBoardFragment dashBoardFragment = (DashBoardFragment) getSupportFragmentManager().findFragmentByTag("dashboardfragmenttag");
            if (dashBoardFragment != null) {
                dashBoardFragment.updateDashBoard();
            }
            hideProgressDialog();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void showTripItemSelectionDialog(ITripCallback iTripCallback) {
        hideProgressDialog();
        DeliveryMainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TripItemsForm tripItemsForm = new TripItemsForm(DeliveryMainActivity.this, getUserLocation.getUserCurrentLocation(), iTripCallback, DeliveryMainActivity.this);
                tripItemsForm.show(getSupportFragmentManager(), "trip items form view");
            }
        });
    }


    public void showHideButtonTabLayout(boolean show) {
        if (buttonsLayoutRl != null) {
            if (!show) {
                buttonsLayoutRl.setVisibility(View.GONE);
            } else {
                buttonsLayoutRl.setVisibility(View.VISIBLE);
            }
        }
    }

    private void enableLocationDialog() {
        try {
            if (getUserLocation != null) {
                boolean isLocationEnable = getUserLocation.checkLocationIsEnable();
                if (!isLocationEnable) {
                    if (enableLocationBottomSheet != null) {
                        if (!enableLocationBottomSheet.isVisible()) {
                            enableLocationBottomSheet = InfoBottomSheet.geInstance(DeliveryMainActivity.this,
                                    "Enable Location", null, "Please Turn On Location", "You can not access the application without location",
                                    AppConstants.LOCATION_REQUEST, 1, "");

                            enableLocationBottomSheet.setCancelable(false);
                            enableLocationBottomSheet.show(DeliveryMainActivity.this.getSupportFragmentManager(), "Location Request");
                        }
                    } else {
                        enableLocationBottomSheet = InfoBottomSheet.geInstance(DeliveryMainActivity.this,
                                "Enable Location", null, "Please Turn On Location", "You can not access the application without location",
                                AppConstants.LOCATION_REQUEST, 1, "");

                        enableLocationBottomSheet.setCancelable(false);
                        enableLocationBottomSheet.show(DeliveryMainActivity.this.getSupportFragmentManager(), "Location Request");
                    }
                }else{
                    disableLocationDialog();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void disableLocationDialog() {
        try {
            if (enableLocationBottomSheet != null) {
                enableLocationBottomSheet.dismiss();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onOkInfoBottomSheetResult(int requestCode, int errorCode, String jurisdictions) {
        if (requestCode == AppConstants.LOCATION_REQUEST) {
            Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(myIntent);
        }else if(requestCode ==AppConstants.LOST_CONNECTION_RETRY){
            if(ConnectivityHelper.isConnectedToInternet(DeliveryMainActivity.this)){
                hideConnectivityDialog();
            }else{
                hideConnectivityDialog();
                showConnectivityDialog();
            }
        }
    }

    @Override
    public void onCancelOkBottomSheetResult(int requestCode, int errorCode) {

    }


    @Override
    public void onBackPressed() {
//        boolean isDeselectionDone = false;
        boolean isDeselectionDone = true;
//        MapFragment mapFragment = (MapFragment) getSupportFragmentManager().findFragmentByTag("mapfragmenttag");
//        if (mapFragment != null) {
//            if (mapFragment.isVisible()) {
//                isDeselectionDone = mapFragment.deSelectTheSelectingFeature();
//            }
//        }
        if (!isDeselectionDone) {
            if (getFragmentManager().getBackStackEntryCount() != 0) {
                getFragmentManager().popBackStack();

            } else {
                super.onBackPressed();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DeliveryDataModel.getInstance().clearAll();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void drawTrail(String geometryGeoJsonStr) {
        try {
            MapFragment fragment = (MapFragment) getSupportFragmentManager().findFragmentByTag("mapfragmenttag");
            if (fragment != null) {
                fragment.showTrailOnMap(geometryGeoJsonStr);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void drawStop(String geometryGeoJsonStr) {
        try{
            MapFragment fragment = (MapFragment) getSupportFragmentManager().findFragmentByTag("mapfragmenttag");
            if (fragment != null) {
                fragment.showStopOnMap(geometryGeoJsonStr);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void showError(String message) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    SystemUtils.showOkDialogBox(message,DeliveryMainActivity.this);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
    }
}