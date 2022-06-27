package com.sixsimplex.phantom.Phantom1.appfragment.profile;

import android.app.Activity;

import com.sixsimplex.phantom.Phantom1.utils.IrefreshResponce;
import com.sixsimplex.phantom.Phantom1.utils.Refreshdata;
import com.sixsimplex.phantom.revelocore.principalEndpoint.view.IPrincipleEndpointView;
import com.sixsimplex.phantom.revelocore.surveyDetails.view.ISurveyDetails;
import com.sixsimplex.phantom.revelocore.util.constants.AppConstants;

public class ProfilePresenter {
    private boolean firstTimeDrawerOpen = true;

    public boolean getFirstTimeOpenProfile() {
        return firstTimeDrawerOpen;
    }

    public void setFirstTimeOpenProfile(boolean b) {
        this.firstTimeDrawerOpen = b;
    }

    public  void refreshAssignment(Activity activity, IrefreshResponce irefreshResponce, boolean refreshPrincipal, boolean refreshSurvey, IPrincipleEndpointView iPrincipleEndpointView, ISurveyDetails iSurveyDetails) {
       Refreshdata.refreshData(activity,AppConstants.REFRESH_DATA_REQUEST,irefreshResponce,refreshPrincipal,refreshSurvey,iPrincipleEndpointView,iSurveyDetails);
    }
}
