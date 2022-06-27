package com.sixsimplex.phantom.Phantom1.trip;

public interface ITripCallback {
    void onSuccessTripAddUpdate();
    void onFailureTripAddUpdate(String message);
}
