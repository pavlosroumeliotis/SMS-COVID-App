package com.unipi.p17112.smscovid;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

public class SQLiteHelper extends SQLiteOpenHelper {

    SQLiteHelper(Context context, String name,
                 SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    //Method for the insertion of data in the database
    public void insertData(int id, String subtitle){
        SQLiteDatabase database = getWritableDatabase();
        String sql = "INSERT INTO MESSAGES VALUES(?, ?)";
        SQLiteStatement statement = database.compileStatement(sql);
        statement.clearBindings();
        statement.bindLong(1, id);
        statement.bindString(2, subtitle);
        statement.executeInsert();
    }

    //Method for the update of data in the database
    public void updateData(int newid, String subtitle, int id){
        SQLiteDatabase database = getWritableDatabase();
        String sql = "UPDATE MESSAGES SET id=?, subtitle=? WHERE id=?";
        SQLiteStatement statement = database.compileStatement(sql);
        statement.bindLong(1, newid);
        statement.bindString(2, subtitle);
        statement.bindDouble(3, id);
        statement.executeUpdateDelete();
    }

    //Method for the deletion of data in the database
    public void deleteData(int id){
        SQLiteDatabase database = getWritableDatabase();
        String sql = "DELETE FROM MESSAGES WHERE id=?";
        SQLiteStatement statement = database.compileStatement(sql);
        statement.clearBindings();
        statement.bindLong(1, id);
        statement.executeUpdateDelete();
    }

    public Cursor getData(String sql){
        SQLiteDatabase database = getReadableDatabase();
        return database.rawQuery(sql,null);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        //Database data for the initialization of the database
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS MESSAGES(id INTEGER PRIMARY KEY, subtitle ΤΕΧΤ)");
        sqLiteDatabase.execSQL("INSERT INTO MESSAGES VALUES(1,'Φαρμακείο/Γιατρός')");
        sqLiteDatabase.execSQL("INSERT INTO MESSAGES VALUES(2,'Κατάστημα αγαθών πρώτης ανάγκης')");
        sqLiteDatabase.execSQL("INSERT INTO MESSAGES VALUES(3,'Δημόσια υπηρεσία/Τράπεζα')");
        sqLiteDatabase.execSQL("INSERT INTO MESSAGES VALUES(4,'Παροχή βοήθειας/Συνοδεία ανηλίκων μαθητών')");
        sqLiteDatabase.execSQL("INSERT INTO MESSAGES VALUES(5,'Τελετή κηδείας/Μετάβαση εν διαστάσει γονέων')");
        sqLiteDatabase.execSQL("INSERT INTO MESSAGES VALUES(6,'Άθληση/Κίνηση με κατοικίδιο ζώο')");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
