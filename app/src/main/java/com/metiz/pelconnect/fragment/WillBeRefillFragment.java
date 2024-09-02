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
import com.metiz.pelconnect.Adapter.WillBeRifillAdapter;
import com.metiz.pelconnect.MyApplication;
import com.metiz.pelconnect.R;
import com.metiz.pelconnect.model.NewModelWillBeRefill;
import com.metiz.pelconnect.model.WillBeRefill;
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
public class WillBeRefillFragment extends BaseFragment {
    List<WillBeRefill> willBeRefillList = new ArrayList<>();
    WillBeRifillAdapter adapter;
    @BindView(R.id.rv_will_be_refill)
    RecyclerView rvWillBeRefill;
    Unbinder unbinder;
    String cycleStartDate = "";

    public WillBeRefillFragment() {
        // Required empty public constructor
    }

    @SuppressLint("ValidFragment")
    public WillBeRefillFragment(String cycleStartDate) {
        this.cycleStartDate = cycleStartDate;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_will_be_refill, container, false);
        unbinder = ButterKnife.bind(this, view);
        initRecyclerView();
        getWillBeRifillList();
        return view;
    }


    private void getWillBeRifillList() {
        showProgressDialog(getActivity());
        NetworkUtility.makeJSONObjectRequest(API.GetCycleListofDetails + "?FacilityID=" + MyApplication.getPrefranceDataInt("ExFacilityID") + "&CycleStartDate=" + cycleStartDate, new JSONObject(), API.GetCycleListofDetails, new VolleyCallBack() {
            @Override
            public void onSuccess(JSONObject result) {
                dismissProgressDialog();

                try {

                    result.getJSONObject("Data").getJSONArray("lstWillberefill").toString();
                    Log.e("MyResponce", result.getJSONObject("Data").getJSONArray("lstWillberefill").toString());
                    ArrayList<NewModelWillBeRefill> newModelWillBeRefillArrayList = new ArrayList<>();
                    for (int i = 0; i < result.getJSONObject("Data").getJSONArray("lstWillberefill").length(); i++) {
                        JSONObject jsonObject = result.getJSONObject("Data").getJSONArray("lstWillberefill").getJSONObject(i);
                        ArrayList<WillBeRefill> myList = MyApplication.getGson().fromJson(jsonObject.getJSONArray("objlist").toString(), new TypeToken<ArrayList<WillBeRefill>>() {
                        }.getType());

                        NewModelWillBeRefill newModelWillBeRefill = new NewModelWillBeRefill();
                        newModelWillBeRefill.setWillBeRefillArrayList(myList);
                        newModelWillBeRefillArrayList.add(newModelWillBeRefill);
                    }

                    adapter = new WillBeRifillAdapter(getActivity(), newModelWillBeRefillArrayList);
                    adapter.notifyDataSetChanged();
                    rvWillBeRefill.setAdapter(adapter);


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
        rvWillBeRefill.setLayoutManager(new LinearLayoutManager(getActivity()));

    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
