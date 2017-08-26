package utils;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

import java.util.ArrayList;


public final class UserAttendance {
    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private UserAttendance() {}

    public static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + UserAttendanceEntry.TABLE_NAME +
            " (" + UserAttendanceEntry._ID + " INTEGER PRIMARY KEY," +
            UserAttendanceEntry.COLUMN_NAME_USER + " INTEGER," +
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
        public static final String COLUMN_NAME_IN_TIME = "in_time";
        public static final String COLUMN_NAME_OUT_TIME = "out_time";
    }

    public static ArrayList<String> getLatestUserAttendance(SQLiteDatabase db, String user_id) {
        ArrayList<String> userAttendanceData = new ArrayList<>();

        Cursor res =  db.rawQuery(
            "select * from " + UserAttendanceEntry.TABLE_NAME +
            " where " + UserAttendanceEntry.COLUMN_NAME_USER + "=" + user_id +
            " ORDER BY "+ UserAttendanceEntry.COLUMN_NAME_IN_TIME + " DESC LIMIT 1",
            null
        );
        res.moveToFirst();
        if(!res.isAfterLast()){
            userAttendanceData.add(res.getString(res.getColumnIndex(UserAttendanceEntry._ID)));
            userAttendanceData.add(res.getString(res.getColumnIndex(UserAttendanceEntry.COLUMN_NAME_IN_TIME)));
            userAttendanceData.add(res.getString(res.getColumnIndex(UserAttendanceEntry.COLUMN_NAME_OUT_TIME)));
        }
        res.close();
        return userAttendanceData;
    }

    public static boolean insertOrUpdateUserAttendance(
            SQLiteDatabase db, String user_id, String in_time, String out_time, Boolean isInsert
    ) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(UserAttendanceEntry.COLUMN_NAME_OUT_TIME, out_time);
        if(isInsert){
            contentValues.put(UserAttendanceEntry.COLUMN_NAME_IN_TIME, in_time);
            contentValues.put(UserAttendanceEntry.COLUMN_NAME_USER, user_id);
            db.insertWithOnConflict(UserAttendanceEntry.TABLE_NAME, null, contentValues,
                    SQLiteDatabase.CONFLICT_REPLACE);
        }
        else{
            db.update(
                    UserAttendanceEntry.TABLE_NAME, contentValues,
                    " " + UserAttendanceEntry.COLUMN_NAME_USER + " =?", new String[]{user_id});
        }
        return true;
    }

    public static ArrayList<ArrayList<String>> getAllUserAttendnces(SQLiteDatabase db) {
        ArrayList<ArrayList<String>> row_list = new ArrayList<>();

        Cursor res =  db.rawQuery(
                "select * from "+ UserAttendanceEntry.TABLE_NAME +
                " ua LEFT JOIN " + User.UserEntry.TABLE_NAME + " u ON u."+User.UserEntry._ID+"=ua."+UserAttendanceEntry.COLUMN_NAME_USER+ " ORDER BY ua."+ UserAttendanceEntry.COLUMN_NAME_IN_TIME + " DESC",
                null
        );
        res.moveToFirst();

        while(!res.isAfterLast()){
            ArrayList<String> column_list = new ArrayList<>();
            column_list.add(res.getString(res.getColumnIndex(UserAttendanceEntry._ID)));
            column_list.add(res.getString(res.getColumnIndex(UserAttendanceEntry.COLUMN_NAME_USER)));
            column_list.add(res.getString(res.getColumnIndex(UserAttendanceEntry.COLUMN_NAME_IN_TIME)));
            column_list.add(res.getString(res.getColumnIndex(UserAttendanceEntry.COLUMN_NAME_OUT_TIME)));
            column_list.add(res.getString(res.getColumnIndex(User.UserEntry.COLUMN_NAME_NAME)));
            row_list.add(column_list);
            res.moveToNext();
        }
        res.close();
        return row_list;
    }

}
