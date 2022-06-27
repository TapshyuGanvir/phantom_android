package com.sixsimplex.trail.utils.dbcalls;

import static com.sixsimplex.trail.utils.dbcalls.DbRelatedConstants.getEditMetadataDatasetInfo;

import android.content.Context;


import com.sixsimplex.revelologger.ReveloLogger;
import com.sixsimplex.trail.Constants;
import com.sixsimplex.trail.gepoackage.GeoPackageRWAgent;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class EditMetaDataTable {

    private static final String LAYER_NAME = "layername";
    private static final String FEATURE_LABEL = "featurelabel";
    private static final String ADD_COL = "addcol";
    private static final String EDIT_COL = "editcol";
    private static final String DELETE_COL = "deletecol";
    private static final String W9_META_DATA_COL = "w9metadata";
    private static final String className = "EditMetaDataTable";
    private static final String EDIT_META_TABLE_NAME = "editmetadata";


    public static boolean insertAddRecord(String featureName, String featureLabel, Context context, String layerName, ReveloLogger reveloLogger) {
        try {
            boolean isPresentInAdds = presentInAdd(layerName, featureName, context,reveloLogger); //check in adds from edit operation
            if (isPresentInAdds) {
                return true;
            }
            else {
                return insertRecord(featureName, ADD_COL, null, context, layerName, featureLabel,reveloLogger);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private static boolean presentInAdd(String layerName, String id, Context context, ReveloLogger reveloLogger) {
        return isRecordExists(id, ADD_COL, context, layerName,reveloLogger);
    }

    private static boolean insertRecord(String featureName, String columnName, String w9Metadata, Context context, String layerName, String featureLabel,ReveloLogger reveloLogger) {
        try {

            JSONArray dataJsonArray = new JSONArray();

            JSONObject dataObject = new JSONObject();
            JSONArray attributesJArray = new JSONArray();

            JSONObject attributeObjLayerName = new JSONObject();
            attributeObjLayerName.put("name", LAYER_NAME);
            attributeObjLayerName.put("value", layerName);
            attributesJArray.put(attributeObjLayerName);

            JSONObject attributeObjColumnFeature = new JSONObject();
            attributeObjColumnFeature.put("name", columnName);
            attributeObjColumnFeature.put("value", featureName);
            attributesJArray.put(attributeObjColumnFeature);

            JSONObject featureLabelJObj = new JSONObject();
            featureLabelJObj.put("name", FEATURE_LABEL);
            featureLabelJObj.put("value", featureLabel);
            attributesJArray.put(featureLabelJObj);

            if (w9Metadata != null) {
                JSONObject attributeObjw9Metadata = new JSONObject();
                attributeObjw9Metadata.put("name", W9_META_DATA_COL);
                attributeObjw9Metadata.put("value", w9Metadata);
                attributesJArray.put(attributeObjw9Metadata);
            }

            dataObject.put("attributes", attributesJArray);
            dataJsonArray.put(dataObject);


            GeoPackageRWAgent gpkgRWAgent = new GeoPackageRWAgent(DbRelatedConstants.getPropertiesJsonForMetdataGpkg(context), reveloLogger, context);
            JSONObject datasetInfo = getEditMetadataDatasetInfo(context,reveloLogger);
            JSONObject respJObj = gpkgRWAgent.writeDatasetContent(context, DbRelatedConstants.getDataSourceInfoForMetdataGpkg(context), datasetInfo, dataJsonArray);

            return respJObj.has("status") && respJObj.getString("status").equalsIgnoreCase("success");


        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    private static boolean isRecordExists(String featureName, String columnName, Context context, String layerName,ReveloLogger reveloLogger) {
        try {
            JSONArray whereClauseArray = new JSONArray();

            if (featureName != null) {
                JSONObject conditionJobjW9id = new JSONObject();
                conditionJobjW9id.put("conditionType", "attribute");
                conditionJobjW9id.put("columnName", columnName);
                conditionJobjW9id.put("valueDataType", "string");
                conditionJobjW9id.put("value", featureName);
                conditionJobjW9id.put("operator", "=");
                whereClauseArray.put(conditionJobjW9id);
            }

            if (layerName != null) {
                JSONObject conditionJobjLayerName = new JSONObject();
                conditionJobjLayerName.put("conditionType", "attribute");
                conditionJobjLayerName.put("columnName", LAYER_NAME);
                conditionJobjLayerName.put("valueDataType", "string");
                conditionJobjLayerName.put("value", layerName);
                conditionJobjLayerName.put("operator", "=");
                whereClauseArray.put(conditionJobjLayerName);
            }

            if (whereClauseArray.length() > 0) {
                GeoPackageRWAgent gpkgRWAgent = new GeoPackageRWAgent(DbRelatedConstants.getPropertiesJsonForMetdataGpkg(context), reveloLogger, context);
                JSONObject datasetInfo = getEditMetadataDatasetInfo(context,reveloLogger);
                JSONObject respJObj = gpkgRWAgent.getDatasetContent(context, DbRelatedConstants.getDataSourceInfoForMetdataGpkg(context),
                                                                    datasetInfo, null, whereClauseArray,
                                                                    "AND", true, - 1, false, false);

                if (respJObj.has("status") && respJObj.getString("status").equalsIgnoreCase("success")) {
                    if (respJObj.has("features")) {
                        JSONObject responseFeatures = respJObj.getJSONObject("features");
                        if (responseFeatures.has("features")) {
                            JSONArray featuresJArray = responseFeatures.getJSONArray("features");
                            if (featuresJArray.length() > 0) {
                                return true;
                            }
                        }
                    }
                }
                else {
                    return false;
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }



    private static boolean deleteRecord(String featureName, String columnName, Context context, String layerName,ReveloLogger reveloLogger) {
        try {
            JSONArray whereClauseArray = new JSONArray();
            JSONObject conditionJobjW9id = new JSONObject();
            conditionJobjW9id.put("conditionType", "attribute");
            conditionJobjW9id.put("columnName", columnName);
            conditionJobjW9id.put("valueDataType", "string");
            conditionJobjW9id.put("value", featureName);
            conditionJobjW9id.put("operator", "=");
            whereClauseArray.put(conditionJobjW9id);

            JSONObject conditionJobjLayerName = new JSONObject();
            conditionJobjLayerName.put("conditionType", "attribute");
            conditionJobjLayerName.put("columnName", LAYER_NAME);
            conditionJobjLayerName.put("valueDataType", "string");
            conditionJobjLayerName.put("value", layerName);
            conditionJobjLayerName.put("operator", "=");
            whereClauseArray.put(conditionJobjLayerName);


            GeoPackageRWAgent gpkgRWAgent = new GeoPackageRWAgent(DbRelatedConstants.getPropertiesJsonForMetdataGpkg(context), reveloLogger, context);
            JSONObject datasetInfo = getEditMetadataDatasetInfo(context,reveloLogger);
            JSONObject respJObj = gpkgRWAgent.deleteDatasetContent(DbRelatedConstants.getDataSourceInfoForMetdataGpkg(context), datasetInfo, whereClauseArray, "AND", context);
            return respJObj.has("status") && respJObj.getString("status").equalsIgnoreCase("success");


        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean insertEditEntry(String layerName, String id, String featureLabel, Context context,ReveloLogger reveloLogger) {

        boolean isInsert=false;
        try {


            boolean isPresentInAdds = presentInAdd(layerName, id, context,reveloLogger); //check in adds from edit operation

            if (isPresentInAdds) {
                isInsert = true; //already in adds
            }
            else {

                boolean isPresentInEdits = presentInEdit(layerName, id, context,reveloLogger);//check in edit

                if (isPresentInEdits) {
                    isInsert = true;//already in edits
                }
                else {

                    try {
                        isInsert = insertRecord(id, EDIT_COL, null, context, layerName, featureLabel,reveloLogger);
                    } catch (Exception e) {
                        isInsert = false;
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            isInsert = false;
        }
        return isInsert;
    }


    public static boolean insertDeleteEntry(String layerName, String id, String featureLabel, String w9MetaData, Context context,ReveloLogger reveloLogger) {

        boolean isDelete = false;

        try {

            boolean isPresentInAdds = presentInAdd(layerName, id, context,reveloLogger); //check in adds from edit operation


            if (isPresentInAdds) { //already in adds
                isDelete = deleteRecord(id, ADD_COL, context, layerName,reveloLogger);
            }
            else {

                boolean isPresentInEdits = presentInEdit(layerName, id, context,reveloLogger);//check in edit

                if (isPresentInEdits) {
                    isDelete = deleteRecord(id, EDIT_COL, context, layerName,reveloLogger);
                }

                try {
                    isDelete = insertRecord(id, DELETE_COL, w9MetaData, context, layerName, featureLabel,reveloLogger);
                } catch (Exception e) {
                    isDelete = false;
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            isDelete = false;
        }
        return isDelete;
    }

    private static boolean presentInEdit(String layerName, String id, Context context,ReveloLogger reveloLogger) {
        return isRecordExists(id, EDIT_COL, context, layerName,reveloLogger);
    }




    public static boolean isDataForUpload(String entityName, Context context,ReveloLogger reveloLogger) {

        int count = 0;
        try {
            GeoPackageRWAgent gpkgRWAgent = new GeoPackageRWAgent(DbRelatedConstants.getPropertiesJsonForMetdataGpkg(context), reveloLogger, context);
            count = gpkgRWAgent.getDatasetItemCount(context, DbRelatedConstants.getDataSourceInfoForMetdataGpkg(context), getEditMetadataDatasetInfo(context,reveloLogger));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return count > 0;
    }


    public static Map<String, HashMap<Object, String>> getAddedId(Context context, ReveloLogger reveloLogger) {

        Map<String, HashMap<Object, String>> featureIdMap = new LinkedHashMap<>();
        try {
            featureIdMap = getRecord(null, ADD_COL, context, null,reveloLogger);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return featureIdMap;
    }

    private static Map<String, HashMap<Object, String>> getRecord(String featureName, String columnName, Context context, String layerName,ReveloLogger reveloLogger) {

        Map<String, HashMap<Object, String>> featureIdMap = new LinkedHashMap<>();
        try {

            JSONArray whereClauseArray = new JSONArray();

            if (featureName != null) {
                JSONObject conditionJobjW9id = new JSONObject();
                conditionJobjW9id.put("conditionType", "attribute");
                conditionJobjW9id.put("columnName", columnName);
                conditionJobjW9id.put("valueDataType", "string");
                conditionJobjW9id.put("value", featureName);
                conditionJobjW9id.put("operator", "=");
                whereClauseArray.put(conditionJobjW9id);
            }
            else {
                JSONObject conditionJobjW9id = new JSONObject();
                conditionJobjW9id.put("conditionType", "attribute");
                conditionJobjW9id.put("columnName", columnName);
                conditionJobjW9id.put("valueDataType", "int");
                conditionJobjW9id.put("value", "\"\"");
                conditionJobjW9id.put("operator", "!=");
                whereClauseArray.put(conditionJobjW9id);

                JSONObject conditionJobjNotNull = new JSONObject();
                conditionJobjNotNull.put("conditionType", "attribute");
                conditionJobjNotNull.put("columnName", columnName);
                conditionJobjNotNull.put("valueDataType", "int");
                conditionJobjNotNull.put("value", "NULL");
                conditionJobjNotNull.put("operator", "IS NOT");
                whereClauseArray.put(conditionJobjNotNull);
            }

            if (layerName != null) {
                JSONObject conditionJobjLayerName = new JSONObject();
                conditionJobjLayerName.put("conditionType", "attribute");
                conditionJobjLayerName.put("columnName", LAYER_NAME);
                conditionJobjLayerName.put("valueDataType", "string");
                conditionJobjLayerName.put("value", layerName);
                conditionJobjLayerName.put("operator", "=");
                whereClauseArray.put(conditionJobjLayerName);
            }

            List<String> requiredColumnsList = new ArrayList<>();
            requiredColumnsList.add(FEATURE_LABEL);
            requiredColumnsList.add(LAYER_NAME);
            requiredColumnsList.add(columnName);

            GeoPackageRWAgent gpkgRWAgent = new GeoPackageRWAgent(DbRelatedConstants.getPropertiesJsonForMetdataGpkg(context), reveloLogger, context);
            JSONObject datasetInfo = getEditMetadataDatasetInfo(context,reveloLogger);
            JSONObject respJObj = gpkgRWAgent.getDatasetContent(context, DbRelatedConstants.getDataSourceInfoForMetdataGpkg(context), datasetInfo, requiredColumnsList, whereClauseArray, "AND", true, - 1, false, false);

            if (respJObj.has("status") && respJObj.getString("status").equalsIgnoreCase("success")) {
                if (respJObj.has("features")) {
                    JSONObject responseFeatures = respJObj.getJSONObject("features");
                    if (responseFeatures.has("features")) {
                        JSONArray featuresJArray = responseFeatures.getJSONArray("features");
                        if (featuresJArray.length() > 0) {

                            for (int i = 0; i < featuresJArray.length(); i++) {
                                JSONObject recordJson = featuresJArray.getJSONObject(i);
                                JSONObject propertiesJobjReceived = recordJson.getJSONObject("properties");
                                String layerNameStr = null;
                                String w9id = null;
                                String featureLabel = null;
                                if (propertiesJobjReceived.has(FEATURE_LABEL)) {
                                    featureLabel = propertiesJobjReceived.getString(FEATURE_LABEL);
                                }
                                if (propertiesJobjReceived.has(LAYER_NAME)) {
                                    layerNameStr = propertiesJobjReceived.getString(LAYER_NAME);
                                }
                                if (propertiesJobjReceived.has(columnName)) {
                                    w9id = propertiesJobjReceived.getString(columnName);
                                }
                                if (layerNameStr != null && w9id != null) {
                                    if (featureIdMap.containsKey(layerNameStr)) {
                                        HashMap<Object, String> idList = featureIdMap.get(layerNameStr);
                                        if (idList == null) {
                                            idList = new HashMap<>();
                                        }
                                        if (! idList.containsKey(w9id)) {
                                            if (featureLabel == null || featureLabel.isEmpty()) {
                                                featureLabel = w9id;
                                            }
                                            idList.put(w9id, featureLabel);
                                        }

                                    }
                                    else {
                                        HashMap<Object, String> idList = new HashMap<>();
                                        if (featureLabel == null || featureLabel.isEmpty()) {
                                            featureLabel = w9id;
                                        }
                                        idList.put(w9id, featureLabel);
                                        featureIdMap.put(layerNameStr, idList);
                                    }
                                }
                            }

                        }
                    }
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
        return featureIdMap;
    }

    public static Map<String, HashMap<Object, String>> getEditedId(Context context,ReveloLogger reveloLogger) {

        Map<String, HashMap<Object, String>> featureIdMap = new LinkedHashMap<>();

        try {
            featureIdMap = getRecord(null, EDIT_COL, context, null,reveloLogger);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return featureIdMap;
    }

    public static Map<String, List<DeleteModel>> getDeletedId(Context context,ReveloLogger reveloLogger) {

        Map<String, List<DeleteModel>> featureIdMap = new LinkedHashMap<>();


        try {
            HashMap<String, JSONObject> conditionMap = new HashMap<>();


            JSONObject conditionJobjW9id = new JSONObject();
            conditionJobjW9id.put("columnType", "int");
            conditionJobjW9id.put("value", "\"\"");
            conditionJobjW9id.put("operator", "!=");
            conditionMap.put(DELETE_COL, conditionJobjW9id);

            JSONObject conditionJobjNotNull = new JSONObject();
            conditionJobjNotNull.put("columnType", "int");
            conditionJobjNotNull.put("value", "NULL");
            conditionJobjNotNull.put("operator", "IS NOT");
            conditionMap.put(DELETE_COL, conditionJobjNotNull);


            List<String> requiredColumnsList = new ArrayList<>();
            requiredColumnsList.add(FEATURE_LABEL);
            requiredColumnsList.add(LAYER_NAME);
            requiredColumnsList.add(DELETE_COL);
            requiredColumnsList.add(W9_META_DATA_COL);

            GeoPackageRWAgent gpkgRWAgent = new GeoPackageRWAgent(DbRelatedConstants.getPropertiesJsonForMetdataGpkg(context), reveloLogger, context);
            JSONObject datasetInfo = getEditMetadataDatasetInfo(context,reveloLogger);
            JSONObject respJObj = gpkgRWAgent.getDatasetContent(context, DbRelatedConstants.getDataSourceInfoForMetdataGpkg(context), datasetInfo, requiredColumnsList, conditionMap, "AND", true, - 1, false, false);

            if (respJObj.has("status") && respJObj.getString("status").equalsIgnoreCase("success")) {
                if (respJObj.has("features")) {
                    JSONObject responseFeatures = respJObj.getJSONObject("features");
                    if (responseFeatures.has("features")) {
                        JSONArray featuresJArray = responseFeatures.getJSONArray("features");
                        if (featuresJArray.length() > 0) {

                            for (int i = 0; i < featuresJArray.length(); i++) {
                                JSONObject recordJson = featuresJArray.getJSONObject(i);
                                JSONObject propertiesJobjReceived = recordJson.getJSONObject("properties");
                                String layerNameStr = null;
                                String w9id = null;
                                String featureLabel = null;
                                String w9Metadata = null;
                                if (propertiesJobjReceived.has(LAYER_NAME)) {
                                    layerNameStr = propertiesJobjReceived.getString(LAYER_NAME);
                                }
                                if (propertiesJobjReceived.has(DELETE_COL)) {
                                    w9id = propertiesJobjReceived.getString(DELETE_COL);
                                }
                                if (propertiesJobjReceived.has(W9_META_DATA_COL)) {
                                    w9Metadata = propertiesJobjReceived.getString(W9_META_DATA_COL);
                                }
                                if (propertiesJobjReceived.has(FEATURE_LABEL)) {
                                    featureLabel = propertiesJobjReceived.getString(FEATURE_LABEL);
                                }

                                if (layerNameStr != null && w9id != null && w9Metadata != null) {

                                    if (featureIdMap.containsKey(layerNameStr)) {
                                        List<DeleteModel> idList = featureIdMap.get(layerNameStr);
                                        if (idList == null) {
                                            idList = new ArrayList<>();
                                        }
                                        if (featureLabel == null || featureLabel.isEmpty()) {
                                            featureLabel = w9id;
                                        }
                                        DeleteModel deleteModel = new DeleteModel(w9id, w9Metadata, featureLabel);
                                        if (! idList.contains(deleteModel)) {
                                            idList.add(deleteModel);
                                        }
                                    }
                                    else {
                                        List<DeleteModel> idList = new ArrayList<>();
                                        if (featureLabel == null || featureLabel.isEmpty()) {
                                            featureLabel = w9id;
                                        }
                                        DeleteModel deleteModel = new DeleteModel(w9id, w9Metadata, featureLabel);
                                        idList.add(deleteModel);
                                        featureIdMap.put(layerNameStr, idList);
                                    }
                                }
                            }
                        }
                    }
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
        }

        return featureIdMap;
    }

    public static boolean deleteOperation(Context context, String operationName,ReveloLogger reveloLogger) {

        boolean isDeleted = false;
        try {
            JSONArray whereClauseArray = new JSONArray();
            JSONObject conditionJobjNotNull = new JSONObject();
            conditionJobjNotNull.put("conditionType", "attribute");
            if (operationName.equalsIgnoreCase(Constants.ADD)) {
                conditionJobjNotNull.put("columnName", ADD_COL);
            }
            else if (operationName.equalsIgnoreCase(Constants.EDIT)) {
                conditionJobjNotNull.put("columnName", EDIT_COL);
            }
            else if (operationName.equalsIgnoreCase(Constants.DELETE)) {
                conditionJobjNotNull.put("columnName", DELETE_COL);
            }
            conditionJobjNotNull.put("valueDataType", "int");
            conditionJobjNotNull.put("value", "NULL");
            conditionJobjNotNull.put("operator", "IS NOT");
            whereClauseArray.put(conditionJobjNotNull);


            JSONObject conditionJobjNotEmpty = new JSONObject();
            conditionJobjNotEmpty.put("conditionType", "attribute");
            if (operationName.equalsIgnoreCase(Constants.ADD)) {
                conditionJobjNotEmpty.put("columnName", ADD_COL);
            }
            else if (operationName.equalsIgnoreCase(Constants.EDIT)) {
                conditionJobjNotEmpty.put("columnName", EDIT_COL);
            }
            else if (operationName.equalsIgnoreCase(Constants.DELETE)) {
                conditionJobjNotEmpty.put("columnName", DELETE_COL);
            }
            conditionJobjNotEmpty.put("valueDataType", "int");
            conditionJobjNotEmpty.put("value", "\"\"");
            conditionJobjNotEmpty.put("operator", "IS NOT");
            whereClauseArray.put(conditionJobjNotNull);

            GeoPackageRWAgent gpkgRWAgent = new GeoPackageRWAgent(DbRelatedConstants.getPropertiesJsonForMetdataGpkg(context), reveloLogger, context);
            JSONObject datasetInfo = getEditMetadataDatasetInfo(context,reveloLogger);
            JSONObject respJObj = gpkgRWAgent.deleteDatasetContent(DbRelatedConstants.getDataSourceInfoForMetdataGpkg(context), datasetInfo, whereClauseArray, "AND", context);
            return respJObj.has("status") && respJObj.getString("status").equalsIgnoreCase("success");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    public static class DeleteModel {

        private final String w9MetaData;
        private final Object w9Id;
        private final String featureLabel;

        DeleteModel(Object id, String w9MetaData, String featureLabel) {
            this.w9Id = id;
            this.w9MetaData = w9MetaData;
            this.featureLabel = featureLabel;
        }

        public String getW9MetaData() {
            return w9MetaData;
        }

        public Object getW9Id() {
            return w9Id;
        }

        public String getFeatureLabel() {
            return featureLabel;
        }
    }

}