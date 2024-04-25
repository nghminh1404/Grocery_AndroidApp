package com.example.grocerystore;

import android.widget.Filter;

import com.example.grocerystore.adapters.AdapterOrder;
import com.example.grocerystore.models.ModelOrder;

import java.util.ArrayList;

public class FilterOrder extends Filter {
    private AdapterOrder adapter;
    private ArrayList<ModelOrder> filterList;

    public FilterOrder(AdapterOrder adapter, ArrayList<ModelOrder> filterList) {
        this.adapter = adapter;
        this.filterList = filterList;
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults results = new FilterResults();
        //validate data for search
        if (constraint != null && constraint.length() > 0) {

            constraint = constraint.toString().toUpperCase();
            ArrayList<ModelOrder> filteredModel = new ArrayList<>();
            for (int i = 1; i < filterList.size(); i++) {
                if (filterList.get(i).getOrderStatus().toUpperCase().contains(constraint)) {
                    filteredModel.add(filterList.get(i));
                }
            }
            results.count = filteredModel.size();
            results.values = filteredModel;
        } else {
            results.count = filterList.size();
            results.values = filterList;
        }
        return results;
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {
        adapter.orderShopArrayList = (ArrayList<ModelOrder>) results.values;
        adapter.notifyDataSetChanged();
    }
}

