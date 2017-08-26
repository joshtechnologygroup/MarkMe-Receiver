package utils;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;


public final class UserAttendance {
    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private UserAttendance() {}

    public static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + UserAttendanceEntry.TABLE_NAME +
            " (" + UserAttendanceEntry._ID + " INTEGER PRIMARY KEY," +
            UserAttendanceEntry.COLUMN_NAME_USER + " TEXT," +
            UserAttendanceEntry.COLUMN_NAME_IN_TIME + " DATETIME," +
            UserAttendanceEntry.COLUMN_NAME_OUT_TIME + " DATETIME," +
            "FOREIGN KEY (" + UserAttendanceEntry.COLUMN_NAME_USER + ") REFERENCES " +
                UserAttendanceEntry.TABLE_NAME + "(" + UserAttendanceEntry._ID + "))";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + UserAttendanceEntry.TABLE_NAME;

    /* Inner class that defines the table contents */
    public static class UserAttendanceEntry implements BaseColumns {
        public static final String TABLE_NAME = "user_attendance";
        public static final String COLUMN_NAME_USER = "user_id";
        public static final String COLUMN_NAME_IN_TIME = "name";
        public static final String COLUMN_NAME_OUT_TIME = "car_num";
    }

    public static boolean insertOrUpdateUserAttendance(
            SQLiteDatabase db, String user_id, String in_time, String out_time
    ) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(UserAttendanceEntry.COLUMN_NAME_USER, user_id);
        contentValues.put(UserAttendanceEntry.COLUMN_NAME_IN_TIME, in_time);
        contentValues.put(UserAttendanceEntry.COLUMN_NAME_OUT_TIME, out_time);
        db.insertWithOnConflict(UserAttendanceEntry.TABLE_NAME, null, contentValues,
                SQLiteDatabase.CONFLICT_REPLACE);
        return true;
    }

}
