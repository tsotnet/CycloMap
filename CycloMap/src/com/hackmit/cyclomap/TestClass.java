package com.hackmit.cyclomap;

import com.google.android.gms.maps.model.LatLng;

public class TestClass {
	
	public static boolean TEST_MODE = true;
	
	public static LatLng[] points =
			new LatLng[] {
		new LatLng(40, 41),
		new LatLng(41, 42),
		new LatLng(44, 44),
	};
	
	public static int currInd = 0;
	public static LatLng[] currpts = new LatLng[] {
		new LatLng(40, 40),
		new LatLng(39, 39),
		new LatLng(39, 41)
	};
	
	public static int index = 0;

}
