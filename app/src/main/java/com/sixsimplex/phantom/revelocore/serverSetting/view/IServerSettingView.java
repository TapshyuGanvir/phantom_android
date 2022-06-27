package com.sixsimplex.phantom.revelocore.serverSetting.view;

import android.view.View;

public interface IServerSettingView {

    void onServerSetting(String message, View view, boolean error, String appServer, String securityServer,String realmName);

    void onErrorServerSetting(String message, View view);
}