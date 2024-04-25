package com.example.grocerystore.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.grocerystore.R;
import com.example.grocerystore.activities.ShopDetailActivity;
import com.example.grocerystore.models.ModelCartItem;

import java.util.ArrayList;

import p32929.androideasysql_library.Column;
import p32929.androideasysql_library.EasyDB;

public class AdapterCartItem extends RecyclerView.Adapter<AdapterCartItem.HolderCartItem> {

    private Context context;
    private ArrayList<ModelCartItem> cartItems;

    public AdapterCartItem(Context context, ArrayList<ModelCartItem> cartItems) {
        this.context = context;
        this.cartItems = cartItems;
    }

    @NonNull
    @Override
    public HolderCartItem onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout row_cartitem.xml
        View view = LayoutInflater.from(context).inflate(R.layout.row_cartitem, parent, false);
        return new HolderCartItem(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderCartItem holder, int position) {
        //get data
        ModelCartItem modelCartItem = cartItems.get(position);
        String id = modelCartItem.getId();
        String pid = modelCartItem.getpId();
        String title = modelCartItem.getName();
        String price = modelCartItem.getPrice();
        String cost = modelCartItem.getCost();
        String quantity = modelCartItem.getQuantity();

        //set data
        holder.tvItemTitle.setText("" + title);
        holder.tvItemPrice.setText("" + cost);
        holder.tvItemQuantity.setText("[" + quantity + "]");
        holder.tvItemPriceEach.setText("" + price);

        //handle remove click listener, delete item from cart
        holder.tvItemRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //will create table if doesn't exist, but in that case will must exist
                EasyDB easyDB = EasyDB.init(context, "ITEMS_DB")
                        .setTableName("ITEMS_TABLE")
                        .addColumn(new Column("Item_Id", new String[]{"text", "unique"}))
                        .addColumn(new Column("Item_PID", new String[]{"text", "not null"}))
                        .addColumn(new Column("Item_Name", new String[]{"text", "not null"}))
                        .addColumn(new Column("Item_Price_Each", new String[]{"text", "not null"}))
                        .addColumn(new Column("Item_Price", new String[]{"text", "not null"}))
                        .addColumn(new Column("Item_Quantity", new String[]{"text", "not null"}))
                        .doneTableColumn();

                easyDB.deleteRow(1, id);
                Toast.makeText(context, "Removed from cart", Toast.LENGTH_SHORT).show();

                //refresh list
                cartItems.remove(position);
                notifyItemChanged(position);
                notifyDataSetChanged();

                double tx = Double.parseDouble((((ShopDetailActivity)context).tvAllTotalPrice.getText().toString().trim().replace("$", "")));
                double totalPrice = tx - Double.parseDouble(cost.replace("$", ""));
                double deliveryFee = Double.parseDouble((((ShopDetailActivity)context).deliveryFee.replace("$", "")));
                double sTotalPrice = Double.parseDouble(String.format("%.2f", totalPrice)) - Double.parseDouble(String.format("%.2f", deliveryFee));
                ((ShopDetailActivity)context).allTotalPrice = 0.0;
                ((ShopDetailActivity)context).tvsTotal.setText("$" + String.format("%.2f", sTotalPrice));
                ((ShopDetailActivity)context).tvAllTotalPrice.setText("$" + String.format("%.2f", totalPrice));
                //after removing item from cart , update count
                ((ShopDetailActivity)context).cartCount();
            }
        });

    }

    @Override
    public int getItemCount() {
        return cartItems.size(); //return number of records
    }

    //view holder class
    class HolderCartItem extends RecyclerView.ViewHolder {

        //ui views of row_cartitem.xml
        private TextView tvItemTitle, tvItemPrice, tvItemPriceEach, tvItemQuantity, tvItemRemove;

        private void bindingView(View view) {
            tvItemTitle = view.findViewById(R.id.tvItemTitle);
            tvItemPrice = view.findViewById(R.id.tvItemPrice);
            tvItemPriceEach = view.findViewById(R.id.tvItemPriceEach);
            tvItemQuantity = view.findViewById(R.id.tvItemQuantity);
            tvItemRemove = view.findViewById(R.id.tvItemRemove);
        }

        private void bindingAction() {

        }

        public HolderCartItem(@NonNull View itemView) {
            super(itemView);

            bindingView(itemView);
        }
    }
}
