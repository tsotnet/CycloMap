package com.hackmit.cyclomap;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

public class MarkerPositionList {
	
	final static double EPS = (1e-3);
	
	private static List<Integer> markerIndices = new ArrayList<Integer>();
	private static List<LatLng> points = new ArrayList<LatLng>();
	
	public static int markersCount() {
		return markerIndices.size();
	}
	
	public static List<LatLng> getMarkers() {
	    List<LatLng> result = new ArrayList<LatLng>();
	    for(int i : markerIndices) {
	        result.add(points.get(i));
	    }
	    return result;
	}
	
	// Synchronized because navigation needs to access it from background thread
	public synchronized static List<LatLng> getPoints() {
	    return new ArrayList<LatLng>(points);
	}
	
	public static void addMarker(final LatLng point, final Handler isDone) {
		Log.d("polyline", "adding point: " + point);
		if (markersCount() == 0) {
			points.add(point);
			markerIndices.add(0);
			Message msg = new Message();
			msg.obj = point;
			isDone.sendMessage(msg);
		} else {
			Handler listHandler = new Handler() {
				@Override
				public void handleMessage(Message msg) {
					super.handleMessage(msg);
					
					List<LatLng> dir = (List<LatLng>)msg.obj;
					Log.d("dir: ", dir.toString());
					if (dir.isEmpty()) {
			            isDone.sendEmptyMessage(0);
						return ;
					}
					points.addAll(dir.subList(1, dir.size()));
					markerIndices.add(points.size() - 1);
					if (point.latitude != points.get(points.size() - 1).latitude ||
						point.longitude != points.get(points.size() - 1).longitude) {
						Log.d("wrong!", "wrong!");
					}
					Log.d("polyline", "markerIndices: " + markerIndices);
					Log.d("polyline", "points: " + points);
					
					Message message = new Message();
					message.obj = points.get(points.size() - 1);
					Log.d("new point:", point.toString());
					Log.d("last pt: ", points.get(points.size() - 1).toString());
					isDone.sendMessage(message /* good */);
				}
			};
			Directions.getDirections(getLastMarker(), point, listHandler);
		}
	}
	
	public static void removeMarker(LatLng point, final Handler isRemovedSuccessfully) {
		Log.d("polyline", "removing: " + point);
		for (int i = 0; i < markerIndices.size(); ++i) {
			if (pointsEqual(point, points.get(markerIndices.get(i)))) {
				removeMarker(i, isRemovedSuccessfully);
				return;
			}
		}
		
		Log.d("polyline", "point not found");
		isRemovedSuccessfully.sendEmptyMessage(1 /* bad */);
	}
	
	private static Polyline polyline = null;
	public static void draw(GoogleMap mMaps) {
		if (polyline != null) {
			polyline.remove();
		}
		PolylineOptions polylineOptions = new PolylineOptions();
		polylineOptions.addAll(points);
		polylineOptions.width(5);
		polylineOptions.color(Color.RED);
		polyline = mMaps.addPolyline(polylineOptions);
	}
	
	private static void removeMarker(final int index, final Handler isRemovedSuccessfully) {
		if (markersCount() == 1) {
			points.clear();
			markerIndices.clear();
			isRemovedSuccessfully.sendEmptyMessage(0 /* good */);
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
			isRemovedSuccessfully.sendEmptyMessage(0 /* good */);
			return;
		}
		
		if (index == markersCount() - 1) {
			points = points.subList(0, markerIndices.get(index - 1) + 1);
			markerIndices.remove(index);
			isRemovedSuccessfully.sendEmptyMessage(0 /* good */);
			return;
		}

		final int a = markerIndices.get(index - 1);
		final int b = markerIndices.get(index + 1);
		
		final int removed = b - a + 1;
		
		final List<LatLng> newPoints = new ArrayList<LatLng>();
		newPoints.addAll(points.subList(0, a));
		
		LatLng origin = points.get(a);
		LatLng destination = points.get(b);
		
		Handler listHandler = new Handler() {
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				
				List<LatLng> dir = (List<LatLng>)msg.obj;
				int added = dir.size();
				newPoints.addAll(dir);
				
				newPoints.addAll(points.subList(b + 1, points.size()));
				
				for (int i = index + 1; i < markerIndices.size(); ++i) {
					int ind = markerIndices.get(i);
					markerIndices.set(i, ind + added - removed);
				}
				markerIndices.remove(index);
				points = newPoints;
				isRemovedSuccessfully.sendEmptyMessage(0 /* good */);
			};
		};
		
		Directions.getDirections(origin, destination, listHandler);
	}
	
	private static LatLng getLastMarker() {
		return points.get(markerIndices.get(markersCount() - 1));
	}

	private static boolean pointsEqual(LatLng p1, LatLng p2) {
		Log.d("p1: ", p1.toString());
		Log.d("p2: ", p2.toString());
		return Math.abs(p1.latitude - p2.latitude) < EPS &&
				Math.abs(p1.longitude - p2.longitude) < EPS;
	}
}
