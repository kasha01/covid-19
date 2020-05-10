package com.sasuke.covid19;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.VisibleRegion;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.GeoPoint;
import com.sasuke.covid19.provider.ConnectivityChangeListener;
import com.sasuke.covid19.provider.LocationProviderBroadcastReceiver;
import com.sasuke.covid19.provider.MainLocationCallback;
import com.sasuke.covid19.util.Constant;
import com.sasuke.covid19.util.LocationViewModel;
import com.sasuke.covid19.util.PermissionUtil;
import com.sasuke.covid19.util.StatusUtil;

import org.imperiumlabs.geofirestore.GeoFirestore;
import org.imperiumlabs.geofirestore.GeoQuery;
import org.imperiumlabs.geofirestore.listeners.GeoQueryDataEventListener;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.text.NumberFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MapsActivity extends BaseActivity implements OnMapReadyCallback, ConnectivityChangeListener {

	public static final String TAG = "maps_activity";

	private static final int REQUEST_CHECK_SETTINGS = 900;
	private static final long UPDATE_INTERVAL = 3600000;                        // Every 1 hour - 1000 * 40
	private static final long FASTEST_UPDATE_INTERVAL = UPDATE_INTERVAL / 3;    // Every 20 minutes
	/**
	 * The max time before batched results are delivered by location services. Results may be
	 * delivered sooner than this interval.
	 */
	private static final long MAX_WAIT_TIME = UPDATE_INTERVAL * 2;              // Every 2 hours
	private static final int LOCATION_PERMISSION_REQUEST_CODE = 900;
	private static final float MINIMUM_RADIUS_THRESHOLD_KM = 0.5f;
	private static final int ZOOM_LEVEL = 13;
	private static final String IS_USER_DATA_INIT_PREF_KEY = "_IS_USER_DATA_INIT";
	private static final String IS_USE_SEEK_CHECKED_PREF_KEY = "_MENU_ITEM_USE_SEEK";
	private static String status = "";

	private LocationManager locationManager;
	private FloatingActionButton myLocationFab;
	private Drawable locationDisabledDrawable;
	private Drawable locationEnabledDrawable;
	private GoogleMap map;
	private CompoundTextView tracesCountCtv;
	private LocationRequest locationRequest;
	private MainLocationCallback locationCallback;
	private FusedLocationProviderClient fusedLocationClient;
	private LocationViewModel locationViewModel;
	private BroadcastReceiver broadcastReceiver;
	private IntentFilter intentFilter;

	public static String getStatus() {
		return status;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_maps);

		Log.d(TAG, "onCreate called");

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
		locationViewModel = new ViewModelProvider(this).get(LocationViewModel.class);

		locationManager = new LocationManager();
		createLocationRequestOnCreate();

		initUiViews();

		intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
		intentFilter.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);
		broadcastReceiver = new LocationProviderBroadcastReceiver(this);

		String userDocId = initUserData();
		status = getStringPreference(Constant.STATUS_REF_KEY, StatusUtil.Status.NotTested.toString());

		Location lastLocationSavedPref = getLastLocationSavedPref();
		locationCallback = new MainLocationCallback(userDocId, lastLocationSavedPref);
		fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

		SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
				.findFragmentById(R.id.map);
		mapFragment.getMapAsync(this);
	}

	private void initUiViews() {
		locationEnabledDrawable = getDrawable(R.drawable.ic_my_location_blue_24dp);
		locationDisabledDrawable = getDrawable(R.drawable.ic_location_disabled_blue_24dp);
		myLocationFab = findViewById(R.id.maps_fab_my_location);

		tracesCountCtv = findViewById(R.id.maps_ctv_traces_count);
		tracesCountCtv.setSecondaryText("traces count");
		tracesCountCtv.setPrimaryText("-");
		tracesCountCtv.setSecondaryFontSize(12);
		tracesCountCtv.setPrimaryColor(ContextCompat.getColor(this, R.color.text_on_secondary));
	}

	private String initUserData() {
		boolean isUserDataInit = getBoolPreference(IS_USER_DATA_INIT_PREF_KEY, false);

		if (isUserDataInit) {
			return getStringPreference(Constant.USER_DOC_ID_PREF_KEY, "");
		}

		// set preference db
		setStringPreference(Constant.STATUS_REF_KEY, StatusUtil.Status.NotTested.toString());

		final DocumentReference userDocument = db.collection(Constant.UserTable.TABLE_NAME).document();
		final String userDocId = userDocument.getId();

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
				setStringPreference(Constant.USER_DOC_ID_PREF_KEY, userDocId);
				setBoolPreference(IS_USER_DATA_INIT_PREF_KEY, true);
				Log.d(TAG, "user " + userDocId + " was initialized");
			}
		});

		return userDocId;
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
				// TODO: remove use seek
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
	protected void onResumeFragments() {
		super.onResumeFragments();
		Log.d(TAG, "on resume fragments");

		this.registerReceiver(broadcastReceiver, intentFilter);

		Boolean isPermissionGranted = locationViewModel.isPermissionGranted();

		// permission not set. indicates app has started/restarted, permission would be handled on mapReady
		if (isPermissionGranted == null) {
			locationViewModel.setPermissionGranted(false);
			return;
		}

		if (!isPermissionGranted) {
			if (isLocationPermitted()) {
				// permission is granted - check location setting to update location state.

				locationViewModel.setPermissionGranted(true);
				buildLocationSettingsRequest();
				Log.d(TAG, "on post resume, permission changed to enable, update viewmodel with the new permission setting.");
			} else {
				// permission is not granted
				locationViewModel.setPermissionGranted(false);
				locationManager.updateLocationState();
				PermissionUtil.PermissionDeniedDialog.newInstance(false).show(getSupportFragmentManager(), "dialog");
			}
		}
	}

	@Override
	protected void onPause() {
		super.onPause();

		unregisterReceiver(broadcastReceiver);

		Location location = locationCallback.getLastLocationSaved();
		if (location != null) {
			setFloatPreference(Constant.LOCATION_UPDATES_SERVICE_RESULT_LONGITUDE_PREF_KEY, (float) location.getLongitude());
			setFloatPreference(Constant.LOCATION_UPDATES_SERVICE_RESULT_LATITUDE_PREF_KEY, (float) location.getLatitude());
			Log.d(TAG, "on pause, lastLocation is saved in shared pref");
		}

		Log.d(TAG, "activity paused");
	}

	@Override
	public void onMapReady(GoogleMap googleMap) {
		Log.d(TAG, "on map ready");

		map = googleMap;

		// set map listeners
		if (map != null) {
			map.getUiSettings().setMyLocationButtonEnabled(false);

			myLocationFab.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					locationManager.moveCameraToCurrentLocation();
				}
			});

			map.setOnCircleClickListener(new GoogleMap.OnCircleClickListener() {
				@Override
				public void onCircleClick(Circle circle) {
					map.clear();
				}
			});

			map.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
				@Override
				public void onCameraIdle() {
					CameraPosition cameraPosition = map.getCameraPosition();
					double latitude = cameraPosition.target.latitude;
					double longitude = cameraPosition.target.longitude;

					Log.d(TAG, "msg:camera moved, longitude:" + longitude + ", latitude:" + latitude);

					Location center = new Location("center");
					center.setLatitude(latitude);
					center.setLongitude(longitude);
					float radius = getRadius(center);
					locationManager.queryLocation(center, radius);
				}
			});

			if (isLocationPermitted()) {
				locationViewModel.setPermissionGranted(true);
				buildLocationSettingsRequest();
			} else {
				locationViewModel.setPermissionGranted(false);
				locationManager.updateLocationState();
				PermissionUtil.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
						Manifest.permission.ACCESS_FINE_LOCATION, false);
			}
		}
	}

	private boolean shouldRecordLocationsUpdates() {
		StatusUtil.Status myStatus = StatusUtil.Status.valueOf(status);
		return !myStatus.equals(StatusUtil.Status.Recovered);
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
			return;
		}

		boolean permissionGranted = false;
		if (PermissionUtil.isPermissionGranted(permissions, grantResults, Manifest.permission.ACCESS_FINE_LOCATION)) {
			permissionGranted = true;
		}

		locationViewModel.setPermissionGranted(permissionGranted);

		buildLocationSettingsRequest();
	}

	private boolean isLocationPermitted() {
		return checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
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

	private void updateUI(int traces, Location location, float radiusKm) {
		int textSize;
		String text = NumberFormat.getInstance().format(traces);

		if (traces < 1000) {
			textSize = 34;
		} else if (traces < 10000) {
			textSize = 28;
		} else if (traces < 100000) {
			textSize = 22;
		} else {
			textSize = 18;
			if (traces >= 1000000) {
				text = "+1 Million";
				textSize = 16;
			}
		}

		tracesCountCtv.setPrimaryFontSize(textSize);
		tracesCountCtv.setPrimaryText(text);

		map.clear();

		if (traces > 0)
			drawCircle(location, radiusKm);
	}

	private void drawCircle(Location location, float radiusKm) {
		float radiusMeter = radiusKm * 1000;

		CircleOptions circleOptions = new CircleOptions()
				.center(new LatLng(location.getLatitude(), location.getLongitude()))
				.radius(radiusMeter) // In meters
				.strokeWidth(6)
				.strokeColor(R.color.blue_700)
				.fillColor(Color.argb(20, 0, 0, 255))
				.clickable(true);

		Circle circle = map.addCircle(circleOptions);
		circle.setClickable(true);
	}

	private void createLocationRequestOnCreate() {
		locationRequest = new LocationRequest();
		locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
		locationRequest.setInterval(UPDATE_INTERVAL);                   // 60 minutes
		locationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL);    // 20 minutes
		locationRequest.setMaxWaitTime(MAX_WAIT_TIME);                  // 2 hours
	}

	private void removeLocationUpdates() {
		Log.d(TAG, "Removing location updates");

		// foreground
		fusedLocationClient.removeLocationUpdates(locationCallback);

		// background
		// fusedLocationClient.removeLocationUpdates(getPendingIntent());
	}

	private Location getLastLocationSavedPref() {
		Location result = null;

		float latitude = getFloatPreference(Constant.LOCATION_UPDATES_SERVICE_RESULT_LATITUDE_PREF_KEY,
				Constant.INVALID_LOCATION_COORDINATE);
		float longitude = getFloatPreference(Constant.LOCATION_UPDATES_SERVICE_RESULT_LONGITUDE_PREF_KEY,
				Constant.INVALID_LOCATION_COORDINATE);

		if (latitude != Constant.INVALID_LOCATION_COORDINATE && longitude != Constant.INVALID_LOCATION_COORDINATE) {
			result = new Location(Constant.CALLBACK_SERVICE_LOCATION_PROVIDER);
			result.setLatitude(latitude);
			result.setLongitude(longitude);
		}

		return result;
	}

	private void buildLocationSettingsRequest() {
		LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
		builder.addLocationRequest(locationRequest);

		Task<LocationSettingsResponse> task = LocationServices.getSettingsClient(this).checkLocationSettings(builder.build());

		task.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
			@Override
			public void onComplete(Task<LocationSettingsResponse> task) {
				try {
					task.getResult(ApiException.class);
					Log.d(TAG, "location setting is enabled");
					locationManager.updateLocationState();
				} catch (ApiException exception) {
					switch (exception.getStatusCode()) {
						case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
							// Location settings are not satisfied. But could be fixed by showing the
							// user a dialog.
							try {
								// Cast to a resolvable exception.
								ResolvableApiException resolvable = (ResolvableApiException) exception;
								// Show the dialog by calling startResolutionForResult(),
								// and check the result in onActivityResult().
								resolvable.startResolutionForResult(MapsActivity.this, REQUEST_CHECK_SETTINGS);
							} catch (IntentSender.SendIntentException e) {
								// Ignore the error.
							} catch (ClassCastException e) {
								// Ignore, should be an impossible error.
							}
							break;
						case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
							// Location settings are not satisfied. However, we have no way to fix the
							// settings so we won't show the dialog.
							break;
					}
				}
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == REQUEST_CHECK_SETTINGS) {
			switch (resultCode) {
				case Activity.RESULT_OK:
					// All required changes were successfully made
					locationManager.moveCameraToCurrentLocation();
					tracesCountCtv.setVisibility(View.VISIBLE);
					break;
				case Activity.RESULT_CANCELED:
					// The user was asked to change settings, but chose not to
					break;
				default:
					break;
			}
		}
	}

	@Override
	public void onConnectivityChange(boolean isEnabled) {
		locationViewModel.setLocationSettingsEnabled(isEnabled);
		locationManager.updateLocationState();
		locationManager.moveCameraToCurrentLocation();
	}

	@SuppressLint("MissingPermission")
	class LocationManager {

		void startLocationUpdatesRequestPermitted() {
			if (!isLocationEnabled()) {
				Log.w(TAG, "location is not enabled, cannot start location update service.");
				return;
			}

			// TODO: comment for testing
			// foreground
			fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, getMainLooper());

			// background
			/*try {
				Log.d(TAG, "Starting location updates service");
				fusedLocationClient.requestLocationUpdates(locationRequest, getPendingIntent());
			} catch (SecurityException e) {
				Log.e(TAG, "starting location updates service failed.", e);
			}*/
		}

		void moveCameraToCurrentLocation() {
			if (!isLocationEnabled()) {
				return;
			}

			fusedLocationClient.getLastLocation()
					.addOnSuccessListener(new OnSuccessListener<Location>() {
						@Override
						public void onSuccess(Location location) {
							// Got last known location. In some rare situations this can be null.
							if (location != null) {
								Log.v(TAG, "accuracy:" + location.getAccuracy());
								LatLng myLocation = new LatLng(location.getLatitude(), location.getLongitude());
								map.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, ZOOM_LEVEL));
							} else {
								Log.w(TAG, "location was null, camera could not be moved");
							}
						}
					});
		}

		void queryLocation(final Location location, final float radiusKm) {
			tracesCountCtv.setPrimaryText("-");

			if (!isLocationEnabled()) {
				Log.w(TAG, "location is not enabled, cannot query locations.");
				return;
			}

			final int traces[] = new int[1];

			final String userDocId = getStringPreference(Constant.USER_DOC_ID_PREF_KEY, "");

			final float radius = Math.max(MINIMUM_RADIUS_THRESHOLD_KM, radiusKm);

			// updateUI(2, location, radius);    // TODO: delete-test

			CollectionReference databaseReference = db.collection(Constant.LocationsTable.TABLE_NAME);
			final GeoFirestore geoFire = new GeoFirestore(databaseReference);
			GeoQuery geoQuery = geoFire.queryAtLocation(new GeoPoint(location.getLatitude(), location.getLongitude()), radius);
			geoQuery.addGeoQueryDataEventListener(new GeoQueryDataEventListener() {
				@Override
				public void onDocumentEntered(DocumentSnapshot documentSnapshot, GeoPoint geoPoint) {
					String snapshotUserDocId = (String) documentSnapshot.get(Constant.LocationsTable.USER_DOCUMENT_ID);
					Timestamp snapshotTimestamp = (Timestamp) documentSnapshot.get(Constant.LocationsTable.CREATE_DATE);

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
					Log.d(TAG, "msg: on GeoQueryReady, tracesCount:" + traces[0] + ", radiusKm:" + radius +
							", longitude:" + location.getLongitude() + ", latitude:" + location.getLatitude());
					updateUI(traces[0], location, radius);
				}

				@Override
				public void onGeoQueryError(Exception e) {
					Log.d(TAG, "query error");
				}
			});
		}

		void updateLocationState() {
			boolean isEnabled = isLocationEnabled();

			Log.d(TAG, "updating location state:" + isEnabled);

			if (isEnabled) {
				myLocationFab.setImageDrawable(locationEnabledDrawable);
				// start Location Updates Request - only record if not recovered (-/+ve and not tested)
				if (shouldRecordLocationsUpdates())
					locationManager.startLocationUpdatesRequestPermitted();
			} else {
				tracesCountCtv.setPrimaryText("-");
				myLocationFab.setImageDrawable(locationDisabledDrawable);
			}

			myLocationFab.setClickable(isEnabled);

			if (map != null) {
				map.setMyLocationEnabled(isEnabled);
			}
		}

		boolean isLocationEnabled() {
			return locationViewModel.isLocationSettingsEnabled() && locationViewModel.isPermissionGranted();
		}
	}
}