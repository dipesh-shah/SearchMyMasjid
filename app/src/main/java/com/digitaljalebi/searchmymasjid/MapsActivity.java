package com.digitaljalebi.searchmymasjid;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyLog;
import com.android.volley.error.VolleyError;
import com.android.volley.request.JsonArrayRequest;
import com.bluelinelabs.logansquare.LoganSquare;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;

import java.io.IOException;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.google.android.gms.location.LocationServices.FusedLocationApi;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("HH:mm");
    private final int PERMISSION_REQUEST_CODE = 1345;
    private final int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1346;
    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private Location mCurrentLocation;
    private List<MasjidsModel> mData;
    private TextView mName;
    private TextView mDistance;
    private TextView mTime1;
    private TextView mTime2;
    private TextView mTime3;
    private TextView mTime4;
    private TextView mTime5;
    private TextView mTime6;
    private TextView mAmpm1;
    private TextView mAmpm2;
    private TextView mAmpm3;
    private TextView mAmpm4;
    private TextView mAmpm5;
    private TextView mAmpm6;
    private ProgressDialog pDialog;
    private Button mRoute;
    private Button mMore;
    private int mCurrentSelected = 0;
    private CameraPosition mCameraPosition;
    protected static final int REQUEST_CHECK_SETTINGS = 0x1;
    private TextView mLocationSearch;
    private Location mSelectedLocation;
    private boolean mLocationRequestForResult = false;
    private NavigationView mNavView;
    private DrawerLayout mDrawerLayout;
    private AlertDialog mAlertDialog;
    private String[] timing = new String[6];
    private TimePickerDialog mTimePickerDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.app_name);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        pDialog = new ProgressDialog(this);
        findViewById(R.id.btn_update_timings).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mData == null || mData.size() == 0) {
                    Toast.makeText(MapsActivity.this, "No masjid data found!", Toast.LENGTH_LONG).show();
                    return;
                }
                // show dialog
                if (mAlertDialog != null && mAlertDialog.isShowing()) {
                    return;
                }
                mAlertDialog = getAlertDialog(MapsActivity.this, mData.get(mCurrentSelected));
                mAlertDialog.show();
            }
        });
        mName = (TextView) findViewById(R.id.masjid_name);
        mDistance = (TextView) findViewById(R.id.distance);
        mTime1 = (TextView) findViewById(R.id.time1);
        mTime2 = (TextView) findViewById(R.id.time2);
        mTime3 = (TextView) findViewById(R.id.time3);
        mTime4 = (TextView) findViewById(R.id.time4);
        mTime5 = (TextView) findViewById(R.id.time5);
        mTime6 = (TextView) findViewById(R.id.time6);
        mAmpm1 = (TextView) findViewById(R.id.ampm1);
        mAmpm2 = (TextView) findViewById(R.id.ampm2);
        mAmpm3 = (TextView) findViewById(R.id.ampm3);
        mAmpm4 = (TextView) findViewById(R.id.ampm4);
        mAmpm5 = (TextView) findViewById(R.id.ampm5);
        mAmpm6 = (TextView) findViewById(R.id.ampm6);
        mMore = (Button) findViewById(R.id.more);
        mNavView = (NavigationView) findViewById(R.id.navgiation_view);
        mLocationSearch = (TextView) findViewById(R.id.place_autocomplete);
        mLocationSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AutocompleteFilter typeFilter = new AutocompleteFilter.Builder()
                        .setTypeFilter(AutocompleteFilter.TYPE_FILTER_ADDRESS)
                        .build();
                try {
                    Intent intent =
                            new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                                    .setFilter(typeFilter)
                                    .setBoundsBias(new LatLngBounds(
                                            new LatLng(12.58, 77.34),
                                            new LatLng(12.79, 77.58)))
                                    .build(MapsActivity.this);
                    startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
                } catch (GooglePlayServicesRepairableException e) {
                    // TODO: Handle the error.
                } catch (GooglePlayServicesNotAvailableException e) {
                    // TODO: Handle the error.
                }
            }
        });
        mMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCurrentLocation != null) {
                    Intent intent = new Intent(MapsActivity.this, CityActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(MapsActivity.this, "Location not available. Please try again after sometime.", Toast.LENGTH_LONG).show();
                }
            }
        });
        ImageButton currentLocation = (ImageButton) findViewById(R.id.current_location);
        currentLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSelectedLocation != null) {
                    mSelectedLocation = mCurrentLocation;
                    mLocationSearch.setText(getString(R.string.search_location));
                    updateMapLocations(mCurrentLocation);
                } else {
                    updateCameraPosition();
                }
            }
        });
        mRoute = (Button) findViewById(R.id.route);
        mRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mData == null || mData.size() == 0) {
                    return;
                }
                MasjidsModel model = mData.get(mCurrentSelected);
                Location location = mCurrentLocation;
                if (mSelectedLocation != null) {
                    location = mSelectedLocation;
                }
                String uri = "http://maps.google.com/maps?f=d&hl=en&saddr=" + location.getLatitude() + "," + location.getLongitude()
                        + "&daddr=" + model.geometry.location.lat + "," + model.geometry.location.lng;
                Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(uri));
                startActivity(intent);
            }
        });
        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .addApi(Places.GEO_DATA_API)
                    .addApi(Places.PLACE_DETECTION_API)
                    .build();
            mGoogleApiClient.connect();
        }

        // Initializing Drawer Layout and ActionBarToggle
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.app_name, R.string.app_name) {

            @Override
            public void onDrawerClosed(View drawerView) {
                // Code here will be triggered once the drawer closes as we dont want anything to happen so we leave this blank
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                // Code here will be triggered once the drawer open as we dont want anything to happen so we leave this blank

                super.onDrawerOpened(drawerView);
            }
        };

        //Setting the actionbarToggle to drawer layout
        mDrawerLayout.addDrawerListener(actionBarDrawerToggle);

        //calling sync state is necessay or else your hamburger icon wont show up
        actionBarDrawerToggle.syncState();

        mNavView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                onItemSelected(item);
                mDrawerLayout.closeDrawers();
                return true;
            }
        });
    }

    private void onItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.about_us:
                showAboutUs();
                break;
            case R.id.rate_us:
                showRatingDialog(this);
                break;
            case R.id.share:
                shareApp();
                break;
        }
    }

    private void shareApp() {
        try {
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("text/plain");
            i.putExtra(Intent.EXTRA_SUBJECT, "Search my masjid");
            String sAux = "\nLet me recommend you this application\n\n";
            sAux = sAux + "https://play.google.com/store/apps/details?id=com.digitaljalebi.searchmymasjid \n\n";
            i.putExtra(Intent.EXTRA_TEXT, sAux);
            startActivity(Intent.createChooser(i, "Send it to a friend"));
        } catch(Exception e) {
        }
    }

    private void showAboutUs() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle("Digital Jalebi");
        dialogBuilder.setMessage("Digital Jalebi is a young and multidisciplinary team of Interaction Designers, New Media Artists, Programmers, Computer and Electronics Engineers, Animators and Game Designers who work together to create memorable interactive experiences.\n" +
                "We are based in India but cater to our clients throughout the world.\n\n" + "To add your city contact us:\n" +
                " searchmymasjid@gmail.com\n");
        dialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        mAlertDialog = dialogBuilder.create();
        mAlertDialog.show();
    }

    private void checkForLocation() {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest).setAlwaysShow(true);

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());

        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult locationSettingsResult) {

                final Status status = locationSettingsResult.getStatus();
                final LocationSettingsStates LS_state = locationSettingsResult.getLocationSettingsStates();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can initialize location
                        // requests here.

                        mGoogleApiClient.connect();
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied. But could be fixed by showing the user
                        // a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(MapsActivity.this, REQUEST_CHECK_SETTINGS);

                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way to fix the
                        // settings so we won't show the dialog.

                        break;
                }
            }
        });
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                int tag = (int) marker.getTag();
                mCurrentSelected = Integer.valueOf(tag);
                if (mCurrentSelected >= 0) {
                    updateSelectedMasjidData(mData.get(mCurrentSelected));
                }
                return false;
            }
        });
        updateMapLocations(mCurrentLocation);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mSelectedLocation == null) {
            checkForLocation();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (areAllPermissionsGranted()) {
            getUpdatedLocation();
            updateMapLocations(mCurrentLocation);
        }
    }

    private void updateMapLocations(Location location) {
        updateCurrentLocationOnMap(location);
        makeRequestForSearch(location);
    }

    private boolean areAllPermissionsGranted() {
        int fineLocationCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        int approxLocationCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION);
        if (fineLocationCheck != PackageManager.PERMISSION_GRANTED) {
            if (approxLocationCheck != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_CODE);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        PERMISSION_REQUEST_CODE);
            }
        } else if (approxLocationCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    PERMISSION_REQUEST_CODE);
        } else {
            return true;
        }
        return false;
    }

    private void getUpdatedLocation() {
        try {
            mCurrentLocation = FusedLocationApi.getLastLocation(mGoogleApiClient);
            mSelectedLocation = mCurrentLocation;
        } catch (SecurityException e) {
        }
    }

    private void updateCurrentLocationOnMap(Location location) {
        if (location != null) {
            if (mMap != null) {
                mMap.clear();
                BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.mipmap.current_location_marker);
                LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                mCameraPosition = new CameraPosition.Builder()
                        .target(currentLocation)
                        .zoom(15).build();
                mMap.addMarker(new MarkerOptions().position(currentLocation).title(getString(R.string.current_location)).icon(icon).snippet("current location")).setTag(-1);
                //Zoom in and animate the camera.
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(mCameraPosition));
                // make volley request.
            }
        }
    }

    private void updateCameraPosition() {
        if (mMap != null) {
            if (mCurrentLocation != null) {
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(mCameraPosition));
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    protected void onStop() {
        if (!mLocationRequestForResult) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    try {
                        mCurrentLocation = FusedLocationApi.getLastLocation(mGoogleApiClient);
                        updateMapLocations(mCurrentLocation);
                    } catch (SecurityException e) {
                        //handle permission denied case
                    }
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
            default: {
                // don't know what to do
            }
        }
    }

    private void makeRequestForSearch(Location location) {
        if (location == null) {
            return;
        }
        pDialog.setMessage("Loading...");
        pDialog.show();
        String url = "http://ec2-18-220-53-7.us-east-2.compute.amazonaws.com/nearestmosques?location="
                + location.getLatitude() + "," + location.getLongitude() + "&" + "count?=3";
        JsonArrayRequest jsonObjReq = new JsonArrayRequest(Request.Method.GET,
                url, null,
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

    private void updateUi(List<MasjidsModel> data) {
        if (mSelectedLocation != null) {
            mData = data;
            int i = 0;
            NumberFormat formatter = NumberFormat.getInstance(Locale.US);
            formatter.setMaximumFractionDigits(2);
            formatter.setMinimumFractionDigits(2);
            formatter.setRoundingMode(RoundingMode.HALF_UP);
            BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.mipmap.masjid_marker);
            MasjidsModel minDistanceModel = null;
            for (MasjidsModel model : data) {
                LatLng location = new LatLng(model.geometry.location.lat, model.geometry.location.lng);
                mMap.addMarker(new MarkerOptions().position(location).title(model.name).icon(icon).snippet(getNextNamazTiming(model))).setTag(i);
                float[] results = new float[1];
                if (mSelectedLocation != null) {
                    Location.distanceBetween(mSelectedLocation.getLatitude(), mSelectedLocation.getLongitude(),
                            model.geometry.location.lat, model.geometry.location.lng, results);
                } else if (mCurrentLocation != null) {
                    Location.distanceBetween(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude(),
                            model.geometry.location.lat, model.geometry.location.lng, results);
                }
                Location mosqueLocation = new Location("");
                mosqueLocation.setLatitude(model.geometry.location.lat);
                mosqueLocation.setLongitude(model.geometry.location.lng);
                String value = formatter.format(mSelectedLocation.distanceTo(mosqueLocation) / 1000);
                String[] splitValue = value.split(",");
                value = "";
                for (String v : splitValue) {
                    value += v;
                }
                model.distanceFromCurrentLocation = Float.valueOf(value);
                if (i == 0) {
                    minDistanceModel = model;
                    mCurrentSelected = i;
                }
                i++;
            }
            icon = BitmapDescriptorFactory.fromResource(R.mipmap.current_selected_marker);
            if (minDistanceModel != null) {
                updateSelectedMasjidData(minDistanceModel);
                LatLng location = new LatLng(minDistanceModel.geometry.location.lat, minDistanceModel.geometry.location.lng);
                mMap.addMarker(new MarkerOptions().position(location).title(minDistanceModel.name).icon(icon).snippet(getNextNamazTiming(minDistanceModel))).setTag(mCurrentSelected);
            }
        }
    }

    private String getNextNamazTiming(MasjidsModel model) {
        Calendar calendar = Calendar.getInstance();
        int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
        if (model.timings != null && model.timings.length > 0) {
            for (String timing : model.timings) {
                if (!TextUtils.isEmpty(timing)) {
                    try {
                        Date date = SIMPLE_DATE_FORMAT.parse(timing);
                        calendar.setTime(date);
                        int hour = calendar.get(Calendar.HOUR_OF_DAY);
                        if (hour >= hourOfDay) {
                            return timing;
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return getString(R.string.no_timing_available);
    }

    private void updateSelectedMasjidData(MasjidsModel model) {
        mName.setText(model.name);
        String na = "n/a";
        if (model.timings != null) {
            Utils.populateMasjidDisplayTimings(model);
            for (int i=0; i<6; i++) {
                setTimeText(i, model.displayTimings[i], model.amPmArray[i]);
            }
        } else {
            for (int i=0; i<6; i++) {
                setTimeText(i, "n/a", "n/a");
            }
        }
        mDistance.setText(String.format(getString(R.string.distance_value), model.distanceFromCurrentLocation));
    }



    private void setTimeText(int timeSlot, String timing, String ampm) {
        switch (timeSlot) {
            case 0:
                mTime1.setText(timing);
                mAmpm1.setText(ampm);
                break;
            case 1:
                mTime2.setText(timing);
                mAmpm2.setText(ampm);
                break;
            case 2:
                mTime3.setText(timing);
                mAmpm3.setText(ampm);
                break;
            case 3:
                mTime4.setText(timing);
                mAmpm4.setText(ampm);
                break;
            case 4:
                mTime5.setText(timing);
                mAmpm5.setText(ampm);
                break;
            case 5:
                mTime6.setText(timing);
                mAmpm6.setText(ampm);
                break;
        }
    }

    public void onDestroy() {
        if (pDialog != null && pDialog.isShowing()) {
            pDialog.dismiss();
        }
        if (mTimePickerDialog != null && mTimePickerDialog.isShowing()) {
            mTimePickerDialog.dismiss();
        }
        if (mAlertDialog != null && mAlertDialog.isShowing()) {
            mAlertDialog.dismiss();
        }
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        final LocationSettingsStates states = LocationSettingsStates.fromIntent(data);
        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        mGoogleApiClient.disconnect();
                        mGoogleApiClient.connect();
                        mLocationRequestForResult = true;
                        new Handler().postDelayed(new Runnable() {

                            @Override
                            public void run() {
                                mLocationRequestForResult = false;
                                getUpdatedLocation();
                                updateMapLocations(mCurrentLocation);
                            }
                        }, 2000);
                        // All required changes were successfully made
                        //GetUserLocation();//FINALLY YOUR OWN METHOD TO GET YOUR USER LOCATION HERE
                        break;
                    case Activity.RESULT_CANCELED:
                        // The user was asked to change settings, but chose not to
                        Toast.makeText(this, "Location information not available!", Toast.LENGTH_LONG).show();
                        break;
                    default:
                        break;
                }
                break;
            case PLACE_AUTOCOMPLETE_REQUEST_CODE: {
                if (resultCode == RESULT_OK) {
                    Place place = PlaceAutocomplete.getPlace(this, data);
                    mLocationSearch.setText(place.getName());
                    mSelectedLocation = new Location("");
                    mSelectedLocation.setLatitude(place.getLatLng().latitude);
                    mSelectedLocation.setLongitude(place.getLatLng().longitude);
                    updateMapLocations(mSelectedLocation);
                } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                    Status status = PlaceAutocomplete.getStatus(this, data);
                    Log.i(MapsActivity.class.getSimpleName(), status.getStatusMessage());

                } else if (resultCode == RESULT_CANCELED) {
                    // The user canceled the operation.
                }
            }
        }
    }

    private void showRatingDialog(final Activity context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("Rate application")
                .setMessage("If you want to give us feedback, do rate our app.")
                .setPositiveButton("RATE", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (context != null && !context.isFinishing()) {
                            String link = "market://details?id=";
                            try {
                                // play market available
                                context.getPackageManager()
                                        .getPackageInfo("com.digitaljalebi.searchmymasjid", 0);
                                // not available
                            } catch (PackageManager.NameNotFoundException e) {
                                e.printStackTrace();
                                // should use browser
                                link = "https://play.google.com/store/apps/details?id=";
                            }
                            // starts external action
                            context.startActivity(new Intent(Intent.ACTION_VIEW,
                                    Uri.parse(link + context.getPackageName())));
                        }
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("CANCEL", null);
        builder.show();
    }

    public  AlertDialog getAlertDialog(final Context context, final MasjidsModel model) {
        timing = new String[6];
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.alertdialog_layout, null);
        dialogBuilder.setView(dialogView);
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
                model.timings = timing;
                model.saveTimings();
                dialog.dismiss();
                // make a call to udpate the timings
            }
        });
        alertDialog.setTitle(model.name);
        return alertDialog;
    }

    private void showTimePickerDialog(Context context, final View v, final int position) {
        if (mTimePickerDialog != null && mTimePickerDialog.isShowing()) {
            return;
        }
        Calendar mcurrentTime = Calendar.getInstance();
        int hour = mcurrentTime.get(Calendar.HOUR);
        int minute = mcurrentTime.get(Calendar.MINUTE);
        mTimePickerDialog = new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                timing[position] = selectedHour + ":" + (selectedMinute < 10 ? "0" + selectedMinute : selectedMinute);
                ((Button)v).setText((selectedHour <=12 ? selectedHour : selectedHour - 12)
                        + ":" + (selectedMinute < 10 ? "0" + selectedMinute : selectedMinute)+ " " + (selectedHour >=12 ? "PM" : "AM"));
            }
        }, hour, minute, false);//Yes 24 hour time
        mTimePickerDialog.setTitle("Select Time");
        mTimePickerDialog.show();
    }
}
