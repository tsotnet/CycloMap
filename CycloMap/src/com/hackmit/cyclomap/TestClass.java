package com.hackmit.cyclomap;

import com.google.android.gms.maps.model.LatLng;

public class TestClass {
	
	public static boolean TEST_MODE = false;
	
	public static LatLng[] points =
			new LatLng[] {
		new LatLng(41, 40),
		new LatLng(42, 41),
		new LatLng(44, 44),
	};
	
	public static int currInd = 0;
	public static LatLng[] currpts = new LatLng[] {
		new LatLng(40, 40),
		new LatLng(39, 39),
		new LatLng(41, 39),
		new LatLng(41, 40),
		new LatLng(41.5, 40.5)
	};
	
	public static int index = 0;

}
