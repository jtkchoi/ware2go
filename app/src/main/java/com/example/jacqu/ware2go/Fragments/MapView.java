package com.example.jacqu.ware2go.Fragments;

import android.graphics.BitmapFactory;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

import com.example.jacqu.ware2go.MainActivity;
import com.example.jacqu.ware2go.R;
import com.example.jacqu.ware2go.VolleyCallback;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by jacqu on 3/2/2017.
 */

public class MapView extends SupportMapFragment implements GoogleApiClient.ConnectionCallbacks,
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

    private Location location = new Location("");
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

    private void initListeners() {
        getMap().setOnMarkerClickListener(this);
        getMap().setOnMapLongClickListener(this);
        getMap().setOnInfoWindowClickListener( this );
        getMap().setOnMapClickListener(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        initButtons(this.getView().getRootView());
        mGoogleApiClient.connect();

        location.setLatitude(49.2677982);
        location.setLongitude(-123.2564914);
        loc = getMap().addMarker(new MarkerOptions()
                .position(new LatLng(location.getLatitude(), location.getLongitude()))
                .title("My Location")
                .visible(false));
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

        initCamera( location );

    }



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

        MarkerOptions options = new MarkerOptions().position( latLng );
        options.title( getAddressFromLatLng( latLng ) );

        options.icon( BitmapDescriptorFactory.defaultMarker() );
       // getMap().addMarker( options );
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        MarkerOptions options = new MarkerOptions().position( latLng );
        options.title( getAddressFromLatLng( latLng ) );

        options.icon( BitmapDescriptorFactory.fromBitmap(
                BitmapFactory.decodeResource( getResources(),
                        R.mipmap.ic_launcher ) ) );

        //getMap().addMarker( options );
    }

    private String getAddressFromLatLng( LatLng latLng ) {
        Geocoder geocoder = new Geocoder( getActivity() );

        String address = "";
        try {
            address = geocoder
                    .getFromLocation( latLng.latitude, latLng.longitude, 1 )
                    .get( 0 ).getAddressLine( 0 );
        } catch (IOException e ) {
        }

        return address;
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

    private void drawPolygon( LatLng startingLocation ) {
        LatLng point2 = new LatLng( startingLocation.latitude + .001,
                startingLocation.longitude );
        LatLng point3 = new LatLng( startingLocation.latitude,
                startingLocation.longitude + .001 );

        PolygonOptions options = new PolygonOptions();
        options.add( startingLocation, point2, point3 );

        options.fillColor( getResources()
                .getColor( R.color.fill_color ) );
        options.strokeColor( getResources()
                .getColor( R.color.stroke_color ) );
        options.strokeWidth( 10 );

        getMap().addPolygon( options );
    }

    private void drawOverlay( LatLng location, int width, int height ) {
        GroundOverlayOptions options = new GroundOverlayOptions();
        options.position( location, width, height );

        options.image( BitmapDescriptorFactory
                .fromBitmap( BitmapFactory
                        .decodeResource( getResources(),
                                R.mipmap.ic_launcher ) ) );
        getMap().addGroundOverlay( options );
    }

    public void setNormal(){
        curMapTypeIndex = 1;
        getMap().setMapType( MAP_TYPES[curMapTypeIndex] );
    }

    public void setHybrid(){
        curMapTypeIndex = 2;
        getMap().setMapType( MAP_TYPES[curMapTypeIndex] );
    }

    public void setTerrain(){
        curMapTypeIndex = 3;
        getMap().setMapType( MAP_TYPES[curMapTypeIndex] );
    }

    public void setSatellite(){
        curMapTypeIndex = 0;
        getMap().setMapType( MAP_TYPES[curMapTypeIndex] );
    }

    public void initButtons(View view){
        final Button normalButton = (Button) view.findViewById(R.id.normal);

        normalButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setNormal();
            }
        });

        final Button terrainButton = (Button) view.findViewById(R.id.terrain);
        terrainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setTerrain();
            }
        });

        final Button satelliteButton = (Button) view.findViewById(R.id.satellite);
        satelliteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setSatellite();
            }
        });

        final Button hybridButton = (Button) view.findViewById(R.id.hybrid);
        hybridButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setHybrid();
            }
        });

        final ToggleButton showLoc = (ToggleButton) view.findViewById(R.id.showloc);
        showLoc.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    loc.setVisible(true);
                } else {
                    loc.setVisible(false);
                }
            }
        });

        final MainActivity ma = (MainActivity) this.getContext();
        final ToggleButton showAll = (ToggleButton) view.findViewById(R.id.showall);
        showAll.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    ma.get_locations(new VolleyCallback() {
                        @Override
                        public void onSuccessResponse(Object result) {
                            allLoc = (HashMap<LatLng, Integer>) result;
                            for(LatLng temp : allLoc.keySet()){
                                float numPeople = allLoc.get(temp);
                                allMarker.add(getMap().addMarker(new MarkerOptions()
                                        .position(temp)
                                        .title(allLoc.get(temp).toString())
                                        .alpha((float) Math.max(numPeople/50, 1.0))
                                        .visible(false))
                                );
                            }
                            for(Marker l : allMarker)
                                l.setVisible(true);
                        }
                    });
                }
                else {
                            for(Marker l : allMarker)
                                l.setVisible(false);
                    }
                }
            });
    }

}