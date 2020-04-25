package com.sasuke.covid19.util;

import androidx.lifecycle.ViewModel;

public class PermissionViewModel extends ViewModel {
	private Boolean permissionGranted;

	public Boolean isPermissionGranted() {
		return permissionGranted;
	}

	public void setPermissionGranted(boolean permissionGranted) {
		this.permissionGranted = permissionGranted;
	}
}
