package com.sixsimplex.trail.gepoackage;


import android.content.Context;

import com.sixsimplex.trail.utils.dbcalls.DbRelatedConstants;

import org.json.JSONObject;

import java.io.File;
import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.GeoPackageFactory;
import mil.nga.geopackage.GeoPackageManager;

public class GeoPackageManagerAgent {

    private static final String RE_GP_DATABASE_NAME = "w9obregdb";
    private static final String className = "GeoPackageManagerAgent";
    private static GeoPackageManager manager = null;
    private static GeoPackage reGeoPackage;
    private static GeoPackage dataGeoPackage;
    private static GeoPackage metaGeoPackage;
    private static String META_DATABASE_NAME = "metadata";
    private static String DATA_DATABASE_NAME = "datagp";
    private static String   metaGeoPackageFolderPath = null;
    private static String   reGeoPackageFolderPath = null;
    private static String  dataGeoPackageFolderPath = null;

    public static GeoPackage getGeoPackage(Context context, String geopackageName) {

        if (geopackageName.equalsIgnoreCase(DbRelatedConstants.getDataDbName(context))) {
           // return dataGeoPackage;
            return getDataGeoPackage(context, DbRelatedConstants.getPropertiesJsonForDataGpkg(context));
        }
        else if (geopackageName.equalsIgnoreCase(DbRelatedConstants.getMetatdataDbName(context))) {
           // return metaGeoPackage;
            return getMetaGeoPackage(context, DbRelatedConstants.getPropertiesJsonForMetdataGpkg(context));
        }
        else if (geopackageName.equalsIgnoreCase(DbRelatedConstants.RE_GP_NAME)) {
           // return reGeoPackage;
            return getReGeoPackage(context, DbRelatedConstants.getPropertiesJsonForREGpkg(context));
        }

        return null;
    }

    public static void setDataGeoPackage(GeoPackage dataGeoPackage) {
        GeoPackageManagerAgent.dataGeoPackage = dataGeoPackage;
    }

    public static void setMetaGeoPackage(GeoPackage metaGeoPackage) {
        GeoPackageManagerAgent.metaGeoPackage = metaGeoPackage;
    }

    public static void setReGeoPackage(GeoPackage reGeoPackage) {
        GeoPackageManagerAgent.reGeoPackage = reGeoPackage;
    }

    public static GeoPackage getReGeoPackage(Context context, JSONObject propertiesJson) {

        if (reGeoPackageFolderPath == null) {

            if (propertiesJson != null) {
                try {
                    String dbPath = propertiesJson.getString("dbPath");
                    File ReGpkgFile = new File(dbPath);
                    reGeoPackageFolderPath  = ReGpkgFile.getParentFile().getAbsolutePath();
                } catch (Exception e) {
                    e.printStackTrace();
                    //ReveloLogger.error(className, "getReGeoPackage", String.valueOf(e.getCause()));
                }
            }
        }

        return reGeoPackage;
    }

    public static GeoPackage getMetaGeoPackage(Context context, JSONObject propertiesJson) {
        //ReveloLogger.debug("GeoPackageManagerAgent", "getMetaGeoPackage", "Fetching metageopackage..");
        if (metaGeoPackageFolderPath == null) {

            if (propertiesJson != null) {
                //ReveloLogger.debug("GeoPackageManagerAgent", "initMetaDbName", "metaGeoPackageFile found. Importing and opening..");
                try {
                    String dbPath = propertiesJson.getString("dbPath");
                    File metadataGpkgFile = new File(dbPath);
                     metaGeoPackageFolderPath  = metadataGpkgFile.getParentFile().getAbsolutePath();
                   } catch (Exception e) {
                    e.printStackTrace();
                    //ReveloLogger.error("GeoPackageManagerAgent", "initMetaDbName", "metaGeoPackageFile found. Importing and opening..failure - " + e.getCause());
                }
            }
            else {
                //ReveloLogger.debug("GeoPackageManagerAgent", "initMetaDbName", "metaGeoPackageFile not found. Geopackage not initalized..");
            }
        }

        return metaGeoPackage;
    }

    public static GeoPackage getDataGeoPackage(Context context, JSONObject propertiesJson) {

        if (dataGeoPackageFolderPath == null) {
            if (propertiesJson != null) {
                try {
                    String dbPath = propertiesJson.getString("dbPath");
                    File dataGpkgFile = new File(dbPath);
                     dataGeoPackageFolderPath  = dataGpkgFile.getParentFile().getAbsolutePath();
                } catch (Exception e) {
                    e.printStackTrace();
                    //ReveloLogger.error(className, "getDataGeoPackage", String.valueOf(e.getCause()));
                }
            }
        }

        return dataGeoPackage;
    }

