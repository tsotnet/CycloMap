package com.hackmit.cyclomap;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.test.suitebuilder.TestMethod;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationRequest;
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
                            GooglePlayServicesClient.OnConnectionFailedListener,
                            com.google.android.gms.location.LocationListener {

    private GoogleMap mMap;
    private MapFragment mMapFragment;
    private LinearLayout mLowerLayout;
    private Button mMoveButton;
    private Button mRemoveButton;
    private Marker mSelectedMarker = null;
    private LocationClient mLocationClient;
    private LocationRequest mLocationRequest;
    private ProgressDialog mProgressDialog;
    // Navigation data update frequency
    public static final int UPDATE_INTERVAL_IN_MILLISECONDS = 5;
    // Fastest navigation data update frequency that our app can handle
    public static final int FASTEST_INTERVAL_IN_MILLISECONDS = 1;
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
        mLocationRequest = LocationRequest.create();
        // Use high accuracy
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL_IN_MILLISECONDS);
        
        mMap.getUiSettings().setZoomControlsEnabled(false);
        mMap.setOnMapClickListener(new MyMapClickListener());
        mMap.setOnMarkerClickListener(new MyMarkerClickListener());
        
        for (LatLng point : MarkerPositionList.getMarkers()) {
            createMarker(point, "Red");
        }
        MarkerPositionList.draw(mMap);
        mProgressDialog = createNewProgressDialog();
        mProgressDialog.show();
    }
    
    @Override
    protected void onDestroy() {
        mLocationClient.disconnect();
        super.onDestroy();
    }    
    /**
     * This class catches onclick event on the map and creates new marker
     * if no marker is selected, otherwise it deselects previous select
     */
    private class MyMapClickListener implements OnMapClickListener {
        @Override
        public void onMapClick(LatLng point) {
        	
        	if (TestClass.TEST_MODE) {
//        		Log.d("(10, 10), (20, 20), (30, 30)", Double.valueOf(MarkerPositionList.getAngle(
//        				new LatLng(10, 10),
//        				new LatLng(20, 20),
//        				new LatLng(30, 30))).toString());
//        		
//        		Log.d("(10, 10), (10, 30), (30, 30)", Double.valueOf(MarkerPositionList.getAngle(
//        				new LatLng(10, 10),
//        				new LatLng(10, 30),
//        				new LatLng(30, 30))).toString());
//        		
//        		Log.d("(10, 10), (30, 10), (30, 30)", Double.valueOf(MarkerPositionList.getAngle(
//        				new LatLng(10, 10),
//        				new LatLng(30, 10),
//        				new LatLng(30, 30))).toString());
        		
        		if (TestClass.index < TestClass.points.length) {
        			point = TestClass.points[TestClass.index++];
        		} else {
        			Log.d("dir", MarkerPositionList.getNextDirection(
        					TestClass.currpts[TestClass.currInd++]).toString());
        			Log.d("DONE", "DONE");
        			return;
        		}
        	}
        	
            if (mSelectedMarker != null) {
                reCreateMarker(mSelectedMarker, "Red");
                mLowerLayout.setVisibility(View.INVISIBLE);
            } else {
                // Adding marker in map
                Handler isMarkerAdded = new Handler() {
                	@Override
                	public void handleMessage(Message msg) {
                		super.handleMessage(msg);
                		if (msg.obj != null) {
                		    LatLng point = (LatLng)msg.obj;
                            Log.d("create marker", "success");
                            createMarker(point, "Red");
                            MarkerPositionList.draw(mMap);
                		}
                		if (mProgressDialog != null) {
                		    mProgressDialog.cancel();
                		}
                	};
                };
                MarkerPositionList.addMarker(point, isMarkerAdded);
                mProgressDialog.show();
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
                	Handler markerRemoved = new Handler() {
                		@Override
                		public void handleMessage(Message msg) {
                            if (mProgressDialog != null) {
                                mProgressDialog.cancel();
                            }
                			if (msg.what == 1) { // unsuccessful delete
                				Log.d("remove marker", "UNsuccessfull remove");
                				return ;
                			}
                            new_marker.remove();
                            Log.d("remove marker", "success");
                            mSelectedMarker = null;
                            mLowerLayout.setVisibility(View.INVISIBLE);	
                            MarkerPositionList.draw(mMap);
                		}
                	};
                    mProgressDialog.show();
                	MarkerPositionList.removeMarker(new_marker.getPosition(), markerRemoved);
                }
            });
            mSelectedMarker = new_marker;
            return true;
        }
    }
    
    @Override
    public void onConnected(Bundle connectionHint) {
        mProgressDialog.hide();
        Location location = mLocationClient.getLastLocation();
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(),location.getLongitude()),17.0f));
        mLocationClient.requestLocationUpdates(mLocationRequest, this);
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
    
    private ProgressDialog createNewProgressDialog() {
        ProgressDialog result = new ProgressDialog(MainActivity.this);
        result.setCancelable(false);
        result.setMessage(getString(R.string.progress_dialog_text));
        return result;
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        if (mProgressDialog == null) {
            mProgressDialog = createNewProgressDialog();
        }
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        if (mProgressDialog != null) {
            mProgressDialog.cancel();
            mProgressDialog = null;
        }
    }
    
    @Override
    public void onDisconnected() { }
    
    @Override
    public void onLocationChanged(Location location) {
        Log.d("TAG", ""+location.getLatitude()+" "+location.getLongitude()+" "+location.getSpeed());
    }
    
}
