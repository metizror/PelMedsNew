package com.metiz.pelconnect.Adapter;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.facebook.drawee.view.SimpleDraweeView;
import com.metiz.pelconnect.MyApplication;
import com.metiz.pelconnect.BaseActivity;
import com.metiz.pelconnect.R;
import com.metiz.pelconnect.activity.PDFOrderActivity;
import com.metiz.pelconnect.model.OrderHistory;
import com.metiz.pelconnect.network.API;
import com.metiz.pelconnect.network.NetworkUtility;
import com.metiz.pelconnect.network.VolleyCallBack;
import com.metiz.pelconnect.util.TouchImageView;
import com.metiz.pelconnect.util.Utils;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * Created by hp on 23/1/18.
 */
public class OrderHistoryAdapter extends RecyclerView.Adapter<OrderHistoryAdapter.OrderHistoryViewHolder> {
    private final Context context;

    private List<OrderHistory> items, filterList;

    public OrderHistoryAdapter(Context context, List<OrderHistory> items) {
        this.items = items;
        this.context = context;
        this.filterList = new ArrayList<>();
        // we copy the original list to the filter list and use it for setting row values
        this.filterList.addAll(this.items);
    }

    @Override
    public OrderHistoryViewHolder onCreateViewHolder(ViewGroup parent,
                                                     int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_order_list, parent, false);
        return new OrderHistoryViewHolder(v);
    }


    @Override
    public void onBindViewHolder(OrderHistoryViewHolder holder, final int position) {
        final OrderHistory item = filterList.get(position);
        //TODO Fill in your logic for binding the view.

        holder.txtFirstName.setText(item.getPatientName());
        holder.txtDrug.setText(item.getDrug());
        try {
            holder.txtUpdatedOn.setText("Last Updated:" + item.getUpdateOn());
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        if (getStatus(item.getMobileOrderStatus()).equalsIgnoreCase("pending")) {
            holder.ll_status.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.edittext_background_red));
        } else {
            holder.ll_status.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.edittext_background_primary));

        }


        holder.txtOrderStatus.setText(getStatus(item.getMobileOrderStatus()));
        if (!item.getDrug_qty().equalsIgnoreCase("")) {
            holder.qty.setText("Qty: " + item.getDrug_qty());
        }else {
            holder.qty.setText(null);

        }
        //


        if (getStatus(item.getMobileOrderStatus()).equalsIgnoreCase("delivered")) {
            holder.imgDelete.setVisibility(View.GONE);
        } else {
            holder.imgDelete.setVisibility(View.GONE);

        }
        holder.imgDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                deleteOrderDialog(item, position);
            }
        });
        holder.imgSignature.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getSignature(item, position);
            }
        });
        if (item.getDocumentPath() != null && !item.getDocumentPath().isEmpty()) {
            holder.ll_img_picture.setVisibility(View.VISIBLE);
        } else {
            holder.ll_img_picture.setVisibility(View.GONE);
        }
        holder.ll_img_picture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (item.getDocumentPath() != null && !item.getDocumentPath().isEmpty()) {
                    // openImageDialog(item);
                    Intent intent = new Intent(context, PDFOrderActivity.class);
                    intent.putExtra("url", item.getDocumentPath());
                    context.startActivity(intent);
                }
                else
                    Utils.showAlertToast(context, "No documents found");
            }
        });
        holder.imgPrescription.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getPrescription(item, position);
            }
        });
    }

    private void deleteOrderDialog(OrderHistory item, final int pos){
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:

                        deleteOrder(item,pos);


                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Are you sure, you want to delete this order?").setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();

    }


    private void openImageDialog(OrderHistory item) {
        final Dialog dialog = new Dialog(context);

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_image);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        Window window = dialog.getWindow();
        lp.copyFrom(window.getAttributes());
//This makes the dialog take up the full width
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(lp);

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        TouchImageView dialog_image = (TouchImageView) dialog.findViewById(R.id.dialog_image);
        SimpleDraweeView drawee_image = (SimpleDraweeView) dialog.findViewById(R.id.drawee_image);
        final ImageView dialog_cancel = (ImageView) dialog.findViewById(R.id.dialog_cancel);


        dialog_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        Glide.with(context).load(item.getDocumentPath()).placeholder(R.drawable.app_icon).error(R.drawable.app_icon).into(dialog_image);
        drawee_image.setVisibility(View.GONE);
        Uri uri = Uri.parse(item.getDocumentPath());
//        Uri uri = Uri.parse("http://www.sachinmittal.com/wp-content/uploads/2017/04/47559184-image.jpg");
        drawee_image.setImageURI(uri);

        dialog.show();

    }

    private void openImageDialog(String imageURL) {
        final Dialog dialog = new Dialog(context);

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_image);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        Window window = dialog.getWindow();
        lp.copyFrom(window.getAttributes());
