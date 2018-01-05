package com.example.nayan.appanalysis2.database;

import android.content.ContentValues;
import android.util.Log;

import com.example.nayan.appanalysis2.MainApplication;
import com.example.nayan.appanalysis2.forgroundApp.Utils;
import com.google.gson.Gson;

import net.sqlcipher.Cursor;
import net.sqlcipher.database.SQLiteDatabase;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by JEWEL on 7/27/2016.
 */
public class DBManager {
    public static final String TABLE_ITEMS = "tbl_items";
    public static final String TABLE_DOWNLOAD = "tbl_download";

    private static final String DB_NAME = "screenshot.db";


    private static final String CREATE_TABLE_DOWNLOAD = DBQuery.init()
            .newTable(TABLE_DOWNLOAD)
            .addField("id", DBQuery.INTEGER_PRI_AUTO)
            .addField("imgName", DBQuery.TEXT)
            .getTable();


    private static DBManager instance;
    private final String TAG = getClass().getSimpleName();
    private SQLiteDatabase db;

    private DBManager() {
        openDB();
        createTable();
    }

    public static DBManager getInstance() {
        if (instance == null)
            instance = new DBManager();
        return instance;
    }

    public static String getQueryDate(String table, String primaryKey) {
        return "select * from " + table + " where " + primaryKey + "='";
    }

    public static String getQueryAll(String table) {
        return "select * from " + table;
    }

    private void openDB() {
        SQLiteDatabase.loadLibs(MainApplication.context);
        File databaseFile = MainApplication.context.getDatabasePath(DB_NAME);
        if (!databaseFile.exists()) {
            databaseFile.mkdirs();
            databaseFile.delete();
        }
        db = SQLiteDatabase.openOrCreateDatabase(databaseFile, Utils.DB_PASS, null);
    }

    private void createTable() {
        db.execSQL(CREATE_TABLE_DOWNLOAD);
    }

    private boolean isExist(String table, String searchField, String value) {
        if (value.equals("") || Integer.valueOf(value) <= 0)
            return false;
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("select * from " + table + " where " + searchField + "='" + value + "'", null);
            if (cursor != null && cursor.getCount() > 0)
                return true;
        } catch (Exception e) {

        } finally {
            if (cursor != null)
                cursor.close();

        }


        return false;
    }


    public void deleteData(String table, String primaryKey, String value) {
        if (!isExist(table, primaryKey, value))
            return;

        int r = db.delete(table, primaryKey + "=?", new String[]{value});
    }

    public void deleteData(String table, String value) {


        int r = db.delete(table, value + "=?", new String[]{value});
    }

    public void close() {
        try {
            if (db.isOpen())
                db.close();
        } catch (Exception e) {
        }

    }


    public void addItemsData(MImage mImage, String tableName) {

        android.database.Cursor cursor = null;
        try {
            ContentValues values = new ContentValues();
            values.put("Id", mImage.getId());
            values.put("InApp", mImage.getImgName());
            String sql = "select * from " + tableName + " where Id='" + mImage.getId() + "'";
            cursor = db.rawQuery(sql, null);
            Log.e("cu", "has" + cursor);
            if (cursor != null && cursor.getCount() > 0) {
                int update = db.update(tableName, values, "Id=?", new String[]{mImage.getId() + ""});
                Log.e("sublevel", "sub level update : " + update);
            } else {
                long v = db.insert(tableName, null, values);
                Log.e("sublevel", "sub level insert : " + v);

            }


        } catch (Exception e) {

        }
        if (cursor != null)
            cursor.close();
    }


    public ArrayList<MImage> getItemsData(int id) {
        ArrayList<MImage> assetArrayList = new ArrayList<>();
        Log.e("DB", "S1");
        Gson gson = new Gson();
        MImage mImage;
        String sql = "select * from " + TABLE_ITEMS + " where CategoryId='" + id + "'";
        android.database.Cursor cursor = db.rawQuery(sql, null);
        if (cursor != null && cursor.moveToFirst()) {
            Log.e("DB", "S2 :" + cursor.getCount());
            do {
                mImage = new MImage();
                mImage.setId(cursor.getInt(cursor.getColumnIndex("Id")));
                mImage.setImgName(cursor.getString(cursor.getColumnIndex("InApp")));


                assetArrayList.add(mImage);

            } while (cursor.moveToNext());

        }
        cursor.close();


        return assetArrayList;
    }


    public long addImage(MImage mImage) {
        long id = 0;
        android.database.Cursor cursor = null;
        try {
            ContentValues values = new ContentValues();
            values.put("imgName", mImage.getImgName());


            String sql = "select * from " + TABLE_DOWNLOAD + " where imgName='" + mImage.getImgName() + "'";
            cursor = db.rawQuery(sql, null);
            Log.e("cu", "has" + cursor);
            if (cursor != null && cursor.getCount() > 0) {
                int update = db.update(TABLE_DOWNLOAD, values, "imgName=?", new String[]{mImage.getImgName() + ""});
                Log.e("Image", "image update : " + update);
            } else {
                long v = db.insert(TABLE_DOWNLOAD, null, values);
                id=v;
                Log.e("Image", "image insert : " + v);

            }


        } catch (Exception e) {

        }
        if (cursor != null)
            cursor.close();
        return id;
    }

    public ArrayList<MImage> getAllImage() {
        ArrayList<MImage> assetArrayList = new ArrayList<>();
        Log.e("DB", "S1");
        Gson gson = new Gson();
        MImage mImage;
        String sql = "select * from " + TABLE_DOWNLOAD;
        android.database.Cursor cursor = db.rawQuery(sql, null);
        if (cursor != null && cursor.moveToFirst()) {
            Log.e("DB", "S2 :" + cursor.getCount());
            do {
                mImage = new MImage();
                mImage.setId(cursor.getInt(cursor.getColumnIndex("id")));
                mImage.setImgName(cursor.getString(cursor.getColumnIndex("imgName")));


                assetArrayList.add(mImage);

            } while (cursor.moveToNext());

        }
        cursor.close();


        return assetArrayList;
    }


}