    private static GeoPackageManager provideGeoPackageManager(Context context) {
        manager = GeoPackageFactory.getManager(context);
        return manager;
    }

    public static void exportDataGeopackage(Context context) {

       /* try {
            initDataDbName();
            File dataGeoPackageFile = new File(AppFolderStructure.getDataBaseFolderPath(context));
            if (manager != null) {
                manager.exportGeoPackage(DATA_DATABASE_NAME, dataGeoPackageFile);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }*/
    }

    public static void initDataDbName(Context context) {
        DATA_DATABASE_NAME = DbRelatedConstants.getDataDbName(context);
        if (DATA_DATABASE_NAME == null || DATA_DATABASE_NAME.isEmpty()) {
            DATA_DATABASE_NAME = "datagp";
        }
        else {
            DATA_DATABASE_NAME = DATA_DATABASE_NAME.split("\\.")[0];
        }
    }

    public static void exportMetaDataGeopackage(Context context) {

       /* try {
            initMetaDbName();
            File dataGeoPackageFile = new File(AppFolderStructure.getDataBaseFolderPath(context));
            if (manager != null) {
                manager.exportGeoPackage(META_DATABASE_NAME, dataGeoPackageFile);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }*/
    }

    public static void initMetaDbName(Context context) {
        //ReveloLogger.debug("GeoPackageManagerAgent", "initMetaDbName", "meta db name initialized(extension separated from name)");
        META_DATABASE_NAME = DbRelatedConstants.getMetatdataDbName(context);
        if (META_DATABASE_NAME == null || META_DATABASE_NAME.isEmpty()) {
            META_DATABASE_NAME = "metadata";
        }
        else {
            META_DATABASE_NAME = META_DATABASE_NAME.split("\\.")[0];
            //ReveloLogger.debug("GeoPackageManagerAgent", "initMetaDbName", "meta db name = " + META_DATABASE_NAME);
        }
    }

    public static void clearAllGeopackage() {
        clearMetaGeoPackage();
        clearDataGeoPackage();
        clearRedbGeopackage();
        if (manager != null) {
            manager = null;
        }
    }

    public static void clearMetaGeoPackage() {
        if (metaGeoPackage != null) {
            metaGeoPackage = null;
        }
        if (metaGeoPackageFolderPath != null) {
            metaGeoPackageFolderPath = null;
        }
    }

    public static void clearDataGeoPackage() {
        if (dataGeoPackage != null) {
            dataGeoPackage = null;
        }
        if (dataGeoPackageFolderPath != null) {
            dataGeoPackageFolderPath = null;
        }
    }

    public static void clearRedbGeopackage() {
        if (reGeoPackage != null) {
            reGeoPackage = null;
        }
    }

