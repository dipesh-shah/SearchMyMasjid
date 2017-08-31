package com.digitaljalebi.searchmymasjid;

import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyLog;
import com.android.volley.error.VolleyError;
import com.android.volley.request.JsonArrayRequest;
import com.bluelinelabs.logansquare.LoganSquare;

import org.json.JSONArray;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by dipesh on 02/10/16.
 */

public class AreaWiseMosques extends AppCompatActivity {
    private ProgressDialog pDialog;
    private ListView mListView;
    private String mTitle;
    private List<MasjidsModel> mData;
    private AlertDialog mAlertDialog;
    private List<Areas.Area> areas;
    private String[] timing = new String[6];

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.area_mosque_layout);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mTitle = getIntent().getExtras().getString("city_key");
        if (TextUtils.isEmpty(mTitle)) {
            mTitle = getIntent().getExtras().getString("area_key");
            areas = getIntent().getParcelableArrayListExtra("areas_list");
        }
        getSupportActionBar().setTitle(mTitle);

        pDialog = new ProgressDialog(this);
        mListView = (ListView) findViewById(R.id.area_list);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (areas != null) {
                    Intent intent = new Intent(AreaWiseMosques.this, AreaWiseMosques.class);
                    intent.putExtra("city_key", areas.get(position).name + " , Bangalore");
                    startActivity(intent);
                }
                else {
                    if (mAlertDialog != null && mAlertDialog.isShowing()) {
                        //do nothing
                    }
                    else {
                        mAlertDialog = getAlertDialog(AreaWiseMosques.this, mData.get(position));
                        mAlertDialog.show();
                    }

                }
            }
        });
        if (areas == null) {
            makeRequest();
        }
        else {
            ArrayAdapter<Areas.Area> adapter = new CustomAdapters(this, android.R.layout.simple_list_item_1, areas);
            mListView.setAdapter(adapter);
        }
    }



    private void makeRequest() {
        pDialog.setMessage("Loading...");
        pDialog.show();
        String url = "http://ec2-18-220-53-7.us-east-2.compute.amazonaws.com/querysearch?query=";
        JsonArrayRequest jsonObjReq = null;
        try {
            jsonObjReq = new JsonArrayRequest(Request.Method.GET,
                    url + URLEncoder.encode("mosques in " +mTitle, "utf-8"), null,
                    new Response.Listener<JSONArray>() {

                        @Override
                        public void onResponse(JSONArray response) {
                            Log.d(MapsActivity.class.getSimpleName(), response.toString());
                            pDialog.dismiss();
                            try {
                                List<MasjidsModel> data = LoganSquare.parseList(response.toString(), MasjidsModel.class);
                                Log.d(MapsActivity.class.getSimpleName(), "" + data.size());
                                updateUi(data);
                            } catch (IOException e) {
                                Toast.makeText(AreaWiseMosques.this, "error1 " + e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(AreaWiseMosques.this, "error2 " + error.getMessage(), Toast.LENGTH_LONG).show();
                    VolleyLog.d(MapsActivity.class.getSimpleName(), "Error: " + error.getMessage());
                    // hide the progress dialog
                    pDialog.dismiss();
                }
            });
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        jsonObjReq.setShouldCache(false);
        SearchApplication.sInstance.getRequestQueue().add(jsonObjReq);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateUi(List<MasjidsModel> data) {
        mData = data;
        if (data != null && data.size() > 0) {
            List<String> names = new ArrayList<>();
            for (MasjidsModel model: data) {
                names.add(model.name);
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, names);
            mListView.setAdapter(adapter);
        }
        else {
            Toast.makeText(this, "Couldn't find any mosques around! Try again", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    public void onDestroy() {
        if (mAlertDialog != null && mAlertDialog.isShowing()) {
            mAlertDialog.dismiss();
        }
        if (pDialog != null && pDialog.isShowing()) {
            pDialog.dismiss();
        }
        super.onDestroy();
    }

    public class CustomAdapters extends ArrayAdapter<Areas.Area> {

        private Context mContext;
        private List<Areas.Area> mAreas;
        private LayoutInflater mLayoutInflater;

        public CustomAdapters(@NonNull Context context, @LayoutRes int resource, List<Areas.Area> data) {
            super(context, resource, data);
            mAreas = data;
            mLayoutInflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return mAreas != null ? mAreas.size() : 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Areas.Area area = mAreas.get(position);
            convertView = mLayoutInflater.inflate(R.layout.item_child_layout, parent, false);
            TextView areaName = (TextView)convertView.findViewById(R.id.txt_area_name);
            TextView pincodeName = (TextView)convertView.findViewById(R.id.txt_area_pincode);
            areaName.setText("" + area.name);
            pincodeName.setText("" + area.pincode);
            return convertView;
        }
    }

    public  AlertDialog getAlertDialog(final Context context, final MasjidsModel model) {
        timing = new String[6];
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.alertdialog_layout, null);
        dialogBuilder.setView(dialogView);
        String selectTime = "Select time";
        Button mTime1 = (Button) dialogView.findViewById(R.id.spinner_time1);
        mTime1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePickerDialog(context, v, 0);
            }
        });
        Button mTime2 = (Button) dialogView.findViewById(R.id.spinner_time2);
        mTime2.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                showTimePickerDialog(context, v, 1);
            }
        });
        Button mTime3 = (Button) dialogView.findViewById(R.id.spinner_time3);
        mTime3.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                showTimePickerDialog(context, v, 2);
            }
        });
        Button mTime4 = (Button) dialogView.findViewById(R.id.spinner_time4);
        mTime4.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                showTimePickerDialog(context, v, 3);
            }
        });
        Button mTime5 = (Button) dialogView.findViewById(R.id.spinner_time5);
        mTime5.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                showTimePickerDialog(context, v, 4);
            }
        });
        Button mTime6 = (Button) dialogView.findViewById(R.id.spinner_time6);
        mTime6.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                showTimePickerDialog(context, v, 5);
            }
        });
        if (model.timings != null) {
            Utils.populateMasjidDisplayTimings(model);
            mTime1.setText(model.displayTimings[0]+ " " + model.amPmArray[0]);
            mTime2.setText(model.displayTimings[1]+ " " + model.amPmArray[1]);
            mTime3.setText(model.displayTimings[2]+ " " + model.amPmArray[2]);
            mTime4.setText(model.displayTimings[3]+ " " + model.amPmArray[3]);
            mTime5.setText(model.displayTimings[4]+ " " + model.amPmArray[4]);
            mTime6.setText(model.displayTimings[5]+ " " + model.amPmArray[5]);
        }
        AlertDialog alertDialog = dialogBuilder.create();
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();

        lp.copyFrom(alertDialog.getWindow().getAttributes());
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        alertDialog.getWindow().setAttributes(lp);

        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                model.timings = timing;
                model.saveTimings();
                // make a call to udpate the timings
            }
        });
        alertDialog.setTitle(model.name);
        return alertDialog;
    }

    private void showTimePickerDialog(Context context, final View v, final int position) {
        Calendar mcurrentTime = Calendar.getInstance();
        int hour = mcurrentTime.get(Calendar.HOUR);
        int minute = mcurrentTime.get(Calendar.MINUTE);
        TimePickerDialog mTimePicker;
        mTimePicker = new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                timing[position] = selectedHour + ":" + (selectedMinute < 10 ? "0" + selectedMinute : selectedMinute);
                ((Button)v).setText((selectedHour <=12 ? selectedHour : selectedHour - 12)
                        + ":" + (selectedMinute < 10 ? "0" + selectedMinute : selectedMinute)+ " " + (selectedHour >=12 ? "PM" : "AM"));
            }
        }, hour, minute, false);//Yes 24 hour time
        mTimePicker.setTitle("Select Time");
        mTimePicker.show();
    }
}
