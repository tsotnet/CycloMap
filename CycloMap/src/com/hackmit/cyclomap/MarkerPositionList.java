package com.hackmit.cyclomap;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Color;
import android.location.Location;
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
		Log.d("marker count: ", markerIndices.toString());
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
	
//	public enum DIRECTION {
//		LEFT, RIGHT, STRAIGHT, WRONG_WAY, DONE
//	};
	
	private static final int LEFT = -1;
	private static final int RIGHT = 1;
	private static final int STRAIGHT = 0;
	private static final int WRONG_WAY = 2;
	private static final int DONE = 3;
	
	private static final double THRESHOLD = 20.0;
	private static final double ACC = 3.0;
	private static Double prevDist = null;
	private static LatLng prevPt = null;
	public static int getNextDirection(LatLng currentPoint) {
		if (points.size() == 0) {
			prevPt = currentPoint;
			return DONE;
		}
		LatLng nextPt = points.get(0);
		double currDist = distance(currentPoint, nextPt);
		if (prevDist != null && prevDist < currDist + distance(prevPt, currentPoint) - ACC) {
			prevDist = currDist;
			prevPt = currentPoint;
			return WRONG_WAY;
		}
		prevPt = currentPoint;
		Log.d("currDist: ", String.format("%f", currDist));
		Log.d("prevDist: ", String.format("%f", prevDist));
		if (currDist < THRESHOLD && prevDist != null) {
			points.remove(0);
			prevDist = null;
			if (markerIndices.get(0) == 0) {
				if (markersCount() == 1) {
					markerIndices.clear();
					points.clear();
				} else {
					int remove = markerIndices.get(1);
					points = points.subList(remove - 1, points.size());
					markerIndices.remove(0);
					for (int i = 0; i < markerIndices.size(); ++i) {
						markerIndices.set(i, markerIndices.get(i) - remove);
					}
				}
			}
			double angle = getAngle(currentPoint, nextPt, points.get(0));
			if (Math.abs(angle) < Math.PI / 8) { // 22.5 degrees
				return STRAIGHT;
			} else {
				if (angle < 0) {
					return RIGHT;
				} else {
					return LEFT;
				}
			}
		} else {
			prevDist = currDist;
			return STRAIGHT;
		}
	}
	
	static double getAngle(LatLng currentPoint, LatLng nextPt,
			LatLng next2) {
		double currToNext = Math.atan2(
									   nextPt.latitude - currentPoint.latitude,
									   nextPt.longitude - currentPoint.longitude);
		
		double nextToNext2 = Math.atan2(
										next2.latitude - nextPt.latitude,
										next2.longitude - nextPt.longitude);
		
		Log.d("currentPt: ", currentPoint.toString());
		Log.d("nextPt: ", nextPt.toString());
		Log.d("angle: ", Double.valueOf(nextToNext2 - currToNext).toString());
//		Log.d("nextToNext2, currToNext", String.format("%f, %f", nextToNext2, currToNext));
		return nextToNext2 - currToNext;
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
	
	private static double distance(LatLng a, LatLng b) {
		float[] res = new float[1];
		Location.distanceBetween(a.latitude, a.longitude, b.latitude, b.longitude, res);
		return res[0];
	}
}
