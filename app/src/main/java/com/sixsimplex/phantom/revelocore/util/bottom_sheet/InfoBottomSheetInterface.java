package com.sixsimplex.phantom.revelocore.util.bottom_sheet;

public interface InfoBottomSheetInterface {

    void onOkInfoBottomSheetResult(int requestCode,int errorCode, String jurisdictions);

    void onCancelOkBottomSheetResult(int requestCode,int errorCode);
}
