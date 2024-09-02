package com.metiz.pelconnect;

import static com.metiz.pelconnect.util.Utils.dialogSHowAlert;
import static com.metiz.pelconnect.util.Utils.mDialogShowAlertBinding;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.an.biometric.BiometricCallback;
import com.an.biometric.BiometricManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;
import com.kaopiz.kprogresshud.KProgressHUD;
import com.metiz.pelconnect.databinding.DialogShowAlertBinding;
import com.metiz.pelconnect.databinding.DialogShowDownloadFileAlertBinding;
import com.metiz.pelconnect.model.LoginModel;
import com.metiz.pelconnect.network.API;
import com.metiz.pelconnect.network.NetworkUtility;
import com.metiz.pelconnect.network.VolleyCallBack;
import com.metiz.pelconnect.retrofit.ApiClient;
import com.metiz.pelconnect.retrofit.ApiInterface;
import com.metiz.pelconnect.util.Constants;
import com.metiz.pelconnect.util.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Objects;
import java.util.logging.Logger;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class LoginActivity extends AppCompatActivity implements BiometricCallback {

    EditText editName, editPswd;
    TextView link_signup;
    Button buttonLogin;
    String name, password;
    @BindView(R.id.ll_finger)
    LinearLayout llFinger;
    @BindView(R.id.img_fingerprint)
    ImageView imgFingerprint;
    BiometricManager mBiometricManager;
    String fingerUsername;
    String fingerPassword;
    String token = "";
    ApiInterface apiService;
    String versionRelease = Build.VERSION.RELEASE;
    private Dialog dialogSHowAlert;
    private DialogShowDownloadFileAlertBinding mDialogShowAlertBinding;

    private KProgressHUD hud;
    //  private BroadcastReceiver MyReceiver = null;

    private void openMainActivity() {
        Intent intent = new Intent(LoginActivity.this, PatientListActivityNew.class);
        intent.putExtra("isFromLogin", true);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        apiService = ApiClient.getClient().create(ApiInterface.class);
        //   MyReceiver = new MyReceiver();

        Intent intent = getIntent();
        String version = intent.getStringExtra("Version");
        MyApplication.setPreferences("Version", version);


        hud = KProgressHUD.create(Objects.requireNonNull(this))
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setLabel("Please wait...")
                .setCancellable(false);

        editName = findViewById(R.id.input_name);
        link_signup = findViewById(R.id.link_signup);
        link_signup.setText(Html.fromHtml("<u>Register</u>"));
        editPswd = findViewById(R.id.input_password);
        buttonLogin = findViewById(R.id.btn_login);
        //demo credential for test
//        editName.setText("mupadhyay");
//        editPswd.setText("test@1234");
        //live credential for test
//        editName.setText("test.admin");
//        editPswd.setText("theadmin@12345");
        name = editName.getText().toString().trim();
        password = editPswd.getText().toString().trim();

        SharedPreferences prefs = getSharedPreferences("Finger", MODE_PRIVATE);
        fingerUsername = prefs.getString("UserName", "");//"No name defined" is the default value.
        fingerPassword = prefs.getString("Password", ""); //0 is the default value.
        /*FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(LoginActivity.this, new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                token = instanceIdResult.getToken();
                Log.e("Token123", token);
            }
        });*/
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                if (!task.isSuccessful()) {
                    Log.w("TAG", "Fetching FCM registration token failed", task.getException());
                    Log.e("TAG", "onComplete: firbase " + task.getException());
                    return;
                }
                token = task.getResult();
//                PreferenceHelper.putString(CustomeConstants.FIREBASE_ID, "" + token);
//                Logger.e("onComplete: >>>>>>>" + token);
            }
        });
        if (fingerUsername.equalsIgnoreCase("") && fingerPassword.equalsIgnoreCase("")) {
            llFinger.setVisibility(View.GONE);
        } else {
            llFinger.setVisibility(View.VISIBLE);

            mBiometricManager = new BiometricManager.BiometricBuilder(LoginActivity.this)
                    .setTitle(getString(R.string.biometric_title))
                    .setSubtitle(getString(R.string.biometric_subtitle))
                    .setDescription(getString(R.string.biometric_description))
                    .setNegativeButtonText(getString(R.string.biometric_negative_button_text))
                    .build();

            //start authentication
            mBiometricManager.authenticate(LoginActivity.this);
        }
        imgFingerprint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBiometricManager = new BiometricManager.BiometricBuilder(LoginActivity.this)
                        .setTitle(getString(R.string.biometric_title))
                        .setSubtitle(getString(R.string.biometric_subtitle))
                        .setDescription(getString(R.string.biometric_description))
                        .setNegativeButtonText(getString(R.string.biometric_negative_button_text))
                        .build();

                //start authentication
                mBiometricManager.authenticate(LoginActivity.this);
            }
        });

        buttonLogin.setOnClickListener(v -> {
            if (isValid())
                loginAPICall();
        });

    }

