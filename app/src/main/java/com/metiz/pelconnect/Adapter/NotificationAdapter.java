package com.metiz.pelconnect.Adapter;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.reflect.TypeToken;
import com.kaopiz.kprogresshud.KProgressHUD;
import com.metiz.pelconnect.MyApplication;
import com.metiz.pelconnect.R;
import com.metiz.pelconnect.customview.ActionEditText;
import com.metiz.pelconnect.model.Notification;
import com.metiz.pelconnect.model.ReminderNotificationModel;
import com.metiz.pelconnect.network.API;
import com.metiz.pelconnect.network.NetworkUtility;
import com.metiz.pelconnect.network.VolleyCallBack;
import com.metiz.pelconnect.retrofit.ApiClient;
import com.metiz.pelconnect.retrofit.ApiInterface;
import com.metiz.pelconnect.util.NotificationListActivity;
import com.metiz.pelconnect.util.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.metiz.pelconnect.util.Utils.dismissProgressDialog;

/**
 * Created by hp on 13/3/18.
 */
public class NotificationAdapter extends
        RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    private static final String TAG = NotificationAdapter.class.getSimpleName();


    NotificationListActivity context;

    private List<Notification> list;

    public NotificationAdapter(NotificationListActivity notificationListActivity, List<Notification> notificationList) {
        this.context = notificationListActivity;
        this.list = notificationList;
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        // Todo Butterknife bindings

        @BindView(R.id.txt_name)
        TextView txtName;
        @BindView(R.id.txt_notes)
        TextView txtNotes;
        @BindView(R.id.txt_last_date)
        TextView txtLastDate;
        @BindView(R.id.detailBT)
        Button detailBT;
        @BindView(R.id.txt_last_time)
        TextView txt_last_time;
        /* @BindView(R.id.txt_notification_date_time)
         TextView txt_notification_date_time;*/
        @BindView(R.id.ll_main)
        LinearLayout llMain;
        /*   @BindView(R.id.txt_order_date)
           TextView txtOrderDate;*/
        @BindView(R.id.txt_patient_name)
        TextView txtPatientName;
        @BindView(R.id.ll_patient)
        LinearLayout llPatient;
        @BindView(R.id.img_reply)
        ImageView imgReply;
        @BindView(R.id.llMainLayout)
        LinearLayout llMainLayout;

        /*   @BindView(R.id.tv_reply_info)
           TextView tvReplyInfo;*/
        /*  @BindView(R.id.ll_reply_info)
        LinearLayout llReplyInfo;*/
        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

        }


    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View view = inflater.inflate(R.layout.row_notification, parent, false);
        ButterKnife.bind(this, view);

        ViewHolder viewHolder = new ViewHolder(view);

        return viewHolder;
    }


    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Notification item = list.get(position);

        try {
            if (MyApplication.getPrefranceData("UserID").equalsIgnoreCase(String.valueOf(item.getCreatedByID()))) {
                holder.llMainLayout.setPadding(140, 0, 0, 0);
            } else {
                holder.llMainLayout.setPadding(0, 0, 140, 0);
            }

            holder.txtName.setText(item.getDrug());
            holder.txtNotes.setText(item.getNote());
            holder.txtLastDate.setText(item.getDosedateStr());


             /*        try{
                Date date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.s").parse(item.getTime());
                String newString = new SimpleDateFormat("H:mm").format(date);
                holder.txt_notification_date_time.setText(item.getTimestr()+","+newString);
            }
            catch (Exception e){
                Date date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(item.getTime());
                String newString = new SimpleDateFormat("H:mm").format(date);
                holder.txt_notification_date_time.setText(item.getTimestr()+","+newString);
            }
*/
            Log.e("item.getDoseTime():", "onBindViewHolder: " + item.getDoseTime());
            if (item.getTime() != null && !item.getTime().isEmpty()) {
                try {
                    String time = "";

                    DateFormat f1 = new SimpleDateFormat("HH:mm:ss.SSS"); //HH for hour of the day (0 - 23)
                    Date d = f1.parse(item.getDoseTime());
                    DateFormat f2 = new SimpleDateFormat("h:mma");
                    time = f2.format(d).toLowerCase(); // "12:18am"
                    System.out.println(f2.format(d).toLowerCase());


                    holder.txt_last_time.setText(time);





                  /*  Date date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.s").parse(item.getTime());
                    String newString = new SimpleDateFormat("H:mm").format(date);
                    holder.txt_last_time.setText(newString);*/

                } catch (Exception e) {
                    Log.e(TAG, "dateIssue: " + e.getMessage());
                  /*  Date date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(item.getTime());
                    String newString = new SimpleDateFormat("H:mm").format(date);
                    holder.txt_last_time.setText(newString);*/
                }

            }

            holder.detailBT.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String time = "";
                    try {
                        DateFormat f1 = new SimpleDateFormat("HH:mm:ss.SSS"); //HH for hour of the day (0 - 23)
                        Date d = f1.parse(item.getDoseTime());
                        DateFormat f2 = new SimpleDateFormat("h:mma");
                        time = f2.format(d).toLowerCase(); // "12:18am"
                        System.out.println(f2.format(d).toLowerCase());
                    } catch (final ParseException e) {
                        e.printStackTrace();
                    }
                    //----------

//                    try {
//                        final SimpleDateFormat sdf = new SimpleDateFormat("H:mm");
//                        final Date dateObj = sdf.parse(holder.txt_last_time.getText().toString());
//                        System.out.println(dateObj);
//                        System.out.println(new SimpleDateFormat("K:mm").format(dateObj));
//                    } catch (final ParseException e) {
//                        e.printStackTrace();
//                    }


                    MissDoseNotification(item.getPatientID(), item.getDosedateStr(), item.getDoseTime(), item.getPatientName(), item.getEmailType());
                }
            });

          /*  if(item.getOrderCreatedDate().equals("")){
                holder.txtOrderDate.setText(item.getFirstName().trim());
            }else{
                holder.txtOrderDate.setText(item.getFirstName().trim() + " " + item.getOrderCreatedDate().trim());
            }*/

          /*

            Log.e(TAG, "time: "+newString );
            Log.e("getPatientName:", "---: "+item.getPatientName() );*/
            if (item.getPatientName() != null && !item.getPatientName().isEmpty()) {
//            holder.llPatient.setVisibility(View.VISIBLE);
                holder.txtPatientName.setText(item.getPatientName());
            } else {
//            holder.llPatient.setVisibility(View.GONE);
                holder.txtPatientName.setText("-");
            }
            /*    if (item.getEmailType().equalsIgnoreCase("2") || item.getEmailType().equalsIgnoreCase("8")) {
                holder.tvReplyInfo.setText("REPLY");
            } else {
                holder.tvReplyInfo.setText("INFO");
            }*/
            /*  if (item.getEmailType().equalsIgnoreCase("12") || item.getEmailType().equalsIgnoreCase("13")) {
                holder.tvReplyInfo.setText("REPLY");
            } else {
                holder.tvReplyInfo.setText("INFO");
            }*/
        } catch (Exception e) {
            e.printStackTrace();
        }

            /*
        holder.llReplyInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (item.getEmailType().equalsIgnoreCase("12") || item.getEmailType().equalsIgnoreCase("13")) {
                    dialogReply(item.getNote(), item.getTimestr(), item.getFirstName(), item.getOrderID());

                } else if (item.getEmailType().equalsIgnoreCase("11")){
                    Intent intent = new Intent(context, NotificationListTypeActivity.class);
                    intent.putExtra("EmailType", item.getEmailType());
                    intent.putExtra("time",item.getTime());
                    intent.putExtra("FacilityID",Application.getPrefranceDataInt("ExFacilityID"));
                    context.startActivity(intent);
                } else{
                    Intent intent = new Intent(context, NotificationDetilsActivity.class);
                    intent.putExtra("OrderID", item.getOrderID());
                    intent.putExtra("HeaderID", item.getHeaderID());
                    intent.putExtra("DailyHoldRxID", item.getDailyHoldRxID());
                    intent.putExtra("EmailType", item.getEmailType());
                    intent.putExtra("PatientID", item.getPatientID());
                    intent.putExtra("PharmacyClientID", item.getPharmacyClientID());
                    intent.putExtra("time",item.getTime());
                    intent.putExtra("FacilityID",Application.getPrefranceDataInt("ExFacilityID"));
                    context.startActivity(intent);
                }
            }
        });*/
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    private void dialogReply(String notes, String date, String first_name, int orderId) {
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_reply_notification);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(true);
        TextView tvNotes = (TextView) dialog.findViewById(R.id.tv_notes);
        TextView pharmacy_team = (TextView) dialog.findViewById(R.id.tv_date);
        ActionEditText edt_reply = (ActionEditText) dialog.findViewById(R.id.edt_reply);
        Button send = (Button) dialog.findViewById(R.id.btn_dialog_send);
        Button cancel = (Button) dialog.findViewById(R.id.btn_dialog_cancel);
        tvNotes.setText(notes);
        pharmacy_team.setText(first_name + " " + date);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (edt_reply.getText().toString().trim().isEmpty()) {
                    Utils.showAlertToast(context, "Please enter reply");
                } else {
                    replyNotificationApiCall(edt_reply.getText().toString().trim(), orderId);
                    dialog.dismiss();
                }

            }
        });
        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    void MissDoseNotification(String patientID, String datestr, String timestr, String patientNameTxt, String emailType) {
        KProgressHUD hud;
        hud = KProgressHUD.create(context)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setLabel("Please wait...")
                .setCancellable(false);
        ApiInterface apiService =
                ApiClient.getClient().create(ApiInterface.class);
        Log.e("date --", "" + datestr);
        hud.show();
        Call<ReminderNotificationModel> call = apiService.GetMissdoseNotificationByPatientID(MyApplication.getPrefranceDataInt("ExFacilityID"), MyApplication.getPrefranceData("UserID"), patientID, datestr, timestr, emailType);
        call.enqueue(new Callback<ReminderNotificationModel>() {
            @SuppressLint("DefaultLocale")
            @Override
            public void onResponse(@NonNull Call<ReminderNotificationModel> call, @NonNull Response<ReminderNotificationModel> response) {
                hud.dismiss();

                try {
                    int response_code = response.body().getResponseStatus();
                    Log.e("onResponse111:", "tag111: " + response.body() + " --- " + response.body().getData().size());

                    if (response_code == 1) {

                        final Dialog dialog = new Dialog(context);
                        dialog.setContentView(R.layout.dialog_notification_list);

                        // set the custom dialog components - text, image and button

                        RecyclerView recyclerView = dialog.findViewById(R.id.recyclerView);
                        TextView patientName = dialog.findViewById(R.id.tvPatientName);
                        TextView tvInfo = dialog.findViewById(R.id.tvInfo);
                        ImageView iv_close = dialog.findViewById(R.id.iv_close);
                        patientName.setText(patientNameTxt);
                        if(emailType.equalsIgnoreCase("14")){
                            tvInfo.setText("You have missed below medication.");
                        }
                        else if(emailType.equalsIgnoreCase("15")){
                            tvInfo.setText("You have reminder below medication.");
                        }


                        iv_close.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                dialog.dismiss();
                            }
                        });
                        recyclerView.setLayoutManager(new LinearLayoutManager(context));
                        MissDoseListAdapter missDoseListAdapter = new MissDoseListAdapter(context, response.body().getData());
                        recyclerView.setAdapter(missDoseListAdapter);
                        //                        // if button is clicked, close the custom dialog
//                        dialogButton.setOnClickListener(new View.OnClickListener() {
//                            @Override
//                            public void onClick(View v) {
//                                dialog.dismiss();
//                            }
//                        });

                        dialog.show();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    //dismissProgressDialog();
                }


            }

            @Override
            public void onFailure(@NonNull Call<ReminderNotificationModel> call, Throwable t) {
                Log.e("TAG", t.toString());
                hud.dismiss();
            }
        });

    }

    private void replyNotificationApiCall(String note, int ordrId) {
        JSONObject json = new JSONObject();
        try {
            json.put("Note", note);
            json.put("OrderID", ordrId);
            json.put("FacilityID", MyApplication.getPrefranceDataInt("ExFacilityID"));
            json.put("CreatedBy", MyApplication.getPrefranceData("UserID"));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        Utils.showProgressDialog(context);
        NetworkUtility.makeJSONObjectRequest(API.SendReply, json, API.SendReply, new VolleyCallBack() {
            @Override
            public void onSuccess(JSONObject result) {
                dismissProgressDialog();

                dismissProgressDialog();
                if (result != null) {

                    Notification notification = null;
                    try {
                        notification = MyApplication.getGson().fromJson(result.getJSONObject("Data").toString(), new TypeToken<Notification>() {
                        }.getType());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    list.add(0, notification);
                    notifyDataSetChanged();
                    context.scrollList();
                }

            }

            @Override
            public void onError(JSONObject result) {
                dismissProgressDialog();
            }
        });
    }


}