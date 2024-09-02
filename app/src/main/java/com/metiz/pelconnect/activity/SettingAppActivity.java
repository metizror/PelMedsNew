package com.metiz.pelconnect.activity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatButton;
import androidx.databinding.DataBindingUtil;

import com.metiz.pelconnect.MyApplication;
import com.metiz.pelconnect.BaseActivity;
import com.metiz.pelconnect.ChangePasswordActivity;
import com.metiz.pelconnect.LoginActivity;
import com.metiz.pelconnect.R;
import com.metiz.pelconnect.databinding.DialogShowDownloadFileAlertBinding;
import com.metiz.pelconnect.network.API;
import com.metiz.pelconnect.network.NetworkUtility;
import com.metiz.pelconnect.network.VolleyCallBack;
import com.metiz.pelconnect.util.MyReceiver;
import com.metiz.pelconnect.util.PreferenceHelper;

import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.metiz.pelconnect.PatientListActivityNew.clickPosition;

import java.io.File;

public class SettingAppActivity extends BaseActivity {

    @BindView(R.id.ll_back)
    LinearLayout llBack;
    @BindView(R.id.ll_change_password)
    LinearLayout llChangePassword;
    @BindView(R.id.btn_logout)
    AppCompatButton btnLogout;
    @BindView(R.id.logoutSwitch)
    Switch logoutSwitch;
    @BindView(R.id.application_version)
    TextView applicationversion;
    ProgressDialog progressDialog;
    private BroadcastReceiver MyReceiver = null;
    String version;
    private Dialog dialogSHowAlert;
    private DialogShowDownloadFileAlertBinding mDialogShowAlertBinding;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_app);
        ButterKnife.bind(this);
        initProgress();
        MyReceiver = new MyReceiver();

        MyApplication.setPreferencesBoolean("isSessionLogoutMin", true);

        logoutSwitch.setChecked(MyApplication.getPrefranceDataBoolean("isSessionLogoutMin"));
        logoutSwitch.setText("Auto logoff every " + MyApplication.getPrefranceDataLong("sessiontime") + " minutes.");
       /* logoutSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                MyApplication.setPreferencesBoolean("isSessionLogoutMin", true);
            }
        });*/

        PackageInfo pInfo = null;
        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        version = pInfo.versionName;
        String data = MyApplication.getPrefranceData("Version");

        applicationversion.setText("Version " + version);
        Log.e("TAG", "onCreate:Version " + MyApplication.getPrefranceData("Version"));

    }

    @OnClick({R.id.ll_change_password, R.id.btn_logout, R.id.ll_back})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ll_change_password:
                Intent intent = new Intent(this, ChangePasswordActivity.class);
                startActivity(intent);
                break;
            case R.id.btn_logout:
                if(PreferenceHelper.getString("LOGOUT","").equalsIgnoreCase("true")){
                    logout();
                }else{
//                    Toast.makeText(context, "Not Download", Toast.LENGTH_SHORT).show();
                    showAlertDialog("Please wait for a while, Medsheets are downloading..","Medsheets downloading");
                }
                break;
            case R.id.ll_back:
                finish();
                break;
        }
    }

    private void initProgress() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);
    }


    private void logout() {

        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:

                        LogoutAPICall();


                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure, you want to logout?").setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();
    }

    @Override
    protected void onResume() {
        broadcastIntent();
        super.onResume();
    }

    private void broadcastIntent() {
        registerReceiver(MyReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(MyReceiver);
    }


    public void LogoutAPICall() {

        progressDialog.show();

        NetworkUtility.makeJSONObjectRequest(API.GetLogout + "?UserID=" + MyApplication.getPrefranceData("UserID") + "&TokenID=" + MyApplication.getPrefranceData("device_token"), new JSONObject(), API.GetLogout + "?UserID=" + MyApplication.getPrefranceData("UserID"), new VolleyCallBack() {
            @Override
            public void onSuccess(JSONObject result) {
                try {
                    progressDialog.dismiss();

                    //Yes button clicked
                    boolean isSessionLogoutMin = MyApplication.getPrefranceDataBoolean("isSessionLogoutMin");

                    MyApplication.clearPrefrences();
                    MyApplication.setPreferencesBoolean("isLogin", false);
                    MyApplication.setPreferencesBoolean("isSessionLogoutMin", isSessionLogoutMin);
                    Intent i = new Intent(SettingAppActivity.this, LoginActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(i);
                    finish();
                    clickPosition = 0;

                } catch (Exception e) {
                    e.printStackTrace();
                    progressDialog.dismiss();
                }
            }

            @Override
            public void onError(JSONObject result) {
                progressDialog.dismiss();
            }
        });


    }

    private void showAlertDialog(String message, String title) {
        dialogSHowAlert = new Dialog(context);//, android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
        mDialogShowAlertBinding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.dialog_show_download_file_alert, null, false);
        dialogSHowAlert.setContentView(mDialogShowAlertBinding.getRoot());
//                            dialogDiscontinue.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation_2;
        dialogSHowAlert.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialogSHowAlert.getWindow().setStatusBarColor(context.getResources().getColor(R.color.white));
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
