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
import com.sasuke.covid19.util.Constant;
import com.sasuke.covid19.util.StatusUtil;

import org.imperiumlabs.geofirestore.core.GeoHash;

import java.util.HashMap;
import java.util.Map;

import static com.sasuke.covid19.MapsActivity.TAG;

public class MainLocationCallback extends LocationCallback {

	private static boolean isFirstRun;
	private static Location lastLocationSaved;

	static {
		isFirstRun = true;
	}

	private String userDocId;
	private String status;

	public MainLocationCallback(String userDocId, String status) {
		this.userDocId = userDocId;
		this.status = status;
		lastLocationSaved = new Location("callback");
	}

	@Override
	public void onLocationResult(LocationResult locationResult) {
		super.onLocationResult(locationResult);

		Log.d(TAG, "location callback is called");

		final double longitude = locationResult.getLastLocation().getLongitude();
		final double latitude = locationResult.getLastLocation().getLatitude();

		// todo: on destroy store lastLocation on db
		if (!isFirstRun) {
			float distanceDeltaMeter = locationResult.getLastLocation().distanceTo(lastLocationSaved);
			if (distanceDeltaMeter < 20) {
				Log.d(TAG, "locationCallBack is called but returned, distance delta < 20 meter.");
				return;
			}
		} else {
			isFirstRun = false;
		}

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
				lastLocationSaved.setLongitude(longitude);
				lastLocationSaved.setLatitude(latitude);
				Log.d(TAG, "msg:locationCallBack location is saved to collection:" + locationCollectionName
						+ " ,latitude:" + latitude + ", longitude:" + longitude);
			}
		});
	}

	@Override
	public void onLocationAvailability(LocationAvailability locationAvailability) {
		super.onLocationAvailability(locationAvailability);
	}
}
