package com.sixsimplex.phantom.revelocore.util.locationModule;

import android.location.Location;
import android.os.Bundle;

public interface LocationReceiverInterface {
    void onLocationChange(Location location);
    void onProviderDisable(String provider);
    void onProviderEnable(String provider);
    void onStatusChanged(String provider, int status, Bundle extras);
}
