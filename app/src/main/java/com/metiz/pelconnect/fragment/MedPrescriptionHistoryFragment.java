package com.metiz.pelconnect.fragment;


import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
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

import com.kaopiz.kprogresshud.KProgressHUD;
import com.metiz.pelconnect.Adapter.MedTakenPrescriptionAdapter;
import com.metiz.pelconnect.MyApplication;
import com.metiz.pelconnect.R;
import com.metiz.pelconnect.model.GetGivenMedPassPrescriptionModel;
import com.metiz.pelconnect.retrofit.ApiClient;
import com.metiz.pelconnect.retrofit.ApiInterface;
import com.metiz.pelconnect.util.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class MedPrescriptionHistoryFragment extends Fragment {

    @BindView(R.id.search)
    SearchView search;
    @BindView(R.id.rv_taken_prescription_list)
    RecyclerView rvTakenPrescriptionList;
    @BindView(R.id.swipe_refresh)
    SwipeRefreshLayout swipeRefresh;
    Unbinder unbinder;
    List<GetGivenMedPassPrescriptionModel.DataBean.ModelTakenPrescription> modelTakenPrescriptionList = new ArrayList<>();
    MedTakenPrescriptionAdapter adapter;
    int PatientId = 0;
    private KProgressHUD hud;
    public MedPrescriptionHistoryFragment() {
    }
    @SuppressLint("ValidFragment")
    public MedPrescriptionHistoryFragment(int patientId) {
        this.PatientId= patientId;
    }

    ApiInterface apiService;
    @Override
    public void onResume() {
        super.onResume();
        initRecyclerView();
        getTakenPrescription(MyApplication.getPrefranceDataInt("ExFacilityID"));

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_med_prescription_history, container, false);
        unbinder = ButterKnife.bind(this, view);
        apiService =
                ApiClient.getClient().create(ApiInterface.class);
        search.setQueryHint("Search Drug");
        search.setIconifiedByDefault(false);
        search.setIconified(false);
        search.setVisibility(View.GONE);
        final InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);

        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getTakenPrescription(MyApplication.getPrefranceDataInt("ExFacilityID"));
            }
        });

        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                try {
                    if (!TextUtils.isEmpty(newText.trim())) {
                        if (rvTakenPrescriptionList.getAdapter() != null) {
                            newText.replace(",", "");
                            ((MedTakenPrescriptionAdapter) rvTakenPrescriptionList.getAdapter()).filter(newText);
                        }
                    } else {
                        ((MedTakenPrescriptionAdapter) rvTakenPrescriptionList.getAdapter()).filter("");

                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                return false;
            }
        });
        return view;

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    private void initRecyclerView() {
        rvTakenPrescriptionList.setLayoutManager(new LinearLayoutManager(getActivity()));
        hud = KProgressHUD.create(getContext())
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setLabel("Please wait...")
                .setCancellable(false);
    }

    private void getTakenPrescription(int facilityID) {
        Log.e("Step", "==============4");
        if (!swipeRefresh.isRefreshing())
            hud.show();

//        Utils.showProgressDialog(getActivity());


        JSONObject param = new JSONObject();
        try {
            param.put("FacilityID", facilityID);
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
        Call<GetGivenMedPassPrescriptionModel> call = apiService.GetGivenMedPassDrugDetailsByPatientID(PatientId,facilityID, new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()).format(new Date()));
        call.enqueue(new Callback<GetGivenMedPassPrescriptionModel>() {
            @SuppressLint("DefaultLocale")
            @Override
            public void onResponse(@NonNull Call<GetGivenMedPassPrescriptionModel> call, @NonNull Response<GetGivenMedPassPrescriptionModel> response) {
                hud.dismiss();
                if (Utils.checkInternetConnection(getContext())) {
                    try {
                        int response_code = response.body().getResponseStatus();
                        Log.e("onResponse111:", "tag111: " + response.body());
                        if (modelTakenPrescriptionList.size() > 0) {
                            modelTakenPrescriptionList.clear();
                        }
                        if (response_code == 1) {

                            modelTakenPrescriptionList = response.body().getData().getObjdetails();
                            adapter = new MedTakenPrescriptionAdapter(getActivity(), getActivity(), modelTakenPrescriptionList);
                            rvTakenPrescriptionList.setAdapter(adapter);
                            search.setVisibility(View.VISIBLE);
                            //      llSearchHistory.setVisibility(View.VISIBLE);
                            search.clearFocus();
                            Utils.hide_keyboard(getActivity());
                            //     dismissProgressDialog();
                            swipeRefresh.setRefreshing(false);
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        //dismissProgressDialog();
                    }
                }

            }

            @Override
            public void onFailure(@NonNull Call<GetGivenMedPassPrescriptionModel> call, Throwable t) {
                Log.e("TAG", t.toString());
                hud.dismiss();
            }
        });
       /* NetworkUtility.makeJSONObjectRequest(API.GetGivenMedPassDrugDetailsByPatientID + "?FacilityID=" + facilityID + "&PatientID=" + PatientId + "&currdate="+ new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()).format(new Date()), new JSONObject(), API.GetGivenMedPassDrugDetailsByPatientID, new VolleyCallBack() {
            @Override
            public void onSuccess(JSONObject result) {

                try {

                    if (modelTakenPrescriptionList.size() > 0) {
                        modelTakenPrescriptionList.clear();
                    }

                    if (result != null) {


                        Type listType = new TypeToken<List<ModelTakenPrescription>>() {
                        }.getType();
                        modelTakenPrescriptionList = Application.getGson().fromJson(result.getJSONArray("Data").toString(), listType);

                        adapter = new MedTakenPrescriptionAdapter(getActivity(), modelTakenPrescriptionList);
                        rvTakenPrescriptionList.setAdapter(adapter);

                        search.setVisibility(View.VISIBLE);
                        search.clearFocus();
                        Utils.hide_keyboard(getActivity());

                        dismissProgressDialog();

                        swipeRefresh.setRefreshing(false);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    dismissProgressDialog();


                }
            }

            @Override
            public void onError(JSONObject result) {
                dismissProgressDialog();

                swipeRefresh.setRefreshing(false);
            }
        });*/
    }

}