//This makes the dialog take up the full width
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(lp);

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        TouchImageView dialog_image = (TouchImageView) dialog.findViewById(R.id.dialog_image);
        SimpleDraweeView drawee_image = (SimpleDraweeView) dialog.findViewById(R.id.drawee_image);
        final ImageView dialog_cancel = (ImageView) dialog.findViewById(R.id.dialog_cancel);

        //http://www.sachinmittal.com/wp-content/uploads/2017/04/47559184-image.jpg

        dialog_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        Glide.with(context).load(imageURL).placeholder(R.drawable.app_icon).error(R.drawable.app_icon).into(dialog_image);
        drawee_image.setVisibility(View.GONE);
        Uri uri = Uri.parse(imageURL);
//        Uri uri = Uri.parse("http://www.sachinmittal.com/wp-content/uploads/2017/04/47559184-image.jpg");
        drawee_image.setImageURI(uri);

        dialog.show();

    }

    private String getValidUrl(String documentPath) {
        if (documentPath.startsWith("http")) {
            return documentPath;
        } else {
            return "http://" + documentPath;
        }
    }

    private void deleteOrder(OrderHistory item, final int pos) {
        NetworkUtility.makeJSONObjectRequest(API.DeleteOrder + item.getOrderID() + "&UserID=" + MyApplication.getPrefranceData("UserID"), new JSONObject(), API.DeleteOrder, new VolleyCallBack() {
            @Override
            public void onSuccess(JSONObject result) {
                try {

                    if (result.getBoolean("Data")) {
                        filterList.remove(pos);
                        items.remove(pos);
                        notifyDataSetChanged();
                        notifyItemRangeChanged(0, filterList.size());
                    }

                    Utils.showAlertToast(context, result.getString("Message"));


                } catch (Exception e) {
                    e.printStackTrace();
                }
            }


            @Override
            public void onError(JSONObject result) {

            }
        });

    }


    private void getSignature(OrderHistory item, final int pos) {

        ((BaseActivity) context).showProgressDialog(context);
        NetworkUtility.makeJSONObjectRequest(API.Signature + "?tran_id=" + item.getTran_id() + "&delivery_date=" + item.getDelivery_date(), new JSONObject(), API.Signature, new VolleyCallBack() {
            @Override
            public void onSuccess(JSONObject result) {

                try {
                    ((BaseActivity) context).dismissProgressDialog();
                    if (result.getString("Data").isEmpty()) {
                        Utils.showAlertToast(context, "No Signature available");
                    } else {
                        openImageDialog(result.getString("Data"));
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }


            @Override
            public void onError(JSONObject result) {
                ((BaseActivity) context).dismissProgressDialog();
            }
        });

    }

    private void getPrescription(OrderHistory item, final int pos) {

        ((BaseActivity) context).showProgressDialog(context);
        NetworkUtility.makeJSONObjectRequest(API.PriscriptionImagePath + "?tran_id=" + item.getTran_id(), new JSONObject(), API.PriscriptionImagePath, new VolleyCallBack() {
            @Override
            public void onSuccess(JSONObject result) {

                try {
                    ((BaseActivity) context).dismissProgressDialog();
                    if (result.getString("Data").isEmpty()) {
                        Utils.showAlertToast(context, "No Priscription Image available");
                    } else {
                        openImageDialog(result.getString("Data"));
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }


            @Override
            public void onError(JSONObject result) {
                ((BaseActivity) context).dismissProgressDialog();
            }
        });

    }


    private String getStatus(String mobileOrderStatus) {
        if (MyApplication.getPrefranceData(mobileOrderStatus).isEmpty()) {
            return mobileOrderStatus;
        } else {
            return MyApplication.getPrefranceData(mobileOrderStatus);
        }
    }

    @Override
    public int getItemCount() {
        return (null != filterList ? filterList.size() : 0);
    }

    public class OrderHistoryViewHolder extends RecyclerView.ViewHolder {
        // init the item view's
        @BindView(R.id.txt_first_name)
        TextView txtFirstName;
        @BindView(R.id.txt_order_status)
        TextView txtOrderStatus;
        @BindView(R.id.txt_drug)
        TextView txtDrug;
        @BindView(R.id.txt_updated_on)
        TextView txtUpdatedOn;
        @BindView(R.id.img_prescription)
        ImageView imgPrescription;
        @BindView(R.id.img_signature)
        ImageView imgSignature;
        @BindView(R.id.img_picture)
        ImageView imgPicture;
        @BindView(R.id.img_delete)
        ImageView imgDelete;
        @BindView(R.id.ll_img_picture)
        LinearLayout ll_img_picture;
        @BindView(R.id.ll_status)
        LinearLayout ll_status;

        @BindView(R.id.tv_qty)
        TextView qty;

        public OrderHistoryViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            // get the reference of item view's
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

                    filterList.addAll(items);

                } else {
                    // Iterate in the original List and add it to filter list...
                    for (OrderHistory item : items) {
                        if (item.getPatientName().toLowerCase().contains(text.toLowerCase())) {
                            // Adding Matched items
                            filterList.add(item);
                        }
                    }
                }

                // Set on UI Thread
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