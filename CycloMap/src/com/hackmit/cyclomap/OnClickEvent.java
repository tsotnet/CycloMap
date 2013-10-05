package com.hackmit.cyclomap;

import android.app.Activity;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class OnClickEvent implements OnMapClickListener {

	private Activity mainActivity;
	public OnClickEvent(MainActivity mainActivity) {
		this.mainActivity = mainActivity;
	}

	@Override
	public void onMapClick(LatLng point) {
		Log.d("Tag: ", "click on " + point.toString());
		MapFragment mMapFragment = (MapFragment)mainActivity
				.getFragmentManager().findFragmentById(R.id.map);
        GoogleMap mMap = mMapFragment.getMap();
        mMap.addMarker(new MarkerOptions().position(point));
	}
}
