package com.hackmit.cyclomap;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

public class Polyline {
	
	final static double EPS = (1e-9);
	
	private static List<Integer> markerIndices = new ArrayList<Integer>();
	private static List<LatLng> points = new ArrayList<LatLng>();
	
	public static int markersCount() {
		return markerIndices.size();
	}
	
	public static void addMarker(LatLng point) {
		Log.d("polyline", "adding point: " + point);
		if (markersCount() == 0) {
			points.add(point);
			markerIndices.add(0);
		} else {
			List<LatLng> dir = Directions.getDirections(getLastMarker(), point);
			if (dir.isEmpty()) {
				return;
			}
			points.addAll(dir.subList(1, dir.size()));
			markerIndices.add(points.size() - 1);
		}
		Log.d("polyline", "markerIndices: " + markerIndices);
		Log.d("polyline", "points: " + points);
	}
	
	public static boolean removeMarker(LatLng point) {
		Log.d("polyline", "removing: " + point);
		for (int i = 0; i < markerIndices.size(); ++i) {
			if (pointsEqual(point, points.get(markerIndices.get(i)))) {
				removeMarker(i);
				
				Log.d("polyline", "markerIndices: " + markerIndices);
				Log.d("polyline", "points: " + points);
				return true;
			}
		}
		
		Log.d("polyline", "point not found");
		return false;
	}
	
	private static void removeMarker(int index) {
		if (markersCount() == 1) {
			points.clear();
			markerIndices.clear();
			return;
		}
		
		if (index == 0) {
			int removed = markerIndices.get(1);
			points = points.subList(removed, points.size());
			markerIndices.remove(0);
			for (int i = 0; i < markerIndices.size(); ++i) {
				int ind = markerIndices.get(i);
				markerIndices.set(i, ind - removed);
			}
			return;
		}
		
		if (index == markersCount() - 1) {
			points = points.subList(0, markerIndices.get(index - 1) + 1);
			markerIndices.remove(index);
			return;
		}

		int a = markerIndices.get(index - 1);
		int b = markerIndices.get(index + 1);
		int removed = b - a + 1;
		
		List<LatLng> newPoints = new ArrayList<LatLng>();
		newPoints.addAll(points.subList(0, a));
		
		LatLng start = points.get(a);
		LatLng end = points.get(b);
		List<LatLng> dir = Directions.getDirections(start, end);
		int added = dir.size();
		newPoints.addAll(dir);
		
		newPoints.addAll(points.subList(b + 1, points.size()));
		
		for (int i = index + 1; i < markerIndices.size(); ++i) {
			int ind = markerIndices.get(i);
			markerIndices.set(i, ind + added - removed);
		}
		markerIndices.remove(index);
		points = newPoints;
	}
	
	private static LatLng getLastMarker() {
		return points.get(markerIndices.get(markersCount() - 1));
	}

	private static boolean pointsEqual(LatLng p1, LatLng p2) {
		return Math.abs(p1.latitude - p2.latitude) < EPS &&
				Math.abs(p1.longitude - p2.longitude) < EPS;
	}
}
