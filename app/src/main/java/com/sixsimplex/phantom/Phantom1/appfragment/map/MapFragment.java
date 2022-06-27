package com.sixsimplex.phantom.Phantom1.appfragment.map;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.sixsimplex.phantom.Phantom1.app.IdeliveryActivityView;
import com.sixsimplex.phantom.Phantom1.cluster.RadiusMarkerCluster;
import com.sixsimplex.phantom.Phantom1.listdelivery.ListDeliveryViewAdapter;
import com.sixsimplex.phantom.R;
import com.sixsimplex.phantom.revelocore.data.Feature;
import com.sixsimplex.phantom.Phantom1.app.view.DeliveryMainActivity;
import com.sixsimplex.phantom.Phantom1.deliveryservice.DeliveryService;
import com.sixsimplex.phantom.Phantom1.direction.DirectionModel;
import com.sixsimplex.phantom.Phantom1.model.DeliveryDataModel;
import com.sixsimplex.phantom.Phantom1.utils.Utils;
import com.sixsimplex.phantom.revelocore.data.GeoJsonUtils;
import com.sixsimplex.phantom.revelocore.graph.concepmodelgraph.CMGraph;
import com.sixsimplex.phantom.revelocore.layer.FeatureLayer;
import com.sixsimplex.phantom.revelocore.util.bottom_sheet.InfoBottomSheet;
import com.sixsimplex.phantom.revelocore.util.bottom_sheet.InfoBottomSheetInterface;
import com.sixsimplex.phantom.revelocore.util.constants.AppConstants;
import com.sixsimplex.phantom.revelocore.util.locationModule.GetUserLocation;
import com.sixsimplex.phantom.revelocore.util.locationModule.LocationReceiverInterface;
import com.vividsolutions.jts.geom.Geometry;

import org.json.JSONArray;
import org.json.JSONObject;
import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.clustering.RadiusMarkerClusterer;
import org.osmdroid.bonuspack.utils.BonusPackHelper;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.FolderOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.Polygon;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay;
import org.osmdroid.views.overlay.infowindow.InfoWindow;
import org.osmdroid.views.overlay.infowindow.MarkerInfoWindow;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;


public class MapFragment extends Fragment implements InfoBottomSheetInterface {
    private static final boolean IS_SHOW_TARGET_TV = false;
    Context context;
    MapFragmentPresenter mapFragmentPresenter;
    GetUserLocation getUserLocation;
    CardView locationCv;
    ImageView locationIv;
    LinearLayout targetFeatureTv;
    TextView targetNameTv, targetAddressTv, targetMobileTv, targetCountTv;
    Toolbar mapToolBar;
//    FolderOverlay featuresOverlay = null;
    FolderOverlay directionFolderOverlay = null;
    Overlay targetOverlay = null;
//    SimpleFastPointOverlay simpleFastLabelOverlay = null;
    FolderOverlay labelOverLay=null;
    Overlay directionGeometryOverlay = null;
    Overlay directionGeometryStartOverlay = null;
    Overlay directionGeometryEndOverlay = null;
    LinkedList<DirectionModel> direcionsDetailsLinkedList = null;
    Handler mHandler = new Handler(Looper.getMainLooper());
    BoundingBox routeBoundingBox = null;
    LinearLayout transparentViewMap;
    List<Overlay> selectedFeatureOverlays = new ArrayList<>();
    RecyclerView delivery_item_list_view;
    ListDeliveryViewAdapter listDeliveryViewAdapter;
    LinearLayoutManager linearLayoutManager;
    IdeliveryActivityView ideliveryActivityView;
    Button startDeliveryBtn, showNextBtn;
    private BottomSheetBehavior deliveryBottomSheet;
    private MapView mapView = null;
    private List<Polyline>  trailPolyLine;

    RadiusMarkerCluster clusterFeatureOverlay=null;

