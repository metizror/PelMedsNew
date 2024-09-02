package com.metiz.pelconnect;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;

import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.reflect.TypeToken;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.kaopiz.kprogresshud.KProgressHUD;
import com.metiz.pelconnect.Adapter.MedPassPatientAdapter;
import com.metiz.pelconnect.activity.GivenMedpassPatientListActivity;
import com.metiz.pelconnect.activity.PDFViewActivity;
import com.metiz.pelconnect.databinding.DialogMultiDrugDetailsBinding;
import com.metiz.pelconnect.databinding.DialogShowDownloadFileAlertBinding;
import com.metiz.pelconnect.fragment.BaseFragment;
import com.metiz.pelconnect.model.MedpassPatientListModel;
import com.metiz.pelconnect.model.MedsheetUrlModel;
import com.metiz.pelconnect.model.ModelMessPrescription;
import com.metiz.pelconnect.network.API;
import com.metiz.pelconnect.network.NetworkUtility;
import com.metiz.pelconnect.network.VolleyCallBack;
import com.metiz.pelconnect.retrofit.ApiClient;
import com.metiz.pelconnect.retrofit.ApiInterface;
import com.metiz.pelconnect.util.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static com.instabug.library.Instabug.getApplicationContext;

public class MedPassPatientListActivity extends BaseFragment {

    @BindView(R.id.search)
    SearchView search;
    @BindView(R.id.med_pass_patient_list)
    RecyclerView medPassPatientList;
    @BindView(R.id.swipe_refresh)
    SwipeRefreshLayout swipeRefresh;
    @BindView(R.id.ll_scanner)
    LinearLayout llScanner;
    @BindView(R.id.fab)
    FloatingActionButton fab;
    String doseTime = "";
    String doseDate = "";


    private int SCANNED_BARCODE_ID = 0;
    private int facility = 0;
    private static final int EXTERNAL_STORAGE_CODE = 103;

    List<ModelMessPrescription> modelMessPrescriptionList = new ArrayList<>();
    List<MedpassPatientListModel.DataBean.MedpassdetailsBean> modelMedPassPatientList = new ArrayList<>();
    List<MedpassPatientListModel.DataBean> modelMedPassPatientDataBeanList = new ArrayList<>();

    MedPassPatientAdapter adapter;
    private Dialog dialogSHowAlert;
    private DialogShowDownloadFileAlertBinding mDialogShowAlertBinding;

    private DialogMultiDrugDetailsBinding mMultiDrugDetailsBinding;

    String RxNumber = "";


    ApiInterface apiService;
    private KProgressHUD hud;

    private Dialog dialogMultiDrugAlert;

    @SuppressLint("ValidFragment")
    public MedPassPatientListActivity(int facility) {
        this.facility = facility;

    }

