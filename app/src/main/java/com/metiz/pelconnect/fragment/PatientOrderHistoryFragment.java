package com.metiz.pelconnect.fragment;

import android.app.Dialog;
import android.os.Bundle;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.gson.reflect.TypeToken;
import com.google.zxing.integration.android.IntentIntegrator;
import com.metiz.pelconnect.Adapter.OrderHistoryAdapter;
import com.metiz.pelconnect.Adapter.SearchableDrugAdapter;
import com.metiz.pelconnect.MyApplication;
import com.metiz.pelconnect.R;
import com.metiz.pelconnect.TabActivity;
import com.metiz.pelconnect.model.Delivery;
import com.metiz.pelconnect.model.Order;
import com.metiz.pelconnect.model.OrderHistory;
import com.metiz.pelconnect.model.PatientPOJO;
import com.metiz.pelconnect.model.PrescriptionData;
import com.metiz.pelconnect.network.API;
import com.metiz.pelconnect.network.NetworkUtility;
import com.metiz.pelconnect.network.VolleyCallBack;
import com.metiz.pelconnect.util.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by hp on 13/2/18.
 */
public class PatientOrderHistoryFragment extends BaseFragment {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";
    @BindView(R.id.patient_list)
    RecyclerView recyclerView;
    @BindView(R.id.swipe_refresh)
    SwipeRefreshLayout swipeRefresh;
    Unbinder unbinder;
    @BindView(R.id.txt_order)
    TextView txtOrder;
    private List<OrderHistory> orderList = new ArrayList<>();
    private OrderHistoryAdapter orderHistoryAdapter;
    private List<Delivery> deliverylist = new ArrayList<>();
    private List<Order> orderlist = new ArrayList<>();
    private EditText edt_rx_number;
    private AutoCompleteTextView edt_drug;
    private int SCANNED_BARCODE_ID = 0;
    private Spinner spinner_delivery, spinner_order;
    private List<PrescriptionData> prescriptionList;

    public PatientOrderHistoryFragment() {
    }

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static PatientOrderHistoryFragment newInstance(String patientID) {
        PatientOrderHistoryFragment fragment = new PatientOrderHistoryFragment();
        Bundle args = new Bundle();
        args.putString(ARG_SECTION_NUMBER, patientID);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_patient_order_history, container, false);
//        TextView textView = (TextView) rootView.findViewById(R.id.section_label);
//        textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));
        unbinder = ButterKnife.bind(this, rootView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getOrdersByPatientID(getArguments().getString(ARG_SECTION_NUMBER));
            }
        });


        txtOrder.setVisibility(View.GONE);
        getOrdersByPatientID(getArguments().getString(ARG_SECTION_NUMBER));

        return rootView;
    }


    private boolean isFragmentLoaded = false;

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            // Load your data here or do network operations here
            isFragmentLoaded = true;
//            getOrdersByPatientID(getArguments().getString(ARG_SECTION_NUMBER));

        }
    }


    public void getOrdersByPatientID(String patientId) {

        Log.e("Step", "==============4");
        if (!swipeRefresh.isRefreshing())
            showProgressDialog(getActivity());

        NetworkUtility.makeJSONObjectRequest(API.GetOrderByPatientID + "?PatientID=" + patientId, new JSONObject(), API.GetOrderByPatientID, new VolleyCallBack() {
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
//                            Collections.reverse(orderList);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                        orderHistoryAdapter = new OrderHistoryAdapter(getActivity(), orderList);
                        recyclerView.setAdapter(orderHistoryAdapter);
                        swipeRefresh.setRefreshing(false);

                        dismissProgressDialog();

                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    dismissProgressDialog();
                    swipeRefresh.setRefreshing(false);
                }
            }

            @Override
            public void onError(JSONObject result) {
                swipeRefresh.setRefreshing(false);
                dismissProgressDialog();

            }
        });
    }

    public void getOrderndDelivery(final PatientPOJO patient) {
        showProgressDialog(getActivity());

        NetworkUtility.makeJSONObjectRequest(API.OrderDelivertype, new JSONObject(), API.OrderDelivertype, new VolleyCallBack() {
            @Override
            public void onSuccess(JSONObject result) {
                try {
                    dismissProgressDialog();
                    if (result != null) {

                        deliverylist = MyApplication.getGson().fromJson(result.getJSONObject("Data").getJSONArray("Delivery").toString(), new TypeToken<List<Delivery>>() {
                        }.getType());
                        orderlist = MyApplication.getGson().fromJson(result.getJSONObject("Data").getJSONArray("Ordertype").toString(), new TypeToken<List<Order>>() {
                        }.getType());

                        openAddOrderDialog(patient);

                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            @Override
            public void onError(JSONObject result) {
                dismissProgressDialog();

            }
        });
    }

    private void openAddOrderDialog(final PatientPOJO patientPojo) {

        final Dialog dialog = new Dialog(getActivity());
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setContentView(R.layout.dialog_add_order);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        Window window = dialog.getWindow();
        lp.copyFrom(window.getAttributes());
//This makes the dialog take up the full width
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(lp);

        Button add = (Button) dialog.findViewById(R.id.btn_dialog_add);
        Button cancel = (Button) dialog.findViewById(R.id.btn_dialog_cancel);
        ImageView img_barcode_drug = (ImageView) dialog.findViewById(R.id.img_barcode_drug);
        ImageView img_barcode_rx = (ImageView) dialog.findViewById(R.id.img_barcode_rx);
        edt_rx_number = (EditText) dialog.findViewById(R.id.edt_rx_number);
        img_barcode_drug.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SCANNED_BARCODE_ID = 1;
                setBarcode("scan drug");
            }
        });

        img_barcode_rx.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SCANNED_BARCODE_ID = 0;
                setBarcode("scan Rx Number");
            }
        });
        edt_drug = (AutoCompleteTextView) dialog.findViewById(R.id.edt_drug);


        final TextView txt_dialog_facility = (TextView) dialog.findViewById(R.id.txt_dialog_facility);
