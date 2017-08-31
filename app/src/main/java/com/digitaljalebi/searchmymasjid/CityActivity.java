package com.digitaljalebi.searchmymasjid;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyLog;
import com.android.volley.error.VolleyError;
import com.android.volley.request.JsonObjectRequest;
import com.bluelinelabs.logansquare.LoganSquare;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by dipesh on 26/09/16.
 */

public class CityActivity extends AppCompatActivity {
    private ProgressDialog pDialog;
    private List<String> mHeaders = new ArrayList<>();
    private Map<String, List<Areas.Area>> mParentChildMapping = new HashMap<>();
    private ExpandableListView mListView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.city_area_layout);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Select a city");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        pDialog = new ProgressDialog(this);
        mListView = (ExpandableListView) findViewById(R.id.area_list);
        mListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                Areas.Area area = mParentChildMapping.get(mHeaders.get(groupPosition)).get(childPosition);
                Intent intent = new Intent(CityActivity.this, AreaWiseMosques.class);
                intent.putExtra("city_key", area.name + ", Bangalore");
                startActivity(intent);
                return false;
            }
        });
        mListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                Intent intent = new Intent(CityActivity.this, AreaWiseMosques.class);
                intent.putExtra("area_key", mHeaders.get(groupPosition) + ", Bangalore");
                intent.putParcelableArrayListExtra("areas_list", (ArrayList<Areas.Area>)mParentChildMapping.get(mHeaders.get(groupPosition)));
                startActivity(intent);
                return false;
            }
        });
        makeRequestForAreas();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void makeRequestForAreas() {
        pDialog.setMessage("Loading...");
        pDialog.show();
        String url = "http://ec2-18-220-53-7.us-east-2.compute.amazonaws.com/getareas";
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                url, null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(MapsActivity.class.getSimpleName(), response.toString());
                        pDialog.dismiss();
                        try {
                            Areas areasList = LoganSquare.parse(response.toString(), Areas.class);
                            parseCities(areasList.areas);
                            Log.d(MapsActivity.class.getSimpleName(), "" + areasList.areas.size());
                        } catch (IOException e) {
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(MapsActivity.class.getSimpleName(), "Error: " + error.getMessage());
                // hide the progress dialog
                pDialog.dismiss();
            }
        });
        jsonObjReq.setShouldCache(false);
        SearchApplication.sInstance.getRequestQueue().add(jsonObjReq);
    }

    private void parseCities(List<Areas.Area> areas) {
        if (areas != null && areas.size() > 0) {
            mHeaders.add("Bengaluru/Bangalore");
            mParentChildMapping.put(mHeaders.get(0), areas);
            CityAreaExpandableListAdapter adapter = new CityAreaExpandableListAdapter(this, mHeaders, null);
            mListView.setAdapter(adapter);
        }
        else {
            Toast.makeText(CityActivity.this, "City areas not found. Please Try again", Toast.LENGTH_LONG).show();
            finish();
        }
    }
}
