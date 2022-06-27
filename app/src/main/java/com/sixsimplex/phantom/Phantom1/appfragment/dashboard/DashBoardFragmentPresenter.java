package com.sixsimplex.phantom.Phantom1.appfragment.dashboard;

import com.sixsimplex.phantom.Phantom1.model.DeliveryDataModel;

public class DashBoardFragmentPresenter {
    public  boolean hasTraversalGraph() {
        boolean has = false;
        try {
            if (DeliveryDataModel.getInstance().getTraversalEntity() != null) {
                if (DeliveryDataModel.getInstance().getTraversalEntity().getTraversalGraph() != null) {
                    has=true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return has;
    }
}
