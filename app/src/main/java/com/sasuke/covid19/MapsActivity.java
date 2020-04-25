package com.sasuke.covid19;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.VisibleRegion;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.GeoPoint;
import com.sasuke.covid19.provider.MainLocationCallback;
import com.sasuke.covid19.util.Constant;
import com.sasuke.covid19.util.PermissionUtil;
import com.sasuke.covid19.util.PermissionViewModel;
import com.sasuke.covid19.util.StatusUtil;

import org.imperiumlabs.geofirestore.GeoFirestore;
import org.imperiumlabs.geofirestore.GeoQuery;
import org.imperiumlabs.geofirestore.listeners.GeoQueryDataEventListener;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MapsActivity extends BaseActivity implements OnMapReadyCallback, LocationDelegate {

	private static final String[] permissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
			Manifest.permission.ACCESS_COARSE_LOCATION};

	private static final int LOCATION_PERMISSION_REQUEST_CODE = 900;

	private static final String TAG = "maps_activity";
	private static final float MINIMUM_RADIUS_THRESHOLD_KM = 0.5f;
	private static final int ZOOM_LEVEL = 13;
	private static final int PERMISSIONS_REQUEST_LOCATION = 300;
	private static final String IS_USER_DATA_INIT_PREF_KEY = "_IS_USER_DATA_INIT";
	private static final String IS_USE_SEEK_CHECKED_PREF_KEY = "_MENU_ITEM_USE_SEEK";

	private static boolean isCameraMoveByAndroid = true;

	private GoogleMap map;

	private LocationRequest locationRequest;
	private MainLocationCallback locationCallback;
	private FusedLocationProviderClient fusedLocationClient;

	private PermissionViewModel permissionViewModel;

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

		// on create called when app starts or permission is turned off (restarts the app)
		permissionViewModel = new ViewModelProvider(this).get(PermissionViewModel.class);

		String userDocId = initUserData();

		locationRequest = new LocationRequest();
		locationCallback = new MainLocationCallback(userDocId);
		fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

		//attemptToStartLocationUpdatesRequest();
		Log.d(TAG, "create");

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
	@SuppressLint("MissingPermission")
	protected void onResumeFragments() {
		super.onResumeFragments();

		Boolean permission = permissionViewModel.isPermissionGranted();

		// permission not set. indicates app has started/restarted, permission would be handled by enableLocation()
		if (permission == null) {
			permissionViewModel.setPermissionGranted(false);
			return;
		}

		if (!permission) {
			if (isLocationPermitted()) {
				permissionViewModel.setPermissionGranted(true);
				if (map != null) {
					map.setMyLocationEnabled(true);
				}
				Log.d(TAG, "on post resume, permission changed to enable, update viewmodel with the new perm. setting.");
			} else {
				// permission is not granted
				PermissionUtil.PermissionDeniedDialog.newInstance(false).show(getSupportFragmentManager(), "dialog");
			}
		}
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
		map = googleMap;
		Log.d(TAG, "map is called");

		FloatingActionButton myLocationFab = findViewById(R.id.maps_fab_my_location);
		myLocationFab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				//attemptPermisionAtCurrentLocationWithQuery(null);
			}
		});

