package com.metiz.pelconnect.fragment;


import android.annotation.SuppressLint;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.reflect.TypeToken;
import com.metiz.pelconnect.Adapter.NeedRifillAdapter;
import com.metiz.pelconnect.MyApplication;
import com.metiz.pelconnect.R;
import com.metiz.pelconnect.model.ModelNeedRifill;
import com.metiz.pelconnect.model.NewModelNeedRifill;
import com.metiz.pelconnect.network.API;
import com.metiz.pelconnect.network.NetworkUtility;
import com.metiz.pelconnect.network.VolleyCallBack;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * A simple {@link Fragment} subclass.
 */
public class NeedrefillFragment extends BaseFragment {


    @BindView(R.id.rv_need_refill)
    RecyclerView rvNeedRefill;
    Unbinder unbinder;
    NeedRifillAdapter adapter;
    List<ModelNeedRifill> modelNeedRifillList = new ArrayList<>();
    String cycleStartDate="";
    public NeedrefillFragment() {
        // Required empty public constructor
    }

    @SuppressLint("ValidFragment")
    public NeedrefillFragment(String cycleStartDate) {
        this.cycleStartDate =cycleStartDate;

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_needrefill, container, false);
        unbinder = ButterKnife.bind(this, view);
        initRecyclerView();
        getNeedRifillList();
        return view;
    }


    private void getNeedRifillList() {
        showProgressDialog(getActivity());
//        Application.getPrefranceDataInt("ExFacilityID")
        NetworkUtility.makeJSONObjectRequest(API.GetCycleListofDetails + "?FacilityID=" +  MyApplication.getPrefranceDataInt("ExFacilityID") + "&CycleStartDate=" + cycleStartDate, new JSONObject(), API.GetCycleListofDetails, new VolleyCallBack() {
            @Override
            public void onSuccess(JSONObject result) {
                dismissProgressDialog();

                try {

                    result.getJSONObject("Data").getJSONArray("lstNeedrefill").toString();
                    Log.e("MyResponce", result.getJSONObject("Data").getJSONArray("lstNeedrefill").toString());
                    ArrayList<NewModelNeedRifill> newModelWillBeRefillArrayList = new ArrayList<>();
                    for (int i = 0; i < result.getJSONObject("Data").getJSONArray("lstNeedrefill").length(); i++) {
                        JSONObject jsonObject = result.getJSONObject("Data").getJSONArray("lstNeedrefill").getJSONObject(i);
                        ArrayList<ModelNeedRifill> myList = MyApplication.getGson().fromJson(jsonObject.getJSONArray("objlist").toString(), new TypeToken<ArrayList<ModelNeedRifill>>() {
                        }.getType());

                        NewModelNeedRifill newModelWillBeRefill = new NewModelNeedRifill();
                        newModelWillBeRefill.setModelNeedRifillArrayList(myList);
                        newModelWillBeRefillArrayList.add(newModelWillBeRefill);
                    }

                    adapter = new NeedRifillAdapter(getActivity(), newModelWillBeRefillArrayList);
                    adapter.notifyDataSetChanged();
                    rvNeedRefill.setAdapter(adapter);


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(JSONObject result) {
                dismissProgressDialog();


            }
        });
    }

    private void initRecyclerView() {
        rvNeedRefill.setLayoutManager(new LinearLayoutManager(getActivity()));

    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
