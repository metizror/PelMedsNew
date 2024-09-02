package com.metiz.pelconnect;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.collection.ArrayMap;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.gson.reflect.TypeToken;
import com.kaopiz.kprogresshud.KProgressHUD;
import com.metiz.pelconnect.Adapter.DrugAdapter;
import com.metiz.pelconnect.Adapter.PatientActiveDrugListAdapter;
import com.metiz.pelconnect.databinding.ActivityMedPrescriptionTabBinding;
import com.metiz.pelconnect.databinding.ActivityPatientActriveDrugListBinding;
import com.metiz.pelconnect.fragment.DrugHistoryFragment;
import com.metiz.pelconnect.model.DrugResponse;
import com.metiz.pelconnect.network.API;
import com.metiz.pelconnect.network.NetworkUtility;
import com.metiz.pelconnect.network.VolleyCallBack;
import com.metiz.pelconnect.network.VolleyStringBack;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import de.hdodenhof.circleimageview.CircleImageView;

public class PatientActiveDrugListActivity extends BaseActivity {


    int patientId = 0;
    String patientName = "";
    String patientGender = "";
    String patientBirthDate = "";

    @BindView(R.id.patient_list)
    RecyclerView drugListView;

    @BindView(R.id.swipe_refresh)
    SwipeRefreshLayout swipeRefreshLayout;

    @BindView(R.id.txt_name)
    TextView txtName;

    @BindView(R.id.txt_gender)
    TextView txtGender;

    @BindView(R.id.txt_birthdate)
    TextView txtBirthdate;

    @BindView(R.id.patient_image)
    CircleImageView patientImg;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.ll_back)
    LinearLayout ll_back;

    Unbinder unbinder;
    String url = "";


    private KProgressHUD hud;

    private PatientActiveDrugListAdapter adapter;
    List<DrugResponse> drugList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_actrive_drug_list);


        unbinder = ButterKnife.bind(this);

//        mBinding.swipeRefresh = findViewById(R.id.swipe_refresh);

        hud = KProgressHUD.create(this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setLabel("Please wait...")
                .setCancellable(false);

        patientId = getIntent().getIntExtra("PatientId", 0);
        patientName = getIntent().getStringExtra("PatientName");
        patientGender = getIntent().getStringExtra("PatientGender");
        patientBirthDate = getIntent().getStringExtra("PatientBirthdate");


       txtName.setText(patientName);
       txtGender.setText(patientGender);
        txtBirthdate.setText(patientBirthDate);
        getUserImage(patientId);



        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getDrugList();
            }
        });
        ll_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });



        drugListView.setLayoutManager(new LinearLayoutManager(this));
        getDrugList();
    }


    private void getUserImage(int patientId) {
        //showProgressDialog(MedPrescriptionTabActivity.this);
        hud.show();
        NetworkUtility.makeJSONObjectRequest(API.PatientImagePath + "?patient_id=" + patientId, new JSONObject(), API.PatientImagePath, new VolleyCallBack() {
            @Override
            public void onSuccess(JSONObject result) {
                hud.dismiss();
                try {

                    url = result.getString("Data");
                    Log.e("TAG","url is :"+url);
                    Glide.with(PatientActiveDrugListActivity.this).load(result.getString("Data")).placeholder(R.drawable.ic_user_placeholder)
                            .error(R.drawable.ic_user_placeholder)
                            .into(patientImg);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }


            @Override
            public void onError(JSONObject result) {
                hud.dismiss();
                //dismissProgressDialog();
            }
        });
    }

    private void getDrugList() {


        Log.e("Step", "==============4");
        if (!swipeRefreshLayout.isRefreshing())
            hud.show();

        NetworkUtility.makeArrayResponseReq(API.GetPrescriptionDataByPatient + patientId, new ArrayMap<String, String>(), API.GetPrescriptionDataByPatient, new VolleyStringBack() {
            @Override
            public void onSuccess(String result) {
                try {

                    drugList = MyApplication.getGson().fromJson(result, new TypeToken<List<DrugResponse>>() {
                    }.getType());

//                    try {
//                        Collections.sort(drugList);
//                        Collections.reverse(drugList);
//                    } catch (Exception ex) {
//                        ex.printStackTrace();
//                    }
//                    Collections.sort(drugList);
                    Collections.sort(drugList, new Comparator<DrugResponse>() {
                        public int compare(DrugResponse o1, DrugResponse o2) {
                            return o1.getDrug().compareTo(o2.getDrug());
                        }
                    });
                    swipeRefreshLayout.setRefreshing(false);
                    drugListView.setAdapter(new PatientActiveDrugListAdapter(PatientActiveDrugListActivity.this, drugList));
                    hud.dismiss();



                } catch (Exception ex) {
                    ex.printStackTrace();
                    hud.dismiss();
                    swipeRefreshLayout.setRefreshing(false);
                }
            }

            @Override
            public void onError(String result) {
                swipeRefreshLayout.setRefreshing(false);
                hud.dismiss();

            }
        });

        Collections.sort(drugList, new Comparator<DrugResponse>() {
            public int compare(DrugResponse o1, DrugResponse o2) {
                return o1.getDrug().compareTo(o2.getDrug());
            }
        });
        adapter = new PatientActiveDrugListAdapter(this, drugList);
        drugListView.setAdapter(adapter);
        swipeRefreshLayout.setRefreshing(false);
    }
}