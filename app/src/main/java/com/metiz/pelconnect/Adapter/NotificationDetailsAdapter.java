package com.metiz.pelconnect.Adapter;

import android.content.Context;

import androidx.recyclerview.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import com.metiz.pelconnect.R;
import com.metiz.pelconnect.model.ModelNotificationDetails;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * Created by shopify-1 on 19/2/18.
 */

public class NotificationDetailsAdapter extends RecyclerView.Adapter<NotificationDetailsAdapter.ViewHolder> {
    Context context;
    List<ModelNotificationDetails> modelNotificationDetailsList = new ArrayList<>();

    public NotificationDetailsAdapter(Context context, List<ModelNotificationDetails> modelNotificationDetailsList) {
        this.context = context;
        this.modelNotificationDetailsList = modelNotificationDetailsList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_item_notification_details, parent, false);

        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final ModelNotificationDetails modelNotificationDetails = modelNotificationDetailsList.get(position);

        if (modelNotificationDetails.getKeyText().equalsIgnoreCase("Patient") ||modelNotificationDetails.getKeyText().equalsIgnoreCase("Allergies")){
            holder.ValueText.setTextColor(context.getResources().getColor(R.color.colorPrimary));

        } if (modelNotificationDetails.getKeyText().equalsIgnoreCase("Note")){
            holder.ValueText.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                    context.getResources().getDimension(R.dimen._12sdp));
        }
        holder.keyText.setText(modelNotificationDetails.getKeyText());
        holder.ValueText.setText(modelNotificationDetails.getValueText());
        }

    @Override
    public int getItemCount() {
        return modelNotificationDetailsList.size();
    }
    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.keyText)
        TextView keyText;
        @BindView(R.id.ValueText)
        TextView ValueText;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
