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

		Log.d(TAG, "Broadcast received");

		if (action.matches(ConnectivityManager.CONNECTIVITY_ACTION) || action.matches(Intent.ACTION_AIRPLANE_MODE_CHANGED)
				|| action.matches(android.location.LocationManager.PROVIDERS_CHANGED_ACTION)) {

			LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

			boolean isGpsEnabled = false;
			try {
				isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
				Log.d(TAG, "is gps enabled:" + isGpsEnabled);
			} catch (Exception ex) {
			}

			boolean isNetworkEnabled = false;
			try {
				isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
				Log.d(TAG, "is network enabled:" + isNetworkEnabled);
			} catch (Exception ex) {
			}

			boolean isLocationEnabled = isGpsEnabled || isNetworkEnabled;

			Log.d(TAG, "isLocation enabled:" + isLocationEnabled);
			listener.onConnectivityChange(isLocationEnabled);
		}
	}
}
