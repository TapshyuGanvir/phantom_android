package com.sixsimplex.trail;

import static com.sixsimplex.trail.Constants.STOP_TABLE_COMMENT;
import static com.sixsimplex.trail.Constants.STOP_TABLE_ENDTIMESTAMP;
import static com.sixsimplex.trail.Constants.STOP_TABLE_NAME;
import static com.sixsimplex.trail.Constants.STOP_TABLE_STARTTIMESTAMP;
import static com.sixsimplex.trail.Constants.STOP_TABLE_STOPID;
import static com.sixsimplex.trail.Constants.STOP_TABLE_TRAILID;
import static com.sixsimplex.trail.Constants.STOP_TABLE_USERNAME;
import static com.sixsimplex.trail.Constants.STOP_TABLE_W9_ENTITY_CLASS_NAME;
import static com.sixsimplex.trail.Constants.STOP_TABLE_W9_METADATA;
import static com.sixsimplex.trail.Constants.TRAIL_TABLE_DESCRIPTION;
import static com.sixsimplex.trail.Constants.TRAIL_TABLE_DISTANCE;
import static com.sixsimplex.trail.Constants.TRAIL_TABLE_ENDTIMESTAMP;
import static com.sixsimplex.trail.Constants.TRAIL_TABLE_ISNEW;
import static com.sixsimplex.trail.Constants.TRAIL_TABLE_JURISDICTION_INFO;
import static com.sixsimplex.trail.Constants.TRAIL_TABLE_NAME;
import static com.sixsimplex.trail.Constants.TRAIL_TABLE_STARTTIMESTAMP;
import static com.sixsimplex.trail.Constants.TRAIL_TABLE_TRAILID;
import static com.sixsimplex.trail.Constants.TRAIL_TABLE_TRANSPORT_MODE;
import static com.sixsimplex.trail.Constants.TRAIL_TABLE_USERNAME;
import static com.sixsimplex.trail.Constants.TRAIL_TABLE_W9_ENTITY_CLASS_NAME;
import static com.sixsimplex.trail.Constants.TRAIL_TABLE_W9_METADATA;
import static com.sixsimplex.trail.TrailLocationRecorderService.StopStatus.SPEED_ZERO_START;
import static com.sixsimplex.trail.TrailLocationRecorderService.StopStatus.STOP_STARTED;

import android.Manifest;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.sixsimplex.revelologger.ReveloLogger;
import com.sixsimplex.trail.utils.AppUtils;
import com.sixsimplex.trail.utils.SystemUtils;
import com.sixsimplex.trail.utils.dbcalls.DbTableUtils;
import com.sixsimplex.trail.utils.dbcalls.GeoJsonUtils;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.PrecisionModel;

