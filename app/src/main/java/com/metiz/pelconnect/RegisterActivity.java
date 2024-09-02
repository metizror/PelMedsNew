package com.metiz.pelconnect;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.gson.reflect.TypeToken;
import com.metiz.pelconnect.model.FacilityPojo;
import com.metiz.pelconnect.network.API;
import com.metiz.pelconnect.network.NetworkUtility;
import com.metiz.pelconnect.network.VolleyCallBack;
import com.metiz.pelconnect.util.Utils;
import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.Email;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.metiz.pelconnect.util.Utils.dismissProgressDialog;
import static com.metiz.pelconnect.util.Utils.showProgressDialog;

public class RegisterActivity extends AppCompatActivity implements Validator.ValidationListener {


  @NotEmpty
  @BindView(R.id.input_fname)
  EditText inputFname;
  @NotEmpty
  @BindView(R.id.input_lname)
  EditText inputLname;
  @NotEmpty
  @Email
  @BindView(R.id.input_email)
  EditText inputEmail;
  @NotEmpty
  @BindView(R.id.input_phone)
  EditText inputPhone;
  @BindView(R.id.btn_login)
  AppCompatButton btnRegister;
  @BindView(R.id.input_facility_name)
  EditText inputFacilityName;
  @BindView(R.id.spinner_user_type)
  Spinner spinnerUserType;
  @BindView(R.id.spinner_facility)
  Spinner spinnerFacility;
  @BindView(R.id.ll_facility)
  LinearLayout llFacility;
  private Validator validator;

  List<FacilityPojo> facilityList = new ArrayList<>();


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_register);
    ButterKnife.bind(this);


    validator = new Validator(this);
    validator.setValidationListener(this);

    initSpinnerListner();
//        getAllFacility();
  }

  private void getAllFacility() {

    showProgressDialog(this);
    NetworkUtility.makeJSONObjectRequest(API.GET_ALL_FACILITY, new JSONObject(), API.GET_ALL_FACILITY, new VolleyCallBack() {
      @Override
      public void onSuccess(JSONObject result) {
        try {
          dismissProgressDialog();
          Log.e("response", result.toString());

          if (result.get("Data") != null && result.get("Data") instanceof JSONArray) {
            Type listType = new TypeToken<List<FacilityPojo>>() {
            }.getType();

            facilityList = MyApplication.getGson().fromJson(result.getJSONArray("Data").toString(), listType);

            FacilityPojo facilityPojo = new FacilityPojo();
            facilityPojo.setFacilityName("Select Facility");
            facilityList.set(0, facilityPojo);
            ArrayAdapter<FacilityPojo> orderAdapter = new ArrayAdapter<FacilityPojo>(RegisterActivity.this,
                    R.layout.simple_spinner_item, facilityList);
            orderAdapter.setDropDownViewResource(R.layout.simple_spinner_item);
            spinnerFacility.setAdapter(orderAdapter);
          }


        } catch (Exception e) {
          e.printStackTrace();
        }
      }

      @Override
      public void onError(JSONObject result) {
        dismissProgressDialog();

        try {
          Utils.showAlertToast(RegisterActivity.this, result.getString("Message"));
        } catch (JSONException e) {
          e.printStackTrace();
        }
      }
    });


  }

  public void openLogin(View view) {
    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
    startActivity(intent);
  }


  private void initSpinnerListner() {

    spinnerUserType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

        if (i == 0) {
          llFacility.setVisibility(View.GONE);
          return;
        }
        if (adapterView.getSelectedItem().toString().equalsIgnoreCase("other")) {
          llFacility.setVisibility(View.GONE);

        } else {
          llFacility.setVisibility(View.VISIBLE);
        }
      }

      @Override
      public void onNothingSelected(AdapterView<?> adapterView) {

      }
    });
  }

  @OnClick(R.id.btn_login)
  public void onViewClicked() {
    validator.validate();
  }

  @Override
  public void onValidationSucceeded() {
    if (inputPhone.getText().toString().length() < 10) {
      inputPhone.setError("Not valid");
      return;
    }

    if (spinnerUserType.getSelectedItemPosition() == 0) {
      Utils.showAlertToast(RegisterActivity.this, "Please select user type");
      return;
    } else if (!spinnerUserType.getSelectedItem().toString().equalsIgnoreCase("other") && inputFacilityName.getText().toString().trim().length() == 0) {
      inputFacilityName.setError("Please enter facility name");
      inputFacilityName.requestFocus();
      return;
    }

    JSONObject params = new JSONObject();
    try {
      params.put("FirstName", inputFname.getText().toString().trim());
      params.put("LastName", inputLname.getText().toString().trim());
      params.put("Email", inputEmail.getText().toString().trim());
      params.put("MobileNumber", inputPhone.getText().toString().trim());
      if (llFacility.getVisibility() == View.VISIBLE) {
        params.put("FacilityName", inputFacilityName.getText().toString());
      }
      params.put("UserType", spinnerUserType.getSelectedItem().toString());
      dismissProgressDialog();
      params.put("device_token", MyApplication.getPrefranceData("device_token"));
    } catch (JSONException ex) {
      ex.printStackTrace();
    }

    showProgressDialog(this);
    NetworkUtility.makeJSONObjectRequest(API.Register_URL, params, API.Register_URL, new VolleyCallBack() {
      @Override
      public void onSuccess(JSONObject result) {
        try {
          dismissProgressDialog();
          Log.e("response", result.toString());
          showDialog(result.getString("Data"));

        } catch (Exception e) {
          e.printStackTrace();
        }
      }

      @Override
      public void onError(JSONObject result) {
        dismissProgressDialog();

        try {
          Utils.showAlertToast(RegisterActivity.this, result.getString("Message"));
        } catch (JSONException e) {
          e.printStackTrace();
        }
      }
    });
  }

  private void showDialog(String message) {
    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        switch (which) {
          case DialogInterface.BUTTON_POSITIVE:
            //Yes button clicked
            finish();
            break;
        }
      }
    };

    AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
    builder.setMessage(message).setPositiveButton("Ok", dialogClickListener);

    AlertDialog dialog = builder.create();
    dialog.setCancelable(false);
    dialog.setCanceledOnTouchOutside(false);
    dialog.show();

  }


  @Override
  public void onValidationFailed(List<ValidationError> errors) {
    for (ValidationError error : errors) {
      View view = error.getView();
      String message = error.getCollatedErrorMessage(this);
      if (!spinnerUserType.getSelectedItem().toString().equalsIgnoreCase("other") && inputFacilityName.getText().toString().trim().length() == 0) {
        inputFacilityName.setError("Please enter facility name");
      }
      // Display error messages ;)
      if (view instanceof EditText) {
        ((EditText) view).setError(message);
      } else {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
      }
    }
  }
}
