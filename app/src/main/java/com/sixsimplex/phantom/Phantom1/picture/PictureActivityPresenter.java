package com.sixsimplex.phantom.Phantom1.picture;

import android.app.Activity;

import android.content.Context;
import android.location.Location;

import com.sixsimplex.phantom.Phantom1.CURD.upload.UploadEditedFeatureAttachments;
import com.sixsimplex.phantom.Phantom1.CURD.upload.UploadInterface;
import com.sixsimplex.phantom.Phantom1.model.DeliveryDataModel;
import com.sixsimplex.phantom.revelocore.conceptModel.CMUtils;
import com.sixsimplex.phantom.revelocore.editing.model.Attachment;
import com.sixsimplex.phantom.revelocore.graph.concepmodelgraph.CMGraph;
import com.sixsimplex.phantom.revelocore.surveyDetails.model.Survey;
import com.sixsimplex.phantom.revelocore.util.sharedPreference.SecurityPreferenceUtility;
import com.sixsimplex.phantom.revelocore.util.sharedPreference.SurveyPreferenceUtility;
import com.sixsimplex.phantom.revelocore.util.sharedPreference.UserInfoPreferenceUtility;

import org.json.JSONObject;

import java.util.List;

public class PictureActivityPresenter {
    Activity activity;
    Context context;
    CMGraph cmGraph;
    IPictureCallback iPictureCallback;

    public PictureActivityPresenter(Activity activity,IPictureCallback iPictureCallback) {
        this.activity = activity;
        this.context = activity;
        this.iPictureCallback=iPictureCallback;
    }

    public void saveAndUploadAttachment(List<Attachment> attachmentFileList, String featureId, String entityName, Location location, UploadInterface uploadInterface) {
        try {
            JSONObject jsonObject=new JSONObject();
            int attachmentsAdded = DeliveryDataModel.getInstance().getTraversalEntity().getFeatureTable().insertAddAttachmentRecord(attachmentFileList, featureId, entityName, context, location);
            if (attachmentsAdded != -1) {
                String accessToken = SecurityPreferenceUtility.getAccessToken();
                String userName = UserInfoPreferenceUtility.getUserName();
                String surveyName = UserInfoPreferenceUtility.getSurveyName();
                Survey survey = SurveyPreferenceUtility.getSurvey(surveyName);
                String conceptModelName = survey.getConceptModelName();
                if (cmGraph == null) {
                    JSONObject graphResult = CMUtils.getCMGraph(context);
                    if (graphResult.has("status") && graphResult.getString("status").equalsIgnoreCase("success")) {
                        cmGraph = (CMGraph) graphResult.get("result");
                    }
                }
                new UploadEditedFeatureAttachments(context, accessToken,entityName, userName, surveyName, conceptModelName, cmGraph, uploadInterface, false).execute();
            }
            iPictureCallback.onTaskCompleted(attachmentsAdded,"");
        } catch (Exception e) {

            e.printStackTrace();
        }
    }
}
