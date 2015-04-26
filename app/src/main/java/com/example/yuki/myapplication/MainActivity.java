package com.example.yuki.myapplication;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import static android.widget.Button.OnClickListener;

public class MainActivity extends Activity implements LocationListener {    // LocationListenerインターフェースを実装
    private LocationManager mLocationManager;   // 位置情報取得用クラス

    // データベース名を設定
    static SQLiteDatabase mydb;
    // データベースファイル名を設定
    static final String DB = "location.db";
    static final int DB_VERSION = 1;
    // テーブルのCREATE文を設定
//    static final String CREATE_TABLE = "create table mytable ( _id integer primary key autoincrement, data integer not null );";
    static final String CREATE_TABLE ="create table location_data (" +
            "_id integer primary key autoincrement," +
            "Latitude double, " +
            "Longitude double, " +
            "Accuracy double, " +
            "Altitude double, " +
            "Time double, " +
            "Speed double, " +
            "Bearing double)";
    // テーブルのDROP文を設定
//    static final String DROP_TABLE = "drop table mytable;";
    static final String DROP_TABLE = "drop table location_data;";

    // データベース操作用ヘルパークラス
    private static class MySQLiteOpenHelper extends SQLiteOpenHelper {
        public MySQLiteOpenHelper(Context c) {
            super(c, DB, null, DB_VERSION);
        }
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_TABLE);
        }
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL(DROP_TABLE);
            onCreate(db);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.clickme).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, SecondActivity.class);
                EditText editText = (EditText) findViewById(R.id.edittext);
                intent.putExtra("inputText", editText.getText().toString());
                startActivity(intent);
            }
        });

        // LocationManager を取得
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // データベースを作成
        MySQLiteOpenHelper hlpr = new MySQLiteOpenHelper(getApplicationContext());
        mydb = hlpr.getWritableDatabase(); // 書き込み可能に設定
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    protected void onResume() {
        if (mLocationManager != null) {
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,   // GPSを使用(ネットワークも含む)
//                LocationManager.NETWORK_PROVIDER,
                    0,
                    0,
                    this);
        }

        super.onResume();
    }

    @Override
    protected void onPause() {
        if (mLocationManager != null) {
            mLocationManager.removeUpdates(this);
        }

        super.onPause();
    }

    @Override
    public void onLocationChanged(Location location) {
        // ログ表示
        Log.v("----------", "----------");
        Log.v("Latitude", String.valueOf(location.getLatitude()));
        Log.v("Longitude", String.valueOf(location.getLongitude()));
        Log.v("Accuracy", String.valueOf(location.getAccuracy()));
        Log.v("Altitude", String.valueOf(location.getAltitude()));
        Log.v("Time", String.valueOf(location.getTime()));
        Log.v("Speed", String.valueOf(location.getSpeed()));
        Log.v("Bearing", String.valueOf(location.getBearing()));

        // データベースに格納
        ContentValues values = new ContentValues();
        values.put("Latitude", String.valueOf(location.getLatitude()));
        values.put("Longitude", String.valueOf(location.getLongitude()));
        values.put("Accuracy", String.valueOf(location.getAccuracy()));
        values.put("Altitude", String.valueOf(location.getAltitude()));
        values.put("Time", String.valueOf(location.getTime()));
        values.put("Speed", String.valueOf(location.getSpeed()));
        values.put("Bearing", String.valueOf(location.getBearing()));
        mydb.insert("location_data", null, values);

        // 画面表示用の文字列を設定
        String postext = "Latitude :" + String.valueOf(location.getLatitude()) + "\n"
                + "Longitude :" + String.valueOf(location.getLongitude()) + "\n"
                + "Accuracy :" + String.valueOf(location.getAccuracy()) + "\n"
                + "Altitude :" + String.valueOf(location.getAltitude()) + "\n"
                + "Time :" + String.valueOf(location.getTime()) + "\n"
                + "Speed :" + String.valueOf(location.getSpeed()) + "\n"
                + "Bearing :" + String.valueOf(location.getBearing()) +"\n"
                + "data_number:" + DatabaseUtils.queryNumEntries(mydb, "location_data");    // テーブルのデータ数をカウント

        // 画面に表示
        TextView logText = (TextView) findViewById(R.id.logText);
        logText.setText(postext);
    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        switch (status) {
            case LocationProvider.AVAILABLE:
                Log.v("Status", "AVAILABLE");
                break;
            case LocationProvider.OUT_OF_SERVICE:
                Log.v("Status", "OUT_OF_SERVICE");
                break;
            case LocationProvider.TEMPORARILY_UNAVAILABLE:
                Log.v("Status", "TEMPORARILY_UNAVAILABLE");
                break;
        }
    }
}
