package com.fufu.globalpic.dataBase

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import android.view.View

class FavoritesDBHandler constructor(context : Context, dbName : String, version : Int = 1)
    : SQLiteOpenHelper(context, dbName, null, version) {

    companion object {
        const val NAME = "name"

        const val TABLE_NAME = "Favorites"

        const val CREATE_TABLE = "CREATE TABLE $TABLE_NAME (" +
                "$NAME TEXT);"

        const val DROP_TABLE = "DROP TABLE IF EXISTS $TABLE_NAME;"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        Log.d("DEBUG", "onCreate : " + (db != null).toString())
        db?.execSQL(CREATE_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL(DROP_TABLE)
        this.onCreate(db)
    }

    fun addFavorite(name: String) {
        val db : SQLiteDatabase
        val values = ContentValues()

        try {
            db = writableDatabase
            values.put(NAME, name)
            db.insert(TABLE_NAME, null, values)
        } catch (e : SQLException) {
            Log.d("DEBUG", "Get db : " + e.message)
        }
    }

    fun deleteFavorite(name : String) {
        var db : SQLiteDatabase?

        try {
            db = writableDatabase
        } catch (e : SQLException) {
            db = null
            Log.d("DEBUG", "Get db : " + e.message)
        }
        db?.delete(TABLE_NAME, NAME + " = ?", arrayOf(name))
    }

    fun getFavorites() : ArrayList<String> {
        val db : SQLiteDatabase = readableDatabase
        val cursor : Cursor
        val favorites = ArrayList<String>()

        cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME, null)
        while (cursor.moveToNext())
            favorites.add(cursor.getString(0))
        cursor.close()
        return (favorites)
    }
}