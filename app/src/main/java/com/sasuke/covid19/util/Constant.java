package com.sasuke.covid19.util;

public class Constant {

	public final static float INVALID_LOCATION_COORDINATE = -300;

	public final static String LOCATION_UPDATES_SERVICE_RESULT_LONGITUDE_PREF_KEY = "_LOCATION_UPDATE_RESULT_LONGITUDE";
	public final static String LOCATION_UPDATES_SERVICE_RESULT_LATITUDE_PREF_KEY = "_LOCATION_UPDATE_RESULT_LATITUDE";

	public static final String CALLBACK_SERVICE_LOCATION_PROVIDER = "Callback";
	public static final String INTENT_SERVICE_LOCATION_PROVIDER = "IntentService";
	public static final String INTENT_EXTRA_KEY_STATUS = "EXTRA_KEY_STATUS";
	public static final String INTENT_EXTRA_KEY_USER_DOC_ID = "EXTRA_KEY_USER_DOC_ID";

	public static final String USER_DOC_ID_PREF_KEY = "_USER_DOC_ID";
	public static final String STATUS_REF_KEY = "STATUS";

	public static final String LOCATIONS_NEG_TABLE_NAME = "locationsNeg";

	public static class UserTable {
		public final static String TABLE_NAME = "users";
		public final static String STATUS = "status";
		public final static String STATUS_MAP = "statusMap";
		public final static String INFECTED = "infected";
		public final static String LAST_STATUS_UPDATE = "lastStatusUpdate";
	}

	public static class LocationsTable {
		public final static String TABLE_NAME = "locations";
		public final static String GEOPOINT = "l";
		public final static String GEOHASH = "g";
		public final static String CREATE_DATE = "createDate";
		public final static String USER_DOCUMENT_ID = "userDocId";
		public final static String IS_COPIED_FROM_LOCATIONS_NEG = "isCopiedFromLocationsNeg";
	}
}
