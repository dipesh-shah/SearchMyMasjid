package com.digitaljalebi.searchmymasjid;

import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyLog;
import com.android.volley.error.VolleyError;
import com.android.volley.request.JsonObjectRequest;
import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

/**
 * Created by dipesh on 25/09/16.
 */

@JsonObject
public class MasjidsModel {


    @JsonObject
    public static class Geometry {
        @JsonField
        Location location;
    }

    @JsonObject
    public static class Location {
        @JsonField
        public double lat;
        @JsonField
        public double lng;
    }

    @JsonField
    public String id;
    @JsonField
    public String name;
    @JsonField
    public String[] timings;
    @JsonField
    public Geometry geometry;
    public float distanceFromCurrentLocation;
    public String[] displayTimings = new String[6];
    public String[] amPmArray = new String[6];

    public void saveTimings() {
        String url = "http://ec2-18-220-53-7.us-east-2.compute.amazonaws.com/updatetimings";
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                url, getJSONObject(),
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(MapsActivity.class.getSimpleName(), response.toString());

                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(MapsActivity.class.getSimpleName(), "Error: " + error.getMessage());
            }
        });
        jsonObjReq.setShouldCache(false);
        SearchApplication.sInstance.getRequestQueue().add(jsonObjReq);
    }

    private JSONObject getJSONObject() {
        JSONObject jsonObj = new JSONObject();
        try {
            jsonObj.put("google_id", id);
            JSONArray jsonArray = new JSONArray(Arrays.asList(timings));
            jsonObj.put("timings", jsonArray);
        } catch (JSONException e) {
        }
        return jsonObj;
    }
}
