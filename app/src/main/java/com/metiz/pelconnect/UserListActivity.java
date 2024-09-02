package com.metiz.pelconnect;

import android.app.Dialog;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.google.gson.reflect.TypeToken;
import com.metiz.pelconnect.Adapter.UserListAdapter;
import com.metiz.pelconnect.model.User;
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
import butterknife.OnClick;

public class UserListActivity extends BaseActivity {

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.fab)
    FloatingActionButton fab;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    private EditText dlg_edt_fname, dlg_edt_lname, dlg_edt_email, dlg_edt_username, dlg_edt_password;

    List<User> userList = new ArrayList<>();
    UserListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        setTitle("Manage User");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        initRecyclerView();
        getUserList();
    }

    private void initRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new UserListAdapter(this, userList);
        recyclerView.setAdapter(adapter);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void getUserList() {
        showProgressDialog(this);

        NetworkUtility.makeJSONObjectRequest(API.GetUserlist, new JSONObject(), API.GetUserlist, new VolleyCallBack() {
            @Override
            public void onSuccess(JSONObject result) {
                try {
                    dismissProgressDialog();

                    if (result != null) {
                        Type listType = new TypeToken<List<User>>() {
                        }.getType();

                        userList = MyApplication.getGson().fromJson(result.getJSONArray("Data").toString(), listType);


                        //for soting list alphabetically by last name
                        Collections.sort(userList, new Comparator<User>() {
                            @Override
                            public int compare(User lhs, User rhs) {
                                return lhs.getLastName().toLowerCase().compareTo(rhs.getLastName().toLowerCase());
                            }
                        });

                        adapter.updateList(userList);

                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    recyclerView.setAdapter(new UserListAdapter(UserListActivity.this, new ArrayList<User>()));
                    adapter.updateList(new ArrayList<User>());

                }
            }

            @Override
            public void onError(JSONObject result) {
                dismissProgressDialog();


            }
        });
    }

    private void addUser(JSONObject jsonObject) {
        showProgressDialog(this);

        NetworkUtility.makeJSONObjectRequest(API.CreateUser, jsonObject, API.CreateUser, new VolleyCallBack() {
            @Override
            public void onSuccess(JSONObject result) {
                try {
                    dismissProgressDialog();

                    if (result != null) {
                        getUserList();

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

    @OnClick(R.id.fab)
    public void onViewClicked() {
        openAddUserDialog();
    }

    private void openAddUserDialog() {


        final Dialog dialog = new Dialog(UserListActivity.this);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setContentView(R.layout.dialog_add_user);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        Window window = dialog.getWindow();
        lp.copyFrom(window.getAttributes());
//This makes the dialog take up the full width
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(lp);

        dlg_edt_fname = (EditText) dialog.findViewById(R.id.txt_first_name);
        dlg_edt_lname = (EditText) dialog.findViewById(R.id.txt_last_name);
        dlg_edt_email = (EditText) dialog.findViewById(R.id.txt_email);
        dlg_edt_username = (EditText) dialog.findViewById(R.id.txt_username);
        dlg_edt_password = (EditText) dialog.findViewById(R.id.txt_password);
        Button btn_dialog_add = (Button) dialog.findViewById(R.id.btn_dialog_add);
        Button btn_dialog_cancel = (Button) dialog.findViewById(R.id.btn_dialog_cancel);

        btn_dialog_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isUserValid()) {
                    dialog.dismiss();

                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put("UserID", 0);
                        jsonObject.put("FirstName", dlg_edt_fname.getText().toString());
                        jsonObject.put("LastName", dlg_edt_lname.getText().toString());
                        jsonObject.put("UserName", dlg_edt_username.getText().toString());
                        jsonObject.put("Email", dlg_edt_email.getText().toString());
                        jsonObject.put("Password", dlg_edt_password.getText().toString());
                        jsonObject.put("Active", true);
                        jsonObject.put("CreatedBy", MyApplication.getPrefranceData("UserID"));
                        jsonObject.put("UpdateBy", MyApplication.getPrefranceData("UserID"));

                        addUser(jsonObject);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }

                }

            }
        });
        btn_dialog_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                dialog.dismiss();
            }
        });


        dialog.show();
    }

    public void openUpdateUserDialog(final User user) {

        final Dialog dialog = new Dialog(UserListActivity.this);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setContentView(R.layout.dialog_update_user);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        Window window = dialog.getWindow();
        lp.copyFrom(window.getAttributes());
//This makes the dialog take up the full width
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(lp);

        dlg_edt_fname =  dialog.findViewById(R.id.txt_first_name);
        dlg_edt_lname =  dialog.findViewById(R.id.txt_last_name);
        dlg_edt_email = dialog.findViewById(R.id.txt_email);
        dlg_edt_username = dialog.findViewById(R.id.txt_username);
        dlg_edt_password =  dialog.findViewById(R.id.txt_password);
        LinearLayout ll_password = dialog.findViewById(R.id.ll_password);
        ll_password.setVisibility(View.GONE);
        if (user != null) {
            dlg_edt_fname.setText(user.getFirstName());
            dlg_edt_lname.setText(user.getLastName());
            dlg_edt_username.setText(user.getUserName());
            dlg_edt_email.setText(user.getEmail());
        }


        Button btn_dialog_add =  dialog.findViewById(R.id.btn_dialog_add);
        Button btn_dialog_cancel =  dialog.findViewById(R.id.btn_dialog_cancel);
        Button btn_dialog_delete = dialog.findViewById(R.id.btn_dialog_delete);

        btn_dialog_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isUpdateUserValid()) {
                    dialog.dismiss();

                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put("UserID", user.getUserID());
                        jsonObject.put("FirstName", dlg_edt_fname.getText().toString());
                        jsonObject.put("LastName", dlg_edt_lname.getText().toString());
                        jsonObject.put("UserName", dlg_edt_username.getText().toString());
                        jsonObject.put("Email", dlg_edt_email.getText().toString());
                        jsonObject.put("Active", user.isActive());
                        jsonObject.put("CreatedBy", user.getCreatedBy());
                        jsonObject.put("UpdateBy", MyApplication.getPrefranceData("UserID"));

                        addUser(jsonObject);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }

            }
        });

        btn_dialog_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                dialog.dismiss();
            }
        });


        dialog.show();
    }

    private boolean isUserValid() {

        if (dlg_edt_fname.getText().toString().trim().length() == 0) {
            dlg_edt_fname.setError("Can't be blank");
            dlg_edt_fname.requestFocus();
            return false;
        } else if (dlg_edt_lname.getText().toString().trim().length() == 0) {
            dlg_edt_lname.setError("Can't be blank");
            dlg_edt_lname.requestFocus();
            return false;
        } else if (dlg_edt_email.getText().toString().trim().length() == 0) {
            dlg_edt_email.setError("Can't be blank");
            dlg_edt_email.requestFocus();
            return false;
        } else if (!Utils.isValidEmail(dlg_edt_email.getText().toString().trim())) {
            dlg_edt_email.setError("Email is not valid");
            dlg_edt_email.requestFocus();
            return false;
        } else if (dlg_edt_username.getText().toString().trim().length() == 0) {
            dlg_edt_username.setError("Can't be blank");
            dlg_edt_username.requestFocus();
            return false;
        } else if (dlg_edt_password.getText().toString().trim().length() == 0) {
            dlg_edt_password.setError("Can't be blank");
            dlg_edt_password.requestFocus();
            return false;
        }
        return true;
    }

    private boolean isUpdateUserValid() {

        if (dlg_edt_fname.getText().toString().trim().length() == 0) {
            dlg_edt_fname.setError("Can't be blank");
            dlg_edt_fname.requestFocus();
            return false;
        } else if (dlg_edt_lname.getText().toString().trim().length() == 0) {
            dlg_edt_lname.setError("Can't be blank");
            dlg_edt_lname.requestFocus();
            return false;
        } else if (dlg_edt_email.getText().toString().trim().length() == 0) {
            dlg_edt_email.setError("Can't be blank");
            dlg_edt_email.requestFocus();
            return false;
        } else if (!Utils.isValidEmail(dlg_edt_email.getText().toString().trim())) {
            dlg_edt_email.setError("Email is not valid");
            dlg_edt_email.requestFocus();
            return false;
        } else if (dlg_edt_username.getText().toString().trim().length() == 0) {
            dlg_edt_username.setError("Can't be blank");
            dlg_edt_username.requestFocus();
            return false;
        }
        return true;
    }

}
