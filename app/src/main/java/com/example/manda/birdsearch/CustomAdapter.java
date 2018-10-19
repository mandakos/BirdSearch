package com.example.manda.birdsearch;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class CustomAdapter extends BaseAdapter implements Filterable {

    private ArrayList<Bird> mOriginalValues; // Original Values
    private ArrayList<Bird> mDisplayedValues;    // Values to be displayed
    LayoutInflater inflater;
    Context context;

    public CustomAdapter(Context context, ArrayList<Bird> mProductArrayList) {
        this.mOriginalValues = mProductArrayList;
        this.mDisplayedValues = mProductArrayList;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return mDisplayedValues.size();
    }

    @Override
    public Bird getItem(int position) {
        Bird bird = mDisplayedValues.get(position);
        return bird;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private class ViewHolder {
        LinearLayout llContainer;
        TextView tvName,tvPrice;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        ViewHolder holder = null;

        if (convertView == null) {

            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.list_item, null);
            holder.llContainer = (LinearLayout)convertView.findViewById(R.id.llContainer);
            holder.tvName = (TextView) convertView.findViewById(R.id.tvName);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.tvName.setText(mDisplayedValues.get(position).name_finnish);

        /*holder.llContainer.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {

                Bird thisBird = (Bird) (mDisplayedValues.get(position));
                // Tässä avataan uusi DescriptionActivity jossa näytetään lajikuvaus
                //Intent intent = new Intent(this, DescriptionActivity.class);
                //intent.putExtra("BIRD", message);
                //startActivity(intent);
                Toast.makeText(context, thisBird.getName_latin(), Toast.LENGTH_SHORT).show();
            }
        });*/

        return convertView;
    }

    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint,FilterResults results) {

                mDisplayedValues = (ArrayList<Bird>) results.values; // has the filtered values
                notifyDataSetChanged();  // notifies the data with new filtered values
            }

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();        // Holds the results of a filtering operation in values
                ArrayList<Bird> FilteredArrList = new ArrayList<Bird>();

                if (mOriginalValues == null) {
                    mOriginalValues = new ArrayList<Bird>(mDisplayedValues); // saves the original data in mOriginalValues
                }

                /********
                 *
                 *  If constraint(CharSequence that is received) is null returns the mOriginalValues(Original) values
                 *  else does the Filtering and returns FilteredArrList(Filtered)
                 *
                 ********/
                if (constraint == null || constraint.length() == 0) {

                    // set the Original result to return
                    results.count = mOriginalValues.size();
                    results.values = mOriginalValues;
                } else {
                    constraint = constraint.toString().toLowerCase();
                    for (int i = 0; i < mOriginalValues.size(); i++) {
                        String data = mOriginalValues.get(i).name_finnish;
                        if (data.toLowerCase().startsWith(constraint.toString())) {
                            FilteredArrList.add(new Bird(mOriginalValues.get(i).name_finnish));
                        }
                    }
                    // set the Filtered result to return
                    results.count = FilteredArrList.size();
                    results.values = FilteredArrList;
                }
                return results;
            }
        };
        return filter;
    }
}