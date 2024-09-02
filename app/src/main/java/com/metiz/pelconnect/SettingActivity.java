
package com.metiz.pelconnect;

import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.metiz.pelconnect.util.GlobalArea;
import com.metiz.pelconnect.util.Utils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SettingActivity extends BaseActivity {

    @BindView(R.id.img_back)
    ImageView imgBack;
    @BindView(R.id.toolbar_title)
    TextView toolbarTitle;
    @BindView(R.id.toolbar_sub_title)
    TextView toolbarSubTitle;
    @BindView(R.id.img_change)
    ImageView imgChange;
    @BindView(R.id.toolbar_title_logo)
    ImageView toolbar_title_logo;
    @BindView(R.id.linearLayout)
    LinearLayout linearLayout;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.edt_done)
    EditText edtDone;
    @BindView(R.id.edt_intake)
    EditText edtIntake;
    @BindView(R.id.edt_pv2)
    EditText edtPv2;
    @BindView(R.id.edt_delivered)
    EditText edtDelivered;
    @BindView(R.id.edt_menifest)
    EditText edtMenifest;
    @BindView(R.id.btn_save)
    Button btnSave;
    @BindView(R.id.edt_filling)
    EditText edtFilling;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        ButterKnife.bind(this);
        initToolbar();
        setValue();
    }

    private void setValue() {
        edtDone.setText(MyApplication.getPrefranceData(GlobalArea.DONE));
        edtMenifest.setText(MyApplication.getPrefranceData(GlobalArea.MENIFEST));
        edtIntake.setText(MyApplication.getPrefranceData(GlobalArea.INTAKE));
        edtPv2.setText(MyApplication.getPrefranceData(GlobalArea.PV2));
        edtDelivered.setText(MyApplication.getPrefranceData(GlobalArea.DELIVERED));
        edtFilling.setText(MyApplication.getPrefranceData(GlobalArea.FILLING));
    }

    private void initToolbar() {
        imgChange.setVisibility(View.GONE);
        imgBack.setVisibility(View.VISIBLE);
        toolbarSubTitle.setVisibility(View.GONE);
        toolbarTitle.setVisibility(View.VISIBLE);
        toolbar_title_logo.setVisibility(View.GONE);
        toolbarTitle.setText("Settings");

        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    @OnClick(R.id.btn_save)
    public void onViewClicked() {
        MyApplication.setPreferences(GlobalArea.DELIVERED, edtDelivered.getText().toString());
        MyApplication.setPreferences(GlobalArea.DONE, edtDone.getText().toString());
        MyApplication.setPreferences(GlobalArea.MENIFEST, edtMenifest.getText().toString());
        MyApplication.setPreferences(GlobalArea.PV2, edtPv2.getText().toString());
        MyApplication.setPreferences(GlobalArea.INTAKE, edtIntake.getText().toString());
        MyApplication.setPreferences(GlobalArea.FILLING, edtFilling.getText().toString());

        Utils.showAlertToast(SettingActivity.this, "Saved");
        finish();
    }
}