import org.json.JSONObject;
import org.osmdroid.views.overlay.Polyline;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TrailLocationRecorderService extends Service {

    public static final int CHANNEL_ID = 123;
    public static final int UPDATE_LOCATION_RESULTCODE_LOCATION = 9902;
    public static final int UPDATE_LOCATION_ERRORCODE_MOCK_ON = 9903;
    public static List<TrailLatLng> currentTrailLatLngList = new ArrayList<>();
  //  public static List<List<TrailLatLng>> completeTrailLatLngList = new ArrayList<>();
    public static List<Polyline> completeTrailPolylineOverLay = null;
    public static String geometryGeoJsonStr = "";
    //public static int currentTrailPolylineIndex = 0;
    public static Location currentLocation = null;
    public static String isServiceRunning = SERVICE_STATE.LOCATION_SERIVCE_STATE_STOPPED;
    public static String trailState = TRAIL_STATE.TRAIL_STATE_NOTSTARTED;
    public static String trailId = null;
    private static Location previousLocation = null;
    private static long previousLocationTime = 0;
    private static long currentLocationTime = 0;
    private final String CREATE_NEW_TRAIL_ENTRY = "createTrail";
    private final String UPDATE_TRAIL_ENTRY = "updateTrail";
    private final String PAUSE_TRAIL_ENTRY = "pauseTrail";
    private final String END_TRAIL_ENTRY = "stopTrail";
    private final String className = "LocationReceiverService";
    private ReveloLogger reveloLogger = null;
    private String transportMode = "Walking";//walking,bicycle,vehicle
    private String userName = "user";private boolean recordStops = false;
    private Notification notification;
    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder notificationBuilder = null;
    private LocationListener mLocationListener;
    private LocationManager mLocationManager;
    private AppPreferences appPreferences;

    public  String currentStopId = null;
    public JSONObject stopGeoJson = null;
    private StopStatus CURRENT_STOP_STATE = StopStatus.NO_STOP;
    public enum StopStatus {NO_STOP, SPEED_ZERO_START,STOP_STARTED,STOP_ENDED}
    private long currentZeroSpeedStartTimeStamp = 0,currentZeroSpeedStopTimeStamp=0;
    double totalZeroSpeedSeconds=0;
    private final double STOP_THRESHOLD=20;//stop threshold secs zero speed = stop started
    private float SPEED_ZERO_THRESHOLD=1;//if speed <= threshold, it is considered as zero speed, i.e. indication for stop

    public static boolean isRunning(Context context) {
        return AppUtils.isServiceRunning(context, TrailLocationRecorderService.class);
    }

    private static Double distance(Location one, Location two) {
        int R = 6371000;
        Double dLat = toRad(two.getLatitude() - one.getLatitude());
        Double dLon = toRad(two.getLongitude() - one.getLongitude());
        Double lat1 = toRad(one.getLatitude());
        Double lat2 = toRad(two.getLatitude());
        Double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.sin(dLon / 2) * Math.sin(dLon / 2) * Math.cos(lat1) * Math.cos(lat2);
        Double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        Double d = R * c;

        return Math.floor(d);
    }

    private static double toRad(Double d) {
        return d * Math.PI / 180;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        appPreferences = new AppPreferences(getBaseContext());
        startForeground();
    }

    private void startForeground() {
        String channelId = "trail_channel";
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Intent resultIntent = new Intent();
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        notificationBuilder = new NotificationCompat.Builder(getApplicationContext(), channelId);
        notificationBuilder.setSmallIcon(R.drawable.revelo_logo);
        notificationBuilder.setContentTitle("Trail");
        notificationBuilder.setDefaults(NotificationCompat.DEFAULT_ALL);
        notificationBuilder.setContentText("Running");
        notificationBuilder.setAutoCancel(false);
        notificationBuilder.setPriority(NotificationCompat.PRIORITY_MAX);
        notificationBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText("Running"));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (mNotificationManager != null && mNotificationManager.getNotificationChannel(channelId) == null) {
                NotificationChannel notificationChannel = new NotificationChannel(channelId, "Trail", NotificationManager.IMPORTANCE_HIGH);
                notificationChannel.setDescription("this channel is used by trail service");
                mNotificationManager.createNotificationChannel(notificationChannel);
            }
        }
        startForeground(CHANNEL_ID, notificationBuilder.build());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent != null && intent.getAction() != null) {
            switch (intent.getAction()) {
                case ACTION.UPDATE_LOCATION_STARTFOREGROUND_ACTION: {
                    isServiceRunning = SERVICE_STATE.LOCATION_SERIVCE_STATE_STARTED;
                    trailState = TRAIL_STATE.TRAIL_STATE_NOTSTARTED;
                    if (ActivityCompat.checkSelfPermission(TrailLocationRecorderService.this,
                                                           Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                            TrailLocationRecorderService.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return START_NOT_STICKY;
                    }
                    reveloLogger = ReveloLogger.getInstance();
                    reveloLogger.initialize(this, userName, appPreferences.getString("SURVEYNAME", "trailuserSurvey"),
                                            appPreferences.getString("LOGFOLDERPATH", ""),
                                            appPreferences.getString("DEVICEINFO", "no device info found"));
                    userName = appPreferences.getString("USERNAME", "user");
                    recordStops = appPreferences.getBoolean("RECORDSTOPS", false);
                    locationListener();
                    // mLocationManager.removeUpdates(mLocationListener);
                    mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 2, mLocationListener);

                    if (! sendExtraCommand("force_time_injection", null)) {
                    }

                    if (! sendExtraCommand("force_xtra_injection", new Bundle())) {
                    }
                    // if(intent.hasExtra("receiver")){
                    //     receiver =(ResultReceiver) intent.getExtras().get("receiver");
                    // }
                    String notificationMessage = "Reading your live location.. ";
                    trailState = "started";
                    currentTrailLatLngList = new ArrayList<>();
                    notificationMessage = "Recording live location updates to collect user trail.";


                    if (notificationBuilder == null) {
                        startForeground();
                    }
                    else {
                        changeNotificationText(notificationMessage);
                    }
                }
                break;

                case ACTION.UPDATE_LOCATION_PAUSEFOREGROUND_ACTION:
                    // isServiceRunning = STATE.LOCATION_SERIVCE_STATE_PAUSED;
                    //SystemUtils.showShortToast("Feature creation paused.", this);
                    reveloLogger.debug(className, "onStartCommand", "Received Pause feature creation Intent");
                    if (trailState.equalsIgnoreCase("started")) {
                        reveloLogger.debug(className, "onStartCommand", "Changing trail state from started to paused");
                        trailState = "paused";
                    }
                    if (! trailState.equalsIgnoreCase("stopped")) {
                        reveloLogger.debug(className, "onStartCommand",
                                           "trail state " + trailState + "..saving pause entry for trail " + trailId + "to db");
                        saveTrailToGdb(PAUSE_TRAIL_ENTRY, this);
                    }
                    changeNotificationText("Paused");//pause request

                    break;

                case ACTION.UPDATE_LOCATION_RESUMEFOREGROUND_ACTION:
                    reveloLogger.debug(className, "onStartCommand", " Received Resume feature creation intent");

                    if (ActivityCompat.checkSelfPermission(TrailLocationRecorderService.this,
                                                           Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                            TrailLocationRecorderService.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return START_NOT_STICKY;
                    }
                    //2000 milliseconds and 0.0f meter
                    if (! sendExtraCommand("force_time_injection", null)) {
                        reveloLogger.error(className, "on Start Command", "Hardware does not support TS");
                    }

                    if (! sendExtraCommand("force_xtra_injection", new Bundle())) {
                        reveloLogger.error(className, "on Start Command", "Hardware does not support Almanac");
                    }
                    if (trailState.equalsIgnoreCase("paused")) {
                        reveloLogger.debug(className, "onStartCommand", "Changing trail state from paused to started");
                        trailState = "started";
                    }
                    changeNotificationText("Waiting for location");//resume request

                    break;

                case ACTION.UPDATE_LOCATION_STOPFOREGROUND_ACTION:

                    reveloLogger.debug(className, "onStartCommand", "Received stop feature creation intent");
                    //1 - operations depend on values of trailstate and drawingFeatureState. Do not change sequence


                    //2
                    if (trailState.equalsIgnoreCase("started")) {
                        reveloLogger.debug(className, "onStartCommand",
                                           "trail state = started and stop entent received for trail..saving end entry in db for trail " + trailId + " sending end location to map");
                        String w9Id = saveTrailToGdb(END_TRAIL_ENTRY, getBaseContext());
                        sendLastLocationToMap(w9Id);
                    }

                    //3

                    trailState = "stopped";

                    //4
                    isServiceRunning = SERVICE_STATE.LOCATION_SERIVCE_STATE_STOPPED;
                    if (mLocationListener != null && mLocationManager != null) {
                        mLocationManager.removeUpdates(mLocationListener);
                    }
                    stopForeground(true);
                    stopSelf();

                    break;
            }
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        //super.onDestroy();

        reveloLogger.error(className, "ondestroy", "service destroyed. trail state - " + trailState);
        if (! trailState.equalsIgnoreCase("stopped") && currentTrailLatLngList != null && currentTrailLatLngList.size() > 0) {
            reveloLogger.error(className, "ondestroy", "saving ongoing trail " + trailId);
            saveTrailToGdb(END_TRAIL_ENTRY, this);
        }
        else {
            if (currentTrailLatLngList != null) {
                reveloLogger.error(className, "ondestroy",
                                   "trail state -" + trailState + " , trailid -" + trailId + " , traillatlnglist size - " + currentTrailLatLngList.size());
            }
            else {
                reveloLogger.error(className, "ondestroy", "trail state -" + trailState + " , trailid -" + trailId + " , traillatlnglist size - 0");
            }
        }
        trailState = "stopped";


        Intent restartService = new Intent(getApplicationContext(), this.getClass());
        PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 1, restartService, PendingIntent.FLAG_ONE_SHOT);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME, 5000, pendingIntent);
        super.onDestroy();

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private String saveTrailToGdb(String operationName, Context context) {
        long startTime = System.nanoTime();
        if (operationName.equalsIgnoreCase(CREATE_NEW_TRAIL_ENTRY)) {
            String startTimeStamp = DatePickerMethods.getCurrentDate_TimeStampString_yyyymmddhhmmss();
            try {
                String startDate = DatePickerMethods.getCurrentDate_DateString_yyyymmddhhmmss();
                TrailFeature trailFeature = DbTableUtils.getTrailFeatureByStartTimeStamp(context, TRAIL_TABLE_STARTTIMESTAMP, "string", startDate, reveloLogger);

                boolean isUpdateOperation = false;
                List<List<TrailLatLng>> completeTrailLatLngList;
                if (trailFeature != null) {
                    trailId = trailFeature.getTrailid();
                    JSONObject geometryGeoJson = trailFeature.getGeometryGeoJson();
                    completeTrailLatLngList = GeoJsonUtils.convertToTrailLatLngList(geometryGeoJson);
                    if (completeTrailLatLngList != null && completeTrailLatLngList.size()>0) {
                        List<TrailLatLng> tempList = new ArrayList<>();
                        tempList.addAll(currentTrailLatLngList);
                        currentTrailLatLngList = null;
                        currentTrailLatLngList=new ArrayList<>();
                        currentTrailLatLngList = completeTrailLatLngList.get(0);
                        currentTrailLatLngList.addAll(tempList);
                        completeTrailLatLngList.clear();
                        completeTrailLatLngList.add(currentTrailLatLngList);
                    }
                    isUpdateOperation = true;

                }
                else {
                    trailId = AppUtils.getTrailId(userName);
                    isUpdateOperation = false;
                    completeTrailLatLngList = new ArrayList<>();
                    completeTrailLatLngList.add(currentTrailLatLngList);
                }

               /* Map<String, Object> jurisdictionValuesMap = ReDbTable.getJurisdictionFromPoint(this, point);
                HashMap<String,String> jurisdictionVariables = createJurisdictionNamesIdMap();
                ReveloLogger.trace("LocationReceiverService","getMultiLineGeometry", "TimeLogs "+ "done getting jurisdiction values "+ SystemUtils.getCurrentDateTimeMiliSec());
                Map<String, Object> properties = new HashMap<>();
                for(String jurisdictionName:jurisdictionVariables.keySet()){
                    String jurisdictionId = jurisdictionVariables.get(jurisdictionName);
                    if(jurisdictionValuesMap.containsKey(jurisdictionId)){
                        properties.put(jurisdictionName,jurisdictionValuesMap.get(jurisdictionId));
                    }
                }*/

                Geometry trailNewGeometry = getMultiLineGeometry(completeTrailLatLngList);

                if (trailNewGeometry != null) {

                    JSONObject geometryGeoJson = GeometryEngine.convertGeometryToGeoJson(trailNewGeometry);
                    completeTrailPolylineOverLay = GeoJsonUtils.toOSMPolylines(geometryGeoJson);
                    assert geometryGeoJson != null;
                    geometryGeoJsonStr = geometryGeoJson.toString();
                    Point point = trailNewGeometry.getCentroid();
                    Map<String, Object> properties = new HashMap<>();

                    properties.put(TRAIL_TABLE_TRAILID, trailId);
                    properties.put(TRAIL_TABLE_STARTTIMESTAMP, startTimeStamp);
                    properties.put(TRAIL_TABLE_ENDTIMESTAMP, startTimeStamp);
                    properties.put(TRAIL_TABLE_ISNEW, "started");
                    properties.put(TRAIL_TABLE_DESCRIPTION, "");
                    properties.put(TRAIL_TABLE_TRANSPORT_MODE, transportMode);
                    properties.put(TRAIL_TABLE_JURISDICTION_INFO, "");
                    properties.put(TRAIL_TABLE_DISTANCE, trailNewGeometry.getLength());
                    properties.put(TRAIL_TABLE_USERNAME, userName);
                    properties.put(TRAIL_TABLE_W9_ENTITY_CLASS_NAME, TRAIL_TABLE_NAME);
                    properties.put(TRAIL_TABLE_W9_METADATA, String.valueOf(createMetadata()));

                    if (isUpdateOperation) {
                        DbTableUtils.updateTrailRecordInDb(context, properties, geometryGeoJson, trailId, reveloLogger);
                    }
                    else {
                        DbTableUtils.insertTrailAddRecordInDb(this, properties, geometryGeoJson, trailId, reveloLogger);
                    }
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            return trailId;
        }
        else if (operationName.equalsIgnoreCase(UPDATE_TRAIL_ENTRY)) {

            if (currentTrailLatLngList == null || currentTrailLatLngList.size() == 0) {
                reveloLogger.debug(className, "saveTrailToGdb",
                                   "no trail latlong found, asking db for latlng for trail id " + trailId + ".. skipping update operation");
                currentTrailLatLngList = getCurrentTrailLatLng(trailId);
            }

                List<List<TrailLatLng>> completeTrailLatLngList = new ArrayList<>();
                completeTrailLatLngList.add(currentTrailLatLngList);

            Geometry trailUpdateGeometry = getMultiLineGeometry(completeTrailLatLngList);

            if (trailUpdateGeometry != null) {
                JSONObject geometryGeoJson = GeometryEngine.convertGeometryToGeoJson(trailUpdateGeometry);
                completeTrailPolylineOverLay = GeoJsonUtils.toOSMPolylines(geometryGeoJson);
                assert geometryGeoJson != null;
                geometryGeoJsonStr = geometryGeoJson.toString();
                try {
                    if (trailId != null) {
                        Map<String, Object> properties = new HashMap<>();
                        properties.put(TRAIL_TABLE_TRANSPORT_MODE, transportMode);
                        properties.put(TRAIL_TABLE_DISTANCE, trailUpdateGeometry.getLength());

                        DbTableUtils.updateTrailRecordInDb(this, properties, geometryGeoJson, trailId, reveloLogger);

                        reveloLogger.debug(className, "saveTrailToGdb", "update trail");
                    }
                    else {
                        reveloLogger.debug(className, "saveTrailToGdb", "no trail id found, skipping update operation. service trail id: " + trailId);
                    }
                }
                catch (Exception e) {
                    reveloLogger.error(className, "saveTrailToGdb", "error updating entry for trail in gdb: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            else {
                reveloLogger.error(className, "saveTrailToGdb", "error updating entry for trail in gdb geometry: null");
            }

        }
        else if (operationName.equalsIgnoreCase(PAUSE_TRAIL_ENTRY)) {
            try {
                  /*  if(trailId==null) {
                        trailId = getTrailW9Id();
                    }*/
                if (currentTrailLatLngList == null || currentTrailLatLngList.size() == 0) {
                    reveloLogger.debug(className, "saveTrailToGdb", "no trail latlong found, asking db for latlng for trail id " + trailId);
                    currentTrailLatLngList = getCurrentTrailLatLng(trailId);
                }

                if (currentTrailLatLngList != null) {
                    List<List<TrailLatLng>> completeTrailLatLngList = new ArrayList<>();
                    completeTrailLatLngList.add(currentTrailLatLngList);
                    reveloLogger.debug(className, "saveTrailToGdb", "updating pause entry");
                    Geometry trailUpdateGeometry = getMultiLineGeometry(completeTrailLatLngList);

                    if (trailUpdateGeometry != null) {
                        JSONObject geometryGeoJson = GeometryEngine.convertGeometryToGeoJson(trailUpdateGeometry);
                        completeTrailPolylineOverLay = GeoJsonUtils.toOSMPolylines(geometryGeoJson);
                        assert geometryGeoJson != null;
                        geometryGeoJsonStr = geometryGeoJson.toString();
                        if (trailId != null) {
                            Map<String, Object> properties = new HashMap<>();
                            properties.put(TRAIL_TABLE_ISNEW, "paused");

                            DbTableUtils.updateTrailRecordInDb(this, properties, geometryGeoJson, trailId, reveloLogger);

                            reveloLogger.debug(className, "saveTrailToGdb", "update trail");
                        }
                        else {
                            reveloLogger.debug(className, "saveTrailToGdb", "trailid null, not updating pause entry");
                        }
                    }
                }
                else {
                    reveloLogger.debug(className, "saveTrailToGdb", "trailLatLngList null, not updating pause entry");
                }
            }
            catch (Exception e) {
                reveloLogger.error(className, "saveTrailToGdb", "error updating entry for trail in gdb: " + e.getMessage());
                e.printStackTrace();
            }


        }
        else if (operationName.equalsIgnoreCase(END_TRAIL_ENTRY)) {
            if (currentTrailLatLngList == null || currentTrailLatLngList.size() == 0) {
                reveloLogger.debug(className, "saveTrailToGdb", "no trail latlong found, asking db for latlng for trail id " + trailId);
                currentTrailLatLngList = getCurrentTrailLatLng(trailId);
            }
            boolean deleteTrailEntry = false;
            if (currentTrailLatLngList != null && currentTrailLatLngList.size() > 1) {
                List<List<TrailLatLng>> completeTrailLatLngList = new ArrayList<>();
                completeTrailLatLngList.add(currentTrailLatLngList);
                Geometry trailEndGeometry = getMultiLineGeometry(completeTrailLatLngList);
                if (trailEndGeometry != null) {
                    JSONObject geometryGeoJson = GeometryEngine.convertGeometryToGeoJson(trailEndGeometry);
                    completeTrailPolylineOverLay = GeoJsonUtils.toOSMPolylines(geometryGeoJson);
                    assert geometryGeoJson != null;
                    geometryGeoJsonStr = geometryGeoJson.toString();
//                    String endTimeStamp = SystemUtils.getCurrentDateTime();
//                    String endTimeStamp = DatePickerMethods.getCurrentDateString_metadata();
                    String endTimeStamp = DatePickerMethods.getCurrentDate_TimeStampString_yyyymmddhhmmss();

                    try {
                       /* if(trailId==null) {
                            trailId = getTrailW9Id();
                        }*/
                        if (trailId != null && ! trailId.isEmpty()) {
                            Map<String, Object> properties = new HashMap<>();
                            properties.put(TRAIL_TABLE_ENDTIMESTAMP, endTimeStamp);
                            properties.put(TRAIL_TABLE_ISNEW, "1");
                            properties.put(TRAIL_TABLE_TRANSPORT_MODE, transportMode);
                            properties.put(TRAIL_TABLE_DISTANCE, trailEndGeometry.getLength());


                            DbTableUtils.updateTrailRecordInDb(this, properties, geometryGeoJson, trailId, reveloLogger);

                            reveloLogger.debug(className, "saveTrailToGdb", "end trail updated");
                        }
                        if (currentTrailLatLngList != null) {
                            reveloLogger.debug(className, "saveTrailToGdb", "Clearing service trail latlong list");
                            currentTrailLatLngList.clear();
                        }
                        appPreferences.putFloat("TRAILSPEED", 0f);//.setTrailSpeed(0f);
                    }
                    catch (Exception e) {
                        deleteTrailEntry = true;

                        reveloLogger.error(className, "saveTrailToGdb", "error updating entry for trail in gdb: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
                else {
                    deleteTrailEntry = true;
                    reveloLogger.debug(className, "saveTrailToGdb", "trail geom null, not updating end entry..moving to delete existing bad feature");
                }
            }
            else {
                deleteTrailEntry = true;
            }
            if (deleteTrailEntry) {
                reveloLogger.debug(className, "saveTrailToGdb", "deleting existing bad feature trail id: " + trailId);
                //delete trail entry
                try {

                    DbTableUtils.deleteFeatureRecordOnly(this, trailId, TRAIL_TABLE_TRAILID, reveloLogger);

                    reveloLogger.debug(className, "saveTrailToGdb", "deleting trail");
                    if (currentTrailLatLngList != null) {
                        reveloLogger.debug(className, "saveTrailToGdb", "Clearing service trail latlong list");
                        currentTrailLatLngList.clear();
                    }
                }
                catch (Exception e) {
                    reveloLogger.error(className, "saveTrailToGdb", " error deleting empty trail entry for trail in gdb");
                    e.printStackTrace();
                }
            }
        }
        reveloLogger.trace(className, "saveTrailToGdb",
                           "TimeLogs " + "done saving by" + SystemUtils.getCurrentDateTimeMiliSec() + " \n time taken(ms) = " + ((System.nanoTime() - startTime) / 1000000));
        return trailId;
    }

    /*private static Geometry getMultiLineGeometry(List<TrailLatLng> geoPoints) {
        if (geoPoints == null || geoPoints.size() == 0) {
            return null;
        }
        try {
            GeometryFactory factory = new GeometryFactory(new PrecisionModel(), 4326);

            Coordinate[] coordinates = new Coordinate[geoPoints.size()];
            for (int i = 0; i < geoPoints.size(); i++) {
                coordinates[i] = new Coordinate(geoPoints.get(i).getLongitude(), geoPoints.get(i).getLatitude());
            }

            LineString lineString = factory.createLineString(coordinates);
            LineString[] lineStrings = {lineString};
            Geometry geometry = factory.createMultiLineString(lineStrings);
            return geometry;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }  */
    private static Geometry getMultiLineGeometry(List<List<TrailLatLng>> trailLatLngList) {
        if (trailLatLngList == null || trailLatLngList.size() == 0) {
            return null;
        }
        try {
            GeometryFactory factory = new GeometryFactory(new PrecisionModel(), 4326);
            LineString[] lineStrings = new LineString[trailLatLngList.size()];
            for (int j = 0; j < trailLatLngList.size(); j++) {
                List<TrailLatLng> geoPoints = trailLatLngList.get(j);
                Coordinate[] coordinates = new Coordinate[geoPoints.size()];
                for (int i = 0; i < geoPoints.size(); i++) {
                    coordinates[i] = new Coordinate(geoPoints.get(i).getLongitude(), geoPoints.get(i).getLatitude());
                }

                LineString lineString = factory.createLineString(coordinates);
                lineStrings[j] = lineString;
            }

            Geometry geometry = factory.createMultiLineString(lineStrings);
            return geometry;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    private static Geometry getPointGeometry(TrailLatLng trailLatLng) {
        if (trailLatLng==null) {
            return null;
        }
        try {
            GeometryFactory factory = new GeometryFactory(new PrecisionModel(), 4326);
            return factory.createPoint(new Coordinate(trailLatLng.getLongitude(), trailLatLng.getLatitude()));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private JSONObject createMetadata() {

        JSONObject metadataJSON = new JSONObject();

        try {

            metadataJSON.put(Constants.W9_LATITUDE, currentLocation.getLatitude());
            metadataJSON.put(Constants.W9_LONGITUDE, currentLocation.getLongitude());
            metadataJSON.put(Constants.W9_ACCURACY, currentLocation.getAccuracy());

            // metadataJSON.put(AppConstants.W9_REVISIT, false);
//            metadataJSON.put(AppConstants.W9_ATTACHMENTS_INFO, new JSONArray());
            //metadataJSON.put(AppConstants.W9_REQUIRES_ADJUSTMENT, false);
//
            //metadataJSON.put(AppConstants.W9_CREATED_BY, ReveloStore.getUsername());
            //metadataJSON.put(AppConstants.W9_CREATION_DATE, SystemUtils.getCurrentDateTime());
            //metadataJSON.put(AppConstants.W9_UPDATED_BY, "");
            metadataJSON.put(Constants.W9_UPDATE_DATE, DatePickerMethods.getCurrentDateString_metadata());

        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return metadataJSON;
    }

    private List<TrailLatLng> getCurrentTrailLatLng(String trailId) {

        try {
            TrailFeature featureTrial = DbTableUtils.getTrailFeature(this, TRAIL_TABLE_TRAILID, "String", trailId, reveloLogger);
            if (featureTrial == null) {
                return null;
            }
            else {
                String trailRunningStatus = featureTrial.getIsnew();
                if (trailRunningStatus == null || trailRunningStatus.isEmpty()) {
                    return null;
                }
                if (trailRunningStatus.equalsIgnoreCase("started") || trailRunningStatus.equalsIgnoreCase("paused")) {
                    List<List<TrailLatLng>> lists = GeometryEngine.convertJTSGeometryToTrailLatLng_multipart(featureTrial.getJtsGeometry());
                    if (lists != null && lists.size() > 0) {
                        return lists.get(0);
                    }
                }

            }
        }
        catch (Exception exception) {
            exception.printStackTrace();
        }
        return null;
    }

    private void sendLastLocationToMap(String trailId) {
        if (currentLocation != null) {
            String errorMessage = "Mock location on. Unable to creating feature.";
            Bundle bundle = new Bundle();

            if (! trailState.equalsIgnoreCase("stopped")) {
                reveloLogger.debug(className, "sendLastLocationToMap", "trailState = " + trailState + ", creating bundle..");
                bundle.putString("trailState", trailState);
                bundle.putBoolean("stopTrail", true);
                bundle.putString("trailId", trailId);
                boolean firstLocation = currentTrailLatLngList == null || currentTrailLatLngList.size() <= 0;
                bundle.putBoolean("firstLocation", firstLocation);
                bundle.putSerializable("trailLatLongs", (Serializable) currentTrailLatLngList);
                bundle.putSerializable("geometryGeoJsonStr", (Serializable) geometryGeoJsonStr);
                if(stopGeoJson != null && recordStops) {
                    bundle.putString("stopJson", stopGeoJson.toString());
                }
                reveloLogger.debug(className, "sendLastLocationToMap",
                                   "bundle created for trails..stoptrail=true, trail id " + trailId + " , first location " + firstLocation);
            }
            Intent locationIntent = new Intent();
            locationIntent.setAction(ACTION.UPDATE_LOCATION_BROADCASTACTION_RESULTCODE_LOCATION);
            locationIntent.putExtra(ACTION.UPDATE_LOCATION_BROADCASTACTION_RESULTCODE_LOCATION, bundle);
            sendBroadcast(locationIntent);
        }
    }

    private void locationListener() {
        reveloLogger.trace(className, "locationListener", "TimeLogs " + "init location listener - " + SystemUtils.getCurrentDateTimeMiliSec());
        mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if (location != null) {
                    try {

//----------------1   //check for mock location
                        boolean isMock = location.isFromMockProvider();

                        isMock = false;
                        if (! isMock) {
                            long timeNow = System.currentTimeMillis();
                            if (currentLocation != null) {
                                previousLocation = currentLocation;
                                previousLocationTime = currentLocationTime;
                            }
                            else {
                                previousLocation = location;
                                previousLocationTime = timeNow;
                            }
                            currentLocation = location;
                            currentLocationTime = timeNow;

     //----------------2    //calculate speed---------------
                            float distanceTravelled = previousLocation.distanceTo(currentLocation);//in meters
                            float calculatedSpeed = 0;
                            if (distanceTravelled != 0) {
                                float timeDiff = currentLocationTime - previousLocationTime;
                                float timeSpentInSec = 0;
                                if (timeDiff != 0) {
                                    timeSpentInSec = timeDiff / 1000;
                                }
                                if (timeSpentInSec != 0) {
                                    calculatedSpeed = distanceTravelled / (timeSpentInSec);
                                }
                            }

     //----------------3    //get speed from location
                            float speedPerSec = location.getSpeed();
                            if(speedPerSec==0){
                               speedPerSec=calculatedSpeed;
                            }


     //----------------4    //generate transport mode based on current and prev speeds
                            float currentSpeed = 3.6f * speedPerSec;

                            float previousSpeed = appPreferences.getFloat("TRAILSPEED", 0f);

                            if (currentSpeed > previousSpeed) {
                                appPreferences.putFloat("TRAILSPEED", currentSpeed);

                                if (currentSpeed < 7.0f) {
                                    transportMode = "Walking";
                                }
                                else if (currentSpeed > 7.0f && currentSpeed < 20.0f) {
                                    transportMode = "Bicycle";
                                }
                                else if (currentSpeed > 20.0f) {
                                    transportMode = "Vehicle";
                                }
                            }


      //----------------5   //save new location to trail if user is not stationary
                            Bundle bundle = null;
                            if (trailState.equalsIgnoreCase("started")) {
                                reveloLogger.debug(className, "locationListener - onLocationChanged ",
                                                   "trailState =  started, creating bundle for it: time: " + SystemUtils.getCurrentDateTimeMiliSec());
                                if (currentTrailLatLngList == null) {
                                    currentTrailLatLngList = new ArrayList<>();
                                }

                                boolean new_location_added_to_trail = false;
                                TrailLatLng currentTrailLatLng = new TrailLatLng(location);
                                //do not record trail if stop is ongoing
                                if(CURRENT_STOP_STATE != STOP_STARTED && CURRENT_STOP_STATE!= SPEED_ZERO_START) {
                                    currentTrailLatLngList.add(currentTrailLatLng);
                                    new_location_added_to_trail=true;
                                }
                                reveloLogger.debug(className, "locationListener - onLocationChanged ",
                                                   "current stop state = "+CURRENT_STOP_STATE+", hence, trailLatLngList size = " + currentTrailLatLngList.size());
                                if (currentTrailLatLngList.size() > 0 && new_location_added_to_trail) {
                                    if (currentTrailLatLngList.size() == 2) {
                                        reveloLogger.debug(className, "locationListener - onLocationChanged ",
                                                           "trailLatLngList size = 2, creating trail feature in db");
                                        saveTrailToGdb(CREATE_NEW_TRAIL_ENTRY, getBaseContext());
                                    }
                                    else if (currentTrailLatLngList.size() > 2) {
                                        reveloLogger.debug(className, "locationListener - onLocationChanged ",
                                                           "trailLatLngList size > 2, updating existing trail feature - id : " + trailId + "  in db");
                                        saveTrailToGdb(UPDATE_TRAIL_ENTRY, getBaseContext());
                                    }
                                }


     //----------------6    //calculcate stop CURRENT_STOP_STATE, even if we dont want to record a stop in db or notify user about it
                                stopGeoJson = null;//stop part start-----------------------
                                if(trailId!=null /*&& recordStops*/) {//calculate a stop but dont inform app or db as if user is in stop, we wont record trail
                                    /*if (transportMode.equalsIgnoreCase("vehicle")) {
                                        SPEED_ZERO_THRESHOLD = 20.0f;
                                    }
                                    else if (transportMode.equalsIgnoreCase("bicycle")) {
                                        SPEED_ZERO_THRESHOLD = 8.0f;
                                    }
                                    else {
                                        SPEED_ZERO_THRESHOLD = 1.0f;
                                    }*/
                                    try {

                                        boolean updateStopOperation = true;

                                        switch (CURRENT_STOP_STATE) {
                                            case NO_STOP:
                                                if (calculatedSpeed <= SPEED_ZERO_THRESHOLD) {
                                                    currentZeroSpeedStartTimeStamp = System.nanoTime();
                                                    CURRENT_STOP_STATE = StopStatus.SPEED_ZERO_START;
                                                }
                                                break;
                                            case SPEED_ZERO_START:
                                                if (calculatedSpeed <= SPEED_ZERO_THRESHOLD) {
                                                    if (currentZeroSpeedStartTimeStamp == 0) {
                                                        currentZeroSpeedStartTimeStamp = System.nanoTime();
                                                    }
                                                    else {
                                                        long stopDuration = System.nanoTime() - currentZeroSpeedStartTimeStamp;
                                                        totalZeroSpeedSeconds = (double) stopDuration / 1000000000;
                                                        if (totalZeroSpeedSeconds >= STOP_THRESHOLD) {
                                                            CURRENT_STOP_STATE = STOP_STARTED;
                                                        }
                                                    }
                                                }
                                                else {
                                                    CURRENT_STOP_STATE = StopStatus.NO_STOP;
                                                    currentStopId = null;
                                                }
                                                break;
                                            case STOP_STARTED:
                                                if (calculatedSpeed <= SPEED_ZERO_THRESHOLD) {
                                                    if (currentZeroSpeedStartTimeStamp == 0) {
                                                        currentZeroSpeedStartTimeStamp = System.nanoTime();
                                                    }
                                                    else {
                                                        long stopDuration = System.nanoTime() - currentZeroSpeedStartTimeStamp;
                                                        totalZeroSpeedSeconds = (double) stopDuration / 1000000000;
                                                    }

                                                    if (currentStopId == null || currentStopId.isEmpty()) {
                                                        currentStopId = AppUtils.getStopId(userName,trailId,currentZeroSpeedStartTimeStamp);/*trailId + "_" + currentZeroSpeedStartTimeStamp;*/
                                                        updateStopOperation=false;
                                                    }

                                                    JSONObject stopPropertiesJson = new JSONObject();
                                                    {
                                                        stopPropertiesJson.put(STOP_TABLE_STOPID              , currentStopId);
                                                        stopPropertiesJson.put(STOP_TABLE_TRAILID             , trailId);
                                                        stopPropertiesJson.put(STOP_TABLE_COMMENT             , "Stop duration :"+totalZeroSpeedSeconds);
                                                        stopPropertiesJson.put(STOP_TABLE_W9_ENTITY_CLASS_NAME, STOP_TABLE_NAME);
                                                        stopPropertiesJson.put(STOP_TABLE_STARTTIMESTAMP      , currentZeroSpeedStartTimeStamp);
                                                        stopPropertiesJson.put(STOP_TABLE_ENDTIMESTAMP        , currentZeroSpeedStopTimeStamp);
                                                        stopPropertiesJson.put(STOP_TABLE_USERNAME            , userName);
                                                        stopPropertiesJson.put(STOP_TABLE_W9_METADATA         , String.valueOf(createMetadata()));
                                                        stopPropertiesJson.put("stopDurationSec", totalZeroSpeedSeconds);
                                                        stopPropertiesJson.put("stopState", "ongoing");
                                                    }


                                                        Geometry stopGeom = getPointGeometry(currentTrailLatLng);
                                                        if(stopGeom!=null){
                                                            stopGeoJson = GeometryEngine.convertGeometryToGeoJson(stopGeom,stopPropertiesJson);
                                                        }

                                                }
                                                else {
                                                    currentZeroSpeedStopTimeStamp = System.nanoTime();
                                                    long stopDuration = currentZeroSpeedStopTimeStamp - currentZeroSpeedStartTimeStamp;
                                                    totalZeroSpeedSeconds = (double) stopDuration / 1000000000;
                                                    CURRENT_STOP_STATE = StopStatus.STOP_ENDED;
                                                    if (currentStopId == null || currentStopId.isEmpty()) {
                                                        currentStopId = AppUtils.getStopId(userName,trailId,currentZeroSpeedStartTimeStamp);//trailId + "_" + currentZeroSpeedStartTimeStamp;
                                                        updateStopOperation=false;
                                                    }
                                                    JSONObject stopPropertiesJson = new JSONObject();
                                                    {
                                                        stopPropertiesJson.put(STOP_TABLE_STOPID              , currentStopId);
                                                        stopPropertiesJson.put(STOP_TABLE_TRAILID             , trailId);
                                                        stopPropertiesJson.put(STOP_TABLE_COMMENT             , "Stop duration :"+totalZeroSpeedSeconds);
                                                        stopPropertiesJson.put(STOP_TABLE_W9_ENTITY_CLASS_NAME, STOP_TABLE_NAME);
                                                        stopPropertiesJson.put(STOP_TABLE_STARTTIMESTAMP      , currentZeroSpeedStartTimeStamp);
                                                        stopPropertiesJson.put(STOP_TABLE_ENDTIMESTAMP        , currentZeroSpeedStopTimeStamp);
                                                        stopPropertiesJson.put(STOP_TABLE_USERNAME            , userName);
                                                        stopPropertiesJson.put(STOP_TABLE_W9_METADATA         , String.valueOf(createMetadata()));
                                                        stopPropertiesJson.put("stopDurationSec", totalZeroSpeedSeconds);
                                                        stopPropertiesJson.put("stopState", "ended");
                                                    }


                                                    Geometry stopGeom = getPointGeometry(currentTrailLatLng);
                                                    if(stopGeom!=null){
                                                        stopGeoJson = GeometryEngine.convertGeometryToGeoJson(stopGeom,stopPropertiesJson);
                                                    }

                                                }
                                                break;
                                            case STOP_ENDED:
                                                if (calculatedSpeed <= SPEED_ZERO_THRESHOLD) {
                                                    currentZeroSpeedStartTimeStamp = System.nanoTime();
                                                    CURRENT_STOP_STATE = StopStatus.SPEED_ZERO_START;
                                                }
                                                else {
                                                    currentZeroSpeedStartTimeStamp = 0;
                                                    currentZeroSpeedStopTimeStamp = 0;
                                                    totalZeroSpeedSeconds = 0;
                                                    CURRENT_STOP_STATE = StopStatus.NO_STOP;
                                                    currentStopId = null;
                                                }
                                                break;
                                        }

                                        if(stopGeoJson!=null) {
                                            if (updateStopOperation) {
                                                DbTableUtils.updateStopRecordInDb(getApplicationContext(),currentStopId,stopGeoJson,reveloLogger);
                                            }
                                            else {
                                                DbTableUtils.insertStopAddRecordInDb(getApplicationContext(),currentStopId,stopGeoJson,reveloLogger);
                                            }
                                        }
                                    }
                                    catch (Exception e) {
                                        e.printStackTrace();
                                        stopGeoJson = null;
                                    }
                                }//stop part ends-----------------------

                                //create bundle for notification. add stop json only if we are asked to record stops
                                if (bundle == null) {
                                    bundle = new Bundle();
                                }
                                bundle.putBoolean("stopTrail", false);
                                bundle.putString("trailState", trailState);
                                bundle.putString("trailId", trailId);
                                boolean firstLocation = currentTrailLatLngList == null || currentTrailLatLngList.size() <= 1;
                                bundle.putBoolean("firstLocation", firstLocation);
                                bundle.putSerializable("trailLatLongs", (Serializable) currentTrailLatLngList);
                                bundle.putSerializable("geometryGeoJsonStr", (Serializable) geometryGeoJsonStr);
                                if(stopGeoJson != null && recordStops) {
                                    bundle.putString("stopJson", stopGeoJson.toString());
                                }
                                reveloLogger.debug(className, "locationListener - onLocationChanged ",
                                                   "bundle created for trail, stop trail = false ,firstlocation = " + firstLocation + ": endtime : " + SystemUtils.getCurrentDateTimeMiliSec());
                            }
                            if (notificationBuilder == null) {
                                // notificationBuilder = showNotification("Captured new location with accuracy of " + location.getAccuracy() + " meters.");
                            }
                            else {
                                if(stopGeoJson != null && recordStops){
                                    changeNotificationText("stop:" + stopGeoJson.toString());
                                }else {
                                    changeNotificationText("Captured new location with accuracy of " + location.getAccuracy() + " meters .\n You are travelling at speed " + location.getSpeed() + ".\n( Calculated Speed = " + calculatedSpeed + " m/s) at time " + SystemUtils.getCurrentDateTimeMiliSec());
                                }
                            }

                            if (bundle != null) {
                                reveloLogger.debug(className, "locationListener - onLocationChanged ", "Sending bundle to activity..");
                                // receiver.send(UPDATE_LOCATION_RESULTCODE_LOCATION, bundle);
                                Intent locationIntent = new Intent();
                                locationIntent.setAction(ACTION.UPDATE_LOCATION_BROADCASTACTION_RESULTCODE_LOCATION);
                                locationIntent.putExtra(ACTION.UPDATE_LOCATION_BROADCASTACTION_RESULTCODE_LOCATION, bundle);
                                sendBroadcast(locationIntent);
                            }

                        }
                        else {
                            reveloLogger.debug(className, "locationListener - onLocationChanged ", "mock location received..warning user");

                            String errorMessage = "Mock location on. Unable to creating feature.";
                            Bundle bundle = new Bundle();
                            bundle.putString("errorMessage", errorMessage);
                            // receiver.send(UPDATE_LOCATION_ERRORCODE_MOCK_ON, bundle);
                            Intent locationIntent = new Intent();
                            locationIntent.setAction(ACTION.UPDATE_LOCATION_BROADCASTACTION_ERRORCODE_MOCK_ON);
                            locationIntent.putExtra(ACTION.UPDATE_LOCATION_BROADCASTACTION_ERRORCODE_MOCK_ON, bundle);
                            sendBroadcast(locationIntent);
                            stopForeground(true);
                            stopSelf();
                        }
                    }
                    catch (Exception e) {
                        reveloLogger.error(className, "on Location Changed", " error. skipping current location: exception " + e.getMessage());
                        // SystemUtils.showShortToast("Skipping current location error found.", DrawGeometryByLocationService.this);
                        e.printStackTrace();
                    }
                }
                else {
                    reveloLogger.debug(className, "locationListener - onLocationChanged ", "null location received");
                }
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {
                reveloLogger.debug(className, "locationListener - onStatusChanged ", "s: " + s + " i: " + i);
            }

            @Override
            public void onProviderEnabled(String s) {
                reveloLogger.debug(className, "locationListener - onProviderEnabled ", "s: " + s);
            }

            @Override
            public void onProviderDisabled(String s) {
                reveloLogger.debug(className, "locationListener - onProviderDisabled ", "s: " + s);
            }
        };


        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
    }

    private boolean sendExtraCommand(String command, Bundle bundle) {
        return mLocationManager.sendExtraCommand(LocationManager.GPS_PROVIDER, command, bundle);
    }

    private NotificationCompat.Builder showNotification(String message) {

        /*Intent notificationIntent = new Intent(this, MapActivity.class);
        notificationIntent.putExtra("callingActivity","locationReceiverService");
        *//*PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);*//*
        PendingIntent pendingIntent = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            pendingIntent = PendingIntent.getActivity
                    (this, 0, notificationIntent, PendingIntent.FLAG_MUTABLE);
        }
        else
        {
            pendingIntent = PendingIntent.getActivity
                    (this, 0, notificationIntent, PendingIntent.FLAG_ONE_SHOT);
        }
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (mNotificationManager != null) {

            String notificationId = "Revelo Notify", channelName = "Revelo Channel",
                    notificationDescription = "Notification for Download File.";

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(notificationId, channelName,
                                                                      NotificationManager.IMPORTANCE_HIGH);
                channel.setDescription(notificationDescription);
                mNotificationManager.createNotificationChannel(channel);
            }

            notificationBuilder = new NotificationCompat.Builder(this, notificationId)
                    .setContentTitle(message)
                    .setSmallIcon(R.drawable.revelo_logo)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true);

            notification = notificationBuilder.build();
            startForeground(CHANNEL_ID, notification);
        }*/
        return notificationBuilder;
    }

    private void changeNotificationText(String text) {
        if (notificationBuilder != null) {
            notificationBuilder.setContentText(text);
            notificationBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(text));
            if (mNotificationManager != null) {
                mNotificationManager.notify(CHANNEL_ID, notificationBuilder.build());
            }

        }
        reveloLogger.debug(className, "changeNotification", text);
    }

    public interface SERVICE_STATE {
        String LOCATION_SERIVCE_STATE_STARTED = "service_started";
        String LOCATION_SERIVCE_STATE_STOPPED = "service_stopped";
    }

    public interface TRAIL_STATE {
        String TRAIL_STATE_NOTSTARTED = "trail_not_started";
        String TRAIL_STATE_STARTED = "trail_started";
        String TRAIL_STATE_PAUSED = "trail_paused";
        String TRAIL_STATE_STOPPED = "trail_stopped";
    }

    public interface DB_STATE {
        String DB_STATE_IDLE = "db_idle";
        String DB_STATE_CREATING_TRAIL = "db_creating_trail";
        String DB_STATE_SEARCHING_TRAIL = "db_searching_trail";
        String DB_STATE_SAVING_UPDATES = "db_saving_updates";
        String DB_STATE_SAVING_AFTER_TRAIL_STOPPED = "db_saving_trail_after_stopped";
        String DB_STATE_TRAIL_SAVE_COMPLETED = "db_saving_trail_completed";
    }

    public interface ACTION {
        String UPDATE_LOCATION_STARTFOREGROUND_ACTION = "startForeground";
        String UPDATE_LOCATION_STOPFOREGROUND_ACTION = "stopForeground";
        String UPDATE_LOCATION_PAUSEFOREGROUND_ACTION = "pauseForeground";
        String UPDATE_LOCATION_RESUMEFOREGROUND_ACTION = "resumeForeground";
        String BROADCAST_ACTION = "broadcastEvent";
        String UPDATE_LOCATION_BROADCASTACTION_RESULTCODE_LOCATION = "location";
        String UPDATE_LOCATION_BROADCASTACTION_ERRORCODE_MOCK_ON = "mockon";
    }
}