    ImapFragmentView imapFragmentView = new ImapFragmentView() {
        @Override
        public void showFeaturesOnUI(LinkedHashMap<String, FeatureLayer> featureLayerLinkedHashMap, CMGraph cmGraph) {
            if (mapView != null) {
                for (FeatureLayer featureLayer : featureLayerLinkedHashMap.values()) {

                    if (featureLayer != null) {
                        if (clusterFeatureOverlay != null) {
                            mapView.getOverlayManager().remove(clusterFeatureOverlay);
                        }
//                        if (simpleFastLabelOverlay != null) {
//                            mapView.getOverlayManager().remove(simpleFastLabelOverlay);
//                        }


//                        cluster setUp
//                        clusterFeatureOverlay = new RadiusMarkerCluster(requireActivity());
//                        Bitmap clusterIcon = BitmapFactory.decodeResource(getResources(), R.drawable.marker_cluster);
//                        clusterFeatureOverlay.setIcon(clusterIcon);


                        clusterFeatureOverlay=featureLayer.getClusterFeatureOverlay();

//                        featuresOverlay = featureLayer.getFeatureOverlay();
//                        simpleFastLabelOverlay = featureLayer.getLabelOverlay();
//                        labelOverLay=featureLayer.getLabelFolderOverlay();
                        if (clusterFeatureOverlay != null) {
//                            mapView.getOverlayManager().add(featuresOverlay);
//                            for(Overlay overlay: featuresOverlay.getItems()){
//                                if (overlay instanceof Marker) {
//                                    Marker marker = (Marker) overlay;
//                                    poiMarkers.add(marker);
//                                }
//                            }
                            mapView.getOverlayManager().add(clusterFeatureOverlay);
                        }
//                        if (simpleFastLabelOverlay != null) {
//                            mapView.getOverlayManager().add(simpleFastLabelOverlay);
//                            for(Overlay overlay: labelOverLay.getItems()){
//                                if (overlay instanceof Marker) {
//                                    Marker marker = (Marker) overlay;
//                                    labelMarkers.add(marker);
//                                }
//                            }
//                            mapView.getOverlayManager().add(labelMarkers);
//                        }
                    }
                }
                mapView.invalidate();
            }
        }

        @Override
        public void onGetSelectedFeatures(List<Overlay> selectedFeatures) {
            try {
                setSelectedFeatureOverlays(selectedFeatures);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        mapFragmentPresenter.RefreshDeliveryState(selectedFeatures,requireActivity());
                    }
                }).start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
    private FolderOverlay locationBufferFolderOverlay = null;
    private Overlay bufferGeometryOverlay = null;
    private boolean zoomToLocation = true;
    private DeliveryService deliveryService;

    private FolderOverlay trailFolderOverlay=null;
    private List<Overlay> trailOverlay = new ArrayList<>();

    private Overlay stopOverlay = null;
    private FolderOverlay stopFolderOverlay=null;

    public MapFragment(Context context, MapFragmentPresenter mapFragmentPresenter, IdeliveryActivityView ideliveryActivityView) {
        // Required empty public constructor
        this.context = context;
        this.mapFragmentPresenter = mapFragmentPresenter;
        this.ideliveryActivityView = ideliveryActivityView;
        mapFragmentPresenter.setImapViewCallBack(imapFragmentView);
    }

    public List<Overlay> getSelectedFeatureOverlays() {
        return selectedFeatureOverlays;
    }

    public void setSelectedFeatureOverlays(List<Overlay> selectedFeatureOverlays) {
        this.selectedFeatureOverlays = selectedFeatureOverlays;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        configureViews(view);
        showDeliveryFeatureOnMap();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setHasOptionsMenu(true);
        showOptionMenu();
        checkIfDeliveryServiceRunningAndUpdateUI();

//        zoomMap();
    }

    private void showList() {
        linearLayoutManager = new LinearLayoutManager(context);
        delivery_item_list_view.setLayoutManager(linearLayoutManager);
        listDeliveryViewAdapter = new ListDeliveryViewAdapter(context, DeliveryDataModel.getFeatureList(), ideliveryActivityView);
        delivery_item_list_view.setAdapter(listDeliveryViewAdapter);
    }

