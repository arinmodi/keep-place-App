package com.example.keepplace.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import com.example.keepplace.models.PlaceModel

class DatabaseHandler(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "KEEPPLACE"
        private const val TABLE_NAME = "PLACES"
        private const val DATABASE_VERSION = 1

        private const val KEY_ID = "_id"
        private const val KEY_TITLE = "title"
        private const val KEY_DESCRIPTION = "description"
        private const val KEY_DATE = "date"
        private const val KEY_LOCATION = "location"
        private const val KEY_LONGITUDE = "longitude"
        private const val KEY_LATITUDE = "latitude"
        private const val KEY_IMAGE = "image"

    }

    override fun onCreate(db: SQLiteDatabase?) {
        // creating table

        val CREATE_TABLE = ( "CREATE TABLE " + TABLE_NAME + " ("
                        + KEY_ID + " INTEGER PRIMARY KEY,"
                        + KEY_TITLE + " TEXT,"
                        + KEY_DESCRIPTION + " TEXT,"
                        + KEY_DATE + " TEXT,"
                        + KEY_LOCATION + " TEXT,"
                        + KEY_LONGITUDE + " TEXT,"
                        + KEY_LATITUDE + " TEXT,"
                        + KEY_IMAGE + " TEXT)"
                )

        db?.execSQL(CREATE_TABLE);
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db);
    }

    fun addPlace(place : PlaceModel) : Long {
        val db = this.writableDatabase

        val contentvalues = ContentValues();
        contentvalues.put(KEY_TITLE,place.title);
        contentvalues.put(KEY_DESCRIPTION,place.description);
        contentvalues.put(KEY_DATE,place.date);
        contentvalues.put(KEY_LOCATION,place.location);
        contentvalues.put(KEY_LONGITUDE,place.longitude);
        contentvalues.put(KEY_LATITUDE,place.latitude);
        contentvalues.put(KEY_IMAGE,place.image);

        val result = db.insert(TABLE_NAME,null,contentvalues);

        db.close();
        return  result;
    }

    fun updatePlace(place : PlaceModel) : Int {
        val db = this.writableDatabase

        val contentvalues = ContentValues();
        contentvalues.put(KEY_TITLE,place.title);
        contentvalues.put(KEY_DESCRIPTION,place.description);
        contentvalues.put(KEY_DATE,place.date);
        contentvalues.put(KEY_LOCATION,place.location);
        contentvalues.put(KEY_LONGITUDE,place.longitude);
        contentvalues.put(KEY_LATITUDE,place.latitude);
        contentvalues.put(KEY_IMAGE,place.image);

        val successs = db.update(TABLE_NAME,contentvalues, KEY_ID + "=" +place.id,null);

        db.close();
        return successs
    }

    fun deletPlace(place : PlaceModel) : Int{
        val db = this.writableDatabase
        val success = db.delete(TABLE_NAME, KEY_ID+"="+place.id,null);
        db.close()
        return success
    }


    fun getPlaces():ArrayList<PlaceModel>{
        val list = ArrayList<PlaceModel>();
        val query = "SELECT * from $TABLE_NAME"
        val db = this.readableDatabase

        try{
            val cursor : Cursor = db.rawQuery(query,null);
            if(cursor.moveToFirst()){
                do{
                    val place = PlaceModel(
                        cursor.getInt(cursor.getColumnIndex(KEY_ID)),
                        cursor.getString(cursor.getColumnIndex(KEY_TITLE)),
                        cursor.getString(cursor.getColumnIndex(KEY_IMAGE)),
                        cursor.getString(cursor.getColumnIndex(KEY_DESCRIPTION)),
                        cursor.getString(cursor.getColumnIndex(KEY_DATE)),
                        cursor.getString(cursor.getColumnIndex(KEY_LOCATION)),
                        cursor.getDouble(cursor.getColumnIndex(KEY_LATITUDE)),
                        cursor.getDouble(cursor.getColumnIndex(KEY_LONGITUDE))
                    )

                    list.add(place);

                }while(cursor.moveToNext())

                cursor.close()
            }
        }catch (e : SQLiteException){
            db.execSQL(query);
            return ArrayList()
        }

        return list
    }
}