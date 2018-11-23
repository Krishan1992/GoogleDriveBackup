package com.ptg.gdrivebackup.utility;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.ptg.gdrivebackup.model.User;

import java.util.ArrayList;

/**
 * Created by Emizen on 1/23/2018.
 */

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "GDriveBackup.db";
    private static final int DATABASE_VERSION = 1;
    public static final String TABLE_NAME = "users";
    public static final String FULLNAME = "title";
    public static final String EMAIL = "image";
    public static final String PHONE = "subscribe_count";


    private Context _context;

    public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        this._context = context;
    }

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this._context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.beginTransaction();
        try {
            sqLiteDatabase.execSQL("CREATE TABLE " + TABLE_NAME + "(" +
                    FULLNAME + " TEXT, " +
                    EMAIL + " TEXT, " +
                    PHONE + " TEXT)"
            );
            sqLiteDatabase.setTransactionSuccessful();
        } finally {
            sqLiteDatabase.endTransaction();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }


    public void saveUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(FULLNAME, user.getFullName()); // Contact Name
        values.put(EMAIL, user.getEmail()); // Status whether entry is updated on server or not [default its 0]
        values.put(PHONE, user.getPhone());
        db.insert(TABLE_NAME, null, values);
        db.close(); // Closing database connection
    }


    public ArrayList<User> getAllUserList() {
        final ArrayList<User> contactList = new ArrayList<>();


                // Select All Query
                String selectQuery = "SELECT  * FROM " + TABLE_NAME;

                SQLiteDatabase db = DatabaseHelper.this.getWritableDatabase();
                Cursor cursor = db.rawQuery(selectQuery, null);
                // looping through all rows and adding to list
                int i = 1;
                if (cursor.moveToFirst()) {
                    do {
                        User user=new User();
                        user.setFullName(cursor.getString(cursor.getColumnIndex(FULLNAME)));
                        user.setPhone(cursor.getString(cursor.getColumnIndex(PHONE)));
                        user.setEmail(cursor.getString(cursor.getColumnIndex(EMAIL)));
                        contactList.add(user);
                    } while (cursor.moveToNext());
                }
                db.close();


        // return contact list
        return contactList;
    }

}
