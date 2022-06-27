package com.sixsimplex.trail.utils.dbcalls;

import android.content.Context;
import android.icu.text.Edits;
import android.widget.Toast;

import com.sixsimplex.revelologger.ReveloLogger;
import com.sixsimplex.trail.Constants;
import com.sixsimplex.trail.TrailFeature;
import com.sixsimplex.trail.gepoackage.GeoPackageRWAgent;
import com.vividsolutions.jts.geom.Geometry;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class DbTableUtils {

    private static final String className = "DbTableUtils";

    public static TrailFeature getTrailFeatureByStartTimeStamp(Context context, String trailTableColumnName, String trailTableColumnDataType, Object value,
                                                               ReveloLogger reveloLogger) {
        TrailFeature feature = null;
        boolean queryGeometry = false;
        try {
            HashMap<String, JSONObject> conditionMap = new HashMap<>();
            JSONObject conditionObj = new JSONObject();
            conditionObj.put("value", value+"%");
            conditionObj.put("operator", "like");
            conditionObj.put("columnType", trailTableColumnDataType);
            conditionMap.put(trailTableColumnName, conditionObj);



            GeoPackageRWAgent gpkgRWAgent = new GeoPackageRWAgent(DbRelatedConstants.getPropertiesJsonForDataGpkg(context), reveloLogger, context);


            queryGeometry = true;

            JSONObject respJObj = gpkgRWAgent
                    .getDatasetContent(context, DbRelatedConstants.getDataSourceInfoForDataGpkg(context),
                                       DbRelatedConstants.getTrailsDatasetInfo(context), null, conditionMap,
                                       "", true, 1, queryGeometry, true);

            if (respJObj.has("status") && respJObj.getString("status").equalsIgnoreCase("success")) {

                if (respJObj.has("features")) {
                    JSONObject responseFeatures = respJObj.getJSONObject("features");
                    if (responseFeatures.has("features")) {
                        JSONArray featuresJArray = responseFeatures.getJSONArray("features");
                        for (int i = 0; i < featuresJArray.length(); i++) {
                            JSONObject featureJObj = featuresJArray.getJSONObject(i);
                            try {
                                feature = constructTrailFeatureFromJson(featureJObj, reveloLogger);
                            }
                            catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }

            }

        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return feature;
    }

    public static TrailFeature getTrailFeature(Context context, String trailTableColumnName, String trailTableColumnDataType, Object value,
                                                               ReveloLogger reveloLogger) {
        TrailFeature feature = null;
        boolean queryGeometry = false;
        try {
            HashMap<String, JSONObject> conditionMap = new HashMap<>();
            JSONObject conditionObj = new JSONObject();
            conditionObj.put("value", value);
            conditionObj.put("operator", "=");
            conditionObj.put("columnType", trailTableColumnDataType);
            conditionMap.put(trailTableColumnName, conditionObj);



            GeoPackageRWAgent gpkgRWAgent = new GeoPackageRWAgent(DbRelatedConstants.getPropertiesJsonForDataGpkg(context), reveloLogger, context);


            queryGeometry = true;

            JSONObject respJObj = gpkgRWAgent
                    .getDatasetContent(context, DbRelatedConstants.getDataSourceInfoForDataGpkg(context),
                                       DbRelatedConstants.getTrailsDatasetInfo(context), null, conditionMap,
                                       "", true, 1, queryGeometry, true);

            if (respJObj.has("status") && respJObj.getString("status").equalsIgnoreCase("success")) {

                if (respJObj.has("features")) {
                    JSONObject responseFeatures = respJObj.getJSONObject("features");
                    if (responseFeatures.has("features")) {
                        JSONArray featuresJArray = responseFeatures.getJSONArray("features");
                        for (int i = 0; i < featuresJArray.length(); i++) {
                            JSONObject featureJObj = featuresJArray.getJSONObject(i);
                            try {
                                feature = constructTrailFeatureFromJson(featureJObj, reveloLogger);
                            }
                            catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }

            }

        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return feature;
    }

    private static TrailFeature constructTrailFeatureFromJson(JSONObject featureJObj, ReveloLogger logger) {
        TrailFeature feature = null;
        try {
            JSONObject propertiesJobjReceived = featureJObj.getJSONObject("properties");
            feature = new TrailFeature();

            try {
                try {
                    if (propertiesJobjReceived.has(Constants.TRAIL_TABLE_TRAILID)) {
                        feature.setTrailid(String.valueOf(propertiesJobjReceived.get(Constants.TRAIL_TABLE_TRAILID)));
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
                try {
                    if (propertiesJobjReceived.has(Constants.TRAIL_TABLE_USERNAME)) {
                        feature.setUsername(String.valueOf(propertiesJobjReceived.get(Constants.TRAIL_TABLE_USERNAME)));
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                    feature.setUsername("");
                }
                try {
                    if (propertiesJobjReceived.has(Constants.TRAIL_TABLE_STARTTIMESTAMP)) {
                        feature.setStarttimestamp(String.valueOf(propertiesJobjReceived.get(Constants.TRAIL_TABLE_STARTTIMESTAMP)));
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
                try {
                    if (propertiesJobjReceived.has(Constants.TRAIL_TABLE_ENDTIMESTAMP)) {
                        feature.setEndtimestamp(String.valueOf(propertiesJobjReceived.get(Constants.TRAIL_TABLE_ENDTIMESTAMP)));
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    if (propertiesJobjReceived.has(Constants.TRAIL_TABLE_ISNEW)) {
                        feature.setIsnew(String.valueOf(propertiesJobjReceived.get(Constants.TRAIL_TABLE_ISNEW)));
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    if (propertiesJobjReceived.has(Constants.TRAIL_TABLE_TRANSPORT_MODE)) {
                        feature.setTransportmode(String.valueOf(propertiesJobjReceived.get(Constants.TRAIL_TABLE_TRANSPORT_MODE)));
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    if (propertiesJobjReceived.has(Constants.TRAIL_TABLE_JURISDICTION_INFO)) {
                        feature.setJurisdictioninfo(String.valueOf(propertiesJobjReceived.get(Constants.TRAIL_TABLE_JURISDICTION_INFO)));
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    if (propertiesJobjReceived.has(Constants.TRAIL_TABLE_DESCRIPTION)) {
                        feature.setDescription(String.valueOf(propertiesJobjReceived.get(Constants.TRAIL_TABLE_DESCRIPTION)));
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    if (propertiesJobjReceived.has(Constants.TRAIL_TABLE_W9_METADATA)) {
                        feature.setW9metadata(new JSONObject(String.valueOf(propertiesJobjReceived.get(Constants.TRAIL_TABLE_W9_METADATA))));
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    if (propertiesJobjReceived.has(Constants.TRAIL_TABLE_DISTANCE)) {
                        feature.setDistance(Double.parseDouble(String.valueOf(propertiesJobjReceived.get(Constants.TRAIL_TABLE_DISTANCE))));
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                    feature.setDistance(0);
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }


            try {
                if (featureJObj.has("geometry")) {
                    JSONObject geometryGeoJson = featureJObj.getJSONObject("geometry");
                    Geometry geometry = GeoJsonUtils.convertToJTSGeometry(geometryGeoJson);
                    feature.setGeometryGeoJson(geometryGeoJson);
                    feature.setJtsGeometry(geometry);
                }
                else {
                    feature.setGeometryGeoJson(null);
                    feature.setJtsGeometry(null);
                }
            }
            catch (Exception e) {
                e.printStackTrace();
                feature.setGeometryGeoJson(null);
                feature.setJtsGeometry(null);
            }
        }
        catch (java.lang.Exception e) {
            e.printStackTrace();
            feature = null;
            logger.error(className, "constructing gdbfeature from json", "feature construction failed, reason: " + e.getMessage());
        }
        if (feature == null || feature.getTrailid() == null || feature.getTrailid().isEmpty() || feature.getGeometryGeoJson() == null) {
            logger.error(className, "constructing gdbfeature from json", "feature construction failed");
            return null;
        }
        return feature;
    }

    public static void insertTrailAddRecordInDb(Context context, Map<String, Object> attributeValueMap, JSONObject geometry, String w9Id, ReveloLogger reveloLogger) {

        try {
            JSONArray dataJsonArray = new JSONArray();

            JSONObject dataObject = new JSONObject();
            JSONArray attributesJArray = new JSONArray();
            for (String attributeName : attributeValueMap.keySet()) {
                JSONObject attributeObj = new JSONObject();
                attributeObj.put("name", attributeName);
                attributeObj.put("value", attributeValueMap.get(attributeName));
                attributesJArray.put(attributeObj);
            }
            dataObject.put("attributes", attributesJArray);
            if (geometry != null) {
                dataObject.put("geometry", geometry);
            }

            dataJsonArray.put(dataObject);

            GeoPackageRWAgent gpkgRWAgent = new GeoPackageRWAgent(DbRelatedConstants.getPropertiesJsonForDataGpkg(context), reveloLogger,
                                                                  context);

            JSONObject respJObj = gpkgRWAgent
                    .writeDatasetContent(context, DbRelatedConstants.getDataSourceInfoForDataGpkg(context), DbRelatedConstants.getTrailsDatasetInfo(context), dataJsonArray);
            if (respJObj.has("status") && respJObj.getString("status").equalsIgnoreCase("success")) {
                EditMetaDataTable.insertAddRecord(w9Id, w9Id, context, Constants.TRAIL_TABLE_NAME,reveloLogger);  // insert add entry to editmetadata.
            }
            else {
                Toast.makeText(context,"Trail not added. Please try after sometime.",  Toast.LENGTH_LONG).show();
            }

        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void deleteFeatureRecordOnly(Context context, String trailId, String trailTableTrailid, ReveloLogger reveloLogger) {
    }

    public static void updateTrailRecordInDb(Context context, Map<String, Object> properties, JSONObject geometryGeoJson, String trailId, ReveloLogger reveloLogger) {
        try {
            JSONArray dataJsonArray = new JSONArray();

            JSONObject dataObject = new JSONObject();
            JSONArray attributesArray = new JSONArray();
            for (String attributeName : properties.keySet()) {
                JSONObject attributesObject = new JSONObject();
                attributesObject.put("name", attributeName);
                if (properties.get(attributeName) != null) {
                    attributesObject.put("value", properties.get(attributeName));
                    attributesArray.put(attributesObject);
                }
            }
            dataObject.put("attributes", attributesArray);
            if (geometryGeoJson != null) {
                dataObject.put("geometry", geometryGeoJson);
            }
            dataJsonArray.put(dataObject);

            JSONObject conditionJobj = new JSONObject();
            conditionJobj.put("conditionType", "attribute");
            conditionJobj.put("columnName", Constants.TRAIL_TABLE_TRAILID);
            conditionJobj.put("valueDataType", "text");
            conditionJobj.put("value", trailId);
            conditionJobj.put("operator", "=");
            JSONArray whereClauseArray = new JSONArray();
            whereClauseArray.put(conditionJobj);

            GeoPackageRWAgent gpkgRWAgent = new GeoPackageRWAgent(DbRelatedConstants.getPropertiesJsonForDataGpkg(context), reveloLogger,
                                                                  context);
            JSONObject datasetInfo = DbRelatedConstants.getTrailsDatasetInfo(context);
            JSONObject respJObj = gpkgRWAgent
                    .updateDatasetContent(DbRelatedConstants.getDataSourceInfoForDataGpkg(context), datasetInfo, dataJsonArray, whereClauseArray,
                                          "AND", context);
            if (respJObj.has("status") && respJObj.getString("status").equalsIgnoreCase("success")) {
                EditMetaDataTable.insertEditEntry(Constants.TRAIL_TABLE_NAME,trailId, trailId, context,reveloLogger);  // insert edit entry to editmetadata.
            }
            else {
                Toast.makeText(context,"Record updated not properly", Toast.LENGTH_LONG).show();
            }

        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void updateStopRecordInDb(Context context, String currentStopId, JSONObject stopGeoJson, ReveloLogger reveloLogger) {
       String methodName = "updateStopRecordInDb";
        try{
            JSONArray attributesJArray = new JSONArray();
            JSONObject stopJsonProp = stopGeoJson.getJSONArray("features").getJSONObject(0).getJSONObject("properties");
            Iterator<String> iterator = stopJsonProp.keys();
            while (iterator.hasNext()){
                String attributeName = iterator.next();
                String value = stopJsonProp.getString(attributeName);

                JSONObject attributeObj = new JSONObject();
                attributeObj.put("name", attributeName);
                attributeObj.put("value", value);
                attributesJArray.put(attributeObj);
            }


            JSONObject dataObject = new JSONObject();
            dataObject.put("attributes", attributesJArray);
            dataObject.put("geometry", stopGeoJson);

            JSONArray dataJsonArray = new JSONArray();
            dataJsonArray.put(dataObject);

            JSONObject conditionJobj = new JSONObject();
            conditionJobj.put("conditionType", "attribute");
            conditionJobj.put("columnName", Constants.STOP_TABLE_STOPID);
            conditionJobj.put("valueDataType", "text");
            conditionJobj.put("value", currentStopId);
            conditionJobj.put("operator", "=");
            JSONArray whereClauseArray = new JSONArray();
            whereClauseArray.put(conditionJobj);


            GeoPackageRWAgent gpkgRWAgent = new GeoPackageRWAgent(DbRelatedConstants.getPropertiesJsonForDataGpkg(context), reveloLogger,
                                                                  context);
            JSONObject datasetInfo = DbRelatedConstants.getStopsDatasetInfo(context);
            JSONObject respJObj = gpkgRWAgent
                    .updateDatasetContent(DbRelatedConstants.getDataSourceInfoForDataGpkg(context), datasetInfo, dataJsonArray, whereClauseArray,
                                          "AND", context);
            if (respJObj.has("status") && respJObj.getString("status").equalsIgnoreCase("success")) {
                EditMetaDataTable.insertEditEntry( Constants.STOP_TABLE_NAME,currentStopId, currentStopId,context,reveloLogger);  // insert edit entry to editmetadata.
            }
            else {
                Toast.makeText(context,"Record updated not properly", Toast.LENGTH_LONG).show();
            }

        }catch (Exception e){
            e.printStackTrace();
            reveloLogger.error(className,methodName,"Exception updating stop "+currentStopId+" : "+e.getMessage());
        }
    }

    public static void insertStopAddRecordInDb(Context context,String stopId, JSONObject stopGeoJson, ReveloLogger reveloLogger) {
        String methodName = "insertStopAddRecordInDb";
        try{

            JSONArray attributesJArray = new JSONArray();
            JSONObject stopJsonProp = stopGeoJson.getJSONArray("features").getJSONObject(0).getJSONObject("properties");
            Iterator<String> iterator = stopJsonProp.keys();
            while (iterator.hasNext()){
                String attributeName = iterator.next();
                String value = stopJsonProp.getString(attributeName);

                JSONObject attributeObj = new JSONObject();
                attributeObj.put("name", attributeName);
                attributeObj.put("value", value);
                attributesJArray.put(attributeObj);
            }


            JSONObject dataObject = new JSONObject();
            dataObject.put("attributes", attributesJArray);
            dataObject.put("geometry", stopGeoJson);

            JSONArray dataJsonArray = new JSONArray();
            dataJsonArray.put(dataObject);


            GeoPackageRWAgent gpkgRWAgent = new GeoPackageRWAgent(DbRelatedConstants.getPropertiesJsonForDataGpkg(context), reveloLogger,
                                                                  context);

            JSONObject respJObj = gpkgRWAgent
                    .writeDatasetContent(context, DbRelatedConstants.getDataSourceInfoForDataGpkg(context), DbRelatedConstants.getStopsDatasetInfo(context), dataJsonArray);
            if (respJObj.has("status") && respJObj.getString("status").equalsIgnoreCase("success")) {
                EditMetaDataTable.insertAddRecord(stopId, stopId, context, Constants.STOP_TABLE_NAME,reveloLogger);  // insert add entry to editmetadata.
            }
            else {
                Toast.makeText(context,"Stop not added. Please try after sometime.",  Toast.LENGTH_LONG).show();
            }
        }catch (Exception e){
            e.printStackTrace();
            reveloLogger.error(className,methodName,"Exception adding stop : "+e.getMessage());
        }
    }
}
