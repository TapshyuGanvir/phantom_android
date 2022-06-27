package com.sixsimplex.trail;

import com.vividsolutions.jts.geom.Geometry;

import org.json.JSONObject;

public class TrailFeature {

  private String trailid;
  private String starttimestamp;
  private String endtimestamp;
  private String isnew;
  private String transportmode;
  private String username;
  private JSONObject w9metadata,geometryGeoJson;
  private String w9entityclassname = Constants.TRAIL_TABLE_NAME;
  private double distance;
  private String jurisdictioninfo;
  private String description;
  private Geometry jtsGeometry;

    public TrailFeature() {
    }

    public TrailFeature(String trailid, String username) {
        this.trailid = trailid;
        this.username = username;
    }

    public String getTrailid() {
        return trailid;
    }

    public void setTrailid(String trailid) {
        this.trailid = trailid;
    }

    public String getStarttimestamp() {
        return starttimestamp;
    }

    public void setStarttimestamp(String starttimestamp) {
        this.starttimestamp = starttimestamp;
    }

    public String getEndtimestamp() {
        return endtimestamp;
    }

    public void setEndtimestamp(String endtimestamp) {
        this.endtimestamp = endtimestamp;
    }

    public String getIsnew() {
        return isnew;
    }

    public void setIsnew(String isnew) {
        this.isnew = isnew;
    }

    public String getTransportmode() {
        return transportmode;
    }

    public void setTransportmode(String transportmode) {
        this.transportmode = transportmode;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public JSONObject getW9metadata() {
        return w9metadata;
    }

    public void setW9metadata(JSONObject w9metadata) {
        this.w9metadata = w9metadata;
    }

    public String getW9entityclassname() {
        return w9entityclassname;
    }
    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public String getJurisdictioninfo() {
        return jurisdictioninfo;
    }

    public void setJurisdictioninfo(String jurisdictioninfo) {
        this.jurisdictioninfo = jurisdictioninfo;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public JSONObject getGeometryGeoJson() {
        return geometryGeoJson;
    }

    public void setGeometryGeoJson(JSONObject geometryGeoJson) {
        this.geometryGeoJson = geometryGeoJson;
    }

    public Geometry getJtsGeometry() {
        return jtsGeometry;
    }

    public void setJtsGeometry(Geometry jtsGeometry) {
        this.jtsGeometry = jtsGeometry;
    }
}
