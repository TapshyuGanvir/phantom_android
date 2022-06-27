package com.sixsimplex.trail.revelomodule;

import org.json.JSONObject;

public interface ReveloModuleInitializer {
    public void init(JSONObject params);
    public void start(JSONObject params);
    public void stop(JSONObject params);
}