    public static void exportGeopackage(Context context, String datasourceName) {
        if (datasourceName.equalsIgnoreCase(DbRelatedConstants.getDataDbName(context)) && dataGeoPackageFolderPath!=null && !dataGeoPackageFolderPath.isEmpty()) {
            try {
                File dataGeoPackageFile = new File(dataGeoPackageFolderPath);
                if(manager==null){
                    manager = provideGeoPackageManager(context);
                }
                if (manager != null) {
                    manager.exportGeoPackage(DATA_DATABASE_NAME, dataGeoPackageFile);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else if (datasourceName.equalsIgnoreCase(DbRelatedConstants.getMetatdataDbName(context))&& metaGeoPackageFolderPath!=null && !metaGeoPackageFolderPath.isEmpty()) {
            try {
                File dataGeoPackageFile = new File(metaGeoPackageFolderPath);
                if(manager==null){
                    manager = provideGeoPackageManager(context);
                }
                if (manager != null) {
                    manager.exportGeoPackage(META_DATABASE_NAME, dataGeoPackageFile);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static JSONObject isGeopackageValid(Context context, String geopackageName) {
        JSONObject validationJson = new JSONObject();
/*
        try {
            String failureMessage = "Could not validate geopackage. Reason - ";
            validationJson.put("status", "failure");
            validationJson.put("message", failureMessage + " unknown");
            if (context == null || geopackageName == null || geopackageName.isEmpty()) {
                if (context == null) {
                    validationJson.put("message", failureMessage + " No context received");
                }
                else {
                    validationJson.put("message", failureMessage + " Invalid Geopackage file name.");
                }
                return validationJson;
            }

            if (geopackageName.equalsIgnoreCase(UserInfoPreferenceUtility.getDataDbName())) {
                return validateDataGeopackage(context);
            }
            else if (geopackageName.equalsIgnoreCase(UserInfoPreferenceUtility.getMetatdataDbName())) {
                return validateMetaGeopackage(context);
            }
            else if (geopackageName.equalsIgnoreCase(DbRelatedConstants.RE_GP_NAME)) {
                return validateReGeopackage(context);
            }
            else {
                validationJson.put("message", failureMessage + " Invalid Geopackage file name.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }*/
        return validationJson;
    }

    private static JSONObject validateReGeopackage(Context context) {
        JSONObject validationJson = new JSONObject();
        /*try {
            String failureMessage = "Could not validate geopackage. Reason - ";
            validationJson.put("status", "failure");
            validationJson.put("message", failureMessage + " unknown");

            GeoPackageRWAgent gpkgRWAgent = new GeoPackageRWAgent(DbRelatedConstants.getPropertiesJsonForREGpkg(context),new ReveloLogger(),context);
            JSONObject respJObj = gpkgRWAgent.getDatasetColumns(context,DbRelatedConstants.getDataSourceInfoForREGpkg(context),DbRelatedConstants.getDataSetInfoForTable("w9obre"));
            if (respJObj.has("status") && respJObj.getString("status").equalsIgnoreCase("success")) {
                HashMap<String,String> jurisdictionIdMap = OrgBoundaryConceptModel.createJurisdictionNamesIdMap();
                List<String> columnNames = new ArrayList<>();
                if (respJObj.has("features")) {
                    JSONObject responseFeatures = respJObj.getJSONObject("features");
                    if (responseFeatures.has("features")) {
                        JSONArray featuresJArray = responseFeatures.getJSONArray("features");
                        for (int i = 0; i < featuresJArray.length(); i++) {
                            columnNames.add(featuresJArray.getString(i));
                        }
                    }
                }
                boolean isTableValid = true;
                if(jurisdictionIdMap!=null && jurisdictionIdMap.size()>0 && columnNames.size()>0){
                    for(String key: jurisdictionIdMap.keySet()){
                     if(!columnNames.contains(key)){
                         isTableValid=false;
                         validationJson.put("status","failure");
                         validationJson.put("message","REdb table columns are not in proper state. "+key+" is missing.");
                         break;
                     }
                    }
                }else if(columnNames.size()>0){
                    isTableValid=true;
                }else {
                    isTableValid=false;
                    validationJson.put("status","failure");
                    validationJson.put("message","REdb table columns are not in proper state");
                }
                if(isTableValid){
                    validationJson.put("status","success");
                    validationJson.put("message","REdb is in proper state");
                }
            }else {
                validationJson.put("status","failure");
                validationJson.put("message","REdb table is not in proper state");
            }


        } catch (Exception e) {
            e.printStackTrace();
        }*/
        return validationJson;
    }

    private static JSONObject validateDataGeopackage(Context context) {
        JSONObject validationJson = new JSONObject();
        try {
            String failureMessage = "Could not validate geopackage. Reason - ";
            validationJson.put("status", "failure");
            validationJson.put("message", failureMessage + " unknown");

            validationJson.put("status","success");
            validationJson.put("message","Data db is in proper state");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return validationJson;
    }

    private static JSONObject validateMetaGeopackage(Context context) {
        String taskName = "validateMetaGeopackage";
        JSONObject validationJson = new JSONObject();
      /*  try {
            String failureMessage = "Could not validate geopackage. Reason - ";
            validationJson.put("status", "failure");
            validationJson.put("message", failureMessage + " unknown");

            boolean entitiesTableOk = false;
            boolean relationsTableOk = false;
            boolean domainsTableOk = false;
            boolean jurisdictionsTableOk = false;
            boolean editMetadataTableOk = false;
            boolean logsTableOk = false;

            GeoPackage metaGeoPackage = GeoPackageManagerAgent.getMetaGeoPackage(context, DbRelatedConstants.getPropertiesJsonForMetdataGpkg(context));
            if (metaGeoPackage != null) {
                //check entities table
                try {
                    //ReveloLogger.debug(className, taskName, "Fetching data access object from metageopackage..");
                    //ReveloLogger.debug(className, taskName, " Geopackage initalized..getting attributes type data access object for non spatial table entities");
                    AttributesDao entitiesDao = metaGeoPackage.getAttributesDao(ENTITIES_TABLE_NAME);
                    if (entitiesDao != null) {
                        entitiesTableOk = true;
                        entitiesDao = null;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    //ReveloLogger.debug(className, taskName, "Exception fetching entities table access object from meta geopackage.." + e.getMessage());
                }

                //checkRelationshipTable
                try {
                    //ReveloLogger.debug(className, taskName, "getting RelationsDao");
                    //ReveloLogger.debug(className, taskName, " Geopackage initalized..getting attributes type data access object for non spatial table relations");
                    AttributesDao relationsDao = metaGeoPackage.getAttributesDao(RELATIONSHIPS_TABLE_NAME);
                    if (relationsDao != null) {
                        relationsTableOk = true;
                        relationsDao = null;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    //ReveloLogger.debug(className, taskName, "Exception fetching relations table access object from meta geopackage.." + e.getMessage());
                }

                //check domains table
                try {
                    //ReveloLogger.debug(className, taskName, "getting DomainsDAO");
                    //ReveloLogger.debug(className, taskName, " Geopackage initialized..getting attributes type data access object for non spatial table domains");
                    AttributesDao domainsDAO = metaGeoPackage.getAttributesDao(DOMAINS_TABLE_NAME);
                    if (domainsDAO != null) {
                        domainsTableOk = true;
                        domainsDAO = null;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    //ReveloLogger.debug(className, taskName, "Exception fetching domains table access object from meta geopackage.." + e.getMessage());
                }

                //check editMetadataTableOk table
                try {
                    //ReveloLogger.debug(className, taskName, "getting editMetadataTableDAO");
                    //ReveloLogger.debug(className, taskName, " Geopackage initialized..getting attributes type data access object for non spatial table editMetadata");
                    AttributesDao editMetadataTableDAO = metaGeoPackage.getAttributesDao(EDITMETADATA_TABLE_NAME);
                    if (editMetadataTableDAO != null) {
                        editMetadataTableOk = true;
                        editMetadataTableDAO = null;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    //ReveloLogger.debug(className, taskName, "Exception fetching editmetadata table access object from meta geopackage.." + e.getMessage());
                }

                //check logs table
                try {
                    //ReveloLogger.debug(className, taskName, "getting logs DAO");
                    //ReveloLogger.debug(className, taskName, " Geopackage initialized..getting attributes type data access object for non spatial table logs");
                    AttributesDao logsDAO = metaGeoPackage.getAttributesDao(LOGS_TABLE_NAME);
                    if (logsDAO != null) {
                        logsTableOk = true;
                        logsDAO = null;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    //ReveloLogger.debug(className, taskName, "Exception fetching logs table access object from meta geopackage.." + e.getMessage());
                }


                //check jurisdictions table
                try {
                    //ReveloLogger.debug(className, taskName, "getting jurisdictionsDAO");
                    //ReveloLogger.debug(className, taskName, " Geopackage initialized..getting feature type data access object for  spatial table jurisdictions");
                    FeatureDao jurisdictionsDao = metaGeoPackage.getFeatureDao(JURISDICTIONS_TABLE_NAME);
                    if (jurisdictionsDao != null) {
                        jurisdictionsTableOk = true;
                        jurisdictionsDao = null;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    //ReveloLogger.debug(className, taskName, "Exception fetching jurisdictions data access object from meta geopackage.." + e.getMessage());
                }

            }
            else {
                //ReveloLogger.error(className, taskName, " Geopackage not initalized..failure getting attributes type data access object for non spatial table entities");
            }


            if(entitiesTableOk && relationsTableOk && domainsTableOk
                    && jurisdictionsTableOk && editMetadataTableOk && logsTableOk){
                validationJson.put("status","success");
                validationJson.put("message","Metadata db is in proper state");
            }else {
                String invalidMessage = "Could not find table - ";
                if(!entitiesTableOk){invalidMessage=invalidMessage+" 'entities' ";}
                if(!relationsTableOk){invalidMessage=invalidMessage+" 'relationships' ";}
                if(!domainsTableOk){invalidMessage=invalidMessage+" 'domains' ";}
                if(!jurisdictionsTableOk){invalidMessage=invalidMessage+" 'jurisdictions' ";}
                if(!editMetadataTableOk){invalidMessage=invalidMessage+" 'editmetadata' ";}
                if(!logsTableOk){invalidMessage=invalidMessage+" 'logs' ";}
                validationJson.put("status","failure");
                validationJson.put("message",invalidMessage);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }*/

        return validationJson;
    }
}