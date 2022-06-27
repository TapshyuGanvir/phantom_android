package com.sixsimplex.trail;

import android.location.Location;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.io.WKTReader;

import org.json.JSONArray;
import org.json.JSONObject;
import org.locationtech.proj4j.CRSFactory;
import org.locationtech.proj4j.CoordinateReferenceSystem;
import org.locationtech.proj4j.CoordinateTransform;
import org.locationtech.proj4j.CoordinateTransformFactory;

import java.util.ArrayList;
import java.util.List;


public class GeometryEngine {

    public static boolean contains(Geometry geometry, Geometry container) {

        return container.contains(geometry);
    }

    public static boolean within(Geometry geometry, Geometry container) {

        return container.within(geometry);
    }

    public static boolean overlaps(Geometry geometry, Geometry container) {

        return container.overlaps(geometry);
    }

    public static boolean intersects(Geometry geometry, Geometry container) {

        return container.intersects(geometry);
    }

    public static boolean crosses(Geometry geometry, Geometry container) {

        return container.crosses(geometry);
    }

    public static Geometry getGeoFromLocation(Location location) {

        GeometryFactory factory = new GeometryFactory(new PrecisionModel(), 4326);
        return factory.createPoint(new Coordinate(location.getLongitude(), location.getLatitude()));
    }

    public static List<TrailLatLng> convertJTSGeometryToLatLngList(Geometry geometry) {
        List<TrailLatLng> trailLatLngList = new ArrayList<>();
        Coordinate[] coordinates = geometry.getCoordinates();

        for (Coordinate coordinate : coordinates) {
            TrailLatLng trailLatLng = new TrailLatLng(coordinate.getOrdinate(1), coordinate.getOrdinate(0));
            trailLatLngList.add(trailLatLng);
        }
        return trailLatLngList;
    }

    public static List<List<TrailLatLng>> convertJTSGeometryToTrailLatLng_multipart(Geometry geometry_multipart) {
        List<List<TrailLatLng>> trailLatLngList = new ArrayList<>();

        for(int i=0;i<geometry_multipart.getNumGeometries();i++) {
            Geometry geometry = geometry_multipart.getGeometryN(i);
            Coordinate[] coordinates = geometry.getCoordinates();
            List<TrailLatLng> trailLatLngSubList = new ArrayList<>();
            for (Coordinate coordinate : coordinates) {
                TrailLatLng trailLatLng = new TrailLatLng(coordinate.getOrdinate(1), coordinate.getOrdinate(0));

                trailLatLngSubList.add(trailLatLng);
            }
            trailLatLngList.add(trailLatLngSubList);
        }
        return trailLatLngList;
    }

