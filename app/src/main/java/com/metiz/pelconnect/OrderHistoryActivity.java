package com.metiz.pelconnect;

import android.app.ProgressDialog;
import android.os.Bundle;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.gson.reflect.TypeToken;
import com.metiz.pelconnect.Adapter.OrderHistoryAdapter;
import com.metiz.pelconnect.model.Facility;
import com.metiz.pelconnect.model.OrderHistory;
import com.metiz.pelconnect.network.API;
import com.metiz.pelconnect.network.NetworkUtility;
import com.metiz.pelconnect.network.VolleyCallBack;
import com.metiz.pelconnect.util.GlobalArea;
import com.metiz.pelconnect.util.Utils;

import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class OrderHistoryActivity extends BaseActivity {

    @BindView(R.id.img_back)
    ImageView imgBack;
    @BindView(R.id.toolbar_title)
    TextView toolbarTitle;
    @BindView(R.id.toolbar_title_logo)
    ImageView toolbar_title_logo;
    @BindView(R.id.toolbar_sub_title)
    TextView toolbarSubTitle;
    @BindView(R.id.img_change)
    ImageView imgChange;
    @BindView(R.id.linearLayout)
    LinearLayout linearLayout;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.search_view)
    SearchView search;
    @BindView(R.id.patient_list)
    RecyclerView orderListRecyclerView;
    @BindView(R.id.swipe_refresh)
    SwipeRefreshLayout swipeRefresh;
    @BindView(R.id.ll_banner)
    LinearLayout llBanner;
    private ProgressDialog progressDialog;
    private List<OrderHistory> orderList = new ArrayList<>();
    private OrderHistoryAdapter orderHistoryAdapter;
    private Spinner spinner_facility;
    private List<Facility> facilityList;
    String patientId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history);
        ButterKnife.bind(this);
        imgBack.setVisibility(View.VISIBLE);
        orderListRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        initView();
        initProgress();
        toolbarTitle.setVisibility(View.GONE);
        toolbar_title_logo.setVisibility(View.VISIBLE);
        llBanner.setVisibility(View.GONE);
        if (getIntent().getBooleanExtra("isFacility", false)) {
            patientId = getIntent().getStringExtra("patientId");
            toolbarSubTitle.setText(GlobalArea.selectedFacility);
            getOrdersByPatientID(patientId);
            return;
        }

        try {
            List<Facility> list = MyApplication.getGson().fromJson(MyApplication.getPrefranceData("Facility"), new TypeToken<List<Facility>>() {
            }.getType());
            if (list.size() == 1) {
                imgChange.setVisibility(View.GONE);
                toolbarSubTitle.setText(list.get(0).getFacilityName());
            } else {
                toolbarSubTitle.setText(list.get(MyApplication.getPrefranceDataInt("facility_selected_index")).getFacilityName());
            }
            getOrdersByFacilityID();
        } catch (Exception ex) {

        }

        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (getIntent().getBooleanExtra("isFacility", false)) {
                    getOrdersByPatientID(patientId);
                } else {
                    getOrdersByFacilityID();

                }

            }
        });
    }




    private void initView() {
        search.setQueryHint("Search Order");
        search.setIconifiedByDefault(false);
        search.setIconified(false);
        Utils.hideKeybord(this);
        search.setVisibility(View.GONE);
        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (!TextUtils.isEmpty(newText.trim())) {
                    if (orderListRecyclerView.getAdapter() != null) {
                        ((OrderHistoryAdapter) orderListRecyclerView.getAdapter()).filter(newText);
                    }
                } else {
                    ((OrderHistoryAdapter) orderListRecyclerView.getAdapter()).filter("");

                }
                return false;
            }
        });
    }

    @OnClick({R.id.img_back, R.id.img_change})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                finish();
                break;
            case R.id.img_change:
                break;
        }
    }

    private void initProgress() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);
    }

    private void getOrdersByFacilityID() {


        Log.e("Step", "==============4");
        if (!swipeRefresh.isRefreshing())
            progressDialog.show();


        NetworkUtility.makeJSONObjectRequest(API.GetOrderByFacilityID + "?FacilityID=" + MyApplication.getPrefranceDataInt("ExFacilityID"), new JSONObject(), API.GetOrderByFacilityID, new VolleyCallBack() {
            @Override
            public void onSuccess(JSONObject result) {
                try {
                    orderList.clear();

                    if (result != null) {
                        Type listType = new TypeToken<List<OrderHistory>>() {
                        }.getType();

                        orderList = MyApplication.getGson().fromJson(result.getJSONArray("Data").toString(), listType);

                        try {
                            Collections.sort(orderList);
                            Collections.reverse(orderList);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }

                        orderHistoryAdapter = new OrderHistoryAdapter(OrderHistoryActivity.this, orderList);
                        orderListRecyclerView.setAdapter(orderHistoryAdapter);

                        search.setVisibility(View.VISIBLE);
                        search.clearFocus();
                        Utils.hideKeybord(OrderHistoryActivity.this);
                        if (progressDialog != null && progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }

                        swipeRefresh.setRefreshing(false);

                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    if (progressDialog != null && progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                    swipeRefresh.setRefreshing(false);
                }
            }

            @Override
            public void onError(JSONObject result) {
                swipeRefresh.setRefreshing(false);
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }

            }
        });
    }

    private void getOrdersByPatientID(String patientId) {
        search.setVisibility(View.GONE);
        imgChange.setVisibility(View.GONE);
        Log.e("Step", "==============4");
        if (!swipeRefresh.isRefreshing())
            progressDialog.show();

        NetworkUtility.makeJSONObjectRequest(API.GetOrderByPatientID + "?PatientID=" + patientId,new JSONObject(), API.GetOrderByPatientID, new VolleyCallBack() {
            @Override
            public void onSuccess(JSONObject result) {
                try {
                    orderList.clear();

                    if (result != null) {
                        Type listType = new TypeToken<List<OrderHistory>>() {
                        }.getType();

                        orderList = MyApplication.getGson().fromJson(result.getJSONArray("Data").toString(), listType);

                        orderHistoryAdapter = new OrderHistoryAdapter(OrderHistoryActivity.this, orderList);
                        orderListRecyclerView.setAdapter(orderHistoryAdapter);

                        Utils.hideKeybord(OrderHistoryActivity.this);

                        swipeRefresh.setRefreshing(false);

                        if (progressDialog != null && progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }

                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    if (progressDialog != null && progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                    swipeRefresh.setRefreshing(false);
                }
            }

            @Override
            public void onError(JSONObject result) {
                swipeRefresh.setRefreshing(false);
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }

            }
        });
    }



    private void fillFacilitySpinner() {
        try {

            Type listType = new TypeToken<List<Facility>>() {
            }.getType();
            facilityList = MyApplication.getGson().fromJson(MyApplication.getPrefranceData("Facility"), listType);

            ArrayAdapter<Facility> dataAdapter = new ArrayAdapter<Facility>(this,
                    R.layout.simple_spinner_item, facilityList);
            dataAdapter.setDropDownViewResource(R.layout.simple_spinner_item);

            spinner_facility.setAdapter(dataAdapter);

            if (MyApplication.getPrefranceDataInt("facility") != 0) {
                spinner_facility.setSelection(MyApplication.getPrefranceDataInt("facility_selected_index"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
