package com.metiz.pelconnect.Adapter;

import android.content.Context;

import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.metiz.pelconnect.R;
import com.metiz.pelconnect.activity.ReceivedDrugListDetailsActivity;
import com.metiz.pelconnect.model.ModelReceivedListDetails;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by hp on 13/3/18.
 */
public class ReceivedDrugListDetailsAdapter extends
        RecyclerView.Adapter<ReceivedDrugListDetailsAdapter.ViewHolder> {

    private static final String TAG = ReceivedDrugListDetailsAdapter.class.getSimpleName();
    private Context context;
    private List<ModelReceivedListDetails> list,filterList;

    public ReceivedDrugListDetailsAdapter(Context context, List<ModelReceivedListDetails> list
    ) {
        this.context = context;
        this.list = list;

        this.filterList = new ArrayList<>();
        // we copy the original list to the filter list and use it for setting row values
        this.filterList.addAll(this.list);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        // Todo Butterknife bindings
        @BindView(R.id.tv_delivery_no)
        TextView tv_delivery_no;
        @BindView(R.id.tv_rx_number)
        TextView tv_rx_number;
        @BindView(R.id.tv_patient_name)
        TextView tv_patient_name;
        @BindView(R.id.tv_drug_name)
        TextView tv_drug_name;
        @BindView(R.id.tv_qty)
        TextView tv_qty;
        @BindView(R.id.tv_creaetd_on)
        TextView tv_creaetd_on;
        @BindView(R.id.tv_notes)
        TextView tv_notes;
        @BindView(R.id.ll_note)
        LinearLayout ll_note;
        @BindView(R.id.view_line)
        View view_line;


        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

        }


    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.row_received_drug_list_details, parent, false);
        ButterKnife.bind(this, view);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.tv_delivery_no.setText(filterList.get(position).getDelivery_no());
        holder.tv_patient_name.setText(filterList.get(position).getPatient());
        holder.tv_drug_name.setText(filterList.get(position).getDrug());
        holder.tv_qty.setText(filterList.get(position).getQty());
        holder.tv_rx_number.setText(filterList.get(position).getRx_No());
        try {
            holder.tv_creaetd_on.setText((getFormate(filterList.get(position).getCreaetdOn())));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (!filterList.get(position).getNote().equalsIgnoreCase("")){
            holder.tv_notes.setText(filterList.get(position).getNote());
            holder.ll_note.setVisibility(View.VISIBLE);
            holder.view_line.setVisibility(View.VISIBLE);
        }else {
            holder.ll_note.setVisibility(View.GONE);
            holder.view_line.setVisibility(View.GONE);
        }

    }

    public String getFormate(String date) throws ParseException {
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

    public void filter(final String text) {

        // Searching could be complex..so we will dispatch it to a different thread...
        new Thread(new Runnable() {
            @Override
            public void run() {

                // Clear the filter list
                filterList.clear();
                // If there is no search value, then add all original list items to filter list
                if (TextUtils.isEmpty(text)) {
                    filterList.addAll(list);

                } else {
                    // Iterate in the original List and add it to filter list...
                    for (ModelReceivedListDetails item : list) {

                        if ((item.getDrug().toLowerCase().trim() + " " + item.getDrug().toLowerCase().trim()).contains(text.toLowerCase().trim()) ||
                                (item.getDrug().toLowerCase().trim() + " " + item.getDrug().toLowerCase().trim()).contains(text.toLowerCase().trim())) {
                            // Adding Matched items
                            filterList.add(item);
                        }
                    }
                }
                // Set on UI Thread
                ((ReceivedDrugListDetailsActivity) context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // Notify the List that the DataSet has changed...
                        notifyDataSetChanged();
                    }
                });

            }
        }).start();

    }



    @Override
    public int getItemCount() {
        return (null != filterList ? filterList.size() : 0);
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }


}