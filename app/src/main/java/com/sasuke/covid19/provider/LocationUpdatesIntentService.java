package com.sasuke.covid19.provider;

import android.app.IntentService;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.android.gms.location.LocationResult;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.sasuke.covid19.util.Constant;
import com.sasuke.covid19.util.LocationUtil;
import com.sasuke.covid19.util.StatusUtil;

import org.imperiumlabs.geofirestore.core.GeoHash;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LocationUpdatesIntentService extends IntentService {
	public static final String ACTION_PROCESS_UPDATES = "com.sasuke.covid19.action.PROCESS_UPDATES";

	private static final String TAG = LocationUpdatesIntentService.class.getSimpleName();
	private static Location lastLocationSaved;

	private String userDocId;
	private String status;

	public LocationUpdatesIntentService() {
		super(TAG);
	}

	@Override
	protected void onHandleIntent(Intent intent) {

		Log.d(TAG, "location service intent is handled");

		if (intent != null) {
			final String action = intent.getAction();
			if (ACTION_PROCESS_UPDATES.equals(action)) {
				Uri uri = intent.getData();
				if (uri != null) {
					status = uri.getQueryParameter(Constant.INTENT_EXTRA_KEY_STATUS);
					userDocId = uri.getQueryParameter(Constant.INTENT_EXTRA_KEY_USER_DOC_ID);

					// Returns locations computed, ordered from oldest to newest.
					LocationResult result = LocationResult.extractResult(intent);
					if (result != null && result.getLocations().size() > 0
							&& !TextUtils.isEmpty(status) && !TextUtils.isEmpty(userDocId)) {

						List<Location> locations = result.getLocations();

						// get newest location
						handleIntentLocation(locations.get(locations.size() - 1));
					}
				}
			}
		}
	}

	private void handleIntentLocation(Location location) {
		if (hasLocationChangedSinceLastUpdate(location)) {
			saveLocationUpdatesResult(location);
		}
	}

	private boolean hasLocationChangedSinceLastUpdate(Location location) {
		boolean hasLocationChanged = false;

		if (lastLocationSaved == null) {
			lastLocationSaved = LocationUtil.getLocationUpdatesResultOnSharedPref(this);
			Log.d(TAG, "location retrieved from shared_pref");
		}

		if (lastLocationSaved != null) {
			float distanceDeltaMeter = location.distanceTo(lastLocationSaved);
			if (distanceDeltaMeter < 20) {
				Log.d(TAG, "location background service is called but returned, distance delta < 20 meter.");
			} else {
				hasLocationChanged = true;
			}
		} else {
			// first time service running, no last location exists.
			Log.d(TAG, "first service run");
			hasLocationChanged = true;
		}

		return hasLocationChanged;
	}

	@Override
	public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
		return super.onStartCommand(intent, flags, startId);
		// return START_STICKY;
	}

	private void saveLocationUpdatesResult(final Location location) {

		final double longitude = location.getLongitude();
		final double latitude = location.getLatitude();

		Map<String, Object> data = new HashMap<>();

		final String locationCollectionName;

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

				if (lastLocationSaved == null) {
					lastLocationSaved = new Location(Constant.INTENT_SERVICE_LOCATION_PROVIDER);
				}

				lastLocationSaved.setLongitude(longitude);
				lastLocationSaved.setLatitude(latitude);

				LocationUtil.setLocationUpdatesResultOnSharedPref(LocationUpdatesIntentService.this, location);

				Log.d(TAG, "msg:locationCallBack location is saved to collection:" + locationCollectionName
						+ " ,latitude:" + latitude + ", longitude:" + longitude);
			}
		});
	}
}
