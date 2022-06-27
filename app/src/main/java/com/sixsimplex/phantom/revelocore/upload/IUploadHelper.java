package com.sixsimplex.phantom.revelocore.upload;

import android.app.ProgressDialog;

import com.sixsimplex.phantom.revelocore.graph.concepmodelgraph.CMGraph;

import org.json.JSONObject;

public interface IUploadHelper {

    void onPayLoad(JSONObject dataJson, CMGraph cmGraph,  ProgressDialog progressDialog);
    void onError(String errorMessage, ProgressDialog progressDialog);


}
