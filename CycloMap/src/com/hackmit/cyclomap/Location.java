package com.hackmit.cyclomap;

import com.google.android.gms.maps.model.LatLng;

public class Location {
	
	private static double EPS = (1e-9);
	
	private LatLng point;
	private boolean isMarker;
	
	Location(LatLng point, boolean isMarker) {
		this.point = point;
		this.isMarker = isMarker;
	}
	
	public LatLng getPoint() {
		return point;
	}
	
	public boolean isMarker() {
		return this.isMarker;
	}
	
	boolean equalsTo(LatLng point) {
		return Math.abs(point.latitude - this.point.latitude) < EPS &&
				Math.abs(point.longitude - this.point.longitude) < EPS;
	}
}
