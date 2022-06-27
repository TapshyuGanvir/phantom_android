package com.sixsimplex.phantom.revelocore.selectJurisdiction;

public interface ISelectedJurisdictionsData {

    void getSelectedJurisdictionsData(String selectedJurisdictions, String fileType);

    void errorGettingJurisdictionData(String message);

    void cancelGettingJurisdictionData(String message,int requestType);

}
