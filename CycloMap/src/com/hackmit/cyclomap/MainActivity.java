package com.hackmit.cyclomap;

import android.app.Activity;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends Activity implements
                            GooglePlayServicesClient.ConnectionCallbacks,
                            GooglePlayServicesClient.OnConnectionFailedListener {

    private GoogleMap mMap;
    private MapFragment mMapFragment;
    private LinearLayout mLowerLayout;
    private Button mMoveButton;
    private Button mRemoveButton;
    private Marker mSelectedMarker = null;
    private LocationClient mLocationClient;
    
    /**
     * @param point Marker coordinates
     * @param color Color of the marker (either "Red" or "Blue" otherwise throws IllegalArgumentException)
     */
    private Marker createMarker(LatLng point, String color) {
        float col;
        if (color == "Red") {
            col = BitmapDescriptorFactory.HUE_RED;
        } else if (color == "Blue") {
            col = BitmapDescriptorFactory.HUE_BLUE;
        } else {
            throw new IllegalArgumentException("Color of the marker must be either Blue or Red");
        }
        return mMap.addMarker(new MarkerOptions()
                                  .position(point)
                                  .icon(BitmapDescriptorFactory.defaultMarker(col)));
    }
    
    /** Creates marker from existing marker in the same position,
     * but with different color. Old marker is destroyed.
     * @param marker
     * @param newColor
     * @return New marker
     */
    private Marker reCreateMarker(Marker marker, String newColor) {
        if (marker == null) {
            return null;
        }
        LatLng point = marker.getPosition();
        marker.remove();
        return createMarker(point, newColor);
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mLowerLayout = (LinearLayout)findViewById(R.id.main_lower_layout);
        mMoveButton = (Button)findViewById(R.id.main_marker_move);
        mRemoveButton = (Button)findViewById(R.id.main_marker_remove);
        mMapFragment = (MapFragment)getFragmentManager().findFragmentById(R.id.map);
        mMap = mMapFragment.getMap();
        mMap.setMyLocationEnabled(true);
        if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(this) != ConnectionResult.SUCCESS) {
            Toast.makeText(this, getString(R.string.location_services_unavailable), Toast.LENGTH_LONG).show();
            this.finish();
        }
        mLocationClient = new LocationClient(this, this, this);
        mLocationClient.connect();
        mMap.getUiSettings().setZoomControlsEnabled(false);
        mMap.setOnMapClickListener(new MyMapClickListener());
        mMap.setOnMarkerClickListener(new MyMarkerClickListener());
    }
    
    @Override
    protected void onDestroy() {
        mLocationClient.disconnect();
    }    
    /**
     * This class catches onclick event on the map and creates new marker
     * if no marker is selected, otherwise it deselects previous select
     */
    private class MyMapClickListener implements OnMapClickListener {
        @Override
        public void onMapClick(LatLng point) {
            if (mSelectedMarker != null) {
                reCreateMarker(mSelectedMarker, "Red");
                mLowerLayout.setVisibility(View.INVISIBLE);
            } else {
                // Adding marker in map
                createMarker(point, "Red");
                Polyline.addMarker(point);
                Log.d("TAG", "CREATE MARKER");
            }
            mSelectedMarker = null;
        }
    }
    
    /**
     * This class catches onclick events for marker. It is responsible
     * for showing and hiding lower button for removing marker
     */
    private class MyMarkerClickListener implements OnMarkerClickListener {
        @Override
        public boolean onMarkerClick(final Marker marker) {
            final Marker new_marker;
            if (mSelectedMarker == null || !marker.getPosition().equals(mSelectedMarker.getPosition())) {
                reCreateMarker(mSelectedMarker, "Red");
                new_marker = reCreateMarker(marker, "Blue");
            } else {
                return true;
            }
            mLowerLayout.setVisibility(View.VISIBLE);
            mRemoveButton.setOnClickListener(new OnClickListener() {
                @Override
                // Event for removing marker from map
                public void onClick(View v) {
                    Polyline.removeMarker(new_marker.getPosition());
                    new_marker.remove();
                    Log.d("TAG", "REMOVE MARKER");
                    mSelectedMarker = null;
                    mLowerLayout.setVisibility(View.INVISIBLE);
                }
            });
            mSelectedMarker = new_marker;
            return true;
        }
    }
    
    @Override
    public void onConnected(Bundle connectionHint) {
        Location location = mLocationClient.getLastLocation();
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(),location.getLongitude()),17.0f));
    }
    
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        if (result.hasResolution()) {
            try {
                result.startResolutionForResult(
                        this,
                        0);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(this, getString(R.string.location_services_unavailable)+" (Error code:"
                                            +result.getErrorCode()+")", Toast.LENGTH_LONG).show();
        }
    }
    
    @Override
    public void onDisconnected() {
        // TODO Auto-generated method stub
        
    }
    
}