//        if (imgChange.getVisibility() == View.VISIBLE) {
//            txt_dialog_facility.setText(facilityList.get(Application.getPrefranceDataInt("facility_selected_index")).getFacilityName());
//        }


        spinner_delivery = (Spinner) dialog.findViewById(R.id.spinner_delivery);
        spinner_order = (Spinner) dialog.findViewById(R.id.spinner_order);
        final EditText edt_notes = (EditText) dialog.findViewById(R.id.edt_notes);

        ArrayAdapter<Order> orderAdapter = new ArrayAdapter<Order>(getActivity(),
                R.layout.simple_spinner_item, orderlist);
        orderAdapter.setDropDownViewResource(R.layout.simple_spinner_item);
        spinner_order.setAdapter(orderAdapter);

        ArrayAdapter<Delivery> dataAdapter = new ArrayAdapter<Delivery>(getActivity(),
                R.layout.simple_spinner_item, deliverylist);
        dataAdapter.setDropDownViewResource(R.layout.simple_spinner_item);
        spinner_delivery.setAdapter(dataAdapter);

        final AutoCompleteTextView auto_patient = (AutoCompleteTextView) dialog.findViewById(R.id.auto_patient);

//        SearchablePatientAdapter adapter = new SearchablePatientAdapter(getActivity(), R.layout.simple_spinner_item, patientList);
//        auto_patient.setAdapter(adapter);
//        auto_patient.setThreshold(1);


        if (patientPojo != null) {
            auto_patient.setText(patientPojo.getLastname() + "," + patientPojo.getFirstname());
            getDrugandRx(patientPojo.getExternal_patient_id());
            auto_patient.setKeyListener(null);
        }

//        else {
//            auto_patient.setText("Patient Name");
//            patient = null;
//        }


        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (auto_patient.getText().toString().trim().length() > 0 && edt_drug.getText().toString().trim().length() > 0 && spinner_delivery.getAdapter() != null && spinner_order.getAdapter() != null) {
                    JSONObject json = new JSONObject();
                    try {
                        json.put("Deliverytype", ((Delivery) spinner_delivery.getAdapter().getItem(spinner_delivery.getSelectedItemPosition())).getDeliveryID());
                        json.put("Ordertype", ((Order) spinner_order.getAdapter().getItem(spinner_order.getSelectedItemPosition())).getOrdertypeID());
                        json.put("FacilityID", MyApplication.getPrefranceDataInt("ExFacilityID"));
                        json.put("Rxnumber", edt_rx_number.getText().toString());
                        json.put("PatientName", auto_patient.getText().toString());
                        json.put("drug", edt_drug.getText().toString());
                        json.put("CreatedBy", MyApplication.getPrefranceData("UserID"));
                        json.put("UpdatedBy", MyApplication.getPrefranceData("UserID"));
                        json.put("Note", edt_notes.getText().toString());
                        if (TabActivity.patient != null) {
                            json.put("PatientID", TabActivity.patient.getExternal_patient_id());
                        } else {
                            json.put("PatientID", "0");
                        }
                    } catch (Exception ex) {

                    }
                    addOrderApicall(json);
                    dialog.dismiss();


                }

            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void getDrugandRx(String externalId) {
        showProgressDialog(getActivity());
        NetworkUtility.makeJSONObjectRequest(API.PrescriptionData + "?ExternalPatientID=" + externalId, new JSONObject(), API.PrescriptionData, new VolleyCallBack() {
            @Override
            public void onSuccess(JSONObject result) {
                dismissProgressDialog();
                try {
                    Type listType = new TypeToken<List<PrescriptionData>>() {
                    }.getType();
                    prescriptionList = MyApplication.getGson().fromJson(result.getJSONArray("Data").toString(), listType);
                    SearchableDrugAdapter adapter = new SearchableDrugAdapter(getActivity(), R.layout.simple_spinner_item, prescriptionList);
                    edt_drug.setAdapter(adapter);
                    edt_drug.setThreshold(1);
                    edt_drug.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            edt_drug.showDropDown();

                        }
                    });


                    edt_drug.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                            edt_drug.setEnabled(false);
                            edt_drug.setText(prescriptionList.get(prescriptionList.indexOf(((ListView) adapterView).getAdapter().getItem(i))).getDrug());
                            edt_drug.setEnabled(true);
                            edt_rx_number.setText(prescriptionList.get(prescriptionList.indexOf(((ListView) adapterView).getAdapter().getItem(i))).getExternal_prescription_id());
                        }
                    });

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


    private void setBarcode(String msg) {
        IntentIntegrator integrator = new IntentIntegrator(getActivity());

        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);

        integrator.setPrompt(msg);
        integrator.setBeepEnabled(true);

        integrator.setOrientationLocked(false);

        integrator.initiateScan();
    }

    private void addOrderApicall(JSONObject json) {
        showProgressDialog(getActivity());
        NetworkUtility.makeJSONObjectRequest(API.AddOrder, json, API.AddOrder, new VolleyCallBack() {
            @Override
            public void onSuccess(JSONObject result) {
                dismissProgressDialog();

                try {
                    Utils.showAlertToast(getActivity(), result.getString("Message"));
                    getOrdersByPatientID(TabActivity.patient.getExternal_patient_id());
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


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @OnClick(R.id.txt_order)
    public void onViewClicked() {
        getOrderndDelivery(TabActivity.patient);
    }
}
