package com.sixsimplex.phantom.Phantom1.appfragment.dashboard;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;
import com.sixsimplex.phantom.Phantom1.model.DeliveryDataModel;
import com.sixsimplex.phantom.R;
import com.sixsimplex.phantom.revelocore.data.Feature;

public class DeliveriesDetailBoard {
    @SuppressLint("StaticFieldLeak")
    public static int pendingCount = 0, completeCount = 0, inCompleteCount = 0, totalCount = 0;

    public static int getPendingCount() {
        return pendingCount;
    }

    public static void setPendingCount(int pendingCount) {
        DeliveriesDetailBoard.pendingCount = pendingCount;
    }

    public static int getCompleteCount() {
        return completeCount;
    }

    public static void setCompleteCount(int completeCount) {
        DeliveriesDetailBoard.completeCount = completeCount;
    }

    public static int getInCompleteCount() {
        return inCompleteCount;
    }

    public static void setInCompleteCount(int inCompleteCount) {
        DeliveriesDetailBoard.inCompleteCount = inCompleteCount;
    }

    public static int getTotalCount() {
        return totalCount;
    }

    public static void setTotalCount(int totalCount) {
        DeliveriesDetailBoard.totalCount = totalCount;
    }

    public static Handler mHandler = new Handler(Looper.getMainLooper());
    @SuppressLint("StaticFieldLeak")

    public static void updateDataSetChange(IDashBoardCallBack iDashBoardCallBack) {
        try {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    calCounts();
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if(iDashBoardCallBack != null){
                                iDashBoardCallBack.setCount();
                            }

                        }
                    });
                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void calCounts() {
        try {
            if(hasTraversalGraph()){
                if (DeliveryDataModel.getFeatureList() != null) {
                    if (!DeliveryDataModel.getFeatureList().isEmpty()) {
                        clearCount();
                        for (Feature feature : DeliveryDataModel.getFeatureList()) {
//                        boolean isVisited = (boolean) feature.getAttributes().get("isvisited");
                            boolean isSkipped = (boolean) feature.getAttributes().get("skipped");
                            boolean isDelivered = (boolean) feature.getAttributes().get("isdelivered");
                            if (!isSkipped && isDelivered) {
                                completeCount++;
                            }
                            if (isSkipped && !isDelivered) {
                                inCompleteCount++;
                            }
                            if (!isSkipped && !isDelivered) {
                                pendingCount++;
                            }
                        }
                        totalCount = DeliveryDataModel.getFeatureList().size();
                    }
                }
            }else{
                clearCount();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean hasTraversalGraph() {
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

    private static void clearCount() {
        pendingCount = 0;
        completeCount = 0;
        inCompleteCount = 0;
        totalCount = 0;
    }

}
