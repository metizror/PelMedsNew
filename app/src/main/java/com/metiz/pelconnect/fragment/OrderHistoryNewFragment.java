package com.metiz.pelconnect.fragment;


import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.google.gson.reflect.TypeToken;
import com.metiz.pelconnect.Adapter.OrderHistoryAdapterForNewClient;
import com.metiz.pelconnect.MyApplication;
import com.metiz.pelconnect.R;
import com.metiz.pelconnect.activity.AddOrderActivity;
import com.metiz.pelconnect.model.Facility;
import com.metiz.pelconnect.model.OrderHistory;
import com.metiz.pelconnect.network.API;
import com.metiz.pelconnect.network.NetworkUtility;
import com.metiz.pelconnect.network.VolleyCallBack;
import com.metiz.pelconnect.util.Utils;

import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class OrderHistoryNewFragment extends Fragment {


    //    @BindView(R.id.toolbar_sub_title)
//    TextView toolbarSubTitle;
//    @BindView(R.id.img_change)
//    ImageView imgChange;
//    @BindView(R.id.linearLayout)
//    LinearLayout linearLayout;
    @BindView(R.id.search)
    SearchView search;
    @BindView(R.id.patient_list)
    RecyclerView orderListRecyclerView;
    @BindView(R.id.swipe_refresh)
    SwipeRefreshLayout swipeRefresh;
    @BindView(R.id.fab)
    FloatingActionButton fab;
    @BindView(R.id.ll_search)
    LinearLayout llSearch;
    // @BindView(R.id.ll_banner)
    //   LinearLayout llBanner;
    private ProgressDialog progressDialog;
    private List<OrderHistory> orderList = new ArrayList<>();
    private OrderHistoryAdapterForNewClient orderHistoryAdapter;
    private Spinner spinner_facility;
    private List<Facility> facilityList;
    String patientId;
    boolean isFromPatientList;
    boolean isFacility;
    int facility =0;

    public OrderHistoryNewFragment() {
        // Required empty public constructor

    }

    @SuppressLint("ValidFragment")
    public OrderHistoryNewFragment(boolean isFromPatientList, boolean isFacility,int facility) {

        this.isFromPatientList = isFromPatientList;
        this.isFacility = isFacility;
        this.facility =facility;
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_order_history_new, container, false);
        ButterKnife.bind(this, view);
        orderListRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        initView();
        initProgress();
        //   llBanner.setVisibility(View.GONE);
        if (isFacility) {
            patientId = getActivity().getIntent().getStringExtra("patientId");
            //   toolbarSubTitle.setText(GlobalArea.selectedFacility);
            getOrdersByPatientID(patientId);

        }

        try {
            List<Facility> list = MyApplication.getGson().fromJson(MyApplication.getPrefranceData("Facility"), new TypeToken<List<Facility>>() {
            }.getType());
            if (list.size() == 1) {
                //   imgChange.setVisibility(View.GONE);
                //   toolbarSubTitle.setText(list.get(0).getFacilityName());
            } else {
                //   toolbarSubTitle.setText(list.get(Application.getPrefranceDataInt("facility_selected_index")).getFacilityName());
            }
            getOrdersByFacilityID();
        } catch (Exception ex) {

        }

        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (isFacility) {
                    getOrdersByPatientID(patientId);
                } else {
                    getOrdersByFacilityID();

                }

            }
        });
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), AddOrderActivity.class));

            }
        });

        orderListRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx,int dy){
                super.onScrolled(recyclerView, dx, dy);

                if (dy >0) {
                    // Scroll Down
                    if (fab.isShown()) {
                        fab.hide();
                    }
                }
                else if (dy <0) {
                    // Scroll Up
                    if (!fab.isShown()) {
                        fab.show();
                    }
                }
            }
        });


        return view;
    }

    private void initView() {
        search.setQueryHint("Search Patient");
        search.setIconifiedByDefault(false);
        search.setIconified(false);
        Utils.hide_keyboard(getActivity());
        llSearch.setVisibility(View.GONE);
        search.setVisibility(View.GONE);
        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.e("TAG", "onQueryTextChange: "+newText );
                try {
                    if (!TextUtils.isEmpty(newText.trim())) {
                        if (orderListRecyclerView.getAdapter() != null) {
                            newText.replace(",", "");
                            ((OrderHistoryAdapterForNewClient) orderListRecyclerView.getAdapter()).filter(newText);
                        }
                    } else {
                        ((OrderHistoryAdapterForNewClient) orderListRecyclerView.getAdapter()).filter("");
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                return false;
            }
        });
    }

    private void initProgress() {
        progressDialog = new ProgressDialog(getActivity());
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
                        orderHistoryAdapter = new OrderHistoryAdapterForNewClient(getActivity(), orderList);
                        orderListRecyclerView.setAdapter(orderHistoryAdapter);
                        search.setVisibility(View.VISIBLE);
                        llSearch.setVisibility(View.VISIBLE);
                        search.clearFocus();
                        final InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
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
        //  imgChange.setVisibility(View.GONE);
        Log.e("Step", "==============4");
        if (!swipeRefresh.isRefreshing())
            progressDialog.show();

        NetworkUtility.makeJSONObjectRequest(API.GetOrderByPatientID + "?PatientID=" + patientId, new JSONObject(), API.GetOrderByPatientID, new VolleyCallBack() {
            @Override
            public void onSuccess(JSONObject result) {
                try {
                    orderList.clear();

                    if (result != null) {
                        Type listType = new TypeToken<List<OrderHistory>>() {
                        }.getType();

                        orderList = MyApplication.getGson().fromJson(result.getJSONArray("Data").toString(), listType);

                        orderHistoryAdapter = new OrderHistoryAdapterForNewClient(getActivity(), orderList);
                        orderListRecyclerView.setAdapter(orderHistoryAdapter);
                        Utils.hide_keyboard(getActivity());
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

//    @OnClick(R.id.img_change)
//    public void onViewClicked() {
//        changeFacilityDialog(false);
//    }


    private void fillFacilitySpinner() {
        try {

            Type listType = new TypeToken<List<Facility>>() {
            }.getType();
            facilityList = MyApplication.getGson().fromJson(MyApplication.getPrefranceData("Facility"), listType);

            ArrayAdapter<Facility> dataAdapter = new ArrayAdapter<Facility>(getActivity(),
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}
