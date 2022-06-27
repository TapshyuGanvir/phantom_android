package com.sixsimplex.trail;

import org.json.JSONObject;

public class Constants {
    public static String TRAIL_TABLE_NAME = "trails",
            TRAIL_OLD_TABLE_TABLENAME = "usertrail",
            TRAIL_TABLE_TRAILID = "trailid",
            TRAIL_TABLE_STARTTIMESTAMP = "starttimestamp",
            TRAIL_TABLE_ENDTIMESTAMP = "endtimestamp",
            TRAIL_TABLE_ISNEW = "isnew",
            TRAIL_TABLE_LABEL = "Trail",
            TRAIL_TABLE_TRANSPORT_MODE = "transportmode",
            TRAIL_TABLE_USERNAME = "username",
            TRAIL_TABLE_W9_METADATA = "w9metadata",
            TRAIL_TABLE_W9_ENTITY_CLASS_NAME = "w9entityclassname",
            TRAIL_TABLE_DISTANCE = "distance",
            TRAIL_TABLE_JURISDICTION_INFO = "jurisdictioninfo",
            TRAIL_TABLE_DESCRIPTION = "description";


    public static String STOP_TABLE_NAME =       "trailstops_delivery" ,
            STOP_TABLE_STOPID                    = "trailstopid" ,
            STOP_TABLE_TRAILID                   = "trailid" ,
            STOP_TABLE_COMMENT                       = "comment" ,
            STOP_TABLE_W9_ENTITY_CLASS_NAME              = "w9entityclassname" ,
            STOP_TABLE_STARTTIMESTAMP             ="starttimestamp",
            STOP_TABLE_ENDTIMESTAMP                  = "endtimestamp" ,
            STOP_TABLE_USERNAME                  ="username" ,
            STOP_TABLE_W9_METADATA                = "w9metadata" ,
            STOP_TABLE_GEOMETRY                       = "the_geom";



    /*
    * {
  "type": "FeatureCollection",
  "features": [
    {
      "type": "Feature",
      "properties": {},
      "geometry": {
        "type": "Point",
        "coordinates": [
          123.74999999999999,
          63.074865690586634
        ]
      }
    }
  ]
}*/
    /*JSONObject stopPropertiesJson = new JSONObject();
    {
        stopPropertiesJson.put(STOP_TABLE_STOPID              , "");
        stopPropertiesJson.put(STOP_TABLE_TRAILID             , "");
        stopPropertiesJson.put(STOP_TABLE_COMMENT             , "");
        stopPropertiesJson.put(STOP_TABLE_W9_ENTITY_CLASS_NAME, "");
        stopPropertiesJson.put(STOP_TABLE_STARTTIMESTAMP      , "");
        stopPropertiesJson.put(STOP_TABLE_ENDTIMESTAMP        , "");
        stopPropertiesJson.put(STOP_TABLE_USERNAME            , "");
        stopPropertiesJson.put(STOP_TABLE_W9_METADATA         , "");
    }*/

    public static final String EDIT = "edit";
    public static final String SHADOW = "shadow";
    public static final String ADD = "add";
    public static final String DELETE = "delete";

    public static final int PERMISSION_ACCESS_LOCATION_CODE = 99;

    public static final String LOCATION_MESSAGE = "LOCATION_DATA";

    public static final String ACTION_CURRENT_LOCATION_BROADCAST = "current.location";

    public static final String ACTION_PERMISSION_DEINED = "location.deined";
    public static final String W9_METADATA = "w9metadata";
    public static final String W9_ENTITY_CLASS_NAME = "w9entityclassname";

    public static final String W9_LATITUDE = "w9latitude";
    public static final String W9_LONGITUDE = "w9longitude";
    public static final String W9_ACCURACY = "w9accuracy";

    public static final String W9_UPDATE_DATE = "w9updatedate";
    public static final String W9_UPDATE_BY = "w9updatedby";
}
