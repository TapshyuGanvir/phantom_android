package com.sixsimplex.phantom.revelocore.principalEndpoint;

import android.app.Activity;
import android.os.AsyncTask;

import com.sixsimplex.phantom.revelocore.upload.UploadFile;
import com.sixsimplex.phantom.revelocore.util.AppFolderStructure;
import com.sixsimplex.phantom.revelocore.util.UrlStore;
import com.sixsimplex.phantom.revelocore.util.log.ReveloLogger;
import com.sixsimplex.phantom.revelocore.util.sharedPreference.UserInfoPreferenceUtility;

import org.json.JSONObject;

import java.io.File;

public class DownloadLogoAsyncTask extends AsyncTask<String,String, JSONObject> {
    Activity activity;

    public DownloadLogoAsyncTask(Activity activity) {
        this.activity=activity;
    }

    @Override
    protected JSONObject doInBackground(String... strings) {

        File orgNameFolder = new File(AppFolderStructure.orgLogoFolderPath(activity));
        orgNameFolder.mkdir();
        ReveloLogger.debug("organization logo downloading","download logo","downloading...");
        String fileName= UserInfoPreferenceUtility.getOrgName()+"AppLogo.png";
        UploadFile.downloadFile(UrlStore.getOrgLogoUrl(),fileName,false,orgNameFolder,activity,null);
        return null;
    }
}
