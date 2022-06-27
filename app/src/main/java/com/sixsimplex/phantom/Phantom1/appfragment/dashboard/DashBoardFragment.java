package com.sixsimplex.phantom.Phantom1.appfragment.dashboard;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.sixsimplex.phantom.Phantom1.app.view.DeliveryMainActivity;
import com.sixsimplex.phantom.Phantom1.appfragment.delproduct.Count;
import com.sixsimplex.phantom.Phantom1.model.DeliveryDataModel;
import com.sixsimplex.phantom.Phantom1.products.ProductSpinnerAdapter;
import com.sixsimplex.phantom.Phantom1.products.Products;
import com.sixsimplex.phantom.Phantom1.trip.ITripCallback;
import com.sixsimplex.phantom.Phantom1.trip.TripDataModel;
import com.sixsimplex.phantom.R;
import com.sixsimplex.phantom.revelocore.data.Feature;

import java.util.ArrayList;
import java.util.List;

public class DashBoardFragment extends Fragment implements IDashBoardCallBack {

    Context context;
    DashBoardFragmentPresenter dashBoardFragmentPresenter;
    TextView editBtn;
    private TextView pendingCountView, completeCountView, inCompleteCountView, totalCountView;
    private LinearLayout itemsContainerLayout;
    TextView noDataText;


    public DashBoardFragment(Context context,
                             DashBoardFragmentPresenter dashBoardFragmentPresenter) {
        this.context = context;
        this.dashBoardFragmentPresenter = dashBoardFragmentPresenter;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_dash_board, container, false);
        pendingCountView = view.findViewById(R.id.pending_cv);
        completeCountView = view.findViewById(R.id.complete_cv);
        inCompleteCountView = view.findViewById(R.id.incomplete_cv);
        totalCountView = view.findViewById(R.id.total_cv);

        itemsContainerLayout = view.findViewById(R.id.container_layout);
        editBtn = view.findViewById(R.id.edit_btn_board2);
        noDataText = view.findViewById(R.id.nodatatext);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setCount();
        setTripItems();

    }

    private void setTripItems() {
        try {

            final Observer<List<Feature>> nameObserver = new Observer<List<Feature>>() {
                @Override
                public void onChanged(@Nullable final List<Feature> tripItems) {
                    showTripItemsOnUI(tripItems);
                }
            };
            TripDataModel.getInstance().getTripItemsLive().observe(requireActivity(), nameObserver);

            editBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(!DeliveryDataModel.getFeatureList().isEmpty()){
                        if(TripDataModel.getInstance().getTodayTripAdded()){
                            ((DeliveryMainActivity) requireActivity()).showTripItemSelectionDialog(new ITripCallback() {
                                @Override
                                public void onSuccessTripAddUpdate() {

                                }

                                @Override
                                public void onFailureTripAddUpdate(String message) {

                                }
                            });
                        }else{
                            Toast.makeText(context, "can not edit the inventory before start", Toast.LENGTH_SHORT).show();
                        }
                    }else {
                        Toast.makeText(context, "You have no assignments.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showTripItemsOnUI(List<Feature> tripItems) {
        try {
            if(dashBoardFragmentPresenter.hasTraversalGraph()){
                if (tripItems != null && !tripItems.isEmpty()) {
                    itemsContainerLayout.removeAllViews();
                    itemsContainerLayout.setVisibility(View.VISIBLE);
                    noDataText.setVisibility(View.GONE);
                    List<Feature> productList = Products.getProducts(context);
                    for (Feature feature : tripItems) {
                        Count count = new Count(Integer.parseInt(String.valueOf(feature.getAttributes().get("quantity"))));

                        @SuppressLint("InflateParams") View productRowView = getLayoutInflater().inflate(R.layout.trip_item_dashboard_row, null, false);
                        TextView productItemsName = productRowView.findViewById(R.id.productname_trip_dashboard);
                        TextView productCount = productRowView.findViewById(R.id.product_count_trip_dashboard);


                        for (Feature product : productList) {
                            if (feature.getAttributes().get("productid").equals(product.getFeatureId())) {
                                String val = product.getAttributes().get("name") + " " +"of"+" " + (Double) product.getAttributes().get("containercapacity") + " " + product.getAttributes().get("containercapacityunit");
                                productItemsName.setText(val);
                                break;
                            }
                        }

                        productCount.setText(String.valueOf(count.getNumber()));
                        productRowView.setTag(String.valueOf(feature.getFeatureId()));
                        itemsContainerLayout.addView(productRowView);
                    }
                }else{
                    itemsContainerLayout.removeAllViews();
                    noDataText.setVisibility(View.VISIBLE);
                    itemsContainerLayout.setVisibility(View.GONE);
                }
            }else{
                itemsContainerLayout.removeAllViews();
                noDataText.setVisibility(View.VISIBLE);
                itemsContainerLayout.setVisibility(View.GONE);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    @SuppressLint("SetTextI18n")
    public void setCount() {
        try {
            if (pendingCountView != null &&
                    completeCountView != null &&
                    inCompleteCountView != null &&
                    totalCountView != null) {
                if (DeliveriesDetailBoard.getPendingCount() < 10) {
                    pendingCountView.setText("0" + String.valueOf(DeliveriesDetailBoard.getPendingCount()));
                } else {
                    pendingCountView.setText(String.valueOf(DeliveriesDetailBoard.getPendingCount()));
                }

                if (DeliveriesDetailBoard.getCompleteCount() < 10) {
                    completeCountView.setText("0" + String.valueOf(DeliveriesDetailBoard.getCompleteCount()));
                } else {
                    completeCountView.setText(String.valueOf(DeliveriesDetailBoard.getCompleteCount()));
                }

                if (DeliveriesDetailBoard.getInCompleteCount() < 10) {
                    inCompleteCountView.setText("0" + String.valueOf(DeliveriesDetailBoard.getInCompleteCount()));
                } else {
                    inCompleteCountView.setText(String.valueOf(DeliveriesDetailBoard.getInCompleteCount()));
                }

                if (DeliveriesDetailBoard.getInCompleteCount() < 10) {
                    totalCountView.setText("0" + String.valueOf(DeliveriesDetailBoard.getTotalCount()));
                } else {
                    totalCountView.setText(String.valueOf(DeliveriesDetailBoard.getTotalCount()));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateDashBoard() {
        DeliveriesDetailBoard.updateDataSetChange(DashBoardFragment.this);
    }

}