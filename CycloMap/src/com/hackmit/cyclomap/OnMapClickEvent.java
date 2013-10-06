package com.hackmit.cyclomap;

import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class OnMapClickEvent implements OnMapClickListener {

	private GoogleMap mMap;
	
	public OnMapClickEvent(GoogleMap map) {
		mMap = map;
	}

	@Override
	public void onMapClick(LatLng point) {
		Log.d("Tag: ", "click on " + point.toString());
        
	}
}
