package com.sasuke.covid19.provider;

import android.location.Location;
import android.util.Log;

import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.sasuke.covid19.MapsActivity;
import com.sasuke.covid19.util.Constant;
import com.sasuke.covid19.util.StatusUtil;

import org.imperiumlabs.geofirestore.core.GeoHash;

import java.util.HashMap;
import java.util.Map;

public class MainLocationCallback extends LocationCallback {
	private static final String TAG = "location_callback";

	private Location lastLocationSaved;
	private String userDocId;

	public MainLocationCallback(String userDocId, Location lastLocationSavedPref) {
		this.userDocId = userDocId;
		this.lastLocationSaved = lastLocationSavedPref;
	}

	@Override
	public void onLocationResult(LocationResult locationResult) {
		super.onLocationResult(locationResult);

		Log.d(TAG, "location callback is called");

		Location lastLocationResult = locationResult != null ? locationResult.getLastLocation() : null;
		if (lastLocationResult != null && hasLocationChangedSinceLastUpdate(lastLocationResult)) {
			saveLocationUpdatesResult(lastLocationResult);
		}
	}

	@Override
	public void onLocationAvailability(LocationAvailability locationAvailability) {
		super.onLocationAvailability(locationAvailability);
	}

	public Location getLastLocationSaved() {
		return lastLocationSaved;
	}

	private boolean hasLocationChangedSinceLastUpdate(Location lastLocationResult) {
		boolean hasLocationChanged = true;

		if (lastLocationSaved != null) {
			float distanceDeltaMeter = lastLocationResult.distanceTo(lastLocationSaved);
			if (distanceDeltaMeter < 20) {
				hasLocationChanged = false;
				Log.d(TAG, "location callback returned, distance delta < 20 meter.");
			}
		} else {
			// first time service running, no last location exists. Instantiate lastLocationSaved
			lastLocationSaved = new Location(Constant.CALLBACK_SERVICE_LOCATION_PROVIDER);
			Log.d(TAG, "first callback run");
		}

		return hasLocationChanged;
	}

	private void saveLocationUpdatesResult(Location lastLocationResult) {
		final double longitude = lastLocationResult.getLongitude();
		final double latitude = lastLocationResult.getLatitude();

		Map<String, Object> data = new HashMap<>();

		final String locationCollectionName;

		String status = MapsActivity.getStatus();
		StatusUtil.Status statusEnum = StatusUtil.Status.valueOf(status);
		if (statusEnum.equals(StatusUtil.Status.Positive)) {
			locationCollectionName = Constant.LocationsTable.TABLE_NAME;
			data.put(Constant.LocationsTable.IS_COPIED_FROM_LOCATIONS_NEG, false);
		} else {
			locationCollectionName = Constant.LOCATIONS_NEG_TABLE_NAME;
		}

		data.put(Constant.LocationsTable.GEOPOINT, new GeoPoint(latitude, longitude));
		data.put(Constant.LocationsTable.GEOHASH, new GeoHash(latitude, longitude).getGeoHashString());
		data.put(Constant.LocationsTable.USER_DOCUMENT_ID, userDocId);
		data.put(Constant.LocationsTable.CREATE_DATE, FieldValue.serverTimestamp());

		FirebaseFirestore db = FirebaseFirestore.getInstance();
		db.collection(locationCollectionName).add(data).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
			@Override
			public void onSuccess(DocumentReference documentReference) {
				lastLocationSaved.setLongitude(longitude);
				lastLocationSaved.setLatitude(latitude);
				Log.d(TAG, "msg:locationCallBack location is saved to collection:" + locationCollectionName
						+ " ,latitude:" + latitude + ", longitude:" + longitude);
			}
		});
	}
}
