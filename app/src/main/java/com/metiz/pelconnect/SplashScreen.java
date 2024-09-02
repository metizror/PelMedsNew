package com.metiz.pelconnect;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.metiz.pelconnect.util.MyReceiver;

import java.io.File;
import java.util.Map;
import java.util.Objects;

public class SplashScreen extends AppCompatActivity {
    private static int SPLASH_TIME_OUT = 500;
    private BroadcastReceiver MyReceiver = null;
    String version;
    TextView applicationVersion;
    Context context;



    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        //permission
        context = SplashScreen.this;


        checkPermissions();
//            if (hasPermissions(context, PERMISSIONS_13)) {
//                ActivityCompat.requestPermissions(SplashScreen.this, PERMISSIONS_13, PERMISSION_ALL);
//            } else {
//                onCreatedMethod();
//            }
//        } else {
//            hasPermissions();
////            if (hasPermissions(context, PERMISSIONS)) {
////                ActivityCompat.requestPermissions(SplashScreen.this, PERMISSIONS, PERMISSION_ALL);
////            } else {
////                onCreatedMethod();
////            }
//        }


     /*   if (Utils.checkInternetConnection(this)) {

            checkApplicationVersion();
            FirebaseApp.initializeApp(this);
            applicationVersion = (TextView) findViewById(R.id.application_version);
            PackageInfo pInfo = null;
            try {
                pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            String version = pInfo.versionName;
            applicationVersion.setText("Version " + version);
        }*/

    }

    private void checkPermissions() {
        if (hasPermissionsGranted()) {
            onCreatedMethod();
        } else {
            requestPermissions();

        }

    }

    public void onCreatedMethod() {
        MyReceiver = new MyReceiver();
        checkApplicationVersion();
        FirebaseApp.initializeApp(this);
        applicationVersion = (TextView) findViewById(R.id.application_version);
        PackageInfo pInfo = null;
        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        version = pInfo.versionName;


        applicationVersion.setText("Version " + version);
        boolean isSessionLogoutMin = MyApplication.getPrefranceDataBoolean("isSessionLogoutMin");
        MyApplication.setPreferencesBoolean("isLogin", false);
        MyApplication.clearPrefrences();
        MyApplication.setPreferencesBoolean("isSessionLogoutMin", isSessionLogoutMin);
    }

 /*   public boolean hasPermissions(Context context, String... permissions) {

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && context != null && permissions != null) {
                for (String permission : permissions) {
                    if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }*/

    public boolean hasPermissionsGranted() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            return
                    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE) == PackageManager.PERMISSION_GRANTED
                            && ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                            && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED;

        }
        else {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE) == PackageManager.PERMISSION_GRANTED
                        && ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                        && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;


        }
        }


    public void requestPermissions() {
        if (context != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ActivityCompat.requestPermissions(SplashScreen.this, MyApplication.PERMISSIONS_33_AND_ABOVE, MyApplication.PERMISSION_ALL);
            }
            else{
                ActivityCompat.requestPermissions(SplashScreen.this, MyApplication.PERMISSIONS, MyApplication.PERMISSION_ALL);
            }
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
//        broadcastIntent();
//        if (Utils.checkInternetConnection(this)) {
        Log.e("TAG", "onResume: internet connected");
        //moveToNextScreen();

        //   checkApplicationVersion();

//        }
    }

    @Override
    protected void onPause() {
        super.onPause();
//        unregisterReceiver(MyReceiver);
    }

    private void broadcastIntent() {
//         registerReceiver(MyReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode==MyApplication.PERMISSION_ALL) {
            if(grantResults.length!=0)
            {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (hasPermissionsGranted())
                    {
                        onCreatedMethod();
                    }

                }
            else {
                if (hasPermissionsGranted()) {
                        onCreatedMethod();

                    } else {
                       finish();
                        Toast.makeText(this, "You must enable Permissions.", Toast.LENGTH_SHORT).show();
                    }
            }
            }
        }
        }

    private void moveToNextScreen() {
        deleteCache(context);
        if (MyApplication.getPrefranceDataBoolean("isForceUpdate")) {
            openUpdateDialog();
            return;
        }

        MyApplication.setPreferencesBoolean("isForceUpdate", false);
        new Handler().postDelayed(() -> {
            if (MyApplication.getPrefranceDataBoolean("isLogin")) {

                Intent i = new Intent(SplashScreen.this, PatientListActivityNew.class);
                i.putExtra("isFromSpash", true);
                startActivity(i);
                finish();
            } else {

                Intent i = new Intent(SplashScreen.this, LoginActivity.class);
                i.putExtra("Version", version);
                startActivity(i);
                finish();
            }
        }, SPLASH_TIME_OUT);

    }


    private void openUpdateDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(SplashScreen.this);
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == DialogInterface.BUTTON_POSITIVE) {
                    //Yes button clicked
                    openPlayStore();
                }
            }
        };
        builder.setMessage("A new version of this application is available and required.").setPositiveButton("Update", dialogClickListener).show();
        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        if (!dialog.isShowing())
            dialog.show();
    }

    private void openPlayStore() {
        final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
        } catch (android.content.ActivityNotFoundException anfe) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
        }
    }

    private void checkApplicationVersion() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Application");
        ref.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        //Get map of users in datasnapshot
                        // Result will be holded Here
                        for (DataSnapshot dsp : dataSnapshot.getChildren()) {
                            if (isForceUpdate((Map<String, Object>) dataSnapshot.getValue())) {
                                MyApplication.setPreferencesBoolean("isForceUpdate", true);
                            } else {
                                MyApplication.setPreferencesBoolean("isForceUpdate", false);
                            }
                            moveToNextScreen();
                            break;
                        }
                    }


                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        moveToNextScreen();

                    }
                });
    }

    private boolean isForceUpdate(Map<String, Object> value) {
        boolean isForceUpdate = false;

        try {
            int versionCode = 0;
            try {
                PackageInfo packageInfo = SplashScreen.this.getPackageManager().getPackageInfo(SplashScreen.this.getPackageName(), 0);
                versionCode = packageInfo.versionCode;
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            if (Integer.parseInt(value.get("AppVersion").toString()) > versionCode) {
                isForceUpdate = true;
            }
        } catch (Exception e) {
            e.printStackTrace();

        }
        return isForceUpdate;
    }

    public static void deleteCache(Context context) {
        try {
            File dir = context.getCacheDir();
            deleteDir(dir);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < Objects.requireNonNull(children).length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
            return dir.delete();
        } else if (dir != null && dir.isFile()) {
            return dir.delete();
        } else {
            return false;
        }
    }

}
