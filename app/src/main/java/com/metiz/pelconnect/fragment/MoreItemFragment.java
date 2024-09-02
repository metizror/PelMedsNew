package com.metiz.pelconnect.fragment;


import static com.metiz.pelconnect.PatientListActivityNew.clickPosition;
import static com.metiz.pelconnect.util.Utils.dismissProgressDialog;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;

import com.metiz.pelconnect.MyApplication;
import com.metiz.pelconnect.ChangePasswordActivity;
import com.metiz.pelconnect.LoginActivity;
import com.metiz.pelconnect.R;
import com.metiz.pelconnect.SettingScreenActivity;
import com.metiz.pelconnect.activity.InventoryActivity;
import com.metiz.pelconnect.activity.ReceivedDrugListActivity;
import com.metiz.pelconnect.network.API;
import com.metiz.pelconnect.network.NetworkUtility;
import com.metiz.pelconnect.network.VolleyCallBack;
import com.metiz.pelconnect.util.Utils;

import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class MoreItemFragment extends Fragment {


    @BindView(R.id.ll_setting)
    LinearLayout llSetting;
    @BindView(R.id.ll_change_password)
    LinearLayout llChangePassword;

    @BindView(R.id.ll_inventory)
    LinearLayout ll_inventory;

    @BindView(R.id.ll_inventory_details)
    LinearLayout ll_inventory_details;
    @BindView(R.id.btn_logout)
    AppCompatButton btnLogout;
    int facility = 0;

    @SuppressLint("ValidFragment")
    public MoreItemFragment(int facility) {
        // Required empty public constructor
        this.facility = facility;
    }

    public MoreItemFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_more_item, container, false);
        ButterKnife.bind(this, view);
        llChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ChangePasswordActivity.class);
                startActivity(intent);
            }
        });
        llSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), SettingScreenActivity.class);
                startActivity(intent);
            }
        });
        ll_inventory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), InventoryActivity.class);
                startActivity(intent);
            }
        });
        ll_inventory_details.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ReceivedDrugListActivity.class);
                startActivity(intent);
            }
        });

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

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

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage("Are you sure, you want to logout?").setPositiveButton("Yes", dialogClickListener)
                        .setNegativeButton("No", dialogClickListener).show();

            }
        });

        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    public void LogoutAPICall() {

        Utils.showProgressDialog(getActivity());

        NetworkUtility.makeJSONObjectRequest(API.GetLogout + "?UserID=" + MyApplication.getPrefranceData("UserID") + "&TokenID=" + MyApplication.getPrefranceData("device_token"), new JSONObject(), API.GetLogout + "?UserID=" + MyApplication.getPrefranceData("UserID"), new VolleyCallBack() {
            @Override
            public void onSuccess(JSONObject result) {
                try {
                    dismissProgressDialog();

                    //Yes button clicked
                    MyApplication.clearPrefrences();
                    MyApplication.setPreferencesBoolean("isLogin", false);
                    Intent i = new Intent(getActivity(), LoginActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(i);
                    getActivity().finish();
                    clickPosition = 0;


                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(JSONObject result) {
                dismissProgressDialog();


            }
        });


    }

}
