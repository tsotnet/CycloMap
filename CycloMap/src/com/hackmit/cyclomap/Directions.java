package com.hackmit.cyclomap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

public class Directions {
	
	public static void getDirections(final LatLng origin, final LatLng destination,
			final Handler listHandler) {

		if (TestClass.TEST_MODE) {
			List<LatLng> dir = new ArrayList<LatLng>();
			dir.add(origin);
			LatLng mid = new LatLng((origin.latitude + 2*destination.latitude) / 3,
					(origin.longitude + destination.longitude) / 2);
			dir.add(mid);
			dir.add(destination);
			Message m = new Message();
			m.obj = dir;
			listHandler.sendMessage(m);
			return;
		} else {
		
			String url = getURL(origin, destination);
			Handler jsonStringHandler = new Handler() {
				@Override
				public void handleMessage(Message msg) {
					super.handleMessage(msg);
					
					String jsonString = (String) msg.obj;
					Message listMessage = new Message();
					if (jsonString == "") {
						listMessage.obj = new ArrayList<LatLng>();
					} else {
						try {
							JSONObject json = new JSONObject(jsonString);
							JSONArray jsonRoutes = (JSONArray)json.get("routes");
							Log.d("jsonRoutes", "calculated");
							JSONObject jsonOverviewPolyline = null;
							for (int i = 0; i < jsonRoutes.length(); ++i) {
								JSONObject item = (JSONObject)jsonRoutes.get(i);
								if (item.has("overview_polyline")) {
									jsonOverviewPolyline = (JSONObject)item.get("overview_polyline");
									break;
								}
							}
							if (jsonOverviewPolyline == null) {
								listMessage.obj = new ArrayList<LatLng>();
							} else {
								String pointsEncoded = jsonOverviewPolyline.getString("points");
								Log.d("polyline_overview", pointsEncoded);
							
								listMessage.obj = decodePoints(pointsEncoded);
							}
						} catch (JSONException e) {
							listMessage.obj = new ArrayList<LatLng>();
							Log.d("JSON parse", "No path found");
						}
					}
					listHandler.sendMessage(listMessage);
				}
			};
			
			getValueFromURL(url, jsonStringHandler);
		}

//		List<LatLng> l = new ArrayList<LatLng>();
//		l.add(origin);
//		l.add(new LatLng(1.0, 2.0));
//		l.add(destination);
//		return l;
	}
	
	private static void getValueFromURL(final String url, final Handler jsonStringHandler) {
		
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				try {
					InputStream is = new URL(url).openStream();
					BufferedReader rd =
							new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
					Message msg = new Message();
					msg.obj = readAll(rd);
					jsonStringHandler.sendMessage(msg);
				} catch (Exception e) {
					Message msg = new Message();
					msg.obj = "";
					jsonStringHandler.sendMessage(msg);
				}
			}
		};

		Thread t = new Thread(runnable);
		t.start();
	}
	
	private static String readAll(Reader reader) throws IOException {
		StringBuilder sb = new StringBuilder();
		int cp;
		//	char[] buffer = new char[100000];
	//	reader.read(buffer);
	//	reader.close();
		while ((cp = reader.read()) != -1) {
			sb.append((char) cp);
		}
//		return new String(buffer);
		return sb.toString();
	}
	
	private static String getURL(LatLng origin, LatLng destination) {
//		http://maps.googleapis.com/maps/api/directions/json?origin=40.000,40.000
		// &destination=41.000,41.0000&sensor=true&mode=bicycle
		return "http://maps.googleapis.com/maps/api/directions/json?origin=" +
				origin.latitude + "," + origin.longitude + "&" +
				"destination=" +
				destination.latitude + "," + destination.longitude +
				"&sensor=true&mode=bicycle";
	}
	
	private static List<LatLng> decodePoints(String encoded) {
        List<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;
 
        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;
 
            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;
 
            LatLng p = new LatLng((((double) lat / 1E5)),
                        (((double) lng / 1E5)));
            poly.add(p);
        }
 
        return poly;
	}
}
