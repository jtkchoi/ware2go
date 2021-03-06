package com.example.jacqu.ware2go.Fragments;

import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.view.View;

import com.example.jacqu.ware2go.MainActivity;
import com.example.jacqu.ware2go.R;
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
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by jacqu on 3/2/2017.
 */

public class JourneyMapView extends SupportMapFragment implements GoogleApiClient.ConnectionCallbacks,
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
    private int curMapTypeIndex = 1;

    private LatLng location;
    private HashMap<LatLng, Integer> allLoc = new HashMap<>();
    private ArrayList<Marker> allMarker = new ArrayList<>();
    private float z = 16f;
    private Marker loc;

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onInfoWindowClick(Marker marker) {

    }

    /*
        Create the map View
     */
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
        List<LatLng> latLngList = ((MainActivity) this.getActivity()).getJourneyLatLng();

        /*
            Render the polyline of GPS coordinates from GPS log
         */
        GoogleMap map = getMap();
        for(int i = 0; i < latLngList.size()-1; i++){
            Polyline line = map.addPolyline(new PolylineOptions()
                    .add(latLngList.get(i), latLngList.get(i+1))
                    .width(5)
                    .color(Color.RED));
        }
    }

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

        location = new LatLng(49.2677982, -123.2564914);

        loc = getMap().addMarker(new MarkerOptions()
                .position(location)
                .title("My Location")
                .visible(false));

        Location l = new Location("");
        l.setLatitude(location.latitude);
        l.setLongitude(location.longitude);
        initCamera(l);

        //When location button pressed, move to current location
        getMap().setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                Location l = new Location("");
                l.setLatitude(location.latitude);
                l.setLongitude(location.longitude);
                initCamera(l);
                return true;
            }
        });
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

        Location l = new Location("");
        l.setLatitude(location.latitude);
        l.setLongitude(location.longitude);
        initCamera( l );

        final MainActivity ma = (MainActivity) this.getActivity();

        getMap().setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                Location l = new Location("");
                location = ma.getCurLocation();
                l.setLatitude(location.latitude);
                l.setLongitude(location.longitude);
                initCamera(l);
                return true;
            }
        });

    }

    /*
        Initialize the view of the Map Fragment
     */
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