    public MedPassPatientListActivity() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_med_pass_patient_list, container, false);
        ButterKnife.bind(this, rootView);
        search.setQueryHint("Search Patient");
        search.setIconifiedByDefault(false);
        search.setIconified(false);
        search.setVisibility(View.GONE);
        initRecyclerView();
        apiService = ApiClient.getClient().create(ApiInterface.class);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), GivenMedpassPatientListActivity.class));

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
                        if (medPassPatientList.getAdapter() != null) {
                            newText.replace(",", "");
                            ((MedPassPatientAdapter) medPassPatientList.getAdapter()).filter(newText);
                        }
                    } else {
                        ((MedPassPatientAdapter) medPassPatientList.getAdapter()).filter("");

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
                getMedPassPatientList(MyApplication.getPrefranceDataInt("ExFacilityID"));
            }
        });
        llScanner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SCANNED_BARCODE_ID = 1;
                setBarcode("Scan Rx Number");

            }
        });
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (MyApplication.getPrefranceDataBoolean("isLogin")) {
            getMedPassPatientList(MyApplication.getPrefranceDataInt("ExFacilityID"));
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {

        try {
            IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);

            if (result != null) {

                if (result.getContents() == null) {

                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setMessage("Invalid scan")
                            .setCancelable(false)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    //do things
                                    dialog.dismiss();
                                }
                            });
                    AlertDialog alert = builder.create();
                    alert.show();
                } else {
                    Log.e("result", result.getContents() + "\n" + result.getBarcodeImagePath() + "\n" + result.getFormatName() + "\n" + result.getErrorCorrectionLevel());

                    if (SCANNED_BARCODE_ID == 0) {
                        RxNumber = result.getContents();
                        Log.e("Rx Number", result.getContents());
                        getPrescriptionDetails(RxNumber);
                    } else {
                        RxNumber = result.getContents();
                        getPrescriptionDetails(RxNumber);
                        Log.e("Rx Number1", result.getContents());

                    }

                }

            } else {

                super.onActivityResult(requestCode, resultCode, intent);

            }

        } catch (Exception ex) {
            ex.printStackTrace();
            super.onActivityResult(requestCode, resultCode, intent);
        }

    }

    private void setBarcode(String msg) {
        IntentIntegrator integrator = new IntentIntegrator(getActivity());
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
        integrator.setPrompt(msg);
        integrator.setBeepEnabled(true);
        integrator.setOrientationLocked(false);
        integrator.initiateScan();
    }

    private void getPrescriptionDetails(String rx) {
        hud.show();
        NetworkUtility.makeJSONObjectRequest(API.GetMedPassDrugDetailsByRxnumber + "?Rxnumber=" + rx, new JSONObject(), API.GetMedPassDrugDetailsByRxnumber, new VolleyCallBack() {
            @Override
            public void onSuccess(JSONObject result) {
                hud.dismiss();
                try {
                    //     dismissProgressDialog();

                    if (result != null) {
                        if(result.has("ResponseStatus")&&result.getString("ResponseStatus").equalsIgnoreCase(String.valueOf(1))) {
                            Type listType = new TypeToken<List<ModelMessPrescription>>() {
                            }.getType();
                            Log.e("On Data ", "On Data");

                            modelMessPrescriptionList = MyApplication.getGson().fromJson(result.getJSONArray("Data").toString(), listType);
                            if (modelMessPrescriptionList.size() > 0) {
                                if(modelMessPrescriptionList.size() > 1)
                                {
                                    String missMsg="";
                                    if (result.getJSONObject("Data").getString("MissMsg") != null && !result.getJSONObject("Data").getString("MissMsg").isEmpty()) {
                                        missMsg = result.getJSONObject("Data").getString("MissMsg");
                                    }
                                    showMultiDrugAlertDialog(missMsg, getContext().getResources().getString(R.string.medpass_alert), modelMessPrescriptionList,result);
                                }
                                else {
                                    dataPassToDetailsActivity(modelMessPrescriptionList.get(0),result, 0);

                                }



                            }
                            else {
                                String msg = result.getJSONObject("Data").getString("Msg");
                                if (TextUtils.isEmpty(msg) || msg.equalsIgnoreCase("null")) {
                                    msg = "There is no medpass available for this prescription";
                                }
                                Utils.showAlertToast(getActivity(), msg);
                            }
                        }else{
                            InsertSometingwantErrorLog(getResources().getString(R.string.something_went_wrong)+"PMgetPrescriptionDetails onSuccess result is "+result.toString(),doseDate,doseTime);
                        }
                    } else {
                        Utils.showAlertToast(getActivity(), "Data not Available");
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            @Override
            public void onError(JSONObject result) {
                hud.dismiss();
                Utils.showAlertToast(getActivity(), "Data not Available");
                InsertSometingwantErrorLog(getResources().getString(R.string.something_went_wrong)+"PMgetPrescriptionDetails onError is "+result.toString(),doseDate,doseTime);
            }
        });
    }

    private void dataPassToDetailsActivity(ModelMessPrescription modelMessPrescriptionItem, JSONObject result, int alertStatus) {
        try{

            Intent intent = new Intent(getActivity(), MedPrescriptionDetailsActivity.class);
            intent.putExtra("drug", modelMessPrescriptionList.get(0).getDrug());
            intent.putExtra("rxNumber", modelMessPrescriptionList.get(0).getRx_number());
            intent.putExtra("sig_detail", modelMessPrescriptionList.get(0).getSig_code());
            intent.putExtra("dose_time", modelMessPrescriptionList.get(0).getDose_time());
            doseTime = modelMessPrescriptionList.get(0).getDose_time();
            doseDate = modelMessPrescriptionList.get(0).getDose_date();
            intent.putExtra("dose_Qty", modelMessPrescriptionList.get(0).getDose_Qty());
            intent.putExtra("tran_id", modelMessPrescriptionList.get(0).getTran_id());
            intent.putExtra("facility_id", modelMessPrescriptionList.get(0).getFacility_id());
            intent.putExtra("patientId", modelMessPrescriptionList.get(0).getPatient_id());
            intent.putExtra("IsLate", modelMessPrescriptionList.get(0).isLate());
            intent.putExtra("IsErly", modelMessPrescriptionList.get(0).isErly());
            intent.putExtra("comeFrom", "rxscan");
            if(alertStatus==0)
            {
                if (result.getJSONObject("Data").getString("Msg") != null && !result.getJSONObject("Data").getString("Msg").isEmpty()) {
                    intent.putExtra("msg", result.getJSONObject("Data").getString("Msg"));
                }
                if (result.getJSONObject("Data").getString("MissMsg") != null && !result.getJSONObject("Data").getString("MissMsg").isEmpty()) {
                    intent.putExtra("missMsg", result.getJSONObject("Data").getString("MissMsg"));
                }
            }
            startActivity(intent);



        }
        catch(Exception ex){
            ex.printStackTrace();
        }

    }


    private void showMultiDrugAlertDialog(String message, String title, List<ModelMessPrescription> modelMessPrescriptionList1, JSONObject result) {

        dialogMultiDrugAlert = new Dialog(getContext());//, android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
        mMultiDrugDetailsBinding = DataBindingUtil.inflate(LayoutInflater.from(getContext()), R.layout.dialog_multi_drug_details, null, false);
        dialogMultiDrugAlert.setContentView(mMultiDrugDetailsBinding.getRoot());
//                            dialogDiscontinue.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation_2;
        dialogMultiDrugAlert.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialogMultiDrugAlert.getWindow().setStatusBarColor(getContext().getResources().getColor(R.color.white));
        dialogMultiDrugAlert.setCancelable(false);
        dialogMultiDrugAlert.setCanceledOnTouchOutside(false);

        mMultiDrugDetailsBinding.tvTitle.setText(title);
        mMultiDrugDetailsBinding.tvMessage.setText(message);


        mMultiDrugDetailsBinding.btnDrugName2.setText(modelMessPrescriptionList1.get(1).getDose_time()); //ok
        mMultiDrugDetailsBinding.btnDrugName1.setText(modelMessPrescriptionList1.get(0).getDose_time()); //cancel
        Log.e("TAG", "showAlertDialog>>>>: " + title);
        Log.e("TAG", "showAlertDialog????: " + message);
//        Log.e("TAG", "showAlertDialog: " + check);

        mMultiDrugDetailsBinding.imgalert.setVisibility(View.GONE);
        mMultiDrugDetailsBinding.tvTitle.setVisibility(View.GONE);

        if (!TextUtils.isEmpty(title)) {
            mMultiDrugDetailsBinding.tvTitle.setVisibility(View.VISIBLE);
        }


        mMultiDrugDetailsBinding.btnDrugName1.setOnClickListener(v -> {
            dialogMultiDrugAlert.dismiss();
            dialogMultiDrugAlert = null;
            dataPassToDetailsActivity(modelMessPrescriptionList1.get(0),result,1);

        });
        mMultiDrugDetailsBinding.btnDrugName2.setOnClickListener(v -> {
            dialogMultiDrugAlert.dismiss();
            dialogMultiDrugAlert = null;
            dataPassToDetailsActivity(modelMessPrescriptionList1.get(1),result,1);
        });

        mMultiDrugDetailsBinding.imgClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogMultiDrugAlert.dismiss();

            }
        });


        dialogMultiDrugAlert.show();
        /*TextView textView = new TextView(context);
        textView.setText(title);
        textView.setPadding(20, 30, 20, 30);
        textView.setTextSize(20F);
        textView.setBackgroundResource(R.color.colorPrimary);
        textView.setTextColor(Color.WHITE);

        if (alertDialog != null) {
            if (alertDialog.isShowing()) {
                alertDialog.dismiss();
            }
        }
        Log.e("TAG", "showAlertDialog: >>>>>" + check, null);
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(context, R.style.AlertDialogTheme);
        builder.setMessage(message);
        if (!TextUtils.isEmpty(title)) {
            builder.setCustomTitle(textView);
        }
        builder.setCancelable(false);
        if (!TextUtils.isEmpty(negativeBtn)) {
            builder.setNegativeButton(negativeBtn, (dialog, id) -> {
                dialog.dismiss();
                if (check.equalsIgnoreCase("1")) {
                    finish();
                } else if (check.equalsIgnoreCase("2")) {

                } else if (check.equalsIgnoreCase("3")) {

                }
            });
        }
        builder.setPositiveButton(positiveBtn, (dialog, id) -> {
            dialog.dismiss();
            if (check.equalsIgnoreCase("1")) {
                openAlertDialog2();
            } else if (check.equalsIgnoreCase("2")) {
                getChangePrescriptionByPatientId();
                isDialogShown = true;
//                    isDialogShownMiss = true;
                if (missCount > 0) {
                    openAlertDialog3();
                }
            } else if (check.equalsIgnoreCase("3")) {
                dialog.dismiss();
            }
        });
        alertDialog = builder.create();
        alertDialog.show();*/
    }



    private void initRecyclerView() {
        medPassPatientList.setLayoutManager(new LinearLayoutManager(getActivity()));
        hud = KProgressHUD.create(Objects.requireNonNull(getActivity()))
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setLabel("Please wait...")
                .setCancellable(false);
    }

    public void setTimeSpinner(final Spinner spinner, String[] list) {

        final ArrayAdapter<String> cycleStartDateArrayAdapter = new ArrayAdapter<String>
                (getContext(), R.layout.simple_spinner_item,
                        list);

        cycleStartDateArrayAdapter.setDropDownViewResource(R.layout
                .spinner_dropdown_item);

        getActivity().runOnUiThread(() -> {
            spinner.setAdapter(cycleStartDateArrayAdapter);//setting the adapter data into the AutoCompleteTextView
        });
    }

    public void openMedsheetDialog(String facilityName, String PatientId) {
        final String[] months = new String[]{"January", "February",
                "March", "April", "May", "June", "July", "August", "September",
                "October", "November", "December"};

        final String[] years = new String[]{"2017", "2018",
                "2019", "2020", "2021", "2022", "2023", "2024", "2025",};
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat month_date = new SimpleDateFormat("MMMM");
        String month_name = month_date.format(cal.getTime());
        Log.e("Month Name", month_name);
        int days = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

        String todayDate = new SimpleDateFormat("dd", Locale.getDefault()).format(new Date());

        Log.e("Today Date", todayDate);

        int count = days / 2;


        Log.e("Count", String.valueOf(count));

        Calendar now = Calendar.getInstance();
        int year = now.get(Calendar.YEAR);
        String yearInString = String.valueOf(year);
        Log.e("Year", yearInString);

        Dialog dialog = new Dialog(getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_med_sheet);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        Window window = dialog.getWindow();
        lp.copyFrom(window.getAttributes());
//This makes the dialog take up the full width
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(lp);
        Spinner spinner_month = (Spinner) dialog.findViewById(R.id.spinner_month);
        Spinner spinner_year = (Spinner) dialog.findViewById(R.id.spinner_year);
        Button tv_save = (Button) dialog.findViewById(R.id.tv_save);
        Button tv_close_dialog = (Button) dialog.findViewById(R.id.tv_close_dialog);
        setTimeSpinner(spinner_month, months);
        setTimeSpinner(spinner_year, years);
        for (int i = 0; i < spinner_month.getCount(); i++) {
            if (Integer.parseInt(todayDate) >= count) {
                if (spinner_month.getItemAtPosition(i).toString().equalsIgnoreCase(month_name)) {
                    spinner_month.setSelection(i + 1);

                }
            } else {
                if (spinner_month.getItemAtPosition(i).toString().equalsIgnoreCase(month_name)) {
                    spinner_month.setSelection(i);

                }

            }

        }

        for (int i = 0; i < spinner_year.getCount(); i++) {
            if (spinner_year.getItemAtPosition(i).toString().equalsIgnoreCase(yearInString)) {
                spinner_year.setSelection(i);

            }
        }

        tv_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getMedpassList(facilityName, spinner_month.getSelectedItem().toString(), spinner_year.getSelectedItem().toString(), PatientId, dialog);

            }
        });
        tv_close_dialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.e("grantResults.length:", "onRequestPermissionsResult: "+grantResults.length );
        if (requestCode == EXTERNAL_STORAGE_CODE) {
            Log.e("grantResults.length:", "onRequestPermissionsResult: "+grantResults.length );
            if (grantResults.length > 0) {
                if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    Log.e("TAG:", "onRequestPermissionsResult: "+ "permission denied");
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

                    //code to Set the message and title from the strings.xml file
                    builder.setMessage("Some Core functionalities of the app might not work correctly without these permission.").setTitle("We Request Again");
                    builder.setCancelable(false)
                            .setPositiveButton("Give Permissions", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    ActivityCompat.requestPermissions(Objects.requireNonNull(getActivity()), new String[]{WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE}, EXTERNAL_STORAGE_CODE);
                                    // request permissions goes here.
                                }
                            })
                            .setNegativeButton("Don't Ask Again", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    //  Action for 'Don't Ask Again' Button
                                    // the sharedpreferences value is true
                                    dialog.cancel();
                                }
                            });
                    //Creating dialog box
                    AlertDialog alert = builder.create();
                    alert.show();
                }

            }

        }


        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void getMedpassList(String facilityName, String month, String year, String patientID, Dialog dialog) {
        KProgressHUD hud1;
        hud1 = KProgressHUD.create(Objects.requireNonNull(getActivity()))
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setLabel("it may take a while to complete. \n\t\t\t\tPlease be patient.")
                .setCancellable(false);
        hud1.show();
        Call<MedsheetUrlModel> call = apiService.getMedsheeturl(facilityName, month, year, patientID);
        call.enqueue(new Callback<MedsheetUrlModel>() {
            @SuppressLint("DefaultLocale")
            @Override
            public void onResponse(@NonNull Call<MedsheetUrlModel> call, @NonNull Response<MedsheetUrlModel> response) {
                if (Utils.checkInternetConnection(Objects.requireNonNull(getApplicationContext()))) {
                    if (response.body() != null) {

                        if (response.body().getResponseStatus() == 1) {
                            if (response.body().getData() != null && !response.body().getData().isEmpty()) {
                                Intent intent = new Intent(getContext(), PDFViewActivity.class);
                                intent.putExtra("url", response.body().getData());
                                startActivity(intent);
                            } else {
                                Utils.showAlertToast(getContext(), "No Medsheet found");
                            }


                        } else {
                            Utils.showAlertToast(getContext(), "No Medsheet found");
                            InsertSometingwantErrorLog(getResources().getString(R.string.something_went_wrong)+"PMgetMedpassList onresponse Status is not 1 and is "+response.body().getResponseStatus(),"","");

                        }

                    } else {
                        Utils.showAlertToast(getContext(), "No Medsheet found");
                        InsertSometingwantErrorLog(getResources().getString(R.string.something_went_wrong)+"PMgetMedpassList onresponse body is null "+response.body(),"","");
                    }

                    hud1.dismiss();

                }
            }

            @Override
            public void onFailure(@NonNull Call<MedsheetUrlModel> call, @NonNull Throwable t) {
                Log.e("TAG", t.toString());
                hud1.dismiss();
                InsertSometingwantErrorLog(getResources().getString(R.string.something_went_wrong)+"PMgetMedpassList onFailure is "+t.getMessage(),"","");
            }
        });

    }

    private void getMedPassPatientList(int facilityID) {
        Log.e("Step", "==============4" + facilityID);
        if (!swipeRefresh.isRefreshing())
            hud.show();
        Call<MedpassPatientListModel> call = apiService.getPatientList(facilityID, new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()).format(new Date()));
        call.enqueue(new Callback<MedpassPatientListModel>() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @SuppressLint("DefaultLocale")
            @Override
            public void onResponse(@NonNull Call<MedpassPatientListModel> call, @NonNull Response<MedpassPatientListModel> response) {
                assert response.body() != null;
                hud.dismiss();
                if (Utils.checkInternetConnection(Objects.requireNonNull(getApplicationContext()))) {
                    try {
                        int response_code = response.body().getResponseStatus();
                        Log.e("onResponse111:", "tag111: " + response.body());
                        if (modelMedPassPatientList.size() > 0) {
                            modelMedPassPatientList.clear();
                        }
                        if (response_code == 1) {
                            modelMedPassPatientList = response.body().getData().getMedpassdetails();

                            adapter = new MedPassPatientAdapter(getActivity(), modelMedPassPatientList);
                            medPassPatientList.setAdapter(adapter);
                            search.setVisibility(View.VISIBLE);
                            search.clearFocus();
                            swipeRefresh.setRefreshing(false);
                        }else{
                            InsertSometingwantErrorLog(getResources().getString(R.string.something_went_wrong)+"PMgetMedPassPatientList onResponse response code is not 1","","");
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();

                    }
                }


            }

            @Override
            public void onFailure(@NonNull Call<MedpassPatientListModel> call, Throwable t) {
                Log.e("TAG", t.toString());
                hud.dismiss();
                InsertSometingwantErrorLog(getResources().getString(R.string.something_went_wrong)+"PMgetMedPassPatientList onFailure is "+t.getMessage(),"","");
            }
        });
    }

    private String getDateTime() {
        DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        Date date = new Date();
        return dateFormat.format(date);
    }

    private void InsertSometingwantErrorLog(String message, String date, String time) {
//        hud.show();
        JSONObject mainObj = new JSONObject();

        try {
            mainObj.put("UserID", MyApplication.getPrefranceData("UserID"));
            mainObj.put("DateAndTime", getDateTime());
            mainObj.put("PatientID", "");
            mainObj.put("FacilityID", MyApplication.getPrefranceDataInt("ExFacilityID"));
            mainObj.put("DivceName", Build.MANUFACTURER + " " + Build.DEVICE);
            mainObj.put("ModelNo", Build.MODEL);
            mainObj.put("DeviceVersion", Build.VERSION.RELEASE);
            mainObj.put("Message", message);
            mainObj.put("ScaningValue", "");
            mainObj.put("Appversion", MyApplication.getPrefranceData("Version"));//
            mainObj.put("DoseDate", date);
            mainObj.put("DoseTime", time);


        } catch (JSONException ex) {
            ex.printStackTrace();
        }


        NetworkUtility.makeJSONObjectRequest(API.InsertSometingwantErrorLog, mainObj, API.InsertSometingwantErrorLog, new VolleyCallBack() {
            @Override
            public void onSuccess(JSONObject result) {
//                hud.dismiss();
                try {
                    if (result != null) {
                        Log.e("TAG", "InsertSometingwant: " + result.get("Data"));
                    }

                } catch (Exception ex) {
                    dismissProgressDialog();
                    ex.printStackTrace();
                    /*AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setMessage("Something Went Wrong!")
                            .setCancelable(false)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    //do things
                                    dialog.dismiss();
                                }
                            });
                    AlertDialog alert = builder.create();
                    alert.show();*/
                    Toast.makeText(getContext(), getResources().getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
//                    showAlertDialog(context.getResources().getString(R.string.something_went_wrong), "", "", "OK", "1");

                }
            }

            @Override
            public void onError(JSONObject result) {
//                hud.dismiss();
               /* AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage("Something Went Wrong!")
                        .setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //do things
                                dialog.dismiss();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();*/
//                showAlertDialog(context.getResources().getString(R.string.something_went_wrong), "", "", "OK", "1");
                Toast.makeText(getContext(), getResources().getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();

            }
        });
    }

    private void showAlertDialog(String message, String title) {
        dialogSHowAlert = new Dialog(getContext());//, android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
        mDialogShowAlertBinding = DataBindingUtil.inflate(LayoutInflater.from(getContext()), R.layout.dialog_show_download_file_alert, null, false);
        dialogSHowAlert.setContentView(mDialogShowAlertBinding.getRoot());
//                            dialogDiscontinue.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation_2;
        dialogSHowAlert.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialogSHowAlert.getWindow().setStatusBarColor(getContext().getResources().getColor(R.color.white));
        dialogSHowAlert.setCancelable(false);
        dialogSHowAlert.setCanceledOnTouchOutside(false);

        mDialogShowAlertBinding.tvTitle.setText(title);
        mDialogShowAlertBinding.tvMessage.setText(message);


//        mDialogShowAlertBinding.imgalert.setVisibility(View.GONE);
        mDialogShowAlertBinding.tvTitle.setVisibility(View.VISIBLE);

        mDialogShowAlertBinding.canleBtn.setOnClickListener(v -> {
            dialogSHowAlert.dismiss();
            dialogSHowAlert = null;

        });
        mDialogShowAlertBinding.okBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            Uri uri = Uri.parse( Environment.getExternalStorageDirectory()
                    + File.separator + Environment.DIRECTORY_DOWNLOADS
                    + File.separator + this.getResources().getString(R.string.app_name));
            intent.setDataAndType(uri, "*/*");
            startActivity(Intent.createChooser(intent, "Open folder"));


//            String path = Environment.getExternalStorageDirectory() + "/" + "Downloads" + "/";
           /* String path = Environment.getExternalStorageDirectory()
                    + File.separator + Environment.DIRECTORY_DOWNLOADS
                    + File.separator + this.getResources().getString(R.string.app_name);
            Uri uri = Uri.parse(path);
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setDataAndType(uri, "");
            startActivity(intent);*/
            dialogSHowAlert.dismiss();
            dialogSHowAlert = null;
        });

        dialogSHowAlert.show();

    }



}
