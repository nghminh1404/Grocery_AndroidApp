package com.example.grocerystore.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.grocerystore.FilterOrder;
import com.example.grocerystore.R;
import com.example.grocerystore.activities.OrderDetailsActivity;
import com.example.grocerystore.activities.OrderShopViewHolder;
import com.example.grocerystore.models.ModelOrder;

import java.util.ArrayList;

public class AdapterOrder extends RecyclerView.Adapter<OrderShopViewHolder> implements Filterable {

    private Context context;
    public ArrayList<ModelOrder> orderShopArrayList;
    private ArrayList<ModelOrder> filterList;
    private FilterOrder filter;
    private LayoutInflater inflater;

    public AdapterOrder(Context context, ArrayList<ModelOrder> orderShopArrayList) {
        this.context = context;
        this.orderShopArrayList = orderShopArrayList;
        inflater = LayoutInflater.from(context);
        this.filterList = orderShopArrayList;
    }

    @NonNull
    @Override
    public OrderShopViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_order_seller,parent,false);
        return new OrderShopViewHolder(view,context);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderShopViewHolder holder, int position) {
        ModelOrder modelOrder = orderShopArrayList.get(position);
        String orderId = modelOrder.getOrderId();
        String orderBy = modelOrder.getOrderBy();
        holder.setOrderShop(modelOrder);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, OrderDetailsActivity.class );
                intent.putExtra("orderId", orderId);
                intent.putExtra("orderBy", orderBy);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return orderShopArrayList.size();
    }

    @Override
    public Filter getFilter() {
        if(filter == null){
            filter = new FilterOrder(this, filterList);
        }
        return filter;
    }
}
