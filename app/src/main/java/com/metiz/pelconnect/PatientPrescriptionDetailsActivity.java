package com.metiz.pelconnect;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.reflect.TypeToken;
import com.metiz.pelconnect.Adapter.CensusListAdapter;
import com.metiz.pelconnect.model.ModelCensusListDetails;
import com.metiz.pelconnect.network.API;
import com.metiz.pelconnect.network.NetworkUtility;
import com.metiz.pelconnect.network.VolleyCallBack;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import java.util.Calendar;

/**
 * Created by paras on 8/8/20.
 */

public class PatientPrescriptionDetailsActivity extends AppCompatActivity {

    private final static int READ_STORAGE_PERMISSION_CODE = 100;
    private int PICK_IMAGE_MULTIPLE = 101;


    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.ll_back)
    LinearLayout llBack;
    @BindView(R.id.tv_activity_name)
    TextView tvActivityName;
    @BindView(R.id.rvList)
    RecyclerView rvList;


    ArrayList<ModelCensusListDetails> modelCensusListDetails = new ArrayList<>();
    CensusListAdapter censusListAdapter;

    @BindView(R.id.btn_save)
    TextView btn_save;

    @BindView(R.id.btn_cancel)
    TextView btn_cancel;

    String cycleStartDate, facultyID, ExternalPatientId, Patient;
    int HeaderId;
    ProgressDialog progressDialog;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_prescription_details);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        llBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        cycleStartDate = getIntent().getStringExtra("cycleStartDate");
        facultyID = getIntent().getStringExtra("Facility_Id");
        HeaderId = getIntent().getIntExtra("HeaderId", 0);
        ExternalPatientId = getIntent().getStringExtra("ExternalPatientId");
        Patient = getIntent().getStringExtra("Patient");
        tvActivityName.setText(Patient);

        initProgress();
        initRecyclerView();
        initView();
        getPatientPrescriptionDetails();
    }

    private void initView() {

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                SavePatientPrescription();
            }
        });


    }

    private void initProgress() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);
    }

    private void initRecyclerView() {
        rvList.setLayoutManager(new LinearLayoutManager(this));
    }

    String status;

    public void getPatientPrescriptionDetails() {
        progressDialog.show();
        NetworkUtility.makeJSONObjectRequest(API.PatientPrescription + "?EPD=" + ExternalPatientId + "&HID=" + HeaderId + "&CycleStartDate=" + cycleStartDate + "&FacilityID=" + facultyID, new JSONObject(), API.PatientPrescription, new VolleyCallBack() {
            @Override
            public void onSuccess(JSONObject result) {
                try {
                    if (modelCensusListDetails.size() > 0) {
                        modelCensusListDetails.clear();
                    }
                    if (result != null) {
                        Type listType = new TypeToken<List<ModelCensusListDetails>>() {
                        }.getType();
                        modelCensusListDetails = MyApplication.getGson().fromJson(result.getJSONArray("Data").toString(), listType);
                        for (int i = 0; i < modelCensusListDetails.size(); i++) {
                            if (modelCensusListDetails.get(i).getFacility_QTY().equals("0")) {
                                modelCensusListDetails.get(i).setFacility_QTY("");
                            }
                            if (modelCensusListDetails.get(i).getProgram_QTY().equals("0")) {
                                modelCensusListDetails.get(i).setProgram_QTY("");
                            }
                            modelCensusListDetails.get(i).setIsChange("False");
                            modelCensusListDetails.get(i).setIsImageSet("False");
                            modelCensusListDetails.get(i).setIsQtyChange("False");
                        }
                        censusListAdapter = new CensusListAdapter(PatientPrescriptionDetailsActivity.this, modelCensusListDetails, HeaderId, ExternalPatientId);
                        rvList.setAdapter(censusListAdapter);

                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    progressDialog.dismiss();
                }
                progressDialog.dismiss();
            }

            @Override
            public void onError(JSONObject result) {
                progressDialog.dismiss();
            }
        });
    }


    public void CycleMedChangesForSinglePatient() {
        JSONObject param = new JSONObject();
        try {
            param = new JSONObject(getIntent().getStringExtra("param"));
            System.out.println(param);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        NetworkUtility.makeJSONObjectRequest(API.CycleMedChangesForSinglePatient, param, API.CycleMedChangesForSinglePatient, new VolleyCallBack() {
            @Override
            public void onSuccess(JSONObject result) {
                try {

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            @Override
            public void onError(JSONObject result) {

            }
        });
    }


    private void SavePatientPrescription() {
        progressDialog.show();
        CycleMedChangesForSinglePatient();

        boolean ischeck = false;
        JSONObject param = new JSONObject();
        try {
            JSONArray array = new JSONArray();

            for (int i = 0; i < modelCensusListDetails.size(); i++) {

                JSONObject obj = new JSONObject();
                String FacilityQty;
                String ProgramQty;
                if (modelCensusListDetails.get(i).getIsChange().equals("true")) {

                    obj.put("HoldStatus", modelCensusListDetails.get(i).getHoldStatus());

                    FacilityQty = modelCensusListDetails.get(i).getFacility_QTY();
                    ProgramQty = modelCensusListDetails.get(i).getProgram_QTY();

                    if (modelCensusListDetails.get(i).getIsQtyChange().equals("true")) {
                        if (!FacilityQty.equals("") && !ProgramQty.equals("")) {
                            ischeck = true;
                            modelCensusListDetails.get(i).setSplitPack("true");
                        } else if (FacilityQty.equals("") && ProgramQty.equals("")) {
                            ischeck = true;
                            FacilityQty = "0";
                            ProgramQty = "0";
                            modelCensusListDetails.get(i).setSplitPack("true");
                        } else {
                            if (FacilityQty.equals("")) {
                                if (!ProgramQty.equals("")) {
                                    Toast.makeText(this, "Enter Facility Qty", Toast.LENGTH_SHORT).show();
                                }
                            }
                            if (ProgramQty.equals("")) {
                                if (!FacilityQty.equals("")) {
                                    Toast.makeText(this, "Enter Program Qty", Toast.LENGTH_SHORT).show();
                                }
                            }
                            ischeck = false;
                            FacilityQty = "0";
                            ProgramQty = "0";
                            modelCensusListDetails.get(i).setSplitPack("false");
                            break;
                        }
                    } else {
                        ischeck = true;
                        FacilityQty = "0";
                        ProgramQty = "0";
                        modelCensusListDetails.get(i).setSplitPack("false");
                    }

                    obj.put("Facility_QTY", FacilityQty);

                    if (modelCensusListDetails.get(i).getRxnumber() != null) {
                        obj.put("Rx", modelCensusListDetails.get(i).getRxnumber());
                    } else {
                        obj.put("Rx", "");
                    }
                    if (modelCensusListDetails.get(i).getLast_pickedup_date() != null) {
                        obj.put("DeliveryDate", modelCensusListDetails.get(i).getLast_pickedup_date());
                    } else {
                        obj.put("DeliveryDate", "");
                    }

                    if (modelCensusListDetails.get(i).getIsImageSet().equals("true")) {
                        if (modelCensusListDetails.get(i).getDCFile() != null) {
                            obj.put("DCFile", modelCensusListDetails.get(i).getDCFile());
                        } else {
                            obj.put("DCFile", "");
                        }
                    } else {
                        obj.put("DCFile", "");
                    }
                    if (modelCensusListDetails.get(i).getIsImageSet().equals("true")) {
                        if (modelCensusListDetails.get(i).getLCFile() != null) {
                            obj.put("LCFile", modelCensusListDetails.get(i).getLCFile());
                            obj.put("LCStatus", modelCensusListDetails.get(i).getLCStatus());
                        } else {
                            obj.put("LCFile", "");
                            obj.put("LCStatus", "false");
                        }
                    } else {
                        obj.put("LCFile", "");
                        obj.put("LCStatus", "false");
                    }
                    //obj.put("LCStatus",modelCensusListDetails.get(i).getLCStatus());
                    obj.put("DCStatus", modelCensusListDetails.get(i).getDCStatus());
                    obj.put("SplitPack", modelCensusListDetails.get(i).getSplitPack());
                    obj.put("External_Patient_ID", modelCensusListDetails.get(i).getExternal_Patient_ID());
                    obj.put("Facility_User_ID", MyApplication.getPrefranceData("UserID"));
                    String date = modelCensusListDetails.get(i).getFacility_Modified_Date();
                    String convertdate = GetDateTime();
                    obj.put("Facility_Modified_Date", convertdate);

                    obj.put("Program_QTY", ProgramQty);
                    if (modelCensusListDetails.get(i).getNote() != null) {
                        obj.put("Note", modelCensusListDetails.get(i).getNote());
                    } else {
                        obj.put("Note", "");
                    }
                    if (modelCensusListDetails.get(i).getDCNote() != null) {
                        obj.put("DCNote", modelCensusListDetails.get(i).getDCNote());
                    } else {
                        obj.put("DCNote", "");
                    }
                    //obj.put("ID",Application.getPrefranceData("UserID"));
                    obj.put("ID", modelCensusListDetails.get(i).getExternal_facility_id());
                    if (modelCensusListDetails.get(i).getNextRefill() != null) {
                        obj.put("NextRefillDate", modelCensusListDetails.get(i).getNextRefill());
                    } else {
                        obj.put("NextRefillDate", "");
                    }
                    obj.put("Drug", modelCensusListDetails.get(i).getDrug());
                    //obj.put("HeaderID",modelCensusListDetails.get(i).getHeaderID());
                    obj.put("HeaderID", HeaderId);
                    obj.put("PrescriptionID", modelCensusListDetails.get(i).getRxID());
                    array.put(obj);
                } else {

                }
            }
            param.put("CyclePrescriptionModel", array.toString());
            System.out.println(param);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (ischeck) {
            progressDialog.show();
            NetworkUtility.makeJSONObjectRequest(API.CensusChangesForPatient, param, API.CensusChangesForPatient, new VolleyCallBack() {
                @Override
                public void onSuccess(JSONObject result) {
                    try {
                        if (result != null) {
                            Toast.makeText(PatientPrescriptionDetailsActivity.this, "" + result.getString("Message"), Toast.LENGTH_SHORT).show();
                            finish();
                        }
                        progressDialog.dismiss();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        progressDialog.dismiss();
                    }
                }

                @Override
                public void onError(JSONObject result) {
                    progressDialog.dismiss();
                }
            });
        } else {
            progressDialog.dismiss();
        }
    }

    public String GetDateTime() {
        Calendar c = Calendar.getInstance();
        System.out.println("Current time => " + c.getTime());

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        String formattedDate = df.format(c.getTime());
        return formattedDate;
    }


    public void pickImage(int position, String type) {
        status = type;
        if (ContextCompat.checkSelfPermission(PatientPrescriptionDetailsActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            requestPermission(PatientPrescriptionDetailsActivity.this, READ_STORAGE_PERMISSION_CODE);
            return;
        }
        openSelectMediaDialog(position, type);
    }

    private void requestPermission(AppCompatActivity activity, int code) {
        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, code);
    }

    @SuppressLint("SetTextI18n")
    private void openSelectMediaDialog(int position, String type) {
        final Dialog dialog1 = new Dialog(this);
        View view = getLayoutInflater().inflate(R.layout.row_image_pic_dialog, null);
        dialog1.setContentView(view);
        dialog1.setCancelable(true);
        dialog1.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        dialog1.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog1.show();

        ImageView imgImage = dialog1.findViewById(R.id.imgImage);
        ImageView imgPdf = dialog1.findViewById(R.id.imgPdf);
        TextView txtName = dialog1.findViewById(R.id.txtUploadName);
        //txtName.setText("Select Document");

        /*TextView txtUploadTitle = dialog1.findViewById(R.id.txtUploadTitle);
        txtUploadTitle.setVisibility(View.VISIBLE);
        txtUploadTitle.setTextColor(ContextCompat.getColor(this, R.color.black));*/
        if (type.equals("DC")) {
            txtName.setText(R.string.str_hcp_order);
        } else if (type.equals("LC")) {
            txtName.setText(R.string.str_document_here);
        }


        imgImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog1.dismiss();
                dialogForImage("image", position);
            }
        });

        imgPdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog1.dismiss();
                dialogForImage("pdf", position);
            }
        });
    }

    private void dialogForImage(String type, int position) {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        //Yes button clicked
                        if (type == "image") {
                            Intent intent = new Intent();
                            intent.setType("image/*");
                            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                            intent.putExtra("position", position);
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            startActivityForResult(
                                    Intent.createChooser(intent, "Select Picture")
                                    , position
                            );
                        } else if ((type == "pdf")) {

                            Intent intent = new Intent();
                            intent.setType("application/pdf");
                            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                            intent.putExtra("position", position);
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            startActivityForResult(
                                    Intent.createChooser(intent, "Select PDF")
                                    , position
                            );
                        }


                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        censusListAdapter.notifyDataSetChanged();
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("I acknowledged that I fully HIPAA compliant to upload picture here.")
                .setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();
    }

    @SuppressLint("ObsoleteSdkInt")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && null != data) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                // Get the Uri of the selected file
                Uri uri = data.getData();
                String Path = uri.getPath();
                int pos = requestCode;
                ConvertToString(uri, pos);
                       /* Bitmap bitmap = null;
                        try {
                            bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        String fileBase64 = encodeTobase64(bitmap);
                        setFileUploadName(pos,fileBase64);*/
                //getFileToByte(Path);
                //ConvertToString(uri, pos);
                        /*String uriString = uri.toString();
                        File myFile = new File(uriString);
                        String path = myFile.getAbsolutePath();
                        String displayName = null;
                        int pos = requestCode;

                        if (uriString.startsWith("content://")) {
                            Cursor cursor = null;
                            try {
                                cursor = getContentResolver().query(uri, null, null, null, null);
                                if (cursor != null && cursor.moveToFirst()) {
                                    displayName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                                }
                            } finally {
                                cursor.close();
                            }
                        } else if (uriString.startsWith("file://")) {
                            displayName = myFile.getName();
                        }*/

                //setFileUploadName(pos,displayName);

            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /*public static String encodeTobase64(Bitmap image) {
        Bitmap immagex=image;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        immagex.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] b = baos.toByteArray();
        String imageEncoded = Base64.encodeToString(b,Base64.DEFAULT);

        Log.e("LOOK", imageEncoded);
        return imageEncoded;
    }*/


    public void ConvertToString(Uri uri, int pos) {
        String uriString = uri.toString();
        Log.d("data", "onActivityResult: uri" + uriString);

        try {
            InputStream in = getContentResolver().openInputStream(uri);
            byte[] bytes = getBytes(in);
            Log.d("data", "onActivityResult: bytes size=" + bytes.length);
            Log.d("data", "onActivityResult: Base64string=" + Base64.encodeToString(bytes, Base64.DEFAULT));
            String ansValue = Base64.encodeToString(bytes, Base64.DEFAULT);
            //String document = Base64.encodeToString(bytes,Base64.DEFAULT);
            setFileUploadName(pos, ansValue);

        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
            Log.d("error", "onActivityResult: " + e.toString());
        }
    }

    public byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    private void setFileUploadName(int position, String fileName) {
        if (fileName != null) {
            if (status.equals("DC")) {
                modelCensusListDetails.get(position).setDCFile(fileName);
                modelCensusListDetails.get(position).setDCStatus("true");
                modelCensusListDetails.get(position).setIsChange("true");
                modelCensusListDetails.get(position).setIsImageSet("true");
                modelCensusListDetails.get(position).setFacility_QTY("");
                modelCensusListDetails.get(position).setProgram_QTY("");
            } else if (status.equals("LC")) {
                modelCensusListDetails.get(position).setLCFile(fileName);
                modelCensusListDetails.get(position).setLCStatus("true");
                modelCensusListDetails.get(position).setIsChange("true");
                modelCensusListDetails.get(position).setIsImageSet("true");
            } else {
                modelCensusListDetails.get(position).setDCStatus("false");
                modelCensusListDetails.get(position).setLCStatus("false");
                modelCensusListDetails.get(position).setIsImageSet("false");
            }
        } else {
            Log.e("Selected File", "File not selected");
        }
        censusListAdapter.notifyDataSetChanged();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case READ_STORAGE_PERMISSION_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission Granted
                } else {
                    // Permission Not Granted
                    Toast.makeText(this, "Permission Not Granted", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }


    /*public void openNoteDialog(int position, String type) {
        System.out.println(position);
        final Dialog dialog = new Dialog(PatientPrescriptionDetailsActivity.this);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.note_dialog);

        EditText text = dialog.findViewById(R.id.dialog_note);
        Button dialogBtnSave = (Button) dialog.findViewById(R.id.dialog_btn_save);
        Button dialogBtnCalcel = (Button) dialog.findViewById(R.id.dialog_btn_cancel);

        if(type.equals("DC")){
            text.setText(modelCensusListDetails.get(position).getDCNote());
        }
        if(type.equals("HOLD")){
            text.setText(modelCensusListDetails.get(position).getNote());
        }


        dialogBtnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String Note = text.getText().toString();
                if(type.equals("DC")){
                    modelCensusListDetails.get(position).setDCNote(Note);
                }else if(type.equals("HOLD")){
                    modelCensusListDetails.get(position).setNote(Note);
                    modelCensusListDetails.get(position).setHoldStatus("true");
                }else{
                    modelCensusListDetails.get(position).setHoldStatus("false");
                }
                dialog.dismiss();
                censusListAdapter.notifyDataSetChanged();
            }
        });
        dialogBtnCalcel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();

    }*/

}
