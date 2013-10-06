package com.hackmit.cyclomap;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
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
    private volatile Location mCurrentLocation;
    // Navigation data update frequency
    public static final int UPDATE_INTERVAL_IN_MILLISECONDS = 100;
    // Fastest navigation data update frequency that our app can handle
    public static final int FASTEST_INTERVAL_IN_MILLISECONDS = 50;
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
        mCurrentLocation = location;
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.menu_start:
            startNavigation();
        default:
            return super.onOptionsItemSelected(item);
        }
    }
    
    private Location LatLngToLocation(LatLng x) {
        Location location = new Location("");
        location.setLatitude(x.latitude);
        location.setLongitude(x.longitude);
        return location;
    }
    
    private LatLng LocationToLatLng(Location x) {
        return new LatLng(x.getLatitude(), x.getLongitude());
    }
    
    long tm;
    
    void startNavigation() {
        final Vibrator vibrator = (Vibrator)getSystemService(VIBRATOR_SERVICE);
        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (tm > System.currentTimeMillis()) {
                    return;
                }
                if (msg.what == 1) {
                    tm = System.currentTimeMillis() + 1000;
                    vibrator.vibrate(900);
                } else if (msg.what == -1) {
                    tm = System.currentTimeMillis() + 600;
                    vibrator.vibrate(500);
                }
            }
        };
        new Thread(new Runnable() {
            @Override
            public void run() {
                Location location;
                List<Location> hist_location = new ArrayList<Location>();
                List<LatLng> points = MarkerPositionList.getPoints();
                int cur = 0; // current index in points of our location
                while (true) {
                    try {
                        Thread.sleep(FASTEST_INTERVAL_IN_MILLISECONDS);
                    } catch (InterruptedException e) {
                        return;
                    }
                    location = mCurrentLocation;
                    while(cur < points.size()) {
                        float dist = location.distanceTo(LatLngToLocation(points.get(cur)));
                        Log.d("TAG", "Distance: "+dist);
                        if (dist < 20) {
                            cur++;
                        } else {
                            break;
                        }
                    }
                    if (cur == points.size()) {
                        return; // Finished, yay!
                    }
                    double angle1 = getAngle(location, LatLngToLocation(points.get(cur)));
                    double angle2 = Float.MAX_VALUE;
                    
                    for (int i = hist_location.size()-1; i>=0; i--) {
                        if (location.distanceTo(hist_location.get(i)) > 10) {
                            angle2 = getAngle(location, hist_location.get(i));
                            break;
                        }
                    }
                    if (angle1 != Float.MAX_VALUE && angle2 != Float.MAX_VALUE) {
                        double angle = angle2-angle1;
                        if (angle < -Math.PI) {
                            angle += 2 * Math.PI;
                        }
                        if (angle > Math.PI) {
                            angle -= 2 * Math.PI;
                        }
                        int what;
                        if (Math.abs(angle) < Math.PI * 0.1) {
                            what = 0;
                        } else if (angle > 0) {
                            what = 1;
                        } else {
                            what = -1;
                        }
                        handler.sendEmptyMessage(what);
                    } else {
                        handler.sendEmptyMessage(0);
                    }
                    hist_location.add(location);
                }
            }
        }).start();
    }
    
    private double getAngle(Location a,Location b) {
        double dx = a.getLatitude() - b.getLatitude();
        double dy = a.getLongitude() - b.getLongitude();
        double res = Math.atan2(dy, dx);
        if (Double.isNaN(res)) {
            return Double.MAX_VALUE;
        } else {
            return res;
        }
    }
}
