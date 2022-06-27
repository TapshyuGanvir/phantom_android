package com.sixsimplex.phantom.Phantom1.appfragment.map;

import com.sixsimplex.phantom.revelocore.graph.concepmodelgraph.CMGraph;
import com.sixsimplex.phantom.revelocore.layer.FeatureLayer;

import org.osmdroid.views.overlay.Overlay;

import java.util.LinkedHashMap;
import java.util.List;

public interface ImapFragmentView {
    void showFeaturesOnUI(LinkedHashMap<String, FeatureLayer> featureLayerLinkedHashMap, CMGraph cmGraph);

    void onGetSelectedFeatures(List<Overlay> features);
}
