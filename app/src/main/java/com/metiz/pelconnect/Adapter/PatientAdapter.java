package com.metiz.pelconnect.Adapter;

import android.content.Context;
import android.content.Intent;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.metiz.pelconnect.PatientListActivityNew;
import com.metiz.pelconnect.R;
import com.metiz.pelconnect.TabActivity;
import com.metiz.pelconnect.model.PatientPOJO;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by abc on 10/3/2017.
 */

public class PatientAdapter extends RecyclerView.Adapter<PatientAdapter.MyViewHolder> {

    private LayoutInflater inflater;
    public List<PatientPOJO> patientList, filterList;
    Context context;

    private String getFormate(String date) throws ParseException {
        if (date != null && !date.isEmpty()) {
            Date d = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a", Locale.ENGLISH).parse(date);
//        5/21/1955 12:00:00 AM
            Log.d("Date", String.valueOf(d));
            Calendar cal = Calendar.getInstance();
            cal.setTime(d);
            String monthName = new SimpleDateFormat("MM/dd/yyyy").format(cal.getTime());
            return monthName;
        } else {
            return "";
        }
    }

    public PatientAdapter(Context context, List<PatientPOJO> patientList) {
        this.patientList = patientList;
        inflater = LayoutInflater.from(context);
        this.context = context;
        this.filterList = new ArrayList<>();
        // we copy the original list to the filter list and use it for setting row values
        this.filterList.addAll(this.patientList);

    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // infalte the item Layout
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_patient_list, parent, false);
        // set the view's size, margins, paddings and layout parameters
        MyViewHolder vh = new MyViewHolder(v) {
        }; // pass the view to View Holder
        return vh;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        // set the data in items
        String myFormate = null;
        try {
            myFormate = getFormate(filterList.get(position).getDob());
            holder.txtDob.setText(myFormate);
            Log.d("date", myFormate);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        holder.txtFirstName.setText(filterList.get(position).getLastname() + ", " + filterList.get(position).getFirstname() + " (" + filterList.get(position).getGender().substring(0, 1) + ")");
        holder.txtgender.setText(filterList.get(position).getGender());

        holder.txtPhone.setText(filterList.get(position).getPhone());

        holder.img_history.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Intent i = new Intent(context, OrderHistoryActivity.class);
//                i.putExtra("isFacility", true);
//                i.putExtra("patientId",filterList.get(position).getExternal_patient_id());
//                context.startActivity(i);


                Intent i = new Intent(context, TabActivity.class);
                i.putExtra("obj", filterList.get(position));
                context.startActivity(i);
            }
        });

        holder.img_create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((PatientListActivityNew) context).getOrderndDelivery(filterList.get(position));
            }
        });

        holder.img_med_sheet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((PatientListActivityNew) context).openMedsheetDialog(filterList.get(position).getExternal_facility_name(),filterList.get(position).getExternal_patient_id());
            }
        });

    }

    @Override
    public int getItemCount() {
        return (null != filterList ? filterList.size() : 0);
    }


    public class MyViewHolder extends RecyclerView.ViewHolder {
        // init the item view's
        TextView txtFirstName;
        TextView txtgender;
        TextView txtDob;
        TextView txtPhone;
        LinearLayout ll_main;
        ImageView img_create;
        ImageView img_history;
        ImageView img_med_sheet;


        public MyViewHolder(View itemView) {
            super(itemView);
            // get the reference of item view's
            txtFirstName = (TextView) itemView.findViewById(R.id.txt_first_name);
            txtgender = (TextView) itemView.findViewById(R.id.txt_gender);
            txtDob = (TextView) itemView.findViewById(R.id.txt_dob);
            txtPhone = (TextView) itemView.findViewById(R.id.txt_phone);
            ll_main = (LinearLayout) itemView.findViewById(R.id.ll_main);
            img_create = (ImageView) itemView.findViewById(R.id.img_create);
            img_history = (ImageView) itemView.findViewById(R.id.img_history);
            img_med_sheet = (ImageView) itemView.findViewById(R.id.img_med_sheet);

            img_create.setVisibility(View.VISIBLE);
            img_history.setVisibility(View.VISIBLE);
        }
    }

    public void filter(final String text) {

        // Searching could be complex..so we will dispatch it to a different thread...
        new Thread(new Runnable() {
            @Override
            public void run() {

                // Clear the filter list
                filterList.clear();
                // If there is no search value, then add all original list items to filter list
                if (TextUtils.isEmpty(text)) {
                    filterList.addAll(patientList);

                } else {
                    // Iterate in the original List and add it to filter list...
                    for (PatientPOJO item : patientList) {

                        if ((item.getLastname().toLowerCase().trim() + " " + item.getFirstname().toLowerCase().trim()).contains(text.toLowerCase().trim()) ||
                                (item.getFirstname().toLowerCase().trim() + " " + item.getLastname().toLowerCase().trim()).contains(text.toLowerCase().trim())) {
                            // Adding Matched items
                            filterList.add(item);
                        }
                    }
                }
                // Set on UI Thread
                ((PatientListActivityNew) context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // Notify the List that the DataSet has changed...
                        notifyDataSetChanged();
                    }
                });

            }
        }).start();

    }
}
