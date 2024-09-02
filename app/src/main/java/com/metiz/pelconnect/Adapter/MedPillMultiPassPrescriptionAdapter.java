package com.metiz.pelconnect.Adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.metiz.pelconnect.MedPrescriptionDetailsActivity;
import com.metiz.pelconnect.R;
import com.metiz.pelconnect.model.MedMultiPrescriptionListModel;
import com.metiz.pelconnect.model.MedPillMultiMedpassModel;
import com.metiz.pelconnect.util.Utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class MedPillMultiPassPrescriptionAdapter extends RecyclerView.Adapter<MedPillMultiPassPrescriptionAdapter.MyViewHolder> {

    private LayoutInflater inflater;
    public List<MedPillMultiMedpassModel.DataBean.ObjdetailsBean> patientList, filterList;
    private Context context;
    private Activity activity;
    private boolean Is_PrescriptionImg;
    private boolean isNote;
    private boolean isMissDose;
    private boolean isListEmpty;
    private int missDoseCount;
    private String multiPack;
    private String pillId;
    private String QRcodeformat;

    public MedPillMultiPassPrescriptionAdapter(Activity activity,Context context, List<MedPillMultiMedpassModel.DataBean.ObjdetailsBean> patientList, boolean Is_PrescriptionImg, boolean isNote, int missDoseCount, boolean isMissDose,boolean isListEmpty,String multiPack,String pillId,String QRcodeformat) {
        this.patientList = patientList;
        inflater = LayoutInflater.from(context);
        this.context = context;
        this.activity = activity;
        this.filterList = new ArrayList<>();
        // we copy the original list to the filter list and use it for setting row values
        this.filterList.addAll(this.patientList);
        this.Is_PrescriptionImg = Is_PrescriptionImg;
        this.isNote = isNote;
        this.isMissDose = isMissDose;
        this.isListEmpty = isListEmpty;
        this.missDoseCount = missDoseCount;
        this.multiPack = multiPack;
        this.pillId = pillId;
        this.QRcodeformat = QRcodeformat;
    }

    @Override
    public MedPillMultiPassPrescriptionAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // infalte the item Layout
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_med_pass_prescription_list, parent, false);
        // set the view's size, margins, paddings and layout parameters
        MedPillMultiPassPrescriptionAdapter.MyViewHolder vh = new MedPillMultiPassPrescriptionAdapter.MyViewHolder(v) {
        }; // pass the view to View Holder
        return vh;

    }

    @Override
    public void onBindViewHolder(MedPillMultiPassPrescriptionAdapter.MyViewHolder holder, @SuppressLint("RecyclerView") final int position) {
        // set the data in items

        holder.drugName.setText(filterList.get(position).getDrug());
        holder.rxNumber.setText("Rx Number: " + filterList.get(position).getRx_number());
        holder.txt_drug_time.setText("Dose Time: " + filterList.get(position).getDose_time());

        String startDateString = Utils.DateFromOnetoAnother(filterList.get(position).getDose_date(), "yyyy-MM-dd'T'HH:mm:ss", "MM/dd/yy");
        holder.txt_drug_date.setText("Dose : " + startDateString + " " + filterList.get(position).getDose_time());
        //holder.txt_drug_date.setText("Dose Date : " + filterList.get(position).getDose_date());

        holder.ll_main.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.e("Check Data","Call here" );
                Log.e("TAG", "getDose_Qty(): " + filterList.get(position).getDose_Qty());
                Intent intent = new Intent(context, MedPrescriptionDetailsActivity.class);
                intent.putExtra("drug", filterList.get(position).getDrug());
                intent.putExtra("isNote", isNote);
                intent.putExtra("listSize", filterList.size());
                intent.putExtra("rxNumber", filterList.get(position).getRx_number());
                intent.putExtra("sig_detail", filterList.get(position).getSig_detail());
                intent.putExtra("dose_time", filterList.get(position).getDose_time());
                intent.putExtra("dose_date", filterList.get(position).getDose_date());
                intent.putExtra("dose_Qty", filterList.get(position).getDose_Qty());
                intent.putExtra("tran_id", filterList.get(position).getTran_id());
                intent.putExtra("isMissDose", isMissDose);
                intent.putExtra("missDoseCount", missDoseCount);
                intent.putExtra("isListEmpty", isListEmpty);
                intent.putExtra("gender", "(" + filterList.get(position).getGender().substring(0, 1) + ")");
                intent.putExtra("patient_fName",  filterList.get(position).getPatient_first());
                intent.putExtra("patient_lName",  filterList.get(position).getPatient_last());
                intent.putExtra("fname",  filterList.get(position).getPatient_first());
                intent.putExtra("lname",  filterList.get(position).getPatient_last());
                intent.putExtra("facility_id", filterList.get(position).getFacility_id());
                intent.putExtra("BirthDate", filterList.get(position).getDOB());
                intent.putExtra("patientId", filterList.get(position).getPatient_id());
                intent.putExtra("ndc", filterList.get(position).getNdc());
                intent.putExtra("name", filterList.get(position).getPatient_last() + ", " + filterList.get(position).getPatient_first());
                intent.putExtra("isMissedDose", filterList.get(position).isIsMissedDose());
                intent.putExtra("Is_PrescriptionImg", Is_PrescriptionImg);
                intent.putExtra("multiPack", multiPack);
                intent.putExtra("pillId", pillId);
                intent.putExtra("QRcodeformat", QRcodeformat);
                intent.putExtra("IsErly", filterList.get(position).isErly());
                intent.putExtra("IsLate", filterList.get(position).isLate());
                intent.putExtra("comeFrom", "scan");
                try {
                    intent.putExtra("BirthDate", getFormate(filterList.get(position).getDOB()));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                intent.putExtra("gender", "(" + filterList.get(position).getGender().substring(0, 1) + ")");
//                context.startActivity(intent);
                activity.startActivityForResult(intent,103);


            }
        });

    }

    private String getFormate(String date) throws ParseException {
        if (date != null && !date.isEmpty()) {
            Date d = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH).parse(date);
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

    @Override
    public int getItemCount() {
        return (null != filterList ? filterList.size() : 0);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        // init the item view's
        TextView drugName, rxNumber, txt_drug_time, txt_drug_date;
        LinearLayout ll_main;

        public MyViewHolder(View itemView) {
            super(itemView);
            // get the reference of item view's
            drugName = (TextView) itemView.findViewById(R.id.txt_drug_name);
            rxNumber = (TextView) itemView.findViewById(R.id.txt_rx_number);
            ll_main = (LinearLayout) itemView.findViewById(R.id.ll_main);
            txt_drug_time = (TextView) itemView.findViewById(R.id.txt_drug_time);
            txt_drug_date = itemView.findViewById(R.id.txt_drug_date);
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
                    for (MedPillMultiMedpassModel.DataBean.ObjdetailsBean item : patientList) {
                        if ((item.getDrug().toLowerCase().trim().contains(text.toLowerCase().trim())) || item.getRx_number().toLowerCase().trim().contains(text.toLowerCase().trim())) {
                            // Adding Matched items
                            filterList.add(item);
                        }
                    }
                }
                ((FragmentActivity) context).runOnUiThread(new Runnable() {
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
