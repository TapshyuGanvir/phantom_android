package com.sixsimplex.trail;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.sixsimplex.trail.gepoackage.GeoPackageManagerAgent;
import com.sixsimplex.trail.utils.dbcalls.DbRelatedConstants;

import org.json.JSONObject;

import java.io.Serializable;

import mil.nga.geopackage.GeoPackage;

public class LocationTacker implements Serializable {

    private static LocationTacker INSTANCE = null;
    boolean useGps, useNetwork;
    int intervalDurationSec, intervalDistanceMeters;
    boolean trackSilently = true, singleTrailPerDay = true, restartAutomatically = true,recordStops=false;
    BroadcastReceiver locationReceiver;
    String userName, surveyName, logFolderPath, deviceInfo;
    JSONObject propertiesJsonForDataGpkg, dataDbDataSourceJson, trailDatasetObject, stopsDatasetObject;
    String dataDbName;
    JSONObject propertiesJsonForMetaGpkg, metaDbDataSourceJson, editMetadataDatasetObject;
    String metaDbName;
    JSONObject propertiesJsonForRedbGpkg, redbDataSourceJson, w9obreDatasetObject;
    String runtimetimestampFormat = "yyyy-MM-dd HH:mm:ss";

    private LocationTacker() {
    }

    public static LocationTacker getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new LocationTacker();
        }
        return (INSTANCE);
    }

    public void initialize(Context context, boolean useGps, boolean useNetwork, int intervalDurationSec, int intervalDistanceMeters,
                           boolean trackSilently, boolean singleTrailPerDay, boolean restartAutomatically,boolean recordStops, BroadcastReceiver locationReceiver,
                           String userName, String surveyName, String logFolderPath, String deviceInfo, JSONObject propertiesJsonForDataGpkg,
                           JSONObject dataDbDataSourceJson, JSONObject trailDatasetObject, JSONObject stopsDatasetObject, String dataDbName,
                           JSONObject propertiesJsonForMetaGpkg, JSONObject metaDbDataSourceJson, JSONObject editMetadataDatasetObject,
                           String metaDbName, JSONObject propertiesJsonForRedbGpkg, JSONObject redbDataSourceJson, JSONObject redbDatasetObject) {
        this.useGps = useGps;
        this.useNetwork = useNetwork;
        this.intervalDurationSec = intervalDurationSec;
        this.intervalDistanceMeters = intervalDistanceMeters;
        this.trackSilently = trackSilently;
        this.singleTrailPerDay = singleTrailPerDay;
        this.restartAutomatically = restartAutomatically;
        this.locationReceiver = locationReceiver;
        this.userName = userName;
        this.surveyName = surveyName;

        this.propertiesJsonForDataGpkg = propertiesJsonForDataGpkg;
        this.dataDbDataSourceJson = dataDbDataSourceJson;
        this.trailDatasetObject = trailDatasetObject;
        this.stopsDatasetObject = stopsDatasetObject;
        if(recordStops && stopsDatasetObject!=null){
            this.recordStops=true;
        }else {
            this.recordStops=false;
        }
        this.dataDbName = dataDbName;
        this.propertiesJsonForMetaGpkg = propertiesJsonForMetaGpkg;
        this.metaDbDataSourceJson = metaDbDataSourceJson;
        this.editMetadataDatasetObject = editMetadataDatasetObject;
        this.metaDbName = metaDbName;
        this.propertiesJsonForRedbGpkg = propertiesJsonForRedbGpkg;
        this.redbDataSourceJson = redbDataSourceJson;
        this.w9obreDatasetObject = redbDatasetObject;


        this.logFolderPath = logFolderPath;
        this.deviceInfo = deviceInfo;

        saveSettingsInLocalStorage(context);

        if (isRestartAutomatically()) {
            start(context);
        }
        else {
            Log.e("TRAIL", "Module not configured to restart automatically");
        }
    }

    private void saveSettingsInLocalStorage(Context context) {
        AppPreferences appPreferences = new AppPreferences(context);
        if (this.intervalDurationSec != 0) {
            appPreferences.putInt("LOCATION_INTERVAL_SEC", this.intervalDurationSec);
        }
        if (this.intervalDistanceMeters != 0) {
            appPreferences.putInt("LOCATION_INTERVAL_METERS", this.intervalDistanceMeters);
        }
        // appPreferences.putString("ACTION", this.actionReceiver);

        appPreferences.putBoolean("GPS", this.useGps);
        appPreferences.putBoolean("NETWORK", this.useNetwork);
        appPreferences.putBoolean("SINGLETRAILPERDAY", this.singleTrailPerDay);
        appPreferences.putBoolean("TRACKSILENTLY", this.trackSilently);
        appPreferences.putBoolean("RESTARTAUTOMATICALLY", this.restartAutomatically);
        appPreferences.putBoolean("RECORDSTOPS", this.recordStops);
        appPreferences.putString("RUNTIMESTAMPFORMAT", this.runtimetimestampFormat);
        appPreferences.putString("DBJSON", this.runtimetimestampFormat);
        appPreferences.putString("USERNAME", this.userName);
        appPreferences.putString("SURVEYNAME", this.surveyName);
        appPreferences.putString("DEVICEINFO", this.deviceInfo);

        appPreferences.putString("LOGFOLDERPATH", this.logFolderPath);

        appPreferences.putString("TRAILDATASETOBJ", this.trailDatasetObject.toString());
        appPreferences.putString("STOPSDATASETOBJ", this.stopsDatasetObject.toString());
        appPreferences.putString("DATADBPROPERTIESJSON", this.propertiesJsonForDataGpkg.toString());
        appPreferences.putString("DATADBDATASOURCJOBJ", this.dataDbDataSourceJson.toString());
        appPreferences.putString("DATA_GP_NAME", this.dataDbName);

        appPreferences.putString("EDITMETADATADATASETOBJ", this.editMetadataDatasetObject.toString());
        appPreferences.putString("METADBPROPERTIESJSON", this.propertiesJsonForMetaGpkg.toString());
        appPreferences.putString("METADBDATASOURCJOBJ", this.metaDbDataSourceJson.toString());
        appPreferences.putString("META_GP_NAME", this.metaDbName);

        appPreferences.putString("REDBDBPROPERTIESJSON", this.propertiesJsonForRedbGpkg.toString());
        appPreferences.putString("REDBDBDATASOURCJOBJ", this.redbDataSourceJson.toString());
        appPreferences.putString("w9OBREDATASETOBJ", this.w9obreDatasetObject.toString());

        appPreferences.putBoolean("PREFERENCESSET", true);

    }

    public boolean isRestartAutomatically() {
        return restartAutomatically;
    }

    public LocationTacker start(Context context) {
        startLocationService(context);

        if (this.locationReceiver != null) {
            IntentFilter intentFilter = new IntentFilter(TrailLocationRecorderService.ACTION.UPDATE_LOCATION_BROADCASTACTION_RESULTCODE_LOCATION);
            intentFilter.addAction(Constants.ACTION_PERMISSION_DEINED);
            context.registerReceiver(this.locationReceiver, intentFilter);
        }
        return this;
    }

    private void startLocationService(Context context) {

        AppPreferences appPreferences = new AppPreferences(context);
        if (! appPreferences.isPreferenceSet()) {
            return;
        }
        Intent intent = new Intent(context, TrailLocationRecorderService.class);
        intent.setAction(TrailLocationRecorderService.ACTION.UPDATE_LOCATION_STARTFOREGROUND_ACTION);
        /*if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        }
        else {
            context.startService(intent);
        }*/
        context.startService(intent);
    }

    public void initGeoPackages(Context context,GeoPackage dataGeoPackage, GeoPackage metadataGeoPackage, GeoPackage reGeoPackage) {
        GeoPackageManagerAgent.setDataGeoPackage(dataGeoPackage);
        GeoPackageManagerAgent.setMetaGeoPackage(metadataGeoPackage);
        GeoPackageManagerAgent.setReGeoPackage(reGeoPackage);
        GeoPackageManagerAgent.initMetaDbName(context);
        GeoPackageManagerAgent.initDataDbName(context);
    }

    public boolean isModuleInitialized(Context context) {
        AppPreferences appPreferences = new AppPreferences(context);
        return appPreferences.isPreferenceSet();
    }

    public void reset(Context context) {
        AppPreferences appPreferences = new AppPreferences(context);
        if (! appPreferences.isPreferenceSet()) {
            return;
        }
        appPreferences.clear();

        this.useGps = true;
        this.useNetwork = false;
        this.intervalDurationSec = 0;
        this.intervalDistanceMeters = 0;
        this.trackSilently = true;
        this.singleTrailPerDay = true;
        this.restartAutomatically = true;
        this.locationReceiver = null;
        this.userName = null;
        this.surveyName = null;
        this.propertiesJsonForDataGpkg = null;
        this.dataDbDataSourceJson = null;
        this.trailDatasetObject = null;
        this.stopsDatasetObject = null;
        this.dataDbName = null;
        this.propertiesJsonForMetaGpkg = null;
        this.metaDbDataSourceJson = null;
        this.editMetadataDatasetObject = null;
        this.metaDbName = null;
        this.propertiesJsonForRedbGpkg = null;
        this.redbDataSourceJson = null;
        this.w9obreDatasetObject = null;
        this.logFolderPath = null;
        this.deviceInfo = null;

        appPreferences.putBoolean("PREFERENCESSET", false);
    }

    public String getRuntimetimestampFormat() {
        return runtimetimestampFormat;
    }

    public void setRuntimetimestampFormat(String runtimetimestampFormat) {
        this.runtimetimestampFormat = runtimetimestampFormat;
    }

    /**
     * Stop locaiton service if running
     *
     * @param context Context
     */
    public void stopLocationService(Context context) {
        AppPreferences appPreferences = new AppPreferences(context);
        if (! appPreferences.isPreferenceSet()) {
            return;
        }
        if (TrailLocationRecorderService.isRunning(context)) {

            if (locationReceiver != null) {
                context.unregisterReceiver(locationReceiver);
            }

            Intent intent = new Intent(context, TrailLocationRecorderService.class);
            intent.setAction(TrailLocationRecorderService.ACTION.UPDATE_LOCATION_STOPFOREGROUND_ACTION);
        /*if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        }
        else {
            context.startService(intent);
        }*/
            context.startService(intent);

            DbRelatedConstants.clearAllConstants();
            GeoPackageManagerAgent.clearAllGeopackage();
        }
    }

    public boolean isUseGps() {
        return useGps;
    }

    public void setUseGps(boolean useGps) {
        this.useGps = useGps;
    }

    public boolean isUseNetwork() {
        return useNetwork;
    }

    public void setUseNetwork(boolean useNetwork) {
        this.useNetwork = useNetwork;
    }

    public int getIntervalDurationSec() {
        return intervalDurationSec;
    }

    public void setIntervalDurationSec(int intervalDurationSec) {
        this.intervalDurationSec = intervalDurationSec;
    }

    public int getIntervalDistanceMeters() {
        return intervalDistanceMeters;
    }

    public void setIntervalDistanceMeters(int intervalDistanceMeters) {
        this.intervalDistanceMeters = intervalDistanceMeters;
    }

    public boolean isTrackSilently() {
        return trackSilently;
    }

    public void setTrackSilently(boolean trackSilently) {
        this.trackSilently = trackSilently;
    }

    public boolean isSingleTrailPerDay() {
        return singleTrailPerDay;
    }

    public void setSingleTrailPerDay(boolean singleTrailPerDay) {
        this.singleTrailPerDay = singleTrailPerDay;
    }

    public BroadcastReceiver getLocationReceiver() {
        return locationReceiver;
    }

    public void setLocationReceiver(BroadcastReceiver locationReceiver) {
        this.locationReceiver = locationReceiver;
    }

    public boolean isTrailStarted() {
        return TrailLocationRecorderService.isServiceRunning.equalsIgnoreCase(
                TrailLocationRecorderService.SERVICE_STATE.LOCATION_SERIVCE_STATE_STARTED);
    }


}
