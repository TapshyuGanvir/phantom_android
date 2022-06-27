package com.sixsimplex.phantom.revelocore.phaseDetails.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.sixsimplex.phantom.R;
import com.sixsimplex.phantom.revelocore.phaseDetails.ISelectPhase;
import com.sixsimplex.phantom.revelocore.phaseDetails.model.Phase;
import com.sixsimplex.phantom.revelocore.phaseDetails.view.IPhaseSelection;
import com.sixsimplex.phantom.revelocore.surveyDetails.model.Survey;
import com.sixsimplex.phantom.revelocore.util.AppMethods;
import com.sixsimplex.phantom.revelocore.util.log.ReveloLogger;
import com.sixsimplex.phantom.revelocore.util.sharedPreference.SurveyPreferenceUtility;
import com.sixsimplex.phantom.revelocore.util.sharedPreference.UserInfoPreferenceUtility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ChoosePhaseFragmentDialog extends BottomSheetDialogFragment implements ISelectPhase {

    private Activity parentActivity;
    private  String currentSurveyName="";
    private  String currentPhaseName="";
    private  String newlySelectedPhaseName="";
    private IPhaseSelection iOnPhaseSelection;
    private int requestType;
    private LineAdapter mAdapter;
    private String className = "ChooseSurveyFragmentDialog";

    public static ChoosePhaseFragmentDialog newInstance(String currentSurveyName,
                                                        Activity activity, int requestType,
                                                        IPhaseSelection iOnPhaseSelection) {

        ChoosePhaseFragmentDialog fragmentDialog = new ChoosePhaseFragmentDialog();
        fragmentDialog.setSurveyName(currentSurveyName);
        fragmentDialog.setParentActivity(activity);
        fragmentDialog.setRequestType(requestType);
        fragmentDialog.setiOnPhaseSelection(iOnPhaseSelection);
        fragmentDialog.setCurrentPhaseName(UserInfoPreferenceUtility.getSurveyPhaseName(currentSurveyName));
        return fragmentDialog;
    }

    public void setCurrentPhaseName(String currentPhaseName) {
        this.currentPhaseName = currentPhaseName;
    }

    private void setParentActivity(Activity parentActivity) {
        this.parentActivity = parentActivity;
    }

    private void setSurveyName(String currentSurveyName) {
      this.currentSurveyName = currentSurveyName;
    }

    private void setRequestType(int requestType) {
        this.requestType = requestType;
    }

    public void setiOnPhaseSelection(IPhaseSelection iOnPhaseSelection) {
        this.iOnPhaseSelection = iOnPhaseSelection;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    @NonNull
    @Override
    public Dialog onCreateDialog( Bundle savedInstanceState) {
        BottomSheetDialog bottomSheetDialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        bottomSheetDialog.setOnShowListener(dialog -> {
            BottomSheetDialog bottomSheetDialog1 = (BottomSheetDialog) dialog;
            View bottomSheet = bottomSheetDialog1.findViewById(R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                BottomSheetBehavior.from(bottomSheet).setPeekHeight(Resources.getSystem().getDisplayMetrics().heightPixels);
                BottomSheetBehavior.from(bottomSheet).setFitToContents(true);
                BottomSheetBehavior.from(bottomSheet).setHideable(false);
                BottomSheetBehavior.from(bottomSheet).setSkipCollapsed(false);
            }
        });

        return bottomSheetDialog;
/*
        Dialog dialog;
        if (requestType == AppConstants.CHANGE_SURVEY_PHASE_REQUEST) {
            dialog = super.onCreateDialog(savedInstanceState);
        } else {
            dialog = new Dialog(parentActivity, getTheme()) {
                @Override
                public void onBackPressed() {
                    if(UserInfoPreferenceUtility.getSurveyName().isEmpty()){
                        onCancelSelection("Selecting a phase, is a crucial step to use this app. " +
                                "Do you really want to cancel?");
                    }else {
                        ChoosePhaseFragmentDialog.this.dismiss();
                    }
                }
            };
        }

        AppMethods.closeKeyboard(dialog.getCurrentFocus(), parentActivity);

        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        return dialog;*/
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_fragment_choose_survey,
                container, false);
        return view;
    }

    @Override
    public void onViewCreated(@Nullable View view, @Nullable Bundle savedInstanceState) {
        if (view != null) {
            super.onViewCreated(view, savedInstanceState);

            try {
                TextView tv_headertext = view.findViewById(R.id.tv_headertext);
                tv_headertext.setText("Select Phase");
                TextView noSurveysAvailable = view.findViewById(R.id.noSurveyTv);
                noSurveysAvailable.setVisibility(View.GONE);
                TextView selectedSurveyName = view.findViewById(R.id.selectedSurveyName);
                RecyclerView surveyRecyclerView = view.findViewById(R.id.surveyRecyclerView);
                TextView selectSurveyFabBt = view.findViewById(R.id.selectSurveyFabBt);
                ImageView chooseSurveyBackIv = view.findViewById(R.id.chooseSurveyBackIv);
                    chooseSurveyBackIv.setVisibility(View.VISIBLE);


                chooseSurveyBackIv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(UserInfoPreferenceUtility.getSurveyPhaseName(currentSurveyName).isEmpty()){
                            onCancelPhaseSelection("Selecting a phase, is a crucial step to use this app. " +
                                    "Do you really want to cancel?");
                        }else {
                            iOnPhaseSelection.onPhaseSelectionCancelled();
                            try {
                                ChoosePhaseFragmentDialog.this.dismissAllowingStateLoss();//.dismiss();
                            }catch (Exception e){
                                ReveloLogger.error(className,"chooseSurveyBackIv","Error closing chooseSurvey fragment "+e.getMessage());
                                e.printStackTrace();
                            }
                        }

                    }
                });

                selectSurveyFabBt.setOnClickListener(v -> {
                    if(mAdapter!=null) {
                        newlySelectedPhaseName = mAdapter.getSelectedPhaseTvTag();
                    }


                    //1. if no survey was selected now, check if previous survey exists.
                    //--- a.if yes, show that selected(done already) and do nothing.on cancelling selection, go to home, show originally selected survey contents.
                    //--- b.if no, don't let user proceed without selecting one survey. on cancelling selection, go to login.
                    //2. if  survey was selected now, check if previous survey exists.
                    //--- a.if yes, check if they are same. if yes, show that selected(done already) and do nothing.on cancelling selection, go to home, show originally selected survey contents.
                    //--- b.if no, proceed with selected survey. on cancelling selection, go to login.

                    if(newlySelectedPhaseName.isEmpty()){
                        if(currentPhaseName.isEmpty()){
                            //do not let proceed
                            AppMethods.showAlertDialog(parentActivity, "Select a phase", "Ok", null,
                                    DialogInterface::dismiss, null);
                        }else {
                            //show same survey
                            iOnPhaseSelection.onPhaseSelected(currentSurveyName,currentPhaseName);
                          try {
                              ChoosePhaseFragmentDialog.this.dismissAllowingStateLoss();//.dismiss();
                          }catch (Exception e){
                            ReveloLogger.error(className,"selectsurvey","Error closing chooseSurvey fragment "+e.getMessage());
                            e.printStackTrace();
                        }
                            /*String msg = "You already have this phase selected. Please select a different phase or press back button to choose another project.";
                            AppMethods.showAlertDialog(parentActivity, msg, "Ok", null,
                                    DialogInterface::dismiss, null);*/
                        }
                    }else {
                        if(currentPhaseName.isEmpty()){
                            //proceed
                            iOnPhaseSelection.onPhaseSelected(currentSurveyName,newlySelectedPhaseName);
                            //ChoosePhaseFragmentDialog.this.dismiss();
                            try {
                                ChoosePhaseFragmentDialog.this.dismissAllowingStateLoss();//.dismiss();
                            }catch (Exception e){
                                ReveloLogger.error(className,"onphaseSelection","Error closing choosephase fragment "+e.getMessage());
                                e.printStackTrace();
                            }
                        }else if(currentPhaseName.equalsIgnoreCase(newlySelectedPhaseName)){
                            //show same survey
                            iOnPhaseSelection.onPhaseSelected(currentSurveyName,newlySelectedPhaseName);
                            //ChoosePhaseFragmentDialog.this.dismiss();
                            try {
                                ChoosePhaseFragmentDialog.this.dismissAllowingStateLoss();//.dismiss();
                            }catch (Exception e){
                                ReveloLogger.error(className,"onphaseselected","Error closing chooseSurvey fragment "+e.getMessage());
                                e.printStackTrace();
                            }
                        }else {
                            //proceed
                            iOnPhaseSelection.onPhaseSelected(currentSurveyName,newlySelectedPhaseName);
                            //ChoosePhaseFragmentDialog.this.dismiss();
                            try {
                                ChoosePhaseFragmentDialog.this.dismissAllowingStateLoss();//.dismiss();
                            }catch (Exception e){
                                ReveloLogger.error(className,"onphaseselected","Error closing chooseSurvey fragment "+e.getMessage());
                                e.printStackTrace();
                            }
                        }
                    }

                });

                setUp(surveyRecyclerView,selectedSurveyName, noSurveysAvailable);

            } catch (Exception e) {
                e.printStackTrace();
                ReveloLogger.error(className, "onViewCreated", String.valueOf(e.getCause()));
            }
        }
    }

    private void setUp(RecyclerView surveyRecyclerView,TextView selectedSurveyName, TextView noSurveysAvailable) {
        Survey survey = SurveyPreferenceUtility.getSurvey(UserInfoPreferenceUtility.getSurveyName());
        HashMap<String,Phase> phaseMap = survey.getPhasesNameMapFromJson();
        String surveyLabel = survey.getLabel();
        selectedSurveyName.setVisibility(View.VISIBLE);
        selectedSurveyName.setText("Project : "+surveyLabel);
        if (phaseMap != null && !phaseMap.isEmpty()) {
            surveyRecyclerView.setVisibility(View.VISIBLE);
            noSurveysAvailable.setVisibility(View.GONE);
            /*LinearLayoutManager mLayoutManager = new LinearLayoutManager(parentActivity);
            mLayoutManager.setOrientation(RecyclerView.VERTICAL);*/
            GridLayoutManager mLayoutManager = new GridLayoutManager(parentActivity,2);
            surveyRecyclerView.setLayoutManager(mLayoutManager);
            surveyRecyclerView.setItemAnimator(new DefaultItemAnimator());

            List<Phase> phaseList = new ArrayList<>(phaseMap.values());
            mAdapter = new LineAdapter(phaseList, parentActivity);
            surveyRecyclerView.setAdapter(mAdapter);
        } else {
            surveyRecyclerView.setVisibility(View.GONE);
            noSurveysAvailable.setVisibility(View.VISIBLE);
            noSurveysAvailable.setText("No phases available. Please contact admin or press back to select another project.");
            //go to download data
        }
    }

    @Override
    public void onCancelPhaseSelection(String message) {
        AppMethods.showAlertDialog(parentActivity, message, "Yes", "No",
                new AppMethods.PositiveBtnCallBack() {
                    @Override
                    public void positiveCallBack(DialogInterface dialog) {
                        iOnPhaseSelection.onPhaseSelectionCancelled();
                        dialog.dismiss();
                        dismiss();
                      //  ChoosePhaseFragmentDialog.this.dismiss();
                        try {
                            ChoosePhaseFragmentDialog.this.dismissAllowingStateLoss();//.dismiss();
                        }catch (Exception e){
                            ReveloLogger.error(className,"onCancelPhaseSelection","Error closing onCancelPhaseSelection alert fragment "+e.getMessage());
                            e.printStackTrace();
                        }
                    }
                }
                , new AppMethods.NegativeBtnCallBack() {
                    @Override
                    public void negativeCallBack(DialogInterface dialog) {
                        dialog.dismiss();
                    }
                });
    }

    private class LineAdapter extends RecyclerView.Adapter<LineAdapter.LineHolder> implements View.OnClickListener {

        private final List<Phase> list;
        private Activity activity;
        private TextView selectedPhaseTv;
        TextView selectedPhaseNextTv;
        private TextView mlastView;

        private LineAdapter(List<Phase> phaseList, Activity activity) {
            list = phaseList;
            this.activity = activity;
        }

        public String getSelectedPhaseTvTag() {
            if(selectedPhaseTv !=null && selectedPhaseTv.getTag()!=null)
           return selectedPhaseTv.getTag().toString();
            return "";
        }
        public String getSelectedPhaseTvText() {
            try {
                if (selectedPhaseTv != null && selectedPhaseTv.getText() != null)
                    return selectedPhaseTv.getText().toString();
            }catch (Exception e){
                e.printStackTrace();
            }
            return "";
        }

        @NonNull
        @Override
        public LineHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new LineHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_survey_list, parent, false));
        }

        @Override
        public void onBindViewHolder(LineHolder holder, int position) {

            Phase phase = list.get(position);

            String phaseLabel = phase.getLabel();
            String name = phase.getName();

            TextView nameTv = holder.dialogPhaseNameTv;
            nameTv.setText(phaseLabel);
            nameTv.setOnClickListener(this);
            nameTv.setTag(name);

            if ((newlySelectedPhaseName.isEmpty() && !currentPhaseName.isEmpty() && name.equalsIgnoreCase(currentPhaseName))
                    ||name.equalsIgnoreCase(newlySelectedPhaseName)
                    ||list.size()==1) {
                mlastView = nameTv;
               // nameTv.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_check, 0);
                nameTv.setSelected(true);
            } else {
                //nameTv.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                nameTv.setSelected(false);
            }
        }

        @Override
        public int getItemCount() {
            return list != null ? list.size() : 0;
        }

        @Override
        public void onClick(View v) {

            if (selectedPhaseTv != null) {
                //selectedPhaseTv.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                selectedPhaseTv.setSelected(false);
                newlySelectedPhaseName = "";
            }
            if (mlastView != null) {
                deselect(mlastView);
            }

            if (v instanceof TextView) {
                selectedPhaseTv = (TextView) v;
                //selectedPhaseTv.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_check, 0);
                selectedPhaseTv.setSelected(true);
                newlySelectedPhaseName = getSelectedPhaseTvTag();
            }
            mlastView = selectedPhaseTv;
        }

        private  class LineHolder extends RecyclerView.ViewHolder {

            private TextView dialogPhaseNameTv;

            private LineHolder(View itemView) {
                super(itemView);
                dialogPhaseNameTv = itemView.findViewById(R.id.dialogSurveyNameTv);
            }
        }
    }

    private static void deselect(TextView v) {
       // v.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        v.setSelected(false);
    }
}