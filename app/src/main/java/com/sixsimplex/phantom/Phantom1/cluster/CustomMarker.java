package com.sixsimplex.phantom.Phantom1.cluster;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;


public class CustomMarker extends Marker {

    private String label="";
    public CustomMarker(MapView mapView) {
        super(mapView);
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }


}
