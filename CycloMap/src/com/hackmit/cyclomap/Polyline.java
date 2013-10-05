package com.hackmit.cyclomap;

import java.util.ArrayList;
import java.util.List;

import com.google.android.gms.maps.model.LatLng;

public class Polyline {
	
	final static double EPS = (1e-9);
	
	private static List<LatLng> points = new ArrayList<LatLng>();
	
	public static int size() {
		return points.size();
	}
	
	public static void addMarker(LatLng point) {
		points.add(point);
	}
	
	public static LatLng getMarkerLatLng(int i) {
		assertValidIndex(i);
		return points.get(i);
	}
	
	public static void removeMarker(int i) {
		assertValidIndex(i);
		points.remove(i);
	}
	
	public static void removeMarker(LatLng point) {
		removeMarker(getIndex(point));
	}
	
	public static int getIndex(LatLng point) {
		for (int i = 0; i < points.size(); ++i) {
			if (pointsEqual(points.get(i), point)) {
				return i;
			}
		}
		return -1;
	}
	
	private static void assertValidIndex(int i) {
		if (!(0 <= i && i < points.size())) {
			throw new IllegalArgumentException();
		}
	}
	
	private static boolean pointsEqual(LatLng a, LatLng b) {
		return Math.abs(a.latitude - b.latitude) < EPS &&
				Math.abs(a.longitude - b.longitude) < EPS;
	}
}
