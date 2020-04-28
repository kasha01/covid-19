package com.sasuke.covid19.util;

public class Constant {

	public static final String USER_DOC_ID_PREF_KEY = "_USER_DOC_ID";
	public static final String STATUS_REF_KEY = "STATUS";

	public final static String RESULT_TO_RECEIVER_DATA_KEY = "RESULT_TO_RECEIVER_DATA_KEY";
	public final static int FAILURE_RESULT = 500;
	public final static int SUCCESS_RESULT = 200;

	public static class UserTable {
		public final static String TABLE_NAME = "users";
		public final static String STATUS = "status";
		public final static String STATUS_MAP = "statusMap";
		public final static String INFECTED = "infected";
		public final static String LAST_STATUS_UPDATE = "lastStatusUpdate";
	}

	public static class LocationsTable{
		public final static String TABLE_NAME = "locations";
		public final static String GEOPOINT = "l";
		public final static String GEOHASH = "g";
		public final static String LAST_STATUS_UPDATE = "lastStatusUpdate";
		public final static String USER_DOCUMENT_ID = "userDocId";

	}
}
