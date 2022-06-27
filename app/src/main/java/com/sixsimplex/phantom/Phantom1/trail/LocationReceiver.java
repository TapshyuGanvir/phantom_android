package com.sixsimplex.phantom.Phantom1.trail;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


/**
 * @author josevieira
 */
public abstract class LocationReceiver extends BroadcastReceiver {

    @Override
    public abstract void onReceive(Context context, Intent intent) ;

}