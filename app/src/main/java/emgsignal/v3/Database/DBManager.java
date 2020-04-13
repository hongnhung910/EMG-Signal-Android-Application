package emgsignal.v3.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

public class DBManager extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "EMG DATABASE";
    private static final String TABLE_USER = "users";
    private static final String NAME = "name";
    private static final String BIRTHDAY = "birthday";
    private static final String HEIGHT = "height";
    private static final String WEIGHT = "weight";
    private static final String BODY_RES = "body_resistance";
    private static final String USER_ID = "userID";
    private static final String TABLE_SENSOR = "sensors";
    private static final String TYPE = "type";
    private static final String RES_MID = "middle_electrode";
    private static final String RES_END = "end_electrode";
    private static final String RES_REF = "reference_electrode";
    private static final String SENSOR_ID = "sensorID";
    private static final int VERSION = 2;
    private Context context;
    private String TAG = "Database Management";
    public DBManager(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
        this.context = context;
    }
    private String CREATE_TABLE_USER = "CREATE TABLE " + TABLE_USER + " (" +
            NAME + " TEXT, " +
            BIRTHDAY + " TEXT, " +
            HEIGHT + " TEXT, " +
            WEIGHT + " TEXT, " +
            BODY_RES + " TEXT, " +
            USER_ID + " TEXT) ";
    private String CREATE_TABLE_SENSOR = "CREATE TABLE " + TABLE_SENSOR + " (" +
            TYPE + " TEXT, " +
            RES_MID + " TEXT, " +
            RES_END + " TEXT, " +
            RES_REF + " TEXT, " +
            SENSOR_ID + " TEXT) ";
    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(CREATE_TABLE_USER);
        Log.i(TAG, "onCreate: Created Table User");
        db.execSQL(CREATE_TABLE_SENSOR);
        Log.i(TAG, "onCreate: Created Table Sensor");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(" DROP TABLE IF EXISTS " + TABLE_USER);
        db.execSQL(" DROP TABLE IF EXISTS " + TABLE_SENSOR);
    }

    public void addUser (UserFormat user)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(NAME,user.getName());
        values.put(BIRTHDAY,user.getBirthday());
        values.put(HEIGHT,user.getHeight());
        values.put(WEIGHT,user.getWeight());
        values.put(BODY_RES,user.getBody_res());
        values.put(USER_ID,user.getId());
        db.insert(TABLE_USER,null,values);
        db.close();
        Log.i(TAG, "addUser: " + user.getName() + " ID: " + user.getId());
    }
    public void addSensor (SensorFormat sensor)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TYPE,sensor.getType());
        values.put(RES_MID,sensor.getResMid());
        values.put(RES_END,sensor.getResEnd());
        values.put(RES_REF,sensor.getResRef());
        values.put(SENSOR_ID,sensor.getId());
        db.insert(TABLE_SENSOR,null,values);
        db.close();
        Log.i(TAG, "addSensor: " + sensor.getType() + " ID: " + sensor.getId());
    }

    public ArrayList<UserFormat> getAllUsers() {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_USER, null);

        ArrayList<UserFormat> listUsers = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                UserFormat user = new UserFormat();
                user.setName(cursor.getString(0));
                user.setBirthday(cursor.getString(1));
                user.setHeight(cursor.getString(2));
                user.setWeight(cursor.getString(3));
                user.setBody_res(cursor.getString(4));
                user.setId(cursor.getString(5));

                listUsers.add(user);
            } while (cursor.moveToNext());
        }
        db.close();
        return listUsers;
    }

    public ArrayList<String> getAllUsersName() {
        String[] columns = {NAME};
        String sortOrder = NAME + " ASC";
        ArrayList<String> username = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USER, //Table to query
                columns,    //columns to return
                null,        //columns for the WHERE clause
                null,        //The values for the WHERE clause
                null,       //group the rows
                null,       //filter by row groups
                sortOrder); //The sort order
        if (cursor.moveToFirst()) {
            do {
                String name = cursor.getString(cursor.getColumnIndex(NAME));
                username.add(name);
                // Adding user record to list
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        Log.i(TAG, "getAllUsersName: " + username);
        return username;
    }

    public ArrayList<String> getAllSensorType() {
        String[] columns = {TYPE};
        ArrayList<String> sensorType = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_SENSOR, //Table to query
                columns,    //columns to return
                null,        //columns for the WHERE clause
                null,        //The values for the WHERE clause
                null,       //group the rows
                null,       //filter by row groups
                null); //The sort order
        if (cursor.moveToFirst()) {
            do {
                String name = cursor.getString(cursor.getColumnIndex(TYPE));
                sensorType.add(name);
                // Adding user record to list
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return sensorType;
    }

    public void deleteUser(String id){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_USER,USER_ID + " = " + id,null);
        db.close();
    }
    UserFormat getUser(String id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_USER, new String[] { NAME, BIRTHDAY, HEIGHT, WEIGHT, BODY_RES },
                USER_ID + "=?",
                new String[] { id }, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();

        UserFormat user = new UserFormat();
        user.setName(cursor.getString(0));
        user.setBirthday(cursor.getString(1));
        user.setHeight(cursor.getString(2));
        user.setWeight(cursor.getString(3));
        user.setBody_res(cursor.getString(4));
        user.setId(cursor.getString(5));
        return user;
    }
    public int NumberOfUsers() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT  * FROM " + TABLE_USER, null);
        return cursor.getCount();
    }
    public int NumberOfSensors() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT  * FROM " + TABLE_SENSOR, null);
        return cursor.getCount();
    }
    /*public void clearDatabase() {
        SQLiteDatabase db = this.getWritableDatabase();
        String clearDBQuery = "DELETE FROM "+TABLE_USER;
        db.execSQL(clearDBQuery);
        Log.i(TAG, "clearDatabase: Cleared");
    }*/

}