package com.sasuke.covid19.util;

import androidx.lifecycle.ViewModel;

public class LocationViewModel extends ViewModel {
	private Boolean permissionGranted;
	private boolean locationSettingsEnabled;

	public Boolean isPermissionGranted() {
		return permissionGranted;
	}

	public void setPermissionGranted(boolean permissionGranted) {
		this.permissionGranted = permissionGranted;
	}

	public boolean isLocationSettingsEnabled() {
		return locationSettingsEnabled;
	}

	public void setLocationSettingsEnabled(boolean locationSettingsEnabled) {
		this.locationSettingsEnabled = locationSettingsEnabled;
	}
}
