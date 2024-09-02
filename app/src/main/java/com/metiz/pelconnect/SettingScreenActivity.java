package com.metiz.pelconnect;

import android.app.ProgressDialog;
import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.metiz.pelconnect.network.API;
import com.metiz.pelconnect.network.NetworkUtility;
import com.metiz.pelconnect.network.VolleyCallBack;

import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SettingScreenActivity extends BaseActivity {
    ProgressDialog progressDialog;
    @BindView(R.id.ll_back)
    LinearLayout llBack;
    @BindView(R.id.tv_activity_name)
    TextView tvActivityName;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.rb_all_order)
    RadioButton rbAllOrder;
    @BindView(R.id.rb_my_order)
    RadioButton rbMyOrder;
    @BindView(R.id.rg_setting)
    RadioGroup rgSetting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_screen);
        ButterKnife.bind(this);
        initProgress();
        if (MyApplication.getPrefranceData("NotificationID").equals("1")) {
            rbAllOrder.setChecked(true);
            rbMyOrder.setChecked(false);

        } else if (MyApplication.getPrefranceData("NotificationID").equals("2")) {
            rbAllOrder.setChecked(false);
            rbMyOrder.setChecked(true);

        } else {
            MyApplication.setPreferences("NotificationID", "2");
            rbAllOrder.setChecked(false);
            rbMyOrder.setChecked(true);
        }

        rgSetting.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                View radioButton = rgSetting.findViewById(checkedId);
                int index = rgSetting.indexOfChild(radioButton);


// Check which radio button was clicked
                switch (radioButton.getId()) {
                    case R.id.rb_all_order:
                        callNotificationSettingAPI(1);
                        break;
                    case R.id.rb_my_order:
                        callNotificationSettingAPI(2);
                        break;
                }
            }
            // Add logic here

//                switch(index)
//
//            {
//                case 0: // first button
//                    callNotificationSettingAPI(1);
//                    break;
//                case 1: // secondbutton
//                    callNotificationSettingAPI(2);
//                    Toast.makeText(getApplicationContext(), "Selected button number " + index, Toast.LENGTH_LONG).show();
//
//                    break;
//            }

        });

        llBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }

    private void initProgress() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);
    }

    private void callNotificationSettingAPI(final int i) {
        progressDialog.show();

        NetworkUtility.makeJSONObjectRequest(API.SetNotification + "?UserID=" + MyApplication.getPrefranceData("UserID") + "&NotificationID=" + i, new JSONObject(), API.SetNotification, new VolleyCallBack() {
            @Override
            public void onSuccess(JSONObject result) {
                try {
                    if (progressDialog != null && progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }

                    MyApplication.setPreferences("NotificationID", i + "");
                    Log.e("CalllSetting", "Call Setting");
                    invalidateOptionsMenu();

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            @Override
            public void onError(JSONObject result) {
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                invalidateOptionsMenu();
            }
        });

    }

}
