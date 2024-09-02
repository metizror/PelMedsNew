package com.metiz.pelconnect.Adapter;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.metiz.pelconnect.BaseActivity;
import com.metiz.pelconnect.R;
import com.metiz.pelconnect.UserListActivity;
import com.metiz.pelconnect.model.User;
import com.metiz.pelconnect.network.API;
import com.metiz.pelconnect.network.NetworkUtility;
import com.metiz.pelconnect.network.VolleyCallBack;
import com.metiz.pelconnect.util.Utils;

import org.json.JSONObject;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class UserListAdapter extends
        RecyclerView.Adapter<UserListAdapter.ViewHolder> {

    private static final String TAG = UserListAdapter.class.getSimpleName();


    private Context context;
    private List<User> list;

    public UserListAdapter(Context context, List<User> list) {
        this.context = context;
        this.list = list;
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        // Todo Butterknife bindings

        @BindView(R.id.txt_name)
        TextView txtName;
        @BindView(R.id.txt_user_name)
        TextView txtUserName;
        @BindView(R.id.ll_main)
        LinearLayout llMain;
        @BindView(R.id.switch_active_deactive)
        SwitchCompat switchActiveDeactive;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View view = inflater.inflate(R.layout.row_user_list_item, parent, false);
        ButterKnife.bind(this, view);

        ViewHolder viewHolder = new ViewHolder(view);

        return viewHolder;
    }

    public void updateList(List<User> list) {
        this.list = list;
        notifyDataSetChanged();
    }


    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final User item = list.get(position);

        holder.txtName.setText(item.getLastName() + ", " + item.getFirstName());
        holder.txtUserName.setText(item.getUserName());

        holder.switchActiveDeactive.setOnCheckedChangeListener(null);
        holder.switchActiveDeactive.setChecked(item.isActive());
        holder.switchActiveDeactive.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                ActiveDeactiveAPICall(item, b);
            }
        });

        holder.llMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((UserListActivity)context).openUpdateUserDialog(item);
            }
        });
        //Todo: Setup viewholder for item
    }

    private void ActiveDeactiveAPICall(User item, boolean b) {

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("UserId", item.getUserID());
            jsonObject.put("flage", b);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        ((BaseActivity) context).showProgressDialog(context);

        NetworkUtility.makeJSONObjectRequest(API.ActiveDeactiveUser, jsonObject, API.ActiveDeactiveUser, new VolleyCallBack() {
            @Override
            public void onSuccess(JSONObject result) {
                try {
                    ((BaseActivity) context).dismissProgressDialog();

                    if (result != null) {

                        Utils.showAlertToast(context, result.getString("Message"));

                    }
                } catch (Exception ex) {
                    ex.printStackTrace();

                }
            }

            @Override
            public void onError(JSONObject result) {
                ((BaseActivity) context).dismissProgressDialog();


            }
        });
    }


    @Override
    public int getItemCount() {
        return list.size();
    }


}