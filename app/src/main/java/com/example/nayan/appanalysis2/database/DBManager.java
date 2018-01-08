package com.example.nayan.appanalysis2.database;

import android.content.ContentValues;
import android.util.Log;

import com.example.nayan.appanalysis2.MainApplication;
import com.example.nayan.appanalysis2.forgroundApp.Utils;
import com.example.nayan.appanalysis2.model.MCalllog;
import com.example.nayan.appanalysis2.model.MImage;
import com.google.gson.Gson;

import net.sqlcipher.Cursor;
import net.sqlcipher.database.SQLiteDatabase;


import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by JEWEL on 7/27/2016.
 */
public class DBManager {
    public static final String TABLE_CALL_LOG = "tbl_call_log";
    public static final String TABLE_SCREENSHOT = "tbl_screenshot";

    private static final String DB_NAME = "screenshot.db";


    private static final String CREATE_TABLE_SCREENSHOT = DBQuery.init()
            .newTable(TABLE_SCREENSHOT)
            .addField("id", DBQuery.INTEGER_PRI_AUTO)
            .addField("imgName", DBQuery.TEXT)
            .getTable();
    private static final String CREATE_TABLE_CALL_LOG = DBQuery.init()
            .newTable(TABLE_CALL_LOG)
            .addField("id", DBQuery.INTEGER_PRI_AUTO)
            .addField("number", DBQuery.TEXT)
            .addField("duration", DBQuery.TEXT)
            .addField("callDate", DBQuery.TEXT)
            .addField("type", DBQuery.TEXT)
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
        db.execSQL(CREATE_TABLE_SCREENSHOT);
        db.execSQL(CREATE_TABLE_CALL_LOG);
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

    public long addData(String tableName, Object dataModelClass, String primaryKey) {
        long result = -1;
        String valueOfKey = "";
        try {
            Class myClass = dataModelClass.getClass();
            Field[] fields = myClass.getDeclaredFields();
            ContentValues contentValues = new ContentValues();

            for (Field field : fields) {
                //for getting access of private field


                String name = field.getName();
                field.setAccessible(true);
                Object value = field.get(dataModelClass);

                if (name.equalsIgnoreCase("serialVersionUID")
                        || name.equalsIgnoreCase("$change")
                        ) {

                } else {
                    contentValues.put(name, value + "");
                    if (name.equalsIgnoreCase(primaryKey)) {

                        valueOfKey = value + "";
                    }

                }


            }
            if (!valueOfKey.equals("") && Integer.parseInt(valueOfKey) < 1)
                contentValues.remove(primaryKey);
            if (!isExist(tableName, primaryKey, valueOfKey)) {
                result = db.insert(tableName, null, contentValues);
                Utils.log("DB", "add:" + valueOfKey);
            } else {

                result = db.update(tableName, contentValues, primaryKey + "=?", new String[]{valueOfKey + ""});
                Utils.log("DB", "update:" + valueOfKey + ":" + dataModelClass.getClass().getSimpleName());
            }


        } catch (Exception e) {
            Utils.log("DB_ERR", e.toString());
        } finally {

        }
        return result;
    }


    public long addData(String tableName, Object dataModelClass, String primaryKey, boolean isAutoInc) {
        long result = -1;
        String valueOfKey = "";
        try {
            Class myClass = dataModelClass.getClass();
            Field[] fields = myClass.getDeclaredFields();
            ContentValues contentValues = new ContentValues();

            for (Field field : fields) {
                //for getting access of private field


                String name = field.getName();
                field.setAccessible(true);
                Object value = field.get(dataModelClass);

                if (name.equalsIgnoreCase("serialVersionUID")
                        || name.equalsIgnoreCase("$change")
                        ) {

                } else {
                    contentValues.put(name, value + "");
                    if (name.equalsIgnoreCase(primaryKey)) {

                        valueOfKey = value + "";
                    }

                }


            }
            if (isAutoInc)
                contentValues.remove(primaryKey);

            if (!isExist(tableName, primaryKey, valueOfKey)) {
                result = db.insert(tableName, null, contentValues);
            } else {

                result = db.update(tableName, contentValues, primaryKey + "=?", new String[]{valueOfKey + ""});
            }


        } catch (Exception e) {
        } finally {

        }
        return result;
    }

    public <T> long addAllData(String tableName, ArrayList<T> dataModelClass, String primaryKey) {
        long result = -1;
        for (Object model : dataModelClass)
            result = addData(tableName, model, primaryKey);
        return result;
    }


    public void addCallLog(MCalllog mImage, String tableName) {

        android.database.Cursor cursor = null;
        try {
            ContentValues values = new ContentValues();
            values.put("type", mImage.getType());
            values.put("number", mImage.getNumber());
            values.put("duration", mImage.getDuration());
            values.put("callDate", mImage.getCallDate());
            String sql = "select * from " + tableName + " where number='" + mImage.getNumber() + "'";
            cursor = db.rawQuery(sql, null);
            Log.e("cu", "has" + cursor);
            if (cursor != null && cursor.getCount() > 0) {
                int update = db.update(tableName, values, "number=?", new String[]{mImage.getNumber()});
                Log.e("calllog", " update : " + update);
            } else {
                long v = db.insert(tableName, null, values);
                Log.e("calllog", " insert : " + v);

            }


        } catch (Exception e) {

        }
        if (cursor != null)
            cursor.close();
    }


    public ArrayList<MCalllog> getCallLog() {
        ArrayList<MCalllog> assetArrayList = new ArrayList<>();
        Log.e("DB", "S1");
        Gson gson = new Gson();
        MCalllog mImage;
        String sql = "select * from " + TABLE_CALL_LOG ;
        android.database.Cursor cursor = db.rawQuery(sql, null);
        if (cursor != null && cursor.moveToFirst()) {
            Log.e("DB", "S2 :" + cursor.getCount());
            do {
                mImage = new MCalllog();
                mImage.setId(cursor.getInt(cursor.getColumnIndex("id")));
                mImage.setNumber(cursor.getString(cursor.getColumnIndex("number")));
                mImage.setCallDate(cursor.getString(cursor.getColumnIndex("callDate")));
                mImage.setDuration(cursor.getString(cursor.getColumnIndex("duration")));
                mImage.setType(cursor.getString(cursor.getColumnIndex("type")));


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


            String sql = "select * from " + TABLE_SCREENSHOT + " where imgName='" + mImage.getImgName() + "'";
            cursor = db.rawQuery(sql, null);
            Log.e("cu", "has" + cursor);
            if (cursor != null && cursor.getCount() > 0) {
                int update = db.update(TABLE_SCREENSHOT, values, "imgName=?", new String[]{mImage.getImgName() + ""});
                Log.e("Image", "image update : " + update);
            } else {
                long v = db.insert(TABLE_SCREENSHOT, null, values);
                id = v;
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
        String sql = "select * from " + TABLE_SCREENSHOT;
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
