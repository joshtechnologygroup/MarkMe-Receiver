package utils;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;
import android.provider.Settings;

import java.util.ArrayList;


public final class User {
    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private User() {}

    public static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + UserEntry.TABLE_NAME +
            " (" + UserEntry._ID + " INTEGER PRIMARY KEY," +
            UserEntry.COLUMN_NAME_EMPLOYEE_ID + " TEXT," +
            UserEntry.COLUMN_NAME_NAME + " TEXT," +
            UserEntry.COLUMN_NAME_CAR_NUM + " TEXT," +
            UserEntry.COLUMN_NAME_SECRET_CODE + " TEXT," +
            "UNIQUE (" + UserEntry.COLUMN_NAME_EMPLOYEE_ID + ") ON CONFLICT REPLACE)";

    private static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + UserEntry.TABLE_NAME;

    /* Inner class that defines the table contents */
    public static class UserEntry implements BaseColumns {
        public static final String TABLE_NAME = "user";
        public static final String COLUMN_NAME_EMPLOYEE_ID = "employee_id";
        public static final String COLUMN_NAME_NAME = "name";
        public static final String COLUMN_NAME_CAR_NUM = "car_num";
        public static final String COLUMN_NAME_SECRET_CODE = "secret_code";
    }

    public static boolean insertOrUpdateUser(
        SQLiteDatabase db, String employee_id, String name, String car_num, String secret_code
    ) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(UserEntry.COLUMN_NAME_EMPLOYEE_ID, employee_id);
        contentValues.put(UserEntry.COLUMN_NAME_NAME, name);
        contentValues.put(UserEntry.COLUMN_NAME_CAR_NUM, car_num);
        contentValues.put(UserEntry.COLUMN_NAME_SECRET_CODE, secret_code);
        System.out.println(contentValues);
        System.out.println(employee_id + "," + name + ","  + car_num + ","  + secret_code);
        int row_id = (int) db.insertWithOnConflict(UserEntry.TABLE_NAME, null, contentValues,
                SQLiteDatabase.CONFLICT_REPLACE);
//        System.out.println("" + employee_id+", "+row_id);
//        // If new, update it.
//        if(row_id == -1){
//            contentValues.put(UserEntry.COLUMN_NAME_NAME, name);
//            contentValues.put(UserEntry.COLUMN_NAME_CAR_NUM, car_num);
//            contentValues.put(UserEntry.COLUMN_NAME_SECRET_CODE, secret_code);
//            db.update(
//                    UserEntry.TABLE_NAME,
//                    contentValues,
//                    UserEntry.COLUMN_NAME_EMPLOYEE_ID+" =?", new String[]{employee_id}
//            );
//        }
        return true;
    }

    public static int deleteUsersWithIN (
        SQLiteDatabase db, String fieldName, ArrayList<String> values
    ) {
//        String selection = fieldName + " IN ?";
//        return db.delete(
//            UserEntry.TABLE_NAME,
//            selection,
//            new String[]{fieldName, "[" + android.text.TextUtils.join(",", values)+"]"}
//        );
        return db.delete(
                UserEntry.TABLE_NAME,
                UserEntry._ID+" < 200",
                new String[]{}
        );
    }

    public static ArrayList<String> getUser(SQLiteDatabase db, String secretCode) {
        ArrayList<String> userData = new ArrayList<>();

        Cursor res =  db.rawQuery(
            "select * from " + UserEntry.TABLE_NAME +
            " where " + UserEntry.COLUMN_NAME_SECRET_CODE + "=\"" + secretCode + "\"",
            null
        );
        res.moveToFirst();

        if(!res.isAfterLast()){
            userData.add(res.getString(res.getColumnIndex(UserEntry.COLUMN_NAME_EMPLOYEE_ID)));
            userData.add(res.getString(res.getColumnIndex(UserEntry.COLUMN_NAME_NAME)));
            userData.add(res.getString(res.getColumnIndex(UserEntry.COLUMN_NAME_CAR_NUM)));
        }
        res.close();
        return userData;
    }

    public static ArrayList<ArrayList<String>> getAllUsers(SQLiteDatabase db) {
        ArrayList<ArrayList<String>> row_list = new ArrayList<>();

        Cursor res =  db.rawQuery( "select * from "+UserEntry.TABLE_NAME, null );
        res.moveToFirst();

        while(!res.isAfterLast()){
            ArrayList<String> column_list = new ArrayList<>();
            column_list.add(res.getString(res.getColumnIndex(UserEntry.COLUMN_NAME_EMPLOYEE_ID)));
            column_list.add(res.getString(res.getColumnIndex(UserEntry.COLUMN_NAME_NAME)));
            column_list.add(res.getString(res.getColumnIndex(UserEntry.COLUMN_NAME_CAR_NUM)));
            column_list.add(res.getString(res.getColumnIndex(UserEntry.COLUMN_NAME_SECRET_CODE)));
            row_list.add(column_list);
            res.moveToNext();
        }
        res.close();
        return row_list;
    }
}
