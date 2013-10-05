package com.hackmit.cyclomap;

import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.model.Marker;

public class OnMarkerClickEvent implements OnMarkerClickListener {
    @Override
    public boolean onMarkerClick(Marker marker) {
        marker.remove();
        return true;
    }
}