//		attemptToEnableLocation();
//
//		attemptPermisionAtCurrentLocationWithQuery(this);
//
//		setCameraIdleListener();

		enableLocationWithPermission();
	}

	@SuppressLint("MissingPermission")
	private void enableLocationWithPermission() {
		boolean permissionGranted = false;

		if (isLocationPermitted()) {
			permissionGranted = true;
			if (map != null) {
				map.setMyLocationEnabled(true);
			}
		} else {
			PermissionUtil.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
					Manifest.permission.ACCESS_FINE_LOCATION, false);
		}

		permissionViewModel.setPermissionGranted(permissionGranted);
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
			return;
		}

		boolean permissionGranted = false;
		if (PermissionUtil.isPermissionGranted(permissions, grantResults, Manifest.permission.ACCESS_FINE_LOCATION)) {
			enableLocationWithPermission();
			permissionGranted = true;
		}

		permissionViewModel.setPermissionGranted(permissionGranted);
	}

	@SuppressLint("MissingPermission")
	private void attemptToEnableLocation() {
		if (isLocationPermitted()) {
			map.setMyLocationEnabled(true);
		} else {
			ActivityCompat.requestPermissions(this, permissions, PERMISSIONS_REQUEST_LOCATION);
		}
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
			startLocationUpdatesRequest();
		} else {
			ActivityCompat.requestPermissions(this, permissions, PERMISSIONS_REQUEST_LOCATION);
		}
	}

	private boolean isLocationPermitted() {
		return checkSelfPermission(permissions[0]) == PackageManager.PERMISSION_GRANTED;
	}

	private void startLocationUpdatesRequest() {
		locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
		locationRequest.setInterval(60 * 60 * 1000);    // 60 minutes
		locationRequest.setFastestInterval(60 * 1000);  // 1 minute
		//locationRequest.setInterval(5000);
		//locationRequest.setFastestInterval(5000);

		//fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, getMainLooper());
	}

	private void attemptPermisionAtCurrentLocationWithQuery(LocationDelegate query) {
		if (isLocationPermitted()) {
			queryAtCurrentLocationPermitted(query);
		} else {
			ActivityCompat.requestPermissions(this, permissions, PERMISSIONS_REQUEST_LOCATION);
		}
	}

	@SuppressLint("MissingPermission")
	private void queryAtCurrentLocationPermitted(final LocationDelegate query) {
		fusedLocationClient.getLastLocation()
				.addOnSuccessListener(this, new OnSuccessListener<Location>() {
					@Override
					public void onSuccess(Location location) {
						// Got last known location. In some rare situations this can be null.
						if (location != null) {
							moveCamera(location);
							if (query != null) {
								query.OnLocationQuery(new Pair<>(location, 0f));
							}
						}
					}
				});
	}

	private void moveCamera(Location location) {
		isCameraMoveByAndroid = true;
		LatLng myLocation = new LatLng(location.getLatitude(), location.getLongitude());
		map.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, ZOOM_LEVEL));
	}

	private void setCameraIdleListener() {
		map.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
			@Override
			public void onCameraIdle() {
				if (!isCameraMoveByAndroid) {
					// user has panned on the map not android (auto-center or start-up)
					Pair<Location, Float> pair = getCenterAndRadius();
					attemptPermisionToQueryAtLocation(pair);
				}

				isCameraMoveByAndroid = false;
			}
		});
	}

	private void attemptPermisionToQueryAtLocation(Pair<Location, Float> location) {
		if (isLocationPermitted()) {
			OnLocationQuery(location);
		} else {
			ActivityCompat.requestPermissions(this, permissions, PERMISSIONS_REQUEST_LOCATION);
		}
	}

	private float getRadius(Location center) {
		VisibleRegion region = map.getProjection().getVisibleRegion();
		double longitude = region.latLngBounds.northeast.longitude;
		double latitude = region.latLngBounds.northeast.latitude;

		Location locationCorner = new Location("corner");
		locationCorner.setLatitude(latitude);
		locationCorner.setLongitude(longitude);

		float distanceKm = locationCorner.distanceTo(center) / 1000;
		float radiusKm = distanceKm / 2;

		return radiusKm;
	}

	private Pair<Location, Float> getCenterAndRadius() {
		VisibleRegion region = map.getProjection().getVisibleRegion();
		double longitudeNe = region.latLngBounds.northeast.longitude;
		double latitudeNe = region.latLngBounds.northeast.latitude;

		double longitudeSw = region.latLngBounds.southwest.longitude;
		double latitudeSw = region.latLngBounds.southwest.latitude;

		double longitudeCenter = (longitudeNe + longitudeSw) / 2;
		double latitudeCenter = (latitudeNe + latitudeSw) / 2;

		Location center = new Location("center");
		center.setLatitude(latitudeCenter);
		center.setLongitude(longitudeCenter);

		Location corner = new Location("corner");
		corner.setLatitude(latitudeNe);
		corner.setLongitude(longitudeNe);

		// equivalent to: (distance_meters/1000) / 2 => radius_km
		float radiusKm = center.distanceTo(corner) / 2000;

		return new Pair<>(center, radiusKm);
	}

	private void updateUI(int traces) {
		Log.d(TAG, traces + "");
	}

	@Override
	public void OnLocationQuery(final Pair<Location, Float> locationRadius) {
		final int traces[] = new int[1];

		final String userDocId = getStringPreference(Constant.USER_DOC_ID_PREF_KEY, "");

		Location location = locationRadius.first;
		float radiusKm = locationRadius.second;

		if (radiusKm < MINIMUM_RADIUS_THRESHOLD_KM) {
			// case when i got my location from gps, so radius is not yet calculated.
			radiusKm = Math.min(MINIMUM_RADIUS_THRESHOLD_KM, getRadius(location));
		}

		CollectionReference databaseReference = db.collection(Constant.LocationsTable.TABLE_NAME);
		final GeoFirestore geoFire = new GeoFirestore(databaseReference);
		GeoQuery geoQuery = geoFire.queryAtLocation(new GeoPoint(location.getLatitude(), location.getLongitude()), radiusKm);
		Log.d(TAG, "query location by radius:" + radiusKm);
		Log.v(TAG, "query location at: Latitude=" + location.getLatitude() + " - Longitude:" + location.getLongitude());

		geoQuery.addGeoQueryDataEventListener(new GeoQueryDataEventListener() {
			@Override
			public void onDocumentEntered(DocumentSnapshot documentSnapshot, GeoPoint geoPoint) {
				String snapshotUserDocId = (String) documentSnapshot.get(Constant.LocationsTable.USER_DOCUMENT_ID);
				Timestamp snapshotTimestamp = (Timestamp) documentSnapshot.get(Constant.LocationsTable.LAST_STATUS_UPDATE);

				Date currentDateMinusTwoWeeks = new DateTime(DateTimeZone.UTC).minusDays(14).toDate();

				if (!snapshotUserDocId.equals(userDocId) && snapshotTimestamp.toDate().after(currentDateMinusTwoWeeks)) {
					traces[0]++;
				}
			}

			@Override
			public void onDocumentExited(DocumentSnapshot documentSnapshot) {
			}

			@Override
			public void onDocumentMoved(DocumentSnapshot documentSnapshot, GeoPoint geoPoint) {
			}

			@Override
			public void onDocumentChanged(DocumentSnapshot documentSnapshot, GeoPoint geoPoint) {
			}

			@Override
			public void onGeoQueryReady() {
				Log.d(TAG, "on geo query ready. All initial data has been loaded.");
				updateUI(traces[0]);
			}

			@Override
			public void onGeoQueryError(Exception e) {
				Log.d(TAG, "query error");
			}
		});
	}
}