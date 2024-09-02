package com.metiz.pelconnect;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
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
import com.google.zxing.integration.android.IntentResult;
import com.metiz.pelconnect.Adapter.PatientAdapter;
import com.metiz.pelconnect.Adapter.SearchableDrugAdapter;
import com.metiz.pelconnect.Adapter.SearchablePatientAdapter;
import com.metiz.pelconnect.model.Delivery;
import com.metiz.pelconnect.model.Facility;
import com.metiz.pelconnect.model.Order;
import com.metiz.pelconnect.model.PatientPOJO;
import com.metiz.pelconnect.model.PrescriptionData;
import com.metiz.pelconnect.network.API;
import com.metiz.pelconnect.network.NetworkUtility;
import com.metiz.pelconnect.network.VolleyCallBack;
import com.metiz.pelconnect.util.GlobalArea;
import com.metiz.pelconnect.util.NotificationListActivity;
import com.metiz.pelconnect.util.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import butterknife.ButterKnife;

public class PatientListActivity extends AppCompatActivity {

    private RecyclerView patientListView;
    List<PatientPOJO> patientList = new ArrayList<>();
    private PatientAdapter patientAdapter;
    Toolbar toolbar;
    ImageView imgLogout, imgChange, imgNotification;
    FloatingActionButton fab;
    ProgressDialog progressDialog;
    Spinner spinner_facility;
    private List<Delivery> deliverylist = new ArrayList<>();
    private List<Order> orderlist = new ArrayList<>();
    private Spinner spinner_delivery, spinner_order;
    private List<Facility> facilityList;
    private TextView toolbar_facility_name;
    private SearchView search;
    private EditText edt_rx_number;
    private AutoCompleteTextView edt_drug;
    private int SCANNED_BARCODE_ID = 0;
    private List<PrescriptionData> prescriptionList;
    private PatientPOJO patient = new PatientPOJO();
    SwipeRefreshLayout swipeRefreshLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_list);
        ButterKnife.bind(this);
        initView();
        initProgress();
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        try {
            if (MyApplication.getPrefranceData("IsConfirm").equalsIgnoreCase("false")) {
                openDialog(true);
                Log.e("Step", "==============1");
            } else if (getIntent().getExtras().containsKey("isFromLogin") && getIntent().getExtras().getBoolean("isFromLogin")) {
                Log.e("Step", "==============2");
                changeFacilityDialog(true);
            } else if (getIntent().getExtras().getBoolean("isFromSpash")) {
                Log.e("Step", "==============3");
                List<Facility> list = MyApplication.getGson().fromJson(MyApplication.getPrefranceData("Facility"), new TypeToken<List<Facility>>() {
                }.getType());
                if (list.size() == 1) {
                    imgChange.setVisibility(View.GONE);
                    toolbar_facility_name.setText(list.get(0).getFacilityName());
                    GlobalArea.selectedFacility = list.get(0).getFacilityName();
                } else {
                    toolbar_facility_name.setText(list.get(MyApplication.getPrefranceDataInt("facility_selected_index")).getFacilityName());
                    GlobalArea.selectedFacility = toolbar_facility_name.getText().toString();
                }
                getPatientList(MyApplication.getPrefranceDataInt("facility"));
            }
        } catch (Exception ex) {

        }

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getPatientList(MyApplication.getPrefranceDataInt("facility"));
            }
        });

    }

    private void initView() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar_facility_name = (TextView) toolbar.findViewById(R.id.toolbar_sub_title);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        search = (SearchView) findViewById(R.id.search);
        imgNotification = (ImageView) findViewById(R.id.img_notification);
        imgNotification.setVisibility(View.VISIBLE);
        imgChange = (ImageView) findViewById(R.id.img_change);
        patientListView = (RecyclerView) findViewById(R.id.patient_list);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        patientListView.setLayoutManager(new LinearLayoutManager(this));

        search.setQueryHint("Search Patient");
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
                try {
                    if (!TextUtils.isEmpty(newText.trim())) {
                        if (patientListView.getAdapter() != null) {
                            ((PatientAdapter) patientListView.getAdapter()).filter(newText);
                        }
                    } else {
                        ((PatientAdapter) patientListView.getAdapter()).filter("");

                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                return false;
            }
        });
    }

    private void initProgress() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);
    }

    private void clickListners() {
        patientListView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0 && fab.getVisibility() == View.VISIBLE) {
                    fab.hide();
                } else if (dy < 0 && fab.getVisibility() != View.VISIBLE) {
                    fab.show();
                }
            }
        });

        // refresh the realm instance

        imgLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                //Yes button clicked
                                MyApplication.clearPrefrences();
                                MyApplication.setPreferencesBoolean("isLogin", false);
                                Intent i = new Intent(PatientListActivity.this, LoginActivity.class);
                                startActivity(i);
                                finish();
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                //No button clicked
                                break;
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(PatientListActivity.this);
                builder.setMessage("Are you sure, you want to logout?").setPositiveButton("Yes", dialogClickListener)
                        .setNegativeButton("No", dialogClickListener).show();

            }
        });
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
////
//                getOrderndDelivery(null);
//            }
//        });

        imgNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent starter = new Intent(PatientListActivity.this, NotificationListActivity.class);
                startActivity(starter);

            }
        });

        imgChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeFacilityDialog(false);
            }
        });

