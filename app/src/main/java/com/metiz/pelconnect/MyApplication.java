package com.metiz.pelconnect;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.google.firebase.FirebaseApp;
import com.google.gson.Gson;
import com.instabug.library.Instabug;
import com.instabug.library.invocation.InstabugInvocationEvent;

import io.realm.Realm;
import io.realm.RealmConfiguration;


/**
 * Created by espl on 27/9/16.
 */
public class MyApplication extends android.app.Application implements LifecycleObserver {
    public static final String FONT = "fonts/Roboto-Regular.ttf";

    public static final String TAG = MyApplication.class.getSimpleName();
    private static MyApplication _instance;
    private static Gson gson = null;
    private static SharedPreferences _preferences;
    private static Context context;
    private RequestQueue mRequestQueue;
    private static MyApplication mInstance;

    public static String[] PERMISSIONS = {//
//            android.Manifest.permission.ACCESS_NETWORK_STATE,
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            //  Manifest.permission.ACCESS_MEDIA_LOCATION,
    };



    public static String[] PERMISSIONS_33_AND_ABOVE = {
//            android.Manifest.permission.ACCESS_NETWORK_STATE,
            android.Manifest.permission.CAMERA,
            Manifest.permission.READ_MEDIA_IMAGES
//            Manifest.permission.READ_EXTERNAL_STORAGE,
    };
    public static int PERMISSION_ALL = 7;
    public static int REQUEST_PERMISSION_SETTINGS = 8;



    public static MyApplication get() {

        return _instance;
    }

    public static Gson getGson() {
        if (gson == null)
            gson = new Gson();
        return gson;
    }

    /**
     * Gets shared preferences.
     *
     * @return the shared preferences
     */
    private static SharedPreferences getSharedPreferences() {
        if (_preferences == null)
            _preferences = PreferenceManager.getDefaultSharedPreferences(_instance);
        return _preferences;
    }

    public static synchronized MyApplication getInstance() {
        return mInstance;
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }

        return mRequestQueue;
    }

    public <T> void cancelRequestInQueue(String tag) {
        getRequestQueue().cancelAll(tag);
    }

    public <T> void addToRequestQueue(Request<T> req, String tag) {
        // set the no_user tag if tag is empty
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        getRequestQueue().add(req);
    }

    public <T> void addToRequestQueue(Request<T> req) {
        req.setTag(TAG);
        getRequestQueue().add(req);
    }

    public void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }

    public static void clearPrefrences() {
        getSharedPreferences().edit().clear().commit();
    }

    /**
     * Sets shared preferences.
     *
     * @return the shared preferences
     */
    public static void setPreferences(String key, String value) {
        getSharedPreferences().edit().putString(key, value).commit();
    }

    public static void setPreferences(String key, int value) {
        getSharedPreferences().edit().putInt(key, value).commit();
    }

    public static void setPreferencesLong(String key, long value) {
        getSharedPreferences().edit().putLong(key, value).commit();
    }

    public static void setPreferencesBoolean(String key, boolean value) {
        getSharedPreferences().edit().putBoolean(key, value).commit();
    }

    public static String getPrefranceData(String key) {
        return getSharedPreferences().getString(key, "");
    }

    public static int getPrefranceDataInt(String key) {
        return getSharedPreferences().getInt(key, 0);
    }

    public static long getPrefranceDataLong(String key) {
        return getSharedPreferences().getLong(key, 0);
    }

    public static boolean getPrefranceDataBoolean(String key) {
        return getSharedPreferences().getBoolean(key, false);
    }

    /**
     * Gets typeface of the FontAwesome
     *
     * @return the typeface
     */
    public static Typeface getTypeface(Context context, String font) {
        return Typeface.createFromAsset(context.getAssets(), font);
    }

    public static Context getAppContext() {
        return MyApplication.context;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder(this)
                .name("PelmedsDatabase")
                .schemaVersion(0)
                .deleteRealmIfMigrationNeeded()
                .build();
        Realm.setDefaultConfiguration(realmConfiguration);
        //     FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        Fresco.initialize(this);
        FirebaseApp.initializeApp(this);
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
        MyApplication.context = getApplicationContext();
        new Instabug.Builder(this, "9467300767a7dd3bfa4f1fbf904f1f22")
                .setInvocationEvents(InstabugInvocationEvent.SHAKE, InstabugInvocationEvent.SCREENSHOT)
                .build();
        try {
            _instance = this;
            /*setUpFonts();*/

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void onMoveToForeground() {
        Log.e("onMoveToForeground:", "onMoveToForeground:00000");


    }

}


