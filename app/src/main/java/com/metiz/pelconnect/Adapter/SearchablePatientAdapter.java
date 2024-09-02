package com.metiz.pelconnect.Adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import com.metiz.pelconnect.R;
import com.metiz.pelconnect.model.PatientPOJO;

import java.util.ArrayList;
import java.util.List;


public class SearchablePatientAdapter extends ArrayAdapter<PatientPOJO> {
    private final Context mContext;
    private final List<PatientPOJO> mDepartments;
    private final List<PatientPOJO> mDepartments_All;
    private final List<PatientPOJO> mDepartments_Suggestion;
    private final int mLayoutResourceId;

    public SearchablePatientAdapter(Context context, int resource, List<PatientPOJO> departments) {
        super(context, resource, departments);
        this.mContext = context;
        this.mLayoutResourceId = resource;
        this.mDepartments = new ArrayList<>(departments);
        this.mDepartments_All = new ArrayList<>(departments);
        this.mDepartments_Suggestion = new ArrayList<>();
    }

    public int getCount() {
        return mDepartments.size();
    }

    public PatientPOJO getItem(int position) {
        return mDepartments.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        try {
            if (convertView == null) {
                LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
                convertView = inflater.inflate(mLayoutResourceId, parent, false);
            }
            PatientPOJO department = getItem(position);
            TextView name = (TextView) convertView.findViewById(R.id.text1);
            name.setText(department.getLastname() + "," + department.getFirstname());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return convertView;
    }


    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            public String convertResultToString(Object resultValue) {
                return ((PatientPOJO) resultValue).getLastname() + "," + ((PatientPOJO) resultValue).getFirstname();
            }

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                if (constraint != null) {
                    mDepartments_Suggestion.clear();
                    for (PatientPOJO department : mDepartments_All) {
                        if (department.getLastname().toLowerCase().contains(constraint.toString().toLowerCase()) || department.getFirstname().toLowerCase().contains(constraint.toString().toLowerCase())) {
                            mDepartments_Suggestion.add(department);
                        }
                    }
                    FilterResults filterResults = new FilterResults();
                    filterResults.values = mDepartments_Suggestion;
                    filterResults.count = mDepartments_Suggestion.size();
                    return filterResults;
                } else {
                    return new FilterResults();
                }
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                mDepartments.clear();
                if (results != null && results.count > 0) {
                    // avoids unchecked cast warning when using mDepartments.addAll((ArrayList<PatientPOJO>) results.values);
                    List<?> result = (List<?>) results.values;
                    for (Object object : result) {
                        if (object instanceof PatientPOJO) {
                            mDepartments.add((PatientPOJO) object);
                        }
                    }
                } else if (constraint == null) {
                    // no filter, add entire original list back in
                    mDepartments.addAll(mDepartments_All);
                }
                notifyDataSetChanged();
            }
        };
    }
}