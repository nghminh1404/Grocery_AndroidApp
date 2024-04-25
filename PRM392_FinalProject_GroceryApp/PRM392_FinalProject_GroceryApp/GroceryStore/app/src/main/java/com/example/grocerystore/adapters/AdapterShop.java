package com.example.grocerystore.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.grocerystore.R;
import com.example.grocerystore.activities.ShopDetailActivity;
import com.example.grocerystore.models.ModelShop;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class AdapterShop extends RecyclerView.Adapter<AdapterShop.HolderShop> {

    private Context context;
    public ArrayList<ModelShop> shopsList;

    public AdapterShop(Context context, ArrayList<ModelShop> shopsList) {
        this.context = context;
        this.shopsList = shopsList;
    }

    @NonNull
    @Override
    public HolderShop onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout row_shop.xml
        View view = LayoutInflater.from(context).inflate(R.layout.row_shop, parent, false);
        return new HolderShop(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderShop holder, int position) {
        //get data
        ModelShop modelShop = shopsList.get(position);
        String accountType = modelShop.getAccountType();
        String address = modelShop.getAddress();
        String city = modelShop.getCity();
        String country = modelShop.getCountry();
        String deliveryFee = modelShop.getDeliveryFee();
        String email = modelShop.getEmail();
        double latitude = modelShop.getLatitude();
        double longitude = modelShop.getLongitude();
        String online = modelShop.getOnline();
        String name = modelShop.getName();
        String phone = modelShop.getPhone();
        final String uid = modelShop.getUid();
        String timestamp = modelShop.getTimestamp();
        boolean shopOpen = modelShop.isShopOpen();
        String state = modelShop.getState();
        String profileImage = modelShop.getProfileImage();
        String shopName = modelShop.getShopName();

        //set data
        holder.tvShopName.setText(shopName);
        holder.tvPhone.setText(phone);
        holder.tvAddress.setText(address);

        //check if online
        if (online.equals("true")) {
            //show owner is online
            holder.ivOnline.setVisibility(View.VISIBLE);
        }
        else {
            //show owner is offline
            holder.ivOnline.setVisibility(View.GONE);
        }

        //check if shop open
        if (String.valueOf(shopOpen).equals("true")) {
            //shop open
            holder.tvShopClosed.setVisibility(View.GONE);
        }
        else  {
            //shop closed
            holder.tvShopClosed.setVisibility(View.VISIBLE);
        }

        try {
            Picasso.get().load(profileImage).placeholder(R.drawable.ic_store_grey).into(holder.ivShop);
        }
        catch (Exception e) {
            holder.ivShop.setImageResource(R.drawable.ic_store_grey);
        }

        //handle click listener, show shop details
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onitemViewClick(modelShop);
            }
        });
    }

    private void onitemViewClick(ModelShop modelShop) {
        //get data
        String accountType = modelShop.getAccountType();
        String address = modelShop.getAddress();
        String city = modelShop.getCity();
        String country = modelShop.getCountry();
        String deliveryFee = modelShop.getDeliveryFee();
        String email = modelShop.getEmail();
        double latitude = modelShop.getLatitude();
        double longitude = modelShop.getLongitude();
        String online = modelShop.getOnline();
        String name = modelShop.getName();
        String phone = modelShop.getPhone();
        final String uid = modelShop.getUid();
        String timestamp = modelShop.getTimestamp();
        boolean shopOpen = modelShop.isShopOpen();
        String state = modelShop.getState();
        String profileImage = modelShop.getProfileImage();
        String shopName = modelShop.getShopName();

        Intent i = new Intent(context, ShopDetailActivity.class);
        i.putExtra("shopUid", uid);
        context.startActivity(i);

    }

    @Override
    public int getItemCount() {
        return shopsList.size();
    }

    //view holder
    class HolderShop extends RecyclerView.ViewHolder {

        //ui views of row_shop.xml
        private ImageView ivShop, ivOnline;
        private TextView tvShopClosed, tvShopName, tvPhone, tvAddress;
//        private RatingBar ratingBar;

        private void bindingView(View view) {
            ivShop = view.findViewById(R.id.ivShop);
            ivOnline = view.findViewById(R.id.ivOnline);
            tvShopClosed = view.findViewById(R.id.tvShopClosed);
            tvShopName = view.findViewById(R.id.tvShopName);
            tvPhone = view.findViewById(R.id.tvPhone);
            tvAddress = view.findViewById(R.id.tvAddress);
//            ratingBar = view.findViewById(R.id.ratingBar);
        }
        private void bindingAction() {}

        public HolderShop(@NonNull View itemView) {
            super(itemView);

            bindingView(itemView);
            bindingAction();
        }
    }
}
