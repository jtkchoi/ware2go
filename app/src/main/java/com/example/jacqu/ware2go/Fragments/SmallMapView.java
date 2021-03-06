package com.example.jacqu.ware2go.Fragments;

import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.jacqu.ware2go.ApplicationController;
import com.example.jacqu.ware2go.MainActivity;
import com.example.jacqu.ware2go.R;
import com.example.jacqu.ware2go.VolleyCallback;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONObject;

/**
 * Created by jacqu on 3/2/2017.
 * Creates a small map for the popup in the assistance fragment
 */

public class SmallMapView extends SupportMapFragment implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        GoogleMap.OnInfoWindowClickListener,
        GoogleMap.OnMapLongClickListener,
        GoogleMap.OnMapClickListener,
        GoogleMap.OnMarkerClickListener {

    private GoogleApiClient mGoogleApiClient;
    private Location mCurrentLocation;

    private final int[] MAP_TYPES = { GoogleMap.MAP_TYPE_SATELLITE,
            GoogleMap.MAP_TYPE_NORMAL,
            GoogleMap.MAP_TYPE_HYBRID,
            GoogleMap.MAP_TYPE_TERRAIN,
            GoogleMap.MAP_TYPE_NONE };
    private final LatLng defaultLocation = new LatLng(49.2677982, -123.2564914);

    private int curMapTypeIndex = 1;

    private int user_id;
    private Location userLocation = new Location("");
    private LatLng curLocation = defaultLocation;
    Handler handler = new Handler();
    private float z = 16f;
    private Marker loc = null;

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onInfoWindowClick(Marker marker) {

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setHasOptionsMenu(true);

        mGoogleApiClient = new GoogleApiClient.Builder( getActivity() )
                .addConnectionCallbacks( this )
                .addOnConnectionFailedListener( this )
                .addApi( LocationServices.API )
                .build();

        initListeners();
    }

    //Null all the listeners - we don't need them
    private void initListeners() {
        getMap().setOnMarkerClickListener(this);
        getMap().setOnMapLongClickListener(this);
        getMap().setOnInfoWindowClickListener( this );
        getMap().setOnMapClickListener(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        if( mGoogleApiClient != null && mGoogleApiClient.isConnected() ) {
            mGoogleApiClient.disconnect();
        }
    }



    @Override
    public void onConnected(Bundle bundle) {
        final MainActivity ma = (MainActivity) this.getActivity();
        user_id = ma.getAssistanceUser();
        curLocation = ma.getCurLocation();

        loc = getMap().addMarker(new MarkerOptions()
                .position(curLocation)
                .title("User ID " + user_id)
                .visible(true));

        //Move the map to move to location when clicked
        getMap().setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                Location l = new Location("");
                curLocation = ma.getCurLocation();
                l.setLatitude(curLocation.latitude);
                l.setLongitude(curLocation.longitude);
                initCamera(l);
                return true;
            }
        });

        //if user invalid, just init to default location
        if(user_id == -1){
            curLocation = defaultLocation;
            initCamera(userLocation);

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    resetLoc(ma);
                    handler.postDelayed(this, 2000);
                }
            },2000);

            return;
        }

        //get the user's location from the server, continuously pulling from the server
        getUserLocation(user_id, new VolleyCallback() {
            @Override
            public void onSuccessResponse(Object result) {
                JSONObject r = (JSONObject) result;
                try {
                    double lat = r.getDouble("latitude");
                    double longt = r.getDouble("longitude");
                    drawLocation(new LatLng(lat, longt));
                    userLocation.setLatitude(lat);
                    userLocation.setLongitude(longt);
                }
                catch (Exception JSONException){
                    userLocation.setLatitude(defaultLocation.latitude);
                    userLocation.setLongitude(defaultLocation.longitude);
                }
            }
        });

        initCamera( userLocation );

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                resetLoc(ma);
                handler.postDelayed(this, 2000);
            }
        },2000);
    }

    //Request from server to get user's up-to-date location
    public void getUserLocation(int user, final VolleyCallback callback) {
        String url = "http://192.168.43.72:3000/assistance/" + user + "/location";
        StringRequest getRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            callback.onSuccessResponse(jsonObject);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Error", error.toString());
            }
        });
        ApplicationController.getInstance().addToRequestQueue(getRequest);
    }

    //Move location marker to new position, to avoid drawing multiple markers
    public void drawLocation(LatLng l){
        loc.setTitle("User ID " + user_id);
        loc.setPosition(l);
        loc.setVisible(true);
    }

    //Reset location by pulling data from server again, move to new location if changed
    public void resetLoc(MainActivity ma){
        user_id = ma.getAssistanceUser();
        if(user_id ==  -1)
            return;

        getUserLocation(user_id, new VolleyCallback() {
            @Override
            public void onSuccessResponse(Object result) {
                JSONObject r = (JSONObject) result;
                try {
                    double lat = r.getDouble("latitude");
                    double longt = r.getDouble("longitude");

                    if(lat != userLocation.getLatitude() || longt != userLocation.getLongitude()) {
                        userLocation.setLatitude(lat);
                        userLocation.setLongitude(longt);
                        initCamera(userLocation);
                    }
                    drawLocation(new LatLng(lat, longt));
                }
                catch (Exception JSONException){
                    userLocation.setLatitude(defaultLocation.latitude);
                    userLocation.setLongitude(defaultLocation.longitude);
                }
            }
        });

    }

    //Change camera position to new location
    private void initCamera( Location location ) {
        CameraPosition position = CameraPosition.builder()
                .target( new LatLng( location.getLatitude(),
                        location.getLongitude() ) )
                .zoom( z )
                .bearing( 0.0f )
                .tilt( 0.0f )
                .build();

        getMap().animateCamera( CameraUpdateFactory
                .newCameraPosition( position ), null );

        getMap().setMapType( MAP_TYPES[curMapTypeIndex] );
        getMap().setTrafficEnabled( true );
        getMap().setMyLocationEnabled( true );
        getMap().getUiSettings().setZoomControlsEnabled( true );
        drawCircle(new LatLng( location.getLatitude(),
                location.getLongitude() ));
    }

    @Override
    public void onMapClick(LatLng latLng) {

    }

    @Override
    public void onMapLongClick(LatLng latLng) {

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        marker.showInfoWindow();
        return true;
    }

    //Draw circle around assistance location, remove old circle
    Circle prevCircle = null;
    private void drawCircle( LatLng location ) {
        if (prevCircle != null){
            prevCircle.remove();
        }
        CircleOptions options = new CircleOptions();
        options.center( location );
        //Radius in meters
        options.radius( 30 );
        options.fillColor( 0x00000000 );
        options.strokeColor( getResources()
                .getColor( R.color.stroke_color ) );
        options.strokeWidth( 3 );

        prevCircle = getMap().addCircle(options);
    }
}