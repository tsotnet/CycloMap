package com.hackmit.cyclomap;

import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.model.Marker;

public class OnMarkerClickEvent implements OnMarkerClickListener {
    @Override
    public boolean onMarkerClick(Marker marker) {
    	// TODO: ask for confirmation to delete.
    	if (Polyline.removeMarker(marker.getPosition())) {
    		marker.remove();
    		return true;
    	}
    	return false;
    }
}
