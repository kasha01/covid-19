package com.sasuke.covid19;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.sasuke.covid19.provider.MainLocationCallback;
import com.sasuke.covid19.util.Constant;
import com.sasuke.covid19.util.StatusUtil;

import java.util.HashMap;
import java.util.Map;

public class MapsActivity extends BaseActivity implements OnMapReadyCallback {

	private static final String[] permissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
			Manifest.permission.ACCESS_COARSE_LOCATION};

	private static final int PERMISSIONS_REQUEST_LOCATION = 300;
	private static final String IS_USER_DATA_INIT_PREF_KEY = "_IS_USER_DATA_INIT";
	private static final String IS_USE_SEEK_CHECKED_PREF_KEY = "_MENU_ITEM_USE_SEEK";

	private GoogleMap mMap;

	private LocationRequest locationRequest;
	private MainLocationCallback locationCallback;
	private FusedLocationProviderClient fusedLocationClient;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_maps);

		Toolbar toolbar = findViewById(R.id.toolbar);
		toolbar.setTitle(getString(R.string.main_activity_toolbar_title));
		toolbar.setNavigationIcon(R.drawable.ic_person_white_48dp);
		setSupportActionBar(toolbar);
		toolbar.setNavigationOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(MapsActivity.this, UserStatusActivity.class);
				startActivity(intent);
			}
		});

		String userDocId = initUserData();

		locationRequest = new LocationRequest();
		locationCallback = new MainLocationCallback(userDocId);

		attemptToStartLocationUpdatesRequest();

		SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
				.findFragmentById(R.id.map);
		mapFragment.getMapAsync(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_maps_toolbar, menu);

		boolean isChecked = getBoolPreference(IS_USE_SEEK_CHECKED_PREF_KEY, false);
		menu.getItem(0).setChecked(isChecked);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_use_seek:
				boolean check = item.isChecked();
				item.setChecked(!check);

				setBoolPreference(IS_USE_SEEK_CHECKED_PREF_KEY, !check);
				break;

			case R.id.menu_about:
				Intent intent = new Intent(this, AboutActivity.class);
				startActivity(intent);
				break;

			default:
				return super.onOptionsItemSelected(item);
		}

		return true;
	}

	@Override
	protected void onPause() {
		super.onPause();
		// remove location locationRequest or throttle down
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		fusedLocationClient.removeLocationUpdates(locationCallback);
	}

	/**
	 * Manipulates the map once available.
	 * This callback is triggered when the map is ready to be used.
	 * This is where we can add markers or lines, add listeners or move the camera. In this case,
	 * we just add a marker near Sydney, Australia.
	 * If Google Play services is not installed on the device, the user will be prompted to install
	 * it inside the SupportMapFragment. This method will only be triggered once the user has
	 * installed Google Play services and returned to the app.
	 */
	@Override
	public void onMapReady(GoogleMap googleMap) {
		mMap = googleMap;

		// Add a marker in Sydney and move the camera
		LatLng sydney = new LatLng(-34, 151);
		mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
		mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
	}

	private String initUserData() {
		boolean isUserDataInit = getBoolPreference(IS_USER_DATA_INIT_PREF_KEY, false);

		if (isUserDataInit) {
			return getStringPreference(Constant.USER_DOC_ID_PREF_KEY, "");
		}

		final DocumentReference userDocument = db.collection(Constant.UserTable.TABLE_NAME).document();

		Map<String, Object> statusMap = new HashMap<>();
		statusMap.put(StatusUtil.Status.NotTested.toString(), FieldValue.serverTimestamp());

		Map<String, Object> user = new HashMap<>();
		user.put(Constant.UserTable.STATUS, StatusUtil.Status.NotTested.toString());
		user.put(Constant.UserTable.STATUS_MAP, statusMap);
		user.put(Constant.UserTable.INFECTED, false);
		user.put(Constant.UserTable.LAST_STATUS_UPDATE, FieldValue.serverTimestamp());

		userDocument.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
			@Override
			public void onSuccess(Void aVoid) {
				setStringPreference(Constant.USER_DOC_ID_PREF_KEY, userDocument.getId());
				setBoolPreference(IS_USER_DATA_INIT_PREF_KEY, true);
			}
		});

		return userDocument.getId();
	}

	private void attemptToStartLocationUpdatesRequest() {
		if (isLocationPermitted()) {
			ActivityCompat.requestPermissions(this, permissions, PERMISSIONS_REQUEST_LOCATION);
		} else {
			startLocationUpdatesRequest();
		}
	}

	private boolean isLocationPermitted() {
		return checkSelfPermission(permissions[0]) != PackageManager.PERMISSION_GRANTED
				&& checkSelfPermission(permissions[1]) != PackageManager.PERMISSION_GRANTED;
	}

	@SuppressLint("MissingPermission")
	private void startLocationUpdatesRequest() {
		locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
		locationRequest.setInterval(60 * 60 * 1000);    // 60 minutes
		locationRequest.setFastestInterval(60 * 1000);  // 1 minute
		//locationRequest.setInterval(5000);
		//locationRequest.setFastestInterval(5000);

		fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
		fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, getMainLooper());
	}
}