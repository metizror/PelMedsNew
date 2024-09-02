package com.metiz.pelconnect.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.widget.Toast;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import com.metiz.pelconnect.MyApplication;
import com.orhanobut.logger.Logger;

import java.util.Objects;

public class PreferenceHelper {

    public static SharedPreferences MySharedPreference;

    public static void putString(String key, String value) {
//      MySharedPreference = MyApplication.getAppContext().getSharedPreferences(CustomeConstants.APP_PREFERENCE_NAME, Context.MODE_PRIVATE);

        try {
            MySharedPreference = getEncryptedSharedPreferences(MyApplication.getAppContext());
            SharedPreferences.Editor editor = MySharedPreference.edit();
            editor.putString(key, value);
            editor.apply();
        }
        catch(Exception e)
        {
            Toast.makeText(MyApplication.getAppContext(),"exception is in putString: "+e.getMessage(),Toast.LENGTH_LONG);
        }
    }

    public static String getString(String key, String defaultValue) {
        try {
            MySharedPreference = getEncryptedSharedPreferences(MyApplication.getAppContext());
            return MySharedPreference.getString(key, defaultValue);
        }
        catch(Exception e)
        {
            Toast.makeText(MyApplication.getAppContext(),"exception is in getString: "+e.getMessage(),Toast.LENGTH_LONG);
        }
        return null;
    }

    public static void putInt(String key, int value) {
        MySharedPreference = getEncryptedSharedPreferences(MyApplication.getAppContext());
        SharedPreferences.Editor editor = MySharedPreference.edit();
        editor.putInt(key, value);
        editor.commit();
    }

    public static int getInt(String key, int defaultValue) {
        MySharedPreference = getEncryptedSharedPreferences(MyApplication.getAppContext());
        int string = MySharedPreference.getInt(key, defaultValue);
        return string;
    }

    public static void putLong(String key, long value) {
        MySharedPreference = getEncryptedSharedPreferences(MyApplication.getAppContext());
        SharedPreferences.Editor editor = MySharedPreference.edit();
        editor.putLong(key, value);
        editor.commit();
    }

    public static long getLong(String key, long defaultValue) {
        MySharedPreference = getEncryptedSharedPreferences(MyApplication.getAppContext());
        long string = MySharedPreference.getLong(key, defaultValue);
        return string;
    }

    public static void putBoolean(String key, boolean value) {
        MySharedPreference = getEncryptedSharedPreferences(MyApplication.getAppContext());
        SharedPreferences.Editor editor = MySharedPreference.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        MySharedPreference = getEncryptedSharedPreferences(MyApplication.getAppContext());
        boolean string = MySharedPreference.getBoolean(key, defaultValue);
        return string;
    }

    public static boolean contains(String key) {
        MySharedPreference = getEncryptedSharedPreferences(MyApplication.getAppContext());
        if (MySharedPreference.contains(key)) {
            return true;
        } else {
            return false;
        }
    }

    public static void remove(String key) {
        MySharedPreference = getEncryptedSharedPreferences(MyApplication.getAppContext());
        SharedPreferences.Editor editor = MySharedPreference.edit();
        editor.remove(key);
        editor.commit();
    }

    public static void clearPreference() {
        MySharedPreference = getEncryptedSharedPreferences(MyApplication.getAppContext());
        MySharedPreference.edit().clear().commit();
    }

    @SuppressLint("NewApi")
    static MasterKey getMasterKey(Context context) {
        try {
            KeyGenParameterSpec spec = new KeyGenParameterSpec.Builder(
                    "_androidx_security_master_key_",
                    KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .build();
//            return new MasterKey.Builder(context)
//                    .setKeyGenParameterSpec(spec)
//                    .build();
            return new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();
        } catch (Exception e) {
            Logger.e("Error on getting master key", e.getMessage());
        }
        return null;
    }

    private static SharedPreferences getEncryptedSharedPreferences(Context context) {
        try {
            return EncryptedSharedPreferences.create(
                    Objects.requireNonNull(context),
                    "_appName_secure_shared_preference_",
                    Objects.requireNonNull(getMasterKey(context)), // calling the method above for creating MasterKey
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (Exception e) {
            Logger.e("Error on getting encrypted shared preferences", e.getMessage());
            Toast.makeText(context,"exception is :"+e.getMessage(),Toast.LENGTH_LONG);
        }
        return null;
    }
}
