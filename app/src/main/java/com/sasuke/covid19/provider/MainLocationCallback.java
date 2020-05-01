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

public class MainLocationCallback extends LocationCallback {

	private static boolean isFirstRun;
	private static Location lastLocation;

	static {
		isFirstRun = true;
	}

	private String userDocId;
	private String status;

	public MainLocationCallback(String userDocId, String status) {
		this.userDocId = userDocId;
		this.status = status;
		lastLocation = new Location("callback");
	}

	@Override
	public void onLocationResult(LocationResult locationResult) {
		super.onLocationResult(locationResult);
		Log.d("maps", locationResult.getLastLocation().getLatitude() + "");

		final double longitude = locationResult.getLastLocation().getLongitude();
		final double latitude = locationResult.getLastLocation().getLatitude();

		if (!isFirstRun) {
			float distanceDeltaMeter = locationResult.getLastLocation().distanceTo(lastLocation);
			if (distanceDeltaMeter < 20) {
				return;
			}
		} else {
			isFirstRun = false;
		}

		StatusUtil.Status statusEnum = StatusUtil.Status.valueOf(status);
		String locationCollectionName = statusEnum.equals(StatusUtil.Status.Positive) ?
				Constant.LocationsTable.TABLE_NAME :
				Constant.LOCATIONS_SAFE_TABLE_NAME;


		Map<String, Object> data = new HashMap<>();
		data.put(Constant.LocationsTable.GEOPOINT, new GeoPoint(latitude, longitude));
		data.put(Constant.LocationsTable.GEOHASH, new GeoHash(latitude, longitude).getGeoHashString());
		data.put(Constant.LocationsTable.USER_DOCUMENT_ID, userDocId);
		data.put(Constant.LocationsTable.CREATE_DATE, FieldValue.serverTimestamp());

		FirebaseFirestore db = FirebaseFirestore.getInstance();
		db.collection(locationCollectionName).add(data).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
			@Override
			public void onSuccess(DocumentReference documentReference) {
				lastLocation.setLongitude(longitude);
				lastLocation.setLatitude(latitude);
			}
		});
	}

	@Override
	public void onLocationAvailability(LocationAvailability locationAvailability) {
		super.onLocationAvailability(locationAvailability);
	}
}
