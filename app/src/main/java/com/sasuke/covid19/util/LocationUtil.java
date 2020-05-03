package com.sasuke.covid19.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.preference.PreferenceManager;

public class LocationUtil {
	private final static String LOCATION_UPDATES_RESULT_LONGITUDE_PREF_KEY = "_LOCATION_UPDATE_RESULT_LONGITUDE";
	private final static String LOCATION_UPDATES_RESULT_LATITUDE_PREF_KEY = "_LOCATION_UPDATE_RESULT_LATITUDE";

	public static void setLocationUpdatesResultOnSharedPref(Context context, Location location) {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

		sharedPreferences.edit().putFloat(LOCATION_UPDATES_RESULT_LATITUDE_PREF_KEY, (float) location.getLatitude()).apply();
		sharedPreferences.edit().putFloat(LOCATION_UPDATES_RESULT_LONGITUDE_PREF_KEY, (float) location.getLongitude()).apply();
	}

	public static Location getLocationUpdatesResultOnSharedPref(Context context) {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

		float longitude = sharedPreferences.getFloat(LOCATION_UPDATES_RESULT_LONGITUDE_PREF_KEY, -300);
		float latitude = sharedPreferences.getFloat(LOCATION_UPDATES_RESULT_LATITUDE_PREF_KEY, -300);

		Location location = null;

		if (longitude != -300 && latitude != -300) {
			// valid
			location = new Location(Constant.INTENT_SERVICE_LOCATION_PROVIDER);
			location.setLongitude(longitude);
			location.setLatitude(latitude);
		}

		return location;
	}
}
