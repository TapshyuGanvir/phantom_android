package com.sixsimplex.trail.utils.dbcalls;

import android.content.Context;

import com.sixsimplex.revelologger.ReveloLogger;
import com.sixsimplex.trail.AppPreferences;

import org.json.JSONObject;

public class DbRelatedConstants {
    public static JSONObject dataSourceInfoForMetdataGpkg = null;
    public static JSONObject propertiesJsonForMetdataGpkg = null;
    public static JSONObject editMetadataDatasetInfo = null;
    public static JSONObject dataSourceInfoForDataGpkg = null;
    public static JSONObject propertiesJsonForDataGpkg = null;
    public static JSONObject trailsDatasetInfo = null;
    public static JSONObject stopsDatasetInfo = null;
    public static JSONObject dataSourceInfoForREGpkg = null;
    public static JSONObject propertiesJsonForREGpkg = null;
    public static JSONObject w9obreDatasetInfo = null;
    public static String RE_GP_NAME = "w9obregdb.gpkg";
    public static String DATA_GP_NAME = null;
    public static String META_GP_NAME = null;


    public static JSONObject getPropertiesJsonForMetdataGpkg(Context context) {
        if (propertiesJsonForMetdataGpkg != null) {
            return propertiesJsonForMetdataGpkg;
        }
        try {
            AppPreferences appPreferences = new AppPreferences(context);
            String metadbpropertiesjson = appPreferences.getString("METADBPROPERTIESJSON", "");
            if (metadbpropertiesjson != null && ! metadbpropertiesjson.isEmpty()) {
                propertiesJsonForMetdataGpkg = new JSONObject(metadbpropertiesjson);

            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return propertiesJsonForMetdataGpkg;
    }

    public static JSONObject getPropertiesJsonForDataGpkg(Context context) {
        if (propertiesJsonForDataGpkg != null) {
            return propertiesJsonForDataGpkg;
        }
        try {
            AppPreferences appPreferences = new AppPreferences(context);
            String datadbpropertiesjson = appPreferences.getString("DATADBPROPERTIESJSON", "");
            if (datadbpropertiesjson != null && ! datadbpropertiesjson.isEmpty()) {
                propertiesJsonForDataGpkg = new JSONObject(datadbpropertiesjson);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return propertiesJsonForDataGpkg;
    }

    public static JSONObject getPropertiesJsonForREGpkg(Context context) {
        if (propertiesJsonForREGpkg != null) {
            return propertiesJsonForREGpkg;
        }
        try {
            AppPreferences appPreferences = new AppPreferences(context);
            String redbdbpropertiesjson = appPreferences.getString("REDBDBPROPERTIESJSON", "");
            if (redbdbpropertiesjson != null && ! redbdbpropertiesjson.isEmpty()) {
                propertiesJsonForREGpkg = new JSONObject(redbdbpropertiesjson);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return propertiesJsonForREGpkg;
    }

    public static JSONObject getDataSourceInfoForMetdataGpkg(Context context) {
        if (dataSourceInfoForMetdataGpkg != null) {
            return dataSourceInfoForMetdataGpkg;
        }
        try {
            AppPreferences appPreferences = new AppPreferences(context);
            String datasurc = appPreferences.getString("METADBDATASOURCJOBJ", "");
            if (datasurc != null && ! datasurc.isEmpty()) {
                dataSourceInfoForMetdataGpkg = new JSONObject(datasurc);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return dataSourceInfoForMetdataGpkg;
    }

    public static JSONObject getDataSourceInfoForDataGpkg(Context context) {
        if (dataSourceInfoForDataGpkg != null) {
            return dataSourceInfoForDataGpkg;
        }
        try {
            AppPreferences appPreferences = new AppPreferences(context);
            String datasurc = appPreferences.getString("DATADBDATASOURCJOBJ", "");
            if (datasurc != null && ! datasurc.isEmpty()) {
                dataSourceInfoForDataGpkg = new JSONObject(datasurc);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return dataSourceInfoForDataGpkg;
    }

    public static JSONObject getDataSourceInfoForREGpkg(Context context) {
        if (dataSourceInfoForREGpkg != null) {
            return dataSourceInfoForREGpkg;
        }
        try {
            AppPreferences appPreferences = new AppPreferences(context);
            String datasurc = appPreferences.getString("REDBDBDATASOURCJOBJ", "");
            if (datasurc != null && ! datasurc.isEmpty()) {
                dataSourceInfoForREGpkg = new JSONObject(datasurc);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return dataSourceInfoForREGpkg;
    }

    public static JSONObject getEditMetadataDatasetInfo(Context context, ReveloLogger reveloLogger) {
        if (editMetadataDatasetInfo != null) {
            return editMetadataDatasetInfo;
        }
        try {
            AppPreferences appPreferences = new AppPreferences(context);
            String editmetadatadatasetobj = appPreferences.getString("EDITMETADATADATASETOBJ", "");
            if (editmetadatadatasetobj != null && ! editmetadatadatasetobj.isEmpty()) {
                editMetadataDatasetInfo = new JSONObject(editmetadatadatasetobj);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return editMetadataDatasetInfo;
    }

    public static JSONObject getTrailsDatasetInfo(Context context) {
        if (trailsDatasetInfo != null) {
            return trailsDatasetInfo;
        }
        try {
            AppPreferences appPreferences = new AppPreferences(context);
            String traildatasetobj = appPreferences.getString("TRAILDATASETOBJ", "");
            if (traildatasetobj != null && ! traildatasetobj.isEmpty()) {
                trailsDatasetInfo = new JSONObject(traildatasetobj);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return trailsDatasetInfo;
    }

    public static JSONObject getStopsDatasetInfo(Context context) {
        if (stopsDatasetInfo != null) {
            return stopsDatasetInfo;
        }
        try {
            AppPreferences appPreferences = new AppPreferences(context);
            String stopsdatasetobj = appPreferences.getString("STOPSDATASETOBJ", "");
            if (stopsdatasetobj != null && ! stopsdatasetobj.isEmpty()) {
                stopsDatasetInfo = new JSONObject(stopsdatasetobj);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return stopsDatasetInfo;
    }

    public static JSONObject getw9obreDatasetInfo(Context context) {
        if (w9obreDatasetInfo != null) {
            return w9obreDatasetInfo;
        }
        try {
            AppPreferences appPreferences = new AppPreferences(context);
            String w9obreDatasetObj = appPreferences.getString("w9OBREDATASETOBJ", "");
            if (w9obreDatasetObj != null && ! w9obreDatasetObj.isEmpty()) {
                w9obreDatasetInfo = new JSONObject(w9obreDatasetObj);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return w9obreDatasetInfo;
    }

    public static String getDataDbName(Context context) {
        if (DATA_GP_NAME == null || DATA_GP_NAME.isEmpty()) {
            try {
                AppPreferences appPreferences = new AppPreferences(context);
                String data_gp_name = appPreferences.getString("DATA_GP_NAME", "");
                if (data_gp_name != null && ! data_gp_name.isEmpty()) {
                    DATA_GP_NAME = data_gp_name;
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        return DATA_GP_NAME;
    }

    public static String getMetatdataDbName(Context context) {
        if (META_GP_NAME == null || META_GP_NAME.isEmpty()) {
            try {
                AppPreferences appPreferences = new AppPreferences(context);
                String meta_gp_name = appPreferences.getString("META_GP_NAME", "");
                if (meta_gp_name != null && ! meta_gp_name.isEmpty()) {
                    META_GP_NAME = meta_gp_name;
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        return META_GP_NAME;
    }

    public static void clearAllConstants() {
       dataSourceInfoForMetdataGpkg = null;
       propertiesJsonForMetdataGpkg = null;
       editMetadataDatasetInfo = null;
       dataSourceInfoForDataGpkg = null;
       propertiesJsonForDataGpkg = null;
       trailsDatasetInfo = null;
       stopsDatasetInfo = null;
       dataSourceInfoForREGpkg = null;
       propertiesJsonForREGpkg = null;
       w9obreDatasetInfo = null;
       RE_GP_NAME = "w9obregdb.gpkg";
       DATA_GP_NAME =null;
       META_GP_NAME =null;
    }
}
