package com.sixsimplex.phantom.revelocore.obConceptModel.view;

import com.sixsimplex.phantom.revelocore.obConceptModel.model.OBDataModel;

public interface IOrgBoundaryConceptModel {

    void onSuccess(OBDataModel OBDataModel);

    void onError(String errorMsg);

}