//        ItemClickSupport.addTo(patientListView).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
//            @Override
//            public void onItemClicked(RecyclerView recyclerView, int position, View v) {
//                PatientPOJO patientPOJO = ((PatientAdapter) recyclerView.getAdapter()).filterList.get(position);
////                getOrderndDelivery(patientPOJO);
//
//                Intent i = new Intent(PatientListActivity.this, TabActivity.class);
//                i.putExtra("obj", patientPOJO);
//                startActivity(i);
//            }
//        });

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



    private void openAddOrderDialog(final PatientPOJO patientPojo) {

        final Dialog dialog = new Dialog(PatientListActivity.this);
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

        txt_dialog_facility.setText(toolbar_facility_name.getText().toString());

        spinner_delivery = (Spinner) dialog.findViewById(R.id.spinner_delivery);
        spinner_order = (Spinner) dialog.findViewById(R.id.spinner_order);
        final EditText edt_notes = (EditText) dialog.findViewById(R.id.edt_notes);

        ArrayAdapter<Order> orderAdapter = new ArrayAdapter<Order>(this,
                R.layout.simple_spinner_item, orderlist);
        orderAdapter.setDropDownViewResource(R.layout.simple_spinner_item);
        spinner_order.setAdapter(orderAdapter);

        ArrayAdapter<Delivery> dataAdapter = new ArrayAdapter<Delivery>(this,
                R.layout.simple_spinner_item, deliverylist);
        dataAdapter.setDropDownViewResource(R.layout.simple_spinner_item);
        spinner_delivery.setAdapter(dataAdapter);

        final AutoCompleteTextView auto_patient = (AutoCompleteTextView) dialog.findViewById(R.id.auto_patient);

        SearchablePatientAdapter adapter = new SearchablePatientAdapter(PatientListActivity.this, R.layout.simple_spinner_item, patientList);
        auto_patient.setAdapter(adapter);
        auto_patient.setThreshold(1);

        auto_patient.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                auto_patient.setEnabled(false);
                auto_patient.setText(patientList.get(patientList.indexOf(((ListView) adapterView).getAdapter().getItem(i))).getLastname() + "," + patientList.get(patientList.indexOf(((ListView) adapterView).getAdapter().getItem(i))).getFirstname()
//                        + " " + patientList.get(patientList.indexOf(((ListView) adapterView).getAdapter().getItem(i))).getDob()
                );
                auto_patient.setEnabled(true);
                edt_rx_number.requestFocus();
                getDrugandRx(patientList.get(patientList.indexOf(((ListView) adapterView).getAdapter().getItem(i))).getExternal_patient_id());
                patient = patientList.get(patientList.indexOf(((ListView) adapterView).getAdapter().getItem(i)));
            }
        });


        if (patientPojo != null) {
            auto_patient.setText(patientPojo.getLastname() + "," + patientPojo.getFirstname());
            getDrugandRx(patientPojo.getExternal_patient_id());
            patient = patientPojo;
        } else {
            auto_patient.setText("Patient Name");
            patient = null;
        }
        auto_patient.setEnabled(false);
        auto_patient.setEnabled(true);
        auto_patient.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.toString().length() == 0) {
                    edt_drug.setText("");
                    edt_rx_number.setText("");
                }

                patient = null;
            }
        });

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (auto_patient.getText().toString().trim().length() > 0 && edt_drug.getText().toString().trim().length() > 0 && spinner_delivery.getAdapter() != null && spinner_order.getAdapter() != null) {
                    if (isPatientValid(auto_patient.getText().toString())) {
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

                            if (patient != null) {
                                json.put("PatientID", patient.getExternal_patient_id());
                            } else {
                                json.put("PatientID", "0");
                            }
                        } catch (Exception ex) {

                        }
                        addOrderApicall(json);
                        dialog.dismiss();
                    } else {
                        Utils.showAlertToast(PatientListActivity.this, "Invalid Patient format [Ex: Lastname,Firstname]");
                    }

                } else {
                    Utils.showAlertToast(PatientListActivity.this, "Some value are missing");
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

    private void setBarcode(String msg) {
        IntentIntegrator integrator = new IntentIntegrator(this);

        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);

        integrator.setPrompt(msg);
        integrator.setBeepEnabled(true);

        integrator.setOrientationLocked(false);

        integrator.initiateScan();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {

        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);

        if (result != null) {

            if (result.getContents() == null) {

//cancel

            } else {
                Log.e("result", result.getContents() + "\n" + result.getBarcodeImagePath() + "\n" + result.getFormatName() + "\n" + result.getErrorCorrectionLevel());

                if (SCANNED_BARCODE_ID == 0) {
                    edt_rx_number.setText(result.getContents());
                } else {
                    edt_drug.setText(result.getContents());
                }
//Scanned successfully

            }

        } else {

            super.onActivityResult(requestCode, resultCode, intent);

        }

    }

    private void addOrderApicall(JSONObject json) {
        progressDialog.show();
        NetworkUtility.makeJSONObjectRequest(API.AddOrder, json, API.AddOrder, new VolleyCallBack() {
            @Override
            public void onSuccess(JSONObject result) {
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();

                    try {
                        Utils.showAlertToast(PatientListActivity.this, result.getString("Message"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onError(JSONObject result) {
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
            }
        });
    }

    private void getDrugandRx(String externalId) {
        progressDialog.show();
        NetworkUtility.makeJSONObjectRequest(API.PrescriptionData + "?ExternalPatientID=" + externalId, new JSONObject(), API.PrescriptionData, new VolleyCallBack() {
            @Override
            public void onSuccess(JSONObject result) {
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();

                    try {
                        Type listType = new TypeToken<List<PrescriptionData>>() {
                        }.getType();
                        prescriptionList = MyApplication.getGson().fromJson(result.getJSONArray("Data").toString(), listType);
                        SearchableDrugAdapter adapter = new SearchableDrugAdapter(PatientListActivity.this, R.layout.simple_spinner_item, prescriptionList);
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
            }

            @Override
            public void onError(JSONObject result) {
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
            }
        });
    }

    private boolean isPatientValid(String value) {
        if (!value.contains(",")) {
            return false;
        } else {
            return true;
        }
    }

    private void openDialog(boolean b) {
        if (b) {
            final Dialog dialog = new Dialog(PatientListActivity.this);
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            dialog.setContentView(R.layout.accept_dialog);
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            Window window = dialog.getWindow();
            lp.copyFrom(window.getAttributes());
//This makes the dialog take up the full width
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            window.setAttributes(lp);

            TextView accept = (TextView) dialog.findViewById(R.id.dialog_txt_accept);
            TextView decline = (TextView) dialog.findViewById(R.id.dialog_txt_decline);

            accept.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    AcceptTermsAPI();
                    changeFacilityDialog(true);
                }
            });
            decline.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    imgLogout.performClick();
                }
            });

            dialog.show();
        }
    }


    public boolean changeFacilityDialog(final boolean isFromLogin) {

        try {
            List<Facility> list = MyApplication.getGson().fromJson(MyApplication.getPrefranceData("Facility"), new TypeToken<List<Facility>>() {
            }.getType());
            if (list != null && list.size() == 1) {
                MyApplication.setPreferences("facility", list.get(0).getFacilityID());
                MyApplication.setPreferences("ExFacilityID", list.get(0).getExFacilityID());
                getPatientList(list.get(0).getFacilityID());
                imgChange.setVisibility(View.GONE);
                toolbar_facility_name.setText(list.get(0).getFacilityName());
                GlobalArea.selectedFacility = toolbar_facility_name.getText().toString();
            } else {
                final Dialog dialog = new Dialog(PatientListActivity.this);
                dialog.setCancelable(false);
                dialog.setCanceledOnTouchOutside(false);
                dialog.setContentView(R.layout.dialog_change_facility);
                WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                Window window = dialog.getWindow();
                lp.copyFrom(window.getAttributes());
//This makes the dialog take up the full width
                lp.width = WindowManager.LayoutParams.MATCH_PARENT;
                lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
                window.setAttributes(lp);

                spinner_facility = (Spinner) dialog.findViewById(R.id.spinner_facility);

                TextView accept = (TextView) dialog.findViewById(R.id.btn_dialog_ok);
                TextView decline = (TextView) dialog.findViewById(R.id.btn_dialog_cancel);


                accept.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        if (spinner_facility.getAdapter() != null) {
                            MyApplication.setPreferences("facility", ((Facility) spinner_facility.getAdapter().getItem(spinner_facility.getSelectedItemPosition())).getFacilityID());
                            MyApplication.setPreferences("ExFacilityID", ((Facility) spinner_facility.getAdapter().getItem(spinner_facility.getSelectedItemPosition())).getExFacilityID());
                            MyApplication.setPreferences("facility_selected_index", spinner_facility.getSelectedItemPosition());
                            getPatientList(((Facility) spinner_facility.getSelectedItem()).getFacilityID());
                            toolbar_facility_name.setText(((Facility) spinner_facility.getAdapter().getItem(spinner_facility.getSelectedItemPosition())).getFacilityName());
                            GlobalArea.selectedFacility = toolbar_facility_name.getText().toString();
                        }
                    }
                });
                decline.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        if (isFromLogin)
                            imgLogout.performClick();
                    }
                });
                fillFacilitySpinner();
                dialog.show();
            }

            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }

    }

    private void AcceptTermsAPI() {
        progressDialog.show();
//        JSONObject param = new JSONObject();
//        try {
//            param.put("RegisterID", Application.getPrefranceData("RegisterID"));
//        } catch (JSONException ex) {
//            ex.printStackTrace();
//        }
        NetworkUtility.makeJSONObjectRequest(API.ConfirmHipa + "?UserID=" + MyApplication.getPrefranceData("UserID"), new JSONObject(), API.ConfirmHipa, new VolleyCallBack() {
            @Override
            public void onSuccess(JSONObject result) {
                try {
                    if (progressDialog != null && progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                    MyApplication.setPreferences("IsConfirm", "true");
                    if (result != null) {
                        Type listType = new TypeToken<List<PatientPOJO>>() {
                        }.getType();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            @Override
            public void onError(JSONObject result) {
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
            }
        });
    }

    private void getPatientList(int facilityID) {
        Log.e("Step", "==============4");
        if (!swipeRefreshLayout.isRefreshing())
            progressDialog.show();

        JSONObject param = new JSONObject();
        try {
            param.put("FacilityID", facilityID);
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
        NetworkUtility.makeJSONObjectRequest(API.Patient_List_URL + "?FacilityID=" + facilityID, new JSONObject(), API.Patient_List_URL, new VolleyCallBack() {
            @Override
            public void onSuccess(JSONObject result) {
                try {
                    patientList.clear();

                    if (result != null) {
                        Type listType = new TypeToken<List<PatientPOJO>>() {
                        }.getType();

                        patientList = MyApplication.getGson().fromJson(result.getJSONArray("Data").toString(), listType);


                        //for soting list alphabetically by last name
                        Collections.sort(patientList, new Comparator<PatientPOJO>() {
                            @Override
                            public int compare(PatientPOJO lhs, PatientPOJO rhs) {
                                return lhs.getLastname().compareTo(rhs.getLastname());
                            }
                        });

                        patientAdapter = new PatientAdapter(PatientListActivity.this, patientList);
                        patientListView.setAdapter(patientAdapter);

                        search.setVisibility(View.VISIBLE);
                        search.clearFocus();
                        Utils.hideKeybord(PatientListActivity.this);
                        if (progressDialog != null && progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }

                        swipeRefreshLayout.setRefreshing(false);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    if (progressDialog != null && progressDialog.isShowing()) {
                        progressDialog.dismiss();

                    }
                    swipeRefreshLayout.setRefreshing(false);
                }
            }

            @Override
            public void onError(JSONObject result) {
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        checkLastAppUsageTime();

    }

    private void checkLastAppUsageTime() {
        if (MyApplication.getPrefranceData("LastUsageDate").isEmpty()) {
            MyApplication.setPreferences("LastUsageDate", new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
            return;
        }

        if (Utils.getDiffBetweenDays(MyApplication.getPrefranceData("LastUsageDate"), new SimpleDateFormat("yyyy-MM-dd").format(new Date())) >= 7) {
            Utils.showAlertToastLong(PatientListActivity.this, "You haven't use app for a week or more, Please login again to use");
            MyApplication.clearPrefrences();
            MyApplication.setPreferencesBoolean("isLogin", false);
            Intent i = new Intent(PatientListActivity.this, LoginActivity.class);
            startActivity(i);
            finish();
        } else {
            MyApplication.setPreferences("LastUsageDate", new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        }
    }

    public void getOrderndDelivery(final PatientPOJO patient) {
        progressDialog.show();

        NetworkUtility.makeJSONObjectRequest(API.OrderDelivertype, new JSONObject(), API.OrderDelivertype, new VolleyCallBack() {
            @Override
            public void onSuccess(JSONObject result) {
                try {
                    if (progressDialog != null && progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                    if (result != null) {

                        List<Delivery> tmpDeliveryList = MyApplication.getGson().fromJson(result.getJSONObject("Data").getJSONArray("Delivery").toString(), new TypeToken<List<Delivery>>() {
                        }.getType());
                        orderlist = MyApplication.getGson().fromJson(result.getJSONObject("Data").getJSONArray("Ordertype").toString(), new TypeToken<List<Order>>() {
                        }.getType());

                        deliverylist.clear();
                        for (int i = 0; i < tmpDeliveryList.size(); i++) {
                            if (tmpDeliveryList.get(i).getDeliverytypeName().equalsIgnoreCase("Today") || tmpDeliveryList.get(i).getDeliverytypeName().equalsIgnoreCase("Tomorrow")) {

                                deliverylist.add(tmpDeliveryList.get(i));
                            }
                         }

                        openAddOrderDialog(patient);

                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            @Override
            public void onError(JSONObject result) {
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }

            }
        });
    }

}
