package com.sixsimplex.phantom.Phantom1.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class ConnectivityHelper {
    public static boolean isConnectedToInternet(Context context){
        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null){
            NetworkInfo info = connectivityManager.getActiveNetworkInfo();
            if (info != null){
                if (info.getType() == ConnectivityManager.TYPE_WIFI){
                    return true;
                }else if (info.getType() == ConnectivityManager.TYPE_MOBILE){
                    return true;
                }
            }
        }
        return false;
    }
}