    private void showOptionMenu() {
        try {
            if (mapToolBar != null) {
                ((DeliveryMainActivity) requireActivity()).setSupportActionBar(mapToolBar);
                Objects.requireNonNull(((DeliveryMainActivity) requireActivity()).getSupportActionBar()).show();
            }
        } catch (Exception e) {
            e.printStackTrace();

        }

    }


    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
    }

    @SuppressLint("UseSupportActionBar")
    private void configureViews(View view) {
        Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context));
        mapView = (MapView) view.findViewById(R.id.map_d_view);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setTilesScaledToDpi(true);
        mapView.setHasTransientState(true);
        mapView.setMultiTouchControls(true);
        mapView.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.NEVER);
        mapView.setHorizontalMapRepetitionEnabled(false);
        mapView.setVerticalMapRepetitionEnabled(false);
        mapView.setScrollableAreaLimitLatitude(MapView.getTileSystem().getMaxLatitude(), MapView.getTileSystem().getMinLatitude(), 0);
        IMapController mapController = mapView.getController();
        mapController.setZoom(11.0);
        GeoPoint startPoint = new GeoPoint(21.1458, 79.0882);
        mapController.setCenter(startPoint);

        mapToolBar = view.findViewById(R.id.map_toolbar);
        mapToolBar.setTitle("Delivery");
        if (mapToolBar != null) {
            startDeliveryBtn = mapToolBar.findViewById(R.id.start_delivery);
            showNextBtn = mapToolBar.findViewById(R.id.shownext_btn);
        }
        startDeliveryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    ((DeliveryMainActivity) requireActivity()).onStartClick();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        showNextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    ((DeliveryMainActivity) requireActivity()).onShowNextClick();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        transparentViewMap = view.findViewById(R.id.transparentViewMap);


        View bottomSheet = view.findViewById(R.id.deliveries_sheet);
        deliveryBottomSheet = BottomSheetBehavior.from(bottomSheet);
        setBottomSheetHeight();
        delivery_item_list_view = view.findViewById(R.id.delivery_item_list_view_map);
        showList();
        bottomSheetConfig(deliveryBottomSheet);



        RotationGestureOverlay mRotationGestureOverlay = new RotationGestureOverlay(context, mapView);
        mRotationGestureOverlay.setEnabled(true);
        mapView.setMultiTouchControls(true);
        mapView.getOverlays().add(mRotationGestureOverlay);


        targetFeatureTv = view.findViewById(R.id.target_feature_tv);
        targetNameTv = view.findViewById(R.id.name_tv);
        targetAddressTv = view.findViewById(R.id.address_tv);
        targetMobileTv = view.findViewById(R.id.mobile_number_tv);
        targetCountTv = view.findViewById(R.id.target_count_tv);

        locationCv = view.findViewById(R.id.locationCv);
        locationIv = view.findViewById(R.id.locationIv);
        locationCv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userLocation();
            }
        });
        getUserLocation = new GetUserLocation(getActivity(), mapView, locationIv, null, null, new LocationReceiverInterface() {
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
//        enableLocationDialog();
    }

    private void setBottomSheetHeight() {
        try {
            if(deliveryBottomSheet != null){
                if (DeliveryDataModel.getInstance().getDeliveryState() != null && DeliveryDataModel.getInstance().getDeliveryState()) {
                    deliveryBottomSheet.setPeekHeight(608,true);
                }else{
                    deliveryBottomSheet.setPeekHeight(330,true);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void bottomSheetConfig(BottomSheetBehavior deliveryBottomSheet) {
        try {
            if(deliveryBottomSheet != null){
                deliveryBottomSheet.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
                    @Override
                    public void onStateChanged(@NonNull View bottomSheet, int newState) {

                        switch (newState) {
                            case BottomSheetBehavior.STATE_COLLAPSED:
                                break;
                            case BottomSheetBehavior.STATE_DRAGGING:
                                break;
                            case BottomSheetBehavior.STATE_EXPANDED:
                                break;
                            case BottomSheetBehavior.STATE_HIDDEN:
                                break;
                            case BottomSheetBehavior.STATE_HALF_EXPANDED:
                                break;

                            case BottomSheetBehavior.STATE_SETTLING:
                                break;
                        }

                    }

                    @Override
                    public void onSlide(@NonNull View bottomSheet, float slideOffset) {

                    }
                });

                mapView.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        if(event.getAction() != MotionEvent.ACTION_MOVE){
                            Log.d("actname", "onTouch: "+deliveryBottomSheet.getPeekHeight());
//                    if(deliveryBottomSheet.getPeekHeight() ==)
                            deliveryBottomSheet.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        }

                        return false;
                    }
                });
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.mapbarmenu, menu);
        try {
            MenuItem item = menu.getItem(0);
            if (DeliveryDataModel.getInstance().getDeliveryState() != null) {
                item.setVisible(DeliveryDataModel.getInstance().getDeliveryState());
                configureStartButtonUi(DeliveryDataModel.getInstance().getDeliveryState());
            } else {
                configureStartButtonUi(false);
            }
            configureShowNextButton(DeliveryDataModel.getInstance().getRiderState());

        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onCreateOptionsMenu(menu, inflater);
    }


    public void showDeliveryFeatureOnMap() {
        if (targetOverlay != null) {
//            featuresOverlay.remove(targetOverlay);
            clusterFeatureOverlay.remove(targetOverlay);
        }
        if (directionFolderOverlay == null) {
            directionFolderOverlay = new FolderOverlay();
            directionFolderOverlay.setName("directions");
            mapView.getOverlayManager().add(directionFolderOverlay);
        }

        mapFragmentPresenter.showDeliveryFeatureOnMap(context, mapView, imapFragmentView);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void notifyListData(int position) {
        listDeliveryViewAdapter.setFeatureList(DeliveryDataModel.getFeatureList());
        if (position != -1) {
            listDeliveryViewAdapter.notifyItemChanged(position);
        } else {
            listDeliveryViewAdapter.notifyDataSetChanged();
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void highlightTargetFeature(Feature targetFeatureData) {
        try{
            if (listDeliveryViewAdapter != null) {
                if(listDeliveryViewAdapter.getTargetFeature() != null){
                    if(targetFeatureData != null){
                        if(!targetFeatureData.getFeatureId().equals(listDeliveryViewAdapter.getTargetFeature().getFeatureId())){
                            listDeliveryViewAdapter.setTargetFeature(targetFeatureData);
                            listDeliveryViewAdapter.notifyDataSetChanged();
                        }
                    }else{
                        listDeliveryViewAdapter.setTargetFeature(null);
                        listDeliveryViewAdapter.notifyDataSetChanged();
                    }

                }else{
                    listDeliveryViewAdapter.setTargetFeature(targetFeatureData);
                    listDeliveryViewAdapter.notifyDataSetChanged();
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void performAutoSelectionFunctionalityInListView(List<Feature> inRangeTargetList) {
        try {
//            homeScreenAdapter.setFeaturesEnable(inRangeTargetList);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    boolean isInRangeListDataChange=mapFragmentPresenter.isInRangeListDataChange(inRangeTargetList);
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if(isInRangeListDataChange) {
                                scrollTopToThePosition(mapFragmentPresenter.getFirstTargetFeaturePosition(inRangeTargetList));
                                listDeliveryViewAdapter.notifyDataSetChanged();
                            }
                        }
                    });
                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void scrollTopToThePosition(int position) {
        delivery_item_list_view.post(new Runnable() {
            @Override
            public void run() {
                linearLayoutManager.scrollToPositionWithOffset(position, 0);
            }
        });
    }

    private void enableLocationDialog() {
        if (getUserLocation != null) {
            boolean isLocationEnable = getUserLocation.checkLocationIsEnable();
            if (!isLocationEnable) {

                InfoBottomSheet infoBottomSheet = InfoBottomSheet.geInstance(getActivity(),
                        "Enable Location", null, "Please Turn On Location", "You can not access the application without location",
                        AppConstants.LOCATION_REQUEST, 1, "");

                infoBottomSheet.setCancelable(false);
                infoBottomSheet.show(requireActivity().getSupportFragmentManager(), "Location Request");
            }
        }
    }

    private void userLocation() {
        if (getUserLocation != null) {
            getUserLocation.zoomLocation(null);
        }
//        enableLocationDialog();
    }


    private void onLocationChanged(Location location) {

        if (getUserLocation != null && zoomToLocation) {
            zoomToLocation = false;
            getUserLocation.zoomLocation(null);
            getUserLocation.showUserCurrentLatLong();
        }
    }

    @Override
    public void onOkInfoBottomSheetResult(int requestCode, int errorCode, String jurisdictions) {
        if (requestCode == AppConstants.LOCATION_REQUEST) {
            Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(myIntent);
        }
    }

    @Override
    public void onCancelOkBottomSheetResult(int requestCode, int errorCode) {

    }

    private void checkIfDeliveryServiceRunningAndUpdateUI() {
        if (DeliveryDataModel.getInstance().getDeliveryState() != null && DeliveryDataModel.getInstance().getDeliveryState()) {
            if (DeliveryDataModel.getInstance().getRoute() != null) {
                showRouteOnMap(DeliveryDataModel.getInstance().getRoute());
                showTargetTv(DeliveryDataModel.getInstance().getTargetFeature());
                updateTargetFeatureUiOnMap(DeliveryDataModel.getInstance().getTargetFeature());
            }
        }
        updateMapFragmentUIIfDeliveryIsOnGoing(DeliveryDataModel.getInstance().getDeliveryState());
    }


    private void showStopOptionInMenu() {
        try {
            if (DeliveryDataModel.getInstance().getDeliveryState() != null) {
                if (DeliveryDataModel.getInstance().getDeliveryState()) {
                    ((DeliveryMainActivity) requireActivity()).showHideStopItemInMenu(true);
                } else {
                    ((DeliveryMainActivity) requireActivity()).showHideStopItemInMenu(false);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("SetTextI18n")
    public void showTargetTv(Feature targetFeature) {
        if (IS_SHOW_TARGET_TV) {
            try {
                if (targetFeature != null) {





                        int position = DeliveryDataModel.getFeatureList().indexOf(targetFeature) + 1;
//                                        String firstname = (String) feature.getAttributes().get("firstname");
//                                        String lastname = (String) feature.getAttributes().get("lastname");

                        String name = (String) targetFeature.getAttributes().get("consumername");
                        String address = (String) targetFeature.getAttributes().get("address");
                        String mobileNumber = String.valueOf(targetFeature.getAttributes().get("consumerphonenumber"));

                        targetNameTv.setText(name);
                        targetAddressTv.setText(address);
                        targetFeatureTv.setVisibility(View.VISIBLE);
                        targetCountTv.setText(String.valueOf(position));

                        targetMobileTv.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", mobileNumber, null));
                                startActivity(intent);
                            }
                        });




//                    new Thread(new Runnable() {
//                        @Override
//                        public void run() {
//                            Feature feature = mapFragmentPresenter.getConsumerFeatureForTargetDelivery(getActivity(), targetFeature);
//                            mHandler.post(new Runnable() {
//                                @Override
//                                public void run() {
//                                    try {
//                                        if (feature != null) {
//                                            int position = DeliveryDataModel.getFeatureList().indexOf(targetFeature) + 1;
////                                        String firstname = (String) feature.getAttributes().get("firstname");
////                                        String lastname = (String) feature.getAttributes().get("lastname");
//
//                                            String name = (String) feature.getAttributes().get("name");
//                                            String address = (String) targetFeature.getAttributes().get("address");
//                                            String mobileNumber = String.valueOf(feature.getAttributes().get("mobilenumber"));
//
//                                            targetNameTv.setText(name);
//                                            targetAddressTv.setText(address);
//                                            targetFeatureTv.setVisibility(View.VISIBLE);
//                                            targetCountTv.setText(String.valueOf(position));
//
//                                            targetMobileTv.setOnClickListener(new View.OnClickListener() {
//                                                @Override
//                                                public void onClick(View view) {
//                                                    Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", mobileNumber, null));
//                                                    startActivity(intent);
//                                                }
//                                            });
//                                        } else {
//                                            String address = (String) targetFeature.getAttributes().get("address");
//                                            targetNameTv.setText("Your Customer");
//                                            targetAddressTv.setText(address);
//                                            targetCountTv.setText(String.valueOf(1));
//                                            targetFeatureTv.setVisibility(View.VISIBLE);
//
//                                        }
//                                    } catch (Exception e) {
//                                        e.printStackTrace();
//                                    }
//                                }
//                            });
//                        }
//                    }).start();
                } else {

                    targetNameTv.setText("");
                    targetAddressTv.setText("");
                    targetCountTv.setText("");
                    targetFeatureTv.setVisibility(View.GONE);


                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void updateTargetFeatureUiOnMap(Feature targetFeature) {
        try {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    selectTargetFeature(targetFeature);
                }
            }).start();

            if(targetFeature != null){
                clusterFeatureOverlay.setTargetFeatureId(String.valueOf(targetFeature.getFeatureId()));
            }else{
                clusterFeatureOverlay.setTargetFeatureId("");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void selectTargetFeature(Feature targetFeature) {
        try {

            if (targetFeature != null) {
                if (clusterFeatureOverlay != null) {
                    List<Marker> markers = clusterFeatureOverlay.getItems();
                    for (Marker marker : markers) {

                        if (targetFeature.getFeatureId().equals(marker.getId())) {
                            String customFillColor = "";
                            boolean isDelivered = (boolean) targetFeature.getAttributes().get("isdelivered");
//                                boolean isVisited = (boolean) targetFeature.getAttributes().get("isvisited");
                            boolean isSkipped = (boolean) targetFeature.getAttributes().get("skipped");

                            if (!isSkipped && !isDelivered) {
                                customFillColor = requireActivity().getResources().getString(R.string.pending);
                            } else if (isSkipped && !isDelivered) {
                                customFillColor = requireActivity().getResources().getString(R.string.incomplete);
                            } else if (isSkipped && isDelivered) {
                                customFillColor = requireActivity().getResources().getString(R.string.complete);
                            }
//                                mapFragmentPresenter.selectFeature(requireActivity(),overlay,customFillColor);
                            if (getSelectedFeatureOverlays() != null) {
                                if (!getSelectedFeatureOverlays().contains(marker)) {
                                    mapFragmentPresenter.selectFeature(requireActivity(), marker, customFillColor);
                                }
                            }
//                                targetOverlay = overlay;
//                                Drawable mDrawable = VectorDrawableUtils.getDrawable(requireActivity(), R.drawable.ic_geom_marker);
//                                Drawable mDrawableNew = mDrawable.getConstantState().newDrawable().mutate();
//                                mHandler.post(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        marker.setIcon(mDrawableNew);
//                                    }
//                                });

//                                marker.setInfoWindow();

                            break;
                        }

                    }
                }
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mapView.invalidate();
                    }
                });
            } else {
                if (((DeliveryMainActivity) requireActivity()).getDeliveryPresenter() != null) {
                    ((DeliveryMainActivity) requireActivity()).getDeliveryPresenter().updateTraversalFeatureList(context, -1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void removeDirectionFromMap() {
        if (directionFolderOverlay != null) {
            List<Overlay> overlays = directionFolderOverlay.getItems();
            if (overlays != null && overlays.size() > 0) {
                directionFolderOverlay.remove(directionGeometryOverlay);
                directionFolderOverlay.remove(directionGeometryStartOverlay);
                directionFolderOverlay.remove(directionGeometryEndOverlay);
                directionGeometryOverlay = null;
                directionGeometryStartOverlay = null;
                directionGeometryEndOverlay = null;
                routeBoundingBox = null;
            }
        }
        mapView.invalidate();
    }

    public void showRouteOnMap(JSONObject routeJsonObj) {
        try {

            if (routeJsonObj != null) {
                JSONObject route = new JSONObject();
                if (routeJsonObj.has("route")) {
                    route = routeJsonObj.getJSONObject("route");
                }
                Location startLocation = (Location) routeJsonObj.get("startLocation");
                Location endLocation = (Location) routeJsonObj.get("endLocation");
//                mapView.getOverlayManager().remove(directionFolderOverlay);
                Polyline polyline = Utils.toOSMPolyline(route);
                Polyline startLine = Utils.toStartLine(route, startLocation);
                Polyline endLine = Utils.toEndLine(route, endLocation);
                if (polyline != null) {

                    if (directionFolderOverlay == null) {
                        directionFolderOverlay = new FolderOverlay();
                        directionFolderOverlay.setName("directions");
                    } else {
                        removeDirectionFromMap();
                    }
                    try {

                        // route Line
                        polyline.setId("directions");
                        polyline.setVisible(true);
                        Paint linePaint = polyline.getOutlinePaint();
                        linePaint.setColor(Color.parseColor("#000000"));
                        linePaint.setStrokeWidth(10);
                        linePaint.setStyle(Paint.Style.STROKE);
                        linePaint.setStrokeCap(Paint.Cap.SQUARE);
//                    linePaint.setPathEffect(new ComposePathEffect());
                           /* polyline.getOutlinePaint().setColor(Color.parseColor("#1890FF"));
                            polyline.getOutlinePaint().setStrokeWidth(20);*/
                        directionFolderOverlay.add(polyline);
                        directionGeometryOverlay = polyline;

                        //start line
                        startLine.setId("directions");
                        startLine.setVisible(true);
                        Paint startLinePaint = startLine.getOutlinePaint();
                        startLinePaint.setColor(Color.parseColor("#000000"));
                        startLinePaint.setStrokeWidth(10);
                        startLinePaint.setStyle(Paint.Style.STROKE);
                        startLinePaint.setStrokeCap(Paint.Cap.SQUARE);
                        startLinePaint.setPathEffect(new DashPathEffect(new float[]{8, 15}, 20));
//                    linePaint.setPathEffect(new ComposePathEffect());
                           /* polyline.getOutlinePaint().setColor(Color.parseColor("#1890FF"));
                            polyline.getOutlinePaint().setStrokeWidth(20);*/
                        directionFolderOverlay.add(startLine);
                        directionGeometryStartOverlay = startLine;


                        //end line
                        endLine.setId("directions");
                        endLine.setVisible(true);
                        Paint endLinePaint = endLine.getOutlinePaint();
                        endLinePaint.setColor(Color.parseColor("#000000"));
                        endLinePaint.setStrokeWidth(10);
                        endLinePaint.setStyle(Paint.Style.STROKE);
                        endLinePaint.setStrokeCap(Paint.Cap.SQUARE);
                        endLinePaint.setPathEffect(new DashPathEffect(new float[]{8, 15}, 20));
//                    linePaint.setPathEffect(new ComposePathEffect());
                           /* polyline.getOutlinePaint().setColor(Color.parseColor("#1890FF"));
                            polyline.getOutlinePaint().setStrokeWidth(20);*/
                        directionFolderOverlay.add(endLine);
                        directionGeometryEndOverlay = endLine;

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
//                    mapView.getOverlayManager().add(directionFolderOverlay);
//                    if (directionFolderOverlay != null) {
//                        routeBoundingBox = directionFolderOverlay.getBounds();
//                        if (zoomOnDirection) {
//                            mapView.zoomToBoundingBox(directionFolderOverlay.getBounds(), true, 100, mapView.getMaxZoomLevel(), null);
//                        } else {
//                            zoomOnDirection = true;
//                        }
//                    }
                    mapView.invalidate();
                }
                if (direcionsDetailsLinkedList == null) {
                    direcionsDetailsLinkedList = new LinkedList<>();
                } else {
                    direcionsDetailsLinkedList.clear();
                }
                if (route.has("features")) {
                    JSONArray features = route.getJSONArray("features");
                    for (int i = 0; i < features.length(); i++) {
                        JSONObject featureJson = features.getJSONObject(i);
                        if (featureJson.has("properties")) {
                            JSONObject propertyJson = featureJson.getJSONObject("properties");
                            JSONArray segmentsJson = propertyJson.getJSONArray("segments");
                            JSONObject directionJson = segmentsJson.getJSONObject(0);
                            String distance = directionJson.getString("distance") + "km";
                            JSONArray directionSteps = directionJson.getJSONArray("steps");

                            for (int j = 0; j < directionSteps.length(); j++) {
                                JSONObject directionStepsObject = directionSteps.getJSONObject(j);
                                DirectionModel directionModel = new DirectionModel();
                                Double distancePerSteps = directionStepsObject.getDouble("distance");
                                String instructionPerSteps = directionStepsObject.getString("instruction");
                                int directionType = directionStepsObject.getInt("type");
                                directionModel.setIndex(j);
                                directionModel.setDistance(distancePerSteps);
                                directionModel.setInstruction(instructionPerSteps);
                                directionModel.setType(directionType);
                                direcionsDetailsLinkedList.add(directionModel);
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void zoomMap() {
        try {
            if (mapView != null) {
                if (routeBoundingBox != null) {
                    mapView.zoomToBoundingBox(routeBoundingBox, true, 100, 10, null);
                } else {
                    if (clusterFeatureOverlay != null) {
                        mapView.zoomToBoundingBox(clusterFeatureOverlay.getBounds(), true, 100, mapView.getMaxZoomLevel(), null);
                    }
                }
                mapView.invalidate();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showBufferOnMap(Geometry bufferGeometry) {
        try {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        List<Polygon> polygons = Utils.convertGeometry(bufferGeometry);
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (polygons != null && polygons.size() > 0) {

                                    if (locationBufferFolderOverlay == null) {
                                        locationBufferFolderOverlay = new FolderOverlay();
                                        locationBufferFolderOverlay.setName("LocationBuffer");
                                        for (Polygon polygon : polygons) {
                                            try {
                                                polygon.setId("locationBuffer");
                                                locationBufferFolderOverlay.add(polygon);
                                                bufferGeometryOverlay = polygon;
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        }

                                        if (clusterFeatureOverlay != null) {
                                            mapView.getOverlayManager().remove(clusterFeatureOverlay);
                                        }
//                                        if (simpleFastLabelOverlay != null) {
//                                            mapView.getOverlayManager().remove(simpleFastLabelOverlay);
//                                        }

                                        mapView.getOverlayManager().add(locationBufferFolderOverlay);
                                        if (clusterFeatureOverlay != null) {
                                            mapView.getOverlayManager().add(clusterFeatureOverlay);
                                        }
//                                        if (simpleFastLabelOverlay != null) {
//                                            mapView.getOverlayManager().add(simpleFastLabelOverlay);
//                                        }
                                    } else {
                                        removeExistingBuffer();
                                        for (Polygon polygon : polygons) {
                                            try {
                                                polygon.setId("locationBuffer");
                                                locationBufferFolderOverlay.add(polygon);
                                                bufferGeometryOverlay = polygon;
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }
                                    mapView.invalidate();
                                }
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void removeExistingBufferOnMap() {
        try {
            removeExistingBuffer();
            mapView.invalidate();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void removeExistingBuffer() {
        try {
            if (locationBufferFolderOverlay != null && bufferGeometryOverlay != null) {
                List<Overlay> overlays = locationBufferFolderOverlay.getItems();
                if (overlays != null && overlays.size() > 0) {
                    locationBufferFolderOverlay.remove(bufferGeometryOverlay);
                    bufferGeometryOverlay = null;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        showStopOptionInMenu();
        if (hidden) {
            ((DeliveryMainActivity) requireActivity()).showHideButtonTabLayout(false);
        } else {
            ((DeliveryMainActivity) requireActivity()).showHideButtonTabLayout(true);
            if (DeliveryDataModel.getInstance().getDeliveryState() != null) {
                ((DeliveryMainActivity) requireActivity()).configureUi(DeliveryDataModel.getInstance().getDeliveryState());
            } else {
                ((DeliveryMainActivity) requireActivity()).configureUi(false);
            }
//            notifyListData(-1);
        }
        super.onHiddenChanged(hidden);
    }

    @Override
    public void onStop() {
        super.onStop();
    }


    public void updateMapFragmentUIIfDeliveryIsOnGoing(Boolean deliveryState) {
        try {
            if (deliveryState != null) {
                if (deliveryState) {
                    if (transparentViewMap != null) {
                        transparentViewMap.setVisibility(View.GONE);
                    }
                } else {
                    if (transparentViewMap != null) {
                        transparentViewMap.setVisibility(View.VISIBLE);
                    }
                }
            } else {
                if (transparentViewMap != null) {
                    transparentViewMap.setVisibility(View.VISIBLE);
                }
            }
            setBottomSheetHeight();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean deSelectTheSelectingFeature() {
        return mapFragmentPresenter.delselectTHeSelectedFeature(requireActivity());
    }

    public void setSelectableFeature(List<Feature> inRangeTargetList) {
        try {
            List<Feature> excludedFeatureList = new ArrayList<>();
            if (DeliveryDataModel.getInstance().getTargetFeature() != null) {
                excludedFeatureList.add(DeliveryDataModel.getInstance().getTargetFeature());
            }
            mapFragmentPresenter.setSelectableFeature(requireActivity(), inRangeTargetList, excludedFeatureList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void configureStartButtonUi(boolean deliveryState) {
        try {

            if (!deliveryState) {
                if (startDeliveryBtn != null) {
                    if (mapFragmentPresenter.hasNextTraversingFeature()) {
                        if (mapFragmentPresenter.getCurrentlyTraversingFeature() != null) {
                            startDeliveryBtn.setVisibility(View.VISIBLE);
                        } else {
                            startDeliveryBtn.setVisibility(View.GONE);
                        }
                    } else {
                        startDeliveryBtn.setVisibility(View.GONE);
                    }
                    ((View) startDeliveryBtn.getParent()).setBackgroundColor(getResources().getColor(android.R.color.transparent));
                }
            } else {
                if (startDeliveryBtn != null) {
                    startDeliveryBtn.setVisibility(View.GONE);
                    ((View) startDeliveryBtn.getParent()).setBackgroundColor(0);
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void configureShowNextButton(JSONObject riderLocationState) {
        try {
            if (showNextBtn != null) {
                if (riderLocationState != null) {
                    if (riderLocationState.has("deliverobject")) {
                        boolean isOrderDelivered = riderLocationState.getBoolean("deliverobject");
                        boolean isReachTarget = riderLocationState.getBoolean("reachTarget");
                        if (isReachTarget && !isOrderDelivered) {
//                    deliverBtn.setVisibility(View.VISIBLE);
                            showNextBtn.setVisibility(View.GONE);
                        } else if (isReachTarget && isOrderDelivered) {
//                        deliverBtn.setVisibility(View.GONE);

                            if (mapFragmentPresenter.hasNextTraversingFeature()) {
                                showNextBtn.setVisibility(View.VISIBLE);
                            } else {
                                showNextBtn.setVisibility(View.GONE);
                            }
                        } else if (!isReachTarget && !isOrderDelivered) {
//                        deliverBtn.setVisibility(View.GONE);
                            showNextBtn.setVisibility(View.GONE);
                        } else if (!isReachTarget && isOrderDelivered) {
//                        deliverBtn.setVisibility(View.GONE);


                            if (mapFragmentPresenter.hasNextTraversingFeature()) {
                                showNextBtn.setVisibility(View.VISIBLE);
                            } else {
                                showNextBtn.setVisibility(View.GONE);
                            }
                        }
                    } else {
                        showNextBtn.setVisibility(View.GONE);
//                    deliverBtn.setVisibility(View.GONE);
                    }
                } else {
                    showNextBtn.setVisibility(View.GONE);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showTrailOnMap(String geometryGeoJsonStr) {
        if(mapView==null || geometryGeoJsonStr ==null|| geometryGeoJsonStr.isEmpty()){
            return;
        }
        try{
            JSONObject geometryGeoJson = new JSONObject(geometryGeoJsonStr);
            List<Polyline> trailPolyLineOverlay = GeoJsonUtils.toOSMPolylines(geometryGeoJson);
            if (trailPolyLineOverlay != null && trailPolyLineOverlay.size() > 0) {

                if (trailFolderOverlay == null) {
                    trailFolderOverlay = new FolderOverlay();
                    trailFolderOverlay.setName("trail");
                    trailOverlay.clear();
                    for (Polyline polyline : trailPolyLineOverlay) {
                        Paint outLinePaint = polyline.getOutlinePaint();
                        outLinePaint.setColor(Color.BLACK);
                        outLinePaint.setStrokeWidth(2);
                        trailFolderOverlay.add(polyline);
                        trailOverlay.add(polyline);
                    }

                    if (clusterFeatureOverlay != null) {
                        mapView.getOverlayManager().remove(clusterFeatureOverlay);
                    }
//                    if (simpleFastLabelOverlay != null) {
//                        mapView.getOverlayManager().remove(simpleFastLabelOverlay);
//                    }

                    mapView.getOverlayManager().add(trailFolderOverlay);
                    if (clusterFeatureOverlay != null) {
                        mapView.getOverlayManager().add(clusterFeatureOverlay);
                    }
//                    if (simpleFastLabelOverlay != null) {
//                        mapView.getOverlayManager().add(simpleFastLabelOverlay);
//                    }
                } else {
                    removeExistingTrails();
                    for (Polyline polyline : trailPolyLineOverlay) {
                        Paint outLinePaint = polyline.getOutlinePaint();
                        outLinePaint.setColor(Color.BLACK);
                        outLinePaint.setStrokeWidth(2);
                        trailFolderOverlay.add(polyline);
                        trailOverlay.add(polyline);
                    }
                }
                mapView.invalidate();
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        mapView.invalidate();
    }

    private void removeExistingTrails() {
        try {
            if (trailFolderOverlay != null && !trailOverlay.isEmpty()) {
                List<Overlay> overlays = trailFolderOverlay.getItems();
                if (overlays != null && overlays.size() > 0) {
                    for(Overlay trailLines:trailOverlay){
                        trailFolderOverlay.remove(trailLines);
                    }
                }
                trailOverlay.clear();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showStopOnMap(String geometryGeoJsonStr) {
        if(mapView==null || geometryGeoJsonStr ==null|| geometryGeoJsonStr.isEmpty()){
            return;
        }
        try{
            JSONObject geometryGeoJson = new JSONObject(geometryGeoJsonStr);
            GeoPoint geoPoint = GeoJsonUtils.toOSMPoint(geometryGeoJson);
            String textlabel = "stop";
            try {
                JSONObject stopJsonProp = geometryGeoJson.getJSONArray("features").getJSONObject(0).getJSONObject("properties");
                if(stopJsonProp.has(AppConstants.STOP_TABLE_COMMENT)){
                    textlabel=stopJsonProp.getString(AppConstants.STOP_TABLE_COMMENT);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
            if (geoPoint!=null) {

                if (stopFolderOverlay == null) {
                    stopFolderOverlay = new FolderOverlay();
                    stopFolderOverlay.setName("trail");
                    if(stopOverlay!=null){
                        mapView.getOverlayManager().remove(stopOverlay);
                    }

                    Marker m = new Marker(mapView);
                    m.setPosition(geoPoint);
                    m.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
                    m.setDraggable(false);
                    m.setTextIcon(textlabel);
                    stopOverlay=m;
                    stopFolderOverlay.add(stopOverlay);
                    mapView.getOverlayManager().add(stopFolderOverlay);

                } else {
                    removeExistingStops();
                    Marker m = new Marker(mapView);
                    m.setPosition(geoPoint);
                    m.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
                    m.setDraggable(false);
                    m.setTextIcon(textlabel);
                    stopOverlay=m;
                    stopFolderOverlay.add(stopOverlay);
                }
                mapView.invalidate();
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        mapView.invalidate();
    }

    private void removeExistingStops() {
        try {
            if (stopFolderOverlay != null && stopOverlay !=null) {
                stopFolderOverlay.remove(stopOverlay);
                stopOverlay=null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}