package com.sasuke.covid19;

import android.location.Location;
import android.util.Pair;

public interface LocationDelegate {
	void OnLocationQuery(final Pair<Location, Float> locationRadius);
}
