package com.metiz.pelconnect.Adapter;

import android.content.Context;
import android.content.Intent;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.metiz.pelconnect.R;
import com.metiz.pelconnect.activity.ReceivedDrugListActivity;
import com.metiz.pelconnect.activity.ReceivedDrugListDetailsActivity;
import com.metiz.pelconnect.model.ModelReceivedDrug;

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
public class ReceivedDrugAdapter extends
        RecyclerView.Adapter<ReceivedDrugAdapter.ViewHolder> {

    private static final String TAG = ReceivedDrugAdapter.class.getSimpleName();


    private Context context;
    private List<ModelReceivedDrug> list,filterList;

    public ReceivedDrugAdapter(Context context, List<ModelReceivedDrug> list
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
        @BindView(R.id.tv_received_by)
        TextView tv_received_by;
        @BindView(R.id.tv_received_on)
        TextView tv_received_on;
        @BindView(R.id.tv_notes)
        TextView tv_notes;
        @BindView(R.id.tv_ok)
        TextView tv_ok;
        @BindView(R.id.ll_note)
        LinearLayout ll_note;


        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

        }
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.row_received_drug, parent, false);
        ButterKnife.bind(this, view);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.tv_delivery_no.setText(filterList.get(position).getDelivery_no().toUpperCase());
        holder.tv_received_by.setText(filterList.get(position).getCreatedName());

        try {
            holder.tv_received_on.setText((getFormate(filterList.get(position).getCreaetdOn())));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (!filterList.get(position).getNote().equalsIgnoreCase("")){
            holder.tv_notes.setText(filterList.get(position).getNote());
            holder.ll_note.setVisibility(View.VISIBLE);
        }else {
            holder.ll_note.setVisibility(View.GONE);

        }
        holder.tv_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ReceivedDrugListDetailsActivity.class);
                intent.putExtra("transID",filterList.get(position).getTran_id());
                context.startActivity(intent);
            }
        });

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
                    for (ModelReceivedDrug item : list) {

                        if ((item.getDelivery_no().toLowerCase().trim() + " " + item.getDelivery_no().toLowerCase().trim()).contains(text.toLowerCase().trim()) ||
                                (item.getDelivery_no().toLowerCase().trim() + " " + item.getDelivery_no().toLowerCase().trim()).contains(text.toLowerCase().trim())) {
                            // Adding Matched items
                            filterList.add(item);
                        }
                    }
                }
                // Set on UI Thread
                ((ReceivedDrugListActivity) context).runOnUiThread(new Runnable() {
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