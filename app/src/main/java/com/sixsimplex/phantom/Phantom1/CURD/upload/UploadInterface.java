package com.sixsimplex.phantom.Phantom1.CURD.upload;

import org.json.JSONObject;

public interface UploadInterface {
    void OnUploadStarted();
    void OnUploadFinished(boolean isSuccessfull, JSONObject uploadResult);
}
