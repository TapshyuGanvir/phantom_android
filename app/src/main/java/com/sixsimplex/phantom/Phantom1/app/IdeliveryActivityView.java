package com.sixsimplex.phantom.Phantom1.app;

import com.sixsimplex.phantom.Phantom1.trip.ITripCallback;
import com.sixsimplex.phantom.revelocore.data.Feature;

public interface IdeliveryActivityView {
    void onTraversalDataFetchComplete();
    void performDeliveryActionForFeature(Feature targetFeature,String mode);
    void onTargetFeatureUpdated(String mode, int position);
    void updateHomeAndMapUI(int position);
    void showTripItemSelectionDialog(ITripCallback iTripCallback);
    void hideProgressDialog();
    void showProgressDialog(String progressText);

    void showProductsUpdateDialogForTargetFeature(Feature targetFeature);

    void drawTrail(String geometryGeoJsonStr);
    void drawStop(String geometryGeoJsonStr);

    void showError(String message);
}
