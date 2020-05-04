package com.sasuke.covid19;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

public class BaseActivity extends AppCompatActivity {
	private static final String APP_SHARED_PREFERENCE = "_APP_SHARED_PREFERENCE";

	protected FirebaseFirestore db;
	private SharedPreferences preferences;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		db = FirebaseFirestore.getInstance();
		preferences = getSharedPreferences(APP_SHARED_PREFERENCE, Context.MODE_PRIVATE);
	}

	protected void setStringPreference(String key, String value) {
		SharedPreferences.Editor editor = preferences.edit();
		editor.putString(key, value).apply();
	}

	protected String getStringPreference(String key, String defaultValue) {
		return preferences.getString(key, defaultValue);
	}

	protected void setBoolPreference(String key, boolean value) {
		SharedPreferences.Editor editor = preferences.edit();
		editor.putBoolean(key, value).apply();
	}

	protected boolean getBoolPreference(String key, boolean defaultValue) {
		return preferences.getBoolean(key, defaultValue);
	}

	protected void setFloatPreference(String key, float value) {
		SharedPreferences.Editor editor = preferences.edit();
		editor.putFloat(key, value).apply();
	}

	protected float getFloatPreference(String key, float defaultValue) {
		return preferences.getFloat(key, defaultValue);
	}
}
