package com.metiz.pelconnect.fragment;


import android.annotation.SuppressLint;
import android.app.ProgressDialog;
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
import android.widget.LinearLayout;

import com.google.gson.reflect.TypeToken;
import com.metiz.pelconnect.Adapter.PatientAdapter;
import com.metiz.pelconnect.AddClientPharmaActivity;
import com.metiz.pelconnect.MyApplication;
import com.metiz.pelconnect.R;
import com.metiz.pelconnect.model.PatientPOJO;
import com.metiz.pelconnect.network.API;
import com.metiz.pelconnect.network.NetworkUtility;
import com.metiz.pelconnect.network.VolleyCallBack;
import com.metiz.pelconnect.util.Utils;

import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * A simple {@link Fragment} subclass.
 */
public class PatientNewFragment extends BaseFragment {


    @BindView(R.id.rv_patient_list)
    RecyclerView rv_patient_list;
    @BindView(R.id.swipe_refresh)
    SwipeRefreshLayout swipeRefresh;
    Unbinder unbinder;
   ProgressDialog progressDialog;
    @BindView(R.id.fab)
    FloatingActionButton fab;
    @BindView(R.id.search)
    SearchView search;
    @BindView(R.id.ll_search)
    LinearLayout llSearch;
    private PatientAdapter patientAdapter;
    List<PatientPOJO> patientList = new ArrayList<>();
    int facilityId = 0;
    private RecyclerView.LayoutManager layoutManager;

    public PatientNewFragment() {
        // Required empty public constructor
    }

    @SuppressLint("ValidFragment")
    public PatientNewFragment(int facility) {
        // Required empty public constructor
        this.facilityId = facility;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_patient_new, container, false);
        ButterKnife.bind(this, view);
      initProgress();

        search.setQueryHint("Search Patient");
        search.setIconifiedByDefault(false);
        search.setIconified(false);
        search.setVisibility(View.GONE);
        llSearch.setVisibility(View.GONE);
        Utils.hide_keyboard(getActivity());

        rv_patient_list.setLayoutManager(new LinearLayoutManager(getActivity()));

        getPatientList(facilityId);

        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                try {
                    if (!TextUtils.isEmpty(newText.trim())) {
                        if (rv_patient_list.getAdapter() != null) {
                            newText.replace(",", "");
                            ((PatientAdapter) rv_patient_list.getAdapter()).filter(newText);
                        }
                    } else {
                        ((PatientAdapter) rv_patient_list.getAdapter()).filter("");
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                return false;
            }
        });
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getPatientList(facilityId);
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), AddClientPharmaActivity.class));
            }
        });




        rv_patient_list.addOnScrollListener(new RecyclerView.OnScrollListener() {
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


    @Override
    public void onDestroyView() {
        super.onDestroyView();

    }

    private void initProgress() {
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);
    }

    private void getPatientList(int facilityID) {
        Log.e("Step", "==============4");
        if (!swipeRefresh.isRefreshing())
            progressDialog.show();

      /*  JSONObject param = new JSONObject();
        try {
            param.put("FacilityID", facilityID);
        } catch (JSONException ex) {
            ex.printStackTrace();
        }*/
        NetworkUtility.makeJSONObjectRequest(API.Patient_List_URL + "?FacilityID=" + facilityID, new JSONObject(), API.Patient_List_URL, new VolleyCallBack() {
            @Override
            public void onSuccess(JSONObject result) {
                try {
                    patientList.clear();
                    if (progressDialog != null && progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                    if (result != null) {
                        Type listType = new TypeToken<List<PatientPOJO>>() {
                        }.getType();
                        patientList = MyApplication.getGson().fromJson(result.getJSONArray("Data").toString(), listType);
                        Log.e("patientList:", "onSuccess: "+patientList );
                        //for sorting list alphabetically by last name
                        Collections.sort(patientList, new Comparator<PatientPOJO>() {
                            @Override
                            public int compare(PatientPOJO lhs, PatientPOJO rhs) {
                                return lhs.getLastname().compareTo(rhs.getLastname());
                            }
                        });
                        patientAdapter = new PatientAdapter(getActivity(), patientList);
                        rv_patient_list.setAdapter(patientAdapter);

                        search.setVisibility(View.VISIBLE);
                        llSearch.setVisibility(View.VISIBLE);

                        search.clearFocus();
                        Utils.hide_keyboard(getActivity());
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
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                swipeRefresh.setRefreshing(false);
            }
        });
    }

}
