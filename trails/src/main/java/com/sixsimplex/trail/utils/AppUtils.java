package com.sixsimplex.trail.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AppUtils {
    /**
     * @return true If device has Android Marshmallow or above version
     */
    public static boolean hasM() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    public static boolean isServiceRunning(Context context, Class serviceClass) {
        if (context != null) {
            Log.d("", "contextIsNotNull: ");
        }
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (manager == null) {
            return false;
        }
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public static String getTrailId(String userName) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy_HH:mm:ss", Locale.getDefault());
        String uniqueTimeStamp = sdf.format(new Date());
        return userName + "_TR_" + uniqueTimeStamp;
    }
    public static String getStopId(String userName,String trailId,long timestamp) {
        if(trailId==null||trailId.isEmpty()){
            trailId = userName;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy_HH:mm:ss", Locale.getDefault());
        String uniqueTimeStamp = sdf.format(new Date(timestamp));
        return trailId + "_ST_" + uniqueTimeStamp;
    }
}
