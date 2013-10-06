package com.hackmit.cyclomap;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

public class Directions {
	public static List<LatLng> getDirections(LatLng origin, LatLng destination) {
		
		String uri = getURI(origin, destination);
		String jsonString = getValueFromuri(uri);
		try {
			JSONObject json = new JSONObject(jsonString);
			JSONObject jsonOverviewPolyline = (JSONObject) json.get("overview_polyline");
			String pointsEncoded = jsonOverviewPolyline.getString("points");
			return decodePoints(pointsEncoded);
		} catch (JSONException e) {
			Log.d("JSON parse", "No path found.");
			return new ArrayList<LatLng>();
		}
		
//		List<LatLng> l = new ArrayList<LatLng>();
//		l.add(origin);
//		l.add(new LatLng(1.0, 2.0));
//		l.add(destination);
//		return l;
	}
	
	private static String getValueFromuri(String uri) {
        try {
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(uri);
            ResponseHandler<String> resHandler = new BasicResponseHandler();
            String page = httpClient.execute(httpGet, resHandler);
            return page;
        } catch (Exception ex) {
            ex.printStackTrace();
            return "zero";
        }
	}
	
	private static String getURI(LatLng origin, LatLng destination) {
//		http://maps.googleapis.com/maps/api/directions/json?origin=40.000,40.000
		// &destination=41.000,41.0000&sensor=true&mode=bicycle
		return "https://maps.googleapis.com/maps/api/directions/json?origin=" +
				origin.latitude + "," + origin.longitude + "&" +
				destination.latitude + "," + origin.longitude +
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
