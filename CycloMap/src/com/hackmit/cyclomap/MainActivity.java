package com.hackmit.cyclomap;

import android.app.Activity;
import android.os.Bundle;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;

public class MainActivity extends Activity {

    private GoogleMap mMap;
    private MapFragment mMapFragment;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mMapFragment = (MapFragment)getFragmentManager().findFragmentById(R.id.map);
        mMap = mMapFragment.getMap();
        mMap.setMyLocationEnabled(true);
        mMap.setOnMapClickListener(new OnClickEvent(this));
    }
}
