package com.hackmit.cyclomap;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends Activity {

    private GoogleMap mMap;
    private MapFragment mMapFragment;
    private LinearLayout mLowerLayout;
    private Button mMoveButton;
    private Button mRemoveButton;
    private Marker mSelectedMarker = null;
    
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
        mMap.getUiSettings().setZoomControlsEnabled(false);
        mMap.setOnMapClickListener(new OnMapClickListener() {
            @Override
            public void onMapClick(LatLng point) {
                if (mSelectedMarker != null) {
                    reCreateMarker(mSelectedMarker, "Red");
                    mLowerLayout.setVisibility(View.INVISIBLE);
                } else {
                    createMarker(point, "Red");
//                    mMap.addMarker(new MarkerOptions()
//                                       .position(point)
//                                       .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                }
                mSelectedMarker = null;
            }
        });
        mMap.setOnMarkerClickListener(new OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(final Marker marker) {
                final Marker new_marker;
                if (marker != mSelectedMarker) {
                    reCreateMarker(mSelectedMarker, "Red");
                    new_marker = reCreateMarker(marker, "Blue");
                } else {
                    new_marker = marker;
                }
                mLowerLayout.setVisibility(View.VISIBLE);
                mRemoveButton.setOnClickListener(new OnClickListener() {
                    @Override
                    // Event for removing marker from map
                    // TODO(hayk): update list appropriately
                    public void onClick(View v) {
                        new_marker.remove();
                        mSelectedMarker = null;
                        mLowerLayout.setVisibility(View.INVISIBLE);
                    }
                });
                mSelectedMarker = new_marker;
                return true;
            }
        });
    }
    
}