    public static Geometry convertWKTToJTSGeom(String wkt) {
        try {
            return new WKTReader().read(wkt);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }

    public static boolean isLocationWithinGeofence(Geometry targetGeometry, Location location,double bufferDistance) {
        if (location != null) {
//          bufferDistance = 100;
            Geometry targetGeomBuffer = targetGeometry.buffer(convertMetersToDecimalDegrees(bufferDistance,
                    targetGeometry.getCentroid().getCoordinate().getOrdinate(1)));
            GeometryFactory geometryFactory = new GeometryFactory();
            Geometry locationPointGeom = geometryFactory.createPoint(new Coordinate(location.getLongitude(),
                    location.getLatitude()));
            if (targetGeomBuffer.contains(locationPointGeom)) {
                return true;
            } else {
                return false;
            }

        } else {
            return false;
        }
    }

    public static double convertMetersToDecimalDegrees(double meters, double latitude) {
        //111.32
        return meters / (111.111 * 1000 * Math.cos(latitude * (Math.PI / 180)));
    }

    public static JSONObject convertGeometryToGeoJson(Geometry geometry) {
        JSONObject geoJsonGeometry = new JSONObject();
        if(geometry==null){
            return null;
        }
        try {
            geoJsonGeometry.put("type","FeatureCollection");
            JSONArray featuresArray = new JSONArray();
            JSONObject featureJsonObj = new JSONObject();
            JSONObject featureProperties = new JSONObject();
            featureJsonObj.put("properties",featureProperties);
            featureJsonObj.put("type","Feature");

            //create jsonobject geometry
            JSONObject geometryJson = new JSONObject();

            String geometryType = geometry.getGeometryType();
            String geometryTypeStr = geometry.getGeometryType();

            if(geometryType.equalsIgnoreCase("polygon")){
                geometryTypeStr = "MultiPolygon";
            }
            if(geometryType.equalsIgnoreCase("polyline")
                    || geometryType.equalsIgnoreCase("linestring")
                    || geometryType.equalsIgnoreCase("multiLineString")){
                geometryTypeStr = "MultiLineString";
            }
            geometryJson.put("type",geometryTypeStr);


            if(geometryType.equalsIgnoreCase("polygon")){
                Polygon polygon = (Polygon) geometry;
                JSONArray polygonJsonArray = getPolygonJsonArray(polygon);
                JSONArray multipolygonJsonArray = new JSONArray();
                try{
                    multipolygonJsonArray.put(polygonJsonArray);
                }catch (Exception e){e.printStackTrace();}
                geometryJson.put("coordinates",multipolygonJsonArray);
            }else if(geometryType.equalsIgnoreCase("multipolygon")){
                com.vividsolutions.jts.geom.MultiPolygon multiPolygon = (com.vividsolutions.jts.geom.MultiPolygon) geometry;
                JSONArray multipolygonJsonArray = new JSONArray();
                try{
                    for(int i=0;i<multiPolygon.getNumGeometries();i++) {
                        Polygon polygon = (Polygon) multiPolygon.getGeometryN(i);
                        JSONArray polygonJsonArray = getPolygonJsonArray(polygon);
                        multipolygonJsonArray.put(polygonJsonArray);
                    }
                }catch (Exception e){e.printStackTrace();}
                geometryJson.put("coordinates",multipolygonJsonArray);
            }else if(geometryType.equalsIgnoreCase("linestring")){
                com.vividsolutions.jts.geom.LineString lineString = (com.vividsolutions.jts.geom.LineString) geometry;
                JSONArray lineStringJsonArray = getLineStringJsonArray(lineString);
                JSONArray polylineJsonArray = new JSONArray();
                try{
                    polylineJsonArray.put(lineStringJsonArray);
                }catch (Exception e){e.printStackTrace();}
                geometryJson.put("coordinates",polylineJsonArray);
            }
            else if(geometryType.equalsIgnoreCase("multilinestring")){
                com.vividsolutions.jts.geom.MultiLineString multiLineString = (com.vividsolutions.jts.geom.MultiLineString) geometry;
                JSONArray multiLineStringJsonArray = new JSONArray();
                try{
                    for(int i=0;i<multiLineString.getNumGeometries();i++) {
                        com.vividsolutions.jts.geom.LineString lineString = (com.vividsolutions.jts.geom.LineString) multiLineString.getGeometryN(i);
                        JSONArray lineStringJsonArray = getLineStringJsonArray(lineString);
                        multiLineStringJsonArray.put(lineStringJsonArray);
                    }
                }catch (Exception e){e.printStackTrace();}
                geometryJson.put("coordinates",multiLineStringJsonArray);
            }
            else if(geometryType.equalsIgnoreCase("point")){
                com.vividsolutions.jts.geom.Point point = (com.vividsolutions.jts.geom.Point) geometry;
                JSONArray pointJsonArray = new JSONArray();
                try{
                    pointJsonArray = getPointJsonArray(point.getCoordinate());
                }catch (Exception e){e.printStackTrace();}
                geometryJson.put("coordinates",pointJsonArray);
            }


            featureJsonObj.put("geometry",geometryJson);
            featuresArray.put(featureJsonObj);
            geoJsonGeometry.put("features",featuresArray);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return geoJsonGeometry;
    }

    public static JSONObject convertGeometryToGeoJson(Geometry geometry,JSONObject featureProperties) {
        JSONObject geoJsonGeometry = new JSONObject();
        if(geometry==null){
            return null;
        }
        try {
            geoJsonGeometry.put("type","FeatureCollection");
            JSONArray featuresArray = new JSONArray();
            JSONObject featureJsonObj = new JSONObject();
            if(featureProperties==null) {
                featureProperties = new JSONObject();
            }
            featureJsonObj.put("properties",featureProperties);
            featureJsonObj.put("type","Feature");

            //create jsonobject geometry
            JSONObject geometryJson = new JSONObject();

            String geometryType = geometry.getGeometryType();
            String geometryTypeStr = geometry.getGeometryType();

            if(geometryType.equalsIgnoreCase("polygon")){
                geometryTypeStr = "MultiPolygon";
            }
            if(geometryType.equalsIgnoreCase("polyline")
                    || geometryType.equalsIgnoreCase("linestring")
                    || geometryType.equalsIgnoreCase("multiLineString")){
                geometryTypeStr = "MultiLineString";
            }
            geometryJson.put("type",geometryTypeStr);


            if(geometryType.equalsIgnoreCase("polygon")){
                Polygon polygon = (Polygon) geometry;
                JSONArray polygonJsonArray = getPolygonJsonArray(polygon);
                JSONArray multipolygonJsonArray = new JSONArray();
                try{
                    multipolygonJsonArray.put(polygonJsonArray);
                }catch (Exception e){e.printStackTrace();}
                geometryJson.put("coordinates",multipolygonJsonArray);
            }else if(geometryType.equalsIgnoreCase("multipolygon")){
                com.vividsolutions.jts.geom.MultiPolygon multiPolygon = (com.vividsolutions.jts.geom.MultiPolygon) geometry;
                JSONArray multipolygonJsonArray = new JSONArray();
                try{
                    for(int i=0;i<multiPolygon.getNumGeometries();i++) {
                        Polygon polygon = (Polygon) multiPolygon.getGeometryN(i);
                        JSONArray polygonJsonArray = getPolygonJsonArray(polygon);
                        multipolygonJsonArray.put(polygonJsonArray);
                    }
                }catch (Exception e){e.printStackTrace();}
                geometryJson.put("coordinates",multipolygonJsonArray);
            }else if(geometryType.equalsIgnoreCase("linestring")){
                com.vividsolutions.jts.geom.LineString lineString = (com.vividsolutions.jts.geom.LineString) geometry;
                JSONArray lineStringJsonArray = getLineStringJsonArray(lineString);
                JSONArray polylineJsonArray = new JSONArray();
                try{
                    polylineJsonArray.put(lineStringJsonArray);
                }catch (Exception e){e.printStackTrace();}
                geometryJson.put("coordinates",polylineJsonArray);
            }
            else if(geometryType.equalsIgnoreCase("multilinestring")){
                com.vividsolutions.jts.geom.MultiLineString multiLineString = (com.vividsolutions.jts.geom.MultiLineString) geometry;
                JSONArray multiLineStringJsonArray = new JSONArray();
                try{
                    for(int i=0;i<multiLineString.getNumGeometries();i++) {
                        com.vividsolutions.jts.geom.LineString lineString = (com.vividsolutions.jts.geom.LineString) multiLineString.getGeometryN(i);
                        JSONArray lineStringJsonArray = getLineStringJsonArray(lineString);
                        multiLineStringJsonArray.put(lineStringJsonArray);
                    }
                }catch (Exception e){e.printStackTrace();}
                geometryJson.put("coordinates",multiLineStringJsonArray);
            }
            else if(geometryType.equalsIgnoreCase("point")){
                com.vividsolutions.jts.geom.Point point = (com.vividsolutions.jts.geom.Point) geometry;
                JSONArray pointJsonArray = new JSONArray();
                try{
                    pointJsonArray = getPointJsonArray(point.getCoordinate());
                }catch (Exception e){e.printStackTrace();}
                geometryJson.put("coordinates",pointJsonArray);
            }


            featureJsonObj.put("geometry",geometryJson);
            featuresArray.put(featureJsonObj);
            geoJsonGeometry.put("features",featuresArray);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return geoJsonGeometry;
    }
    //todo CHECK
    private static JSONArray getPointJsonArray(Coordinate point) {
        JSONArray pointJSONArray = new JSONArray();
        try {
            pointJSONArray.put(point.x);
            pointJSONArray.put(point.y);
        }catch (Exception e){
            e.printStackTrace();
        }
        return pointJSONArray;
    }

    //todo CHECK
    private static JSONArray getLineStringJsonArray(com.vividsolutions.jts.geom.LineString lineString) {
        JSONArray linestringJSONArray = new JSONArray();
        try {

            for (int i = 0; i < lineString.getCoordinates().length; i++) {
                JSONArray pointJSONArray =getPointJsonArray(lineString.getCoordinateN(i));
                linestringJSONArray.put(pointJSONArray);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return linestringJSONArray;
    }

    //todo CHECK
    private static JSONArray getPolygonJsonArray(Polygon polygon) {
        JSONArray polygonJSONArray = new JSONArray();
        try {


                for (int i = 0;i<polygon.getNumGeometries();i++) {
                    JSONArray lineStringJsonArray = new JSONArray();
                    Polygon lineString =(Polygon) polygon.getGeometryN(i);
                    for (int j = 0; j < lineString.getCoordinates().length; j++) {
                        JSONArray pointJSONArray = getPointJsonArray(lineString.getCoordinates()[j]);
                        lineStringJsonArray.put(pointJSONArray);
                    }
                    polygonJSONArray.put(lineStringJsonArray);
                }

        }catch (Exception e){
            e.printStackTrace();
        }
        return polygonJSONArray;
    }

    public static Geometry transform(Geometry geom,
                                     String srcParams,
                                     String tgtParams) {

        try {
            CoordinateTransformFactory ctFactory = new CoordinateTransformFactory();
            CRSFactory crsFactory = new CRSFactory();

            CoordinateReferenceSystem srcCrs = crsFactory.createFromParameters(null, srcParams);
            CoordinateReferenceSystem tgtCrs = crsFactory.createFromParameters(null, tgtParams);

            CoordinateTransform coordTransform = ctFactory.createTransform(srcCrs, tgtCrs);
            //return transformGeometry(coordTransform, geom);
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
