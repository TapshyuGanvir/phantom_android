package com.sixsimplex.phantom.Phantom1.trip;

import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.sixsimplex.phantom.revelocore.data.Feature;

import java.util.ArrayList;
import java.util.List;

public class TripDataModel extends ViewModel {

    public static Handler mHandler = new Handler(Looper.getMainLooper());
    private static TripDataModel instance = instance = new TripDataModel();
    private Boolean isTodayTripAdded = null;
    private Boolean isInventoryItemAddedForCurrentTrip = null;
    private Feature todayTripFeature = null;
    private List<Feature> tripItems = new ArrayList<>();
    private MutableLiveData<List<Feature>> tripItemsLive = new MutableLiveData<>();

    public static TripDataModel getInstance() {
        return instance;
    }

    public MutableLiveData<List<Feature>> getTripItemsLive() {
        return tripItemsLive;
    }

    public List<Feature> getTripItems() {
        return tripItems;
    }

    public void setTripItems(List<Feature> tripItems) {
        this.tripItems = tripItems;
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                tripItemsLive.setValue(tripItems);
            }
        });
    }

    public Boolean getTodayTripAdded() {
        return isTodayTripAdded;
    }

    public void setTodayTripAdded(Boolean todayTripAdded) {
        isTodayTripAdded = todayTripAdded;
    }

    public Boolean getInventoryItemAddedForCurrentTrip() {
        return isInventoryItemAddedForCurrentTrip;
    }

    public void setInventoryItemAddedForCurrentTrip(Boolean inventoryItemAddedForCurrentTrip) {
        isInventoryItemAddedForCurrentTrip = inventoryItemAddedForCurrentTrip;
    }

    public Feature getTodayTripFeature() {
        return todayTripFeature;
    }

    public void setTodayTripFeature(Feature todayTripFeature) {
        this.todayTripFeature = todayTripFeature;
    }

    public void clearAll() {
        tripItems.clear();
        isTodayTripAdded = null;
        isInventoryItemAddedForCurrentTrip = null;
        todayTripFeature = null;
        tripItemsLive.setValue(tripItems);

    }
}
