package com.metiz.pelconnect.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.metiz.pelconnect.R;
import com.metiz.pelconnect.model.ModelNotificationDetails;
import com.metiz.pelconnect.model.ModelNotificationListDetails;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by paras on 8/16/20.
 */
public class NotificationListAdapter extends RecyclerView.Adapter<NotificationListAdapter.ViewHolder> {

    private Context context;
    private List<ModelNotificationListDetails> list;
    JSONArray arrData;
    List<ModelNotificationDetails> modelNotificationDetailsList = new ArrayList<>();

    public NotificationListAdapter(Context context, JSONArray arrData) {
        this.context = context;
        this.arrData = arrData;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.row_item_notification_list, parent, false);
        ButterKnife.bind(this, view);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        //final ModelNotificationDetails modelNotificationDetails = modelNotificationDetailsList.get(position);

        try {
            JSONObject arr = arrData.getJSONObject(position);

            holder.tv_dl_number.setText(arr.getString("DeliveryNo"));
            holder.tv_drug_name.setText(arr.getString("drug"));
            holder.tv_qty.setText(arr.getString("qty"));
            holder.tv_rx.setText(arr.getString("Rx"));
            holder.tv_patient_name.setText(arr.getString("Patient"));
            holder.tv_driver_name.setText(arr.getString("Driver"));

            System.out.println(arr);

        } catch (JSONException e) {
            e.printStackTrace();
        }


          /*  if(position == 0){
                holder.tv_dl_number.setText(modelNotificationDetails.getValueText());
            }
            if(position == 1){
                holder.tv_patient_name.setText(modelNotificationDetails.getValueText());
            }
            if(position == 2){
                holder.tv_drug_name.setText(modelNotificationDetails.getValueText());
            }
            if(position == 3){
                holder.tv_rx.setText(modelNotificationDetails.getValueText());
            }
            if(position == 4){
                holder.tv_qty.setText(modelNotificationDetails.getValueText());
            }
            if(position == 5){
                holder.tv_driver_name.setText(modelNotificationDetails.getValueText());
            }*/


    }

    @Override
    public int getItemCount() {
        return arrData.length();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        // Todo Butterknife bindings

        @BindView(R.id.tv_dl_number)
        TextView tv_dl_number;
        @BindView(R.id.tv_drug_name)
        TextView tv_drug_name;
        @BindView(R.id.tv_patient_name)
        TextView tv_patient_name;
        @BindView(R.id.tv_rx)
        TextView tv_rx;
        @BindView(R.id.tv_qty)
        TextView tv_qty;
        @BindView(R.id.tv_driver_name)
        TextView tv_driver_name;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

}