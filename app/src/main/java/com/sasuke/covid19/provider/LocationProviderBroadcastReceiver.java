package com.sasuke.covid19.provider;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.util.Log;

public class LocationProviderBroadcastReceiver extends BroadcastReceiver {
	private final static String TAG = "broadcast_receiver";

	private ConnectivityChangeListener listener;

	public LocationProviderBroadcastReceiver(Context listener) {
		this.listener = (ConnectivityChangeListener) listener;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();

		if (action.matches(ConnectivityManager.CONNECTIVITY_ACTION) || action.matches(Intent.ACTION_AIRPLANE_MODE_CHANGED)) {
			Log.i(TAG, "Location Providers changed");

			LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
			boolean isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

			listener.onConnectivityChange(isGpsEnabled);
			//MapsActivity.setMyLocationFabDrawable(isGpsEnabled);
		}
	}
}