//    private void broadcastIntent() {
//        registerReceiver(MyReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
//    }

    private boolean isValid() {
        if (editName.getText().toString().trim().isEmpty()) {
            editName.requestFocus();
            editName.setError("Can't be blank");
            return false;
        } else if (editPswd.getText().toString().trim().isEmpty()) {
            editPswd.requestFocus();
            editPswd.setError("Can't be blank");
            return false;
        }
        return true;
    }

    //login Api call
    private void loginAPICall() {
        if (Utils.checkInternetConnection(getApplicationContext())) {

            hud.show();
            JSONObject params = new JSONObject();
            String versionCode = "";
            try {
                PackageInfo packageInfo = getApplicationContext().getPackageManager().getPackageInfo(getApplicationContext().getPackageName(), 0);
                versionCode = packageInfo.versionName;
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            try {
                params.put("UserName", editName.getText().toString().trim());
                params.put("Password", editPswd.getText().toString().trim());
                params.put("DivceName", Build.MANUFACTURER + " " + Build.DEVICE);
                params.put("ModelNo", Build.MODEL);
                params.put("ApplicationVersion", versionCode);
                params.put("DeviceVersion", Build.VERSION.RELEASE);
                if (token.equalsIgnoreCase("")) {
                    params.put("device_token", "");
                } else {
                    params.put("device_token", token);
                }
            } catch (JSONException ex) {
                ex.printStackTrace();
            }
            RequestBody bodyRequest = RequestBody.create(MediaType.parse("application/json"), params.toString());
            Call<LoginModel> call = apiService.makeLogin(bodyRequest);

            call.enqueue(new Callback<LoginModel>() {
                @Override
                public void onResponse(@NonNull Call<LoginModel> call, @NonNull Response<LoginModel> response) {
                    hud.dismiss();
                    if (Utils.checkInternetConnection(getApplicationContext())) {
                        if (response.body() != null) {
                            int response_code = response.body().getResponseStatus();
                            if (response_code == 1) {
                                try {

                                    JSONObject userData = new JSONObject(new Gson().toJson(response.body().getData()));


                                    Iterator<String> iter = userData.keys();
                                    while (iter.hasNext()) {
                                        String key = iter.next();
                                        try {
                                            String value = userData.getString(key);
                                            MyApplication.setPreferences(key, value);
                                        } catch (JSONException e) {

                                        }
                                    }

                                    String time = String.valueOf(response.body().getData().getSessionTimeOut());
                                    MyApplication.setPreferencesBoolean("isLogin", true);
                                    MyApplication.setPreferencesLong("sessiontime", Long.parseLong(time));
                                    MyApplication.setPreferences("UserName", fingerUsername);
                                    MyApplication.setPreferences("Password", fingerPassword);
                                    MyApplication.setPreferencesBoolean("IsChangePassword", response.body().getData().isIsChangePassword());
                                    MyApplication.setPreferences(Constants.LastPasswordChangeDate,
                                            Utils.formatDateFromOnetoAnother(response.body().getData().getLastpasswordChangeDate(), "yyyy-MM-dd'T'HH:mm:ss.SSS", "yyyy-MM-dd"));
                                    SharedPreferences.Editor editor = getSharedPreferences("Finger", MODE_PRIVATE).edit();
                                    editor.putString("UserName", fingerUsername);
                                    editor.putString("Password", fingerPassword);
                                    editor.apply();
                                    openMainActivity();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } else {
                                Toast.makeText(LoginActivity.this, response.body().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull Call<LoginModel> call, @NonNull Throwable t) {
                    Log.e("TAG", t.toString());
                    hud.dismiss();
//                    Toast.makeText(LoginActivity.this, "Connection Fail..", Toast.LENGTH_SHORT).show();
                    showAlertDialog(getString(R.string.connection_fail),getString(R.string.please_wait));
//                    Utils.showAlertToast(LoginActivity.this, t.toString());
                   /* try {
                        throw new Exception("The password provided is too short",t);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }*/
                }
            });
        } else {
            Toast.makeText(this, "No Internet Connection", Toast.LENGTH_SHORT).show();
        }
    }
//    @Override
//    protected void onResume() {
//        broadcastIntent();
//        super.onResume();
//    }

    private void loginAPICallFinger(String UserName, String Password) {
        JSONObject params = new JSONObject();
        try {
            params.put("UserName", UserName);
            params.put("Password", Password);
            if (token.equalsIgnoreCase("")) {
                params.put("device_token", "");

            } else {
                params.put("device_token", token);
            }
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
        hud.show();
        RequestBody bodyRequest = RequestBody.create(MediaType.parse("application/json"), params.toString());
        Call<LoginModel> call = apiService.makeLogin(bodyRequest);
        call.enqueue(new Callback<LoginModel>() {
            @Override
            public void onResponse(@NonNull Call<LoginModel> call, @NonNull Response<LoginModel> response) {
                hud.dismiss();
                if (Utils.checkInternetConnection(getApplicationContext())) {
                    if (response.body() != null) {
                        int response_code = response.body().getResponseStatus();
                        if (response_code == 1) {
                            try {
                                Log.e("response", response.body().toString());
                                JSONObject userData = new JSONObject(new Gson().toJson(response.body().getData()));
                                Iterator<String> iter = userData.keys();
                                String time = String.valueOf(response.body().getData().getSessionTimeOut());
                                MyApplication.setPreferencesBoolean("isLogin", true);
                                MyApplication.setPreferencesLong("sessiontime", Long.parseLong(time));
                                MyApplication.setPreferences("UserName", fingerUsername);
                                MyApplication.setPreferences("Password", fingerPassword);
                                MyApplication.setPreferencesBoolean("IsChangePassword", response.body().getData().isIsChangePassword());
                                MyApplication.setPreferences(Constants.LastPasswordChangeDate,
                                        Utils.formatDateFromOnetoAnother(response.body().getData().getLastpasswordChangeDate(), "yyyy-MM-dd'T'HH:mm:ss.SSS", "yyyy-MM-dd"));
                                SharedPreferences.Editor editor = getSharedPreferences("Finger", MODE_PRIVATE).edit();
                                editor.putString("UserName", fingerUsername);
                                editor.putString("Password", fingerPassword);
                                editor.apply();
                                openMainActivity();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            Toast.makeText(LoginActivity.this, response.body().getMessage(), Toast.LENGTH_SHORT).show();
                        }

                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<LoginModel> call, @NonNull Throwable t) {
                Log.e("TAG", t.toString());
                hud.dismiss();
//                Utils.showAlertToast(LoginActivity.this, t.toString());
                try {
                    throw new Exception("The password provided is too short",t);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

//    @Override
//    protected void onPause() {
//        super.onPause();
//        unregisterReceiver(MyReceiver);
//    }

    public void openRegister(View view) {
        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(intent);
    }

    public void openForgotPassword(View view) {

        Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
        startActivity(intent);
    }


    @Override
    public void onSdkVersionNotSupported() {
        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), getString(R.string.biometric_error_sdk_not_supported), Snackbar.LENGTH_SHORT);
        snackbar.show();
    }

    @Override
    public void onBiometricAuthenticationNotSupported() {

        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), getString(R.string.biometric_error_hardware_not_supported), Snackbar.LENGTH_SHORT);
        snackbar.show();
    }

    @Override
    public void onBiometricAuthenticationNotAvailable() {
        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), getString(R.string.biometric_error_fingerprint_not_available), Snackbar.LENGTH_SHORT);
        snackbar.show();

    }

    @Override
    public void onBiometricAuthenticationPermissionNotGranted() {
        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), getString(R.string.biometric_error_permission_not_granted), Snackbar.LENGTH_SHORT);
        snackbar.show();
    }

    @Override
    public void onBiometricAuthenticationInternalError(String error) {
        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), error, Snackbar.LENGTH_SHORT);
        snackbar.show();
    }

    @Override
    public void onAuthenticationFailed() {
        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), getString(R.string.biometric_failure), Snackbar.LENGTH_SHORT);
        snackbar.show();
    }

    @Override
    public void onAuthenticationCancelled() {
        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), getString(R.string.biometric_cancelled), Snackbar.LENGTH_SHORT);
        snackbar.show();
        mBiometricManager.cancelAuthentication();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finishAffinity();
        System.exit(0);
    }

    @Override
    public void onAuthenticationSuccessful() {
        loginAPICallFinger(fingerUsername, fingerPassword);
    }

    @Override
    public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
//        Toast.makeText(getApplicationContext(), helpString, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onAuthenticationError(int errorCode, CharSequence errString) {
//        Toast.makeText(getApplicationContext(), errString, Toast.LENGTH_LONG).show();
    }
    private String getDateTime() {
        DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        Date date = new Date();
        return dateFormat.format(date);
    }
    private void InsertSometingwantErrorLog(String message, String date, String time) {
//        hud.show();
        JSONObject mainObj = new JSONObject();

        try {
            mainObj.put("UserID", MyApplication.getPrefranceData("UserID"));
            mainObj.put("DateAndTime", getDateTime());
            mainObj.put("PatientID", "");
            mainObj.put("FacilityID", MyApplication.getPrefranceDataInt("ExFacilityID"));
            mainObj.put("DivceName", Build.MANUFACTURER + " " + Build.DEVICE);
            mainObj.put("ModelNo", Build.MODEL);
            mainObj.put("DeviceVersion", Build.VERSION.RELEASE);
            mainObj.put("Message", message);
            mainObj.put("ScaningValue", "");
            mainObj.put("Appversion", MyApplication.getPrefranceData("Version"));//
            mainObj.put("DoseDate", date);
            mainObj.put("DoseTime", time);


        } catch (JSONException ex) {
            ex.printStackTrace();
        }


        NetworkUtility.makeJSONObjectRequest(API.InsertSometingwantErrorLog, mainObj, API.InsertSometingwantErrorLog, new VolleyCallBack() {
            @Override
            public void onSuccess(JSONObject result) {
//                hud.dismiss();
                try {
                    if (result != null) {
                        Log.e("TAG", "InsertSometingwant: " + result.get("Data"));
                    }

                } catch (Exception ex) {
                    try {
                        throw new Exception("The password provided is too short",ex);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    /*AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setMessage("Something Went Wrong!")
                            .setCancelable(false)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    //do things
                                    dialog.dismiss();
                                }
                            });
                    AlertDialog alert = builder.create();
                    alert.show();*/
//                    Toast.makeText(getContext(), getResources().getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
//                    showAlertDialog(context.getResources().getString(R.string.something_went_wrong), "", "", "OK", "1");

                }
            }

            @Override
            public void onError(JSONObject result) {
//                hud.dismiss();
               /* AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage("Something Went Wrong!")
                        .setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //do things
                                dialog.dismiss();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();*/
//                showAlertDialog(context.getResources().getString(R.string.something_went_wrong), "", "", "OK", "1");
//                Toast.makeText(getContext(), getResources().getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();

            }
        });
    }

    //display dialog box for connection not get
    private void showAlertDialog(String message, String title) {
        dialogSHowAlert = new Dialog(LoginActivity.this);//, android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
        mDialogShowAlertBinding = DataBindingUtil.inflate(LayoutInflater.from(LoginActivity.this), R.layout.dialog_show_download_file_alert, null, false);
        dialogSHowAlert.setContentView(mDialogShowAlertBinding.getRoot());
//                            dialogDiscontinue.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation_2;
        dialogSHowAlert.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialogSHowAlert.getWindow().setStatusBarColor(LoginActivity.this.getResources().getColor(R.color.white));
        dialogSHowAlert.setCancelable(false);
        dialogSHowAlert.setCanceledOnTouchOutside(false);

        mDialogShowAlertBinding.tvTitle.setText(title);
        mDialogShowAlertBinding.tvMessage.setText(message);


//        mDialogShowAlertBinding.imgalert.setVisibility(View.GONE);
        mDialogShowAlertBinding.tvTitle.setVisibility(View.VISIBLE);

        mDialogShowAlertBinding.canleBtn.setOnClickListener(v -> {
            dialogSHowAlert.dismiss();
            dialogSHowAlert = null;

        });
        mDialogShowAlertBinding.okBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            Uri uri = Uri.parse( Environment.getExternalStorageDirectory()
                    + File.separator + Environment.DIRECTORY_DOWNLOADS
                    + File.separator + this.getResources().getString(R.string.app_name));
            intent.setDataAndType(uri, "*/*");
            startActivity(Intent.createChooser(intent, "Open folder"));


//            String path = Environment.getExternalStorageDirectory() + "/" + "Downloads" + "/";
           /* String path = Environment.getExternalStorageDirectory()
                    + File.separator + Environment.DIRECTORY_DOWNLOADS
                    + File.separator + this.getResources().getString(R.string.app_name);
            Uri uri = Uri.parse(path);
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setDataAndType(uri, "");
            startActivity(intent);*/
            dialogSHowAlert.dismiss();
            dialogSHowAlert = null;
        });

        dialogSHowAlert.show();

    }


}

