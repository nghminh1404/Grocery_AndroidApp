package com.example.grocerystore.activities;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.grocerystore.R;
import com.example.grocerystore.models.ModelOrder;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;

public class OrderShopViewHolder extends RecyclerView.ViewHolder {

    private TextView orderIdTv;
    private TextView orderDateTv;
    private TextView emailTv;
    private TextView amountTv;
    private TextView statusTv;
    private Context context;

    private void bindingView(){
        orderIdTv = itemView.findViewById(R.id.orderIdTv);
        orderDateTv = itemView.findViewById(R.id.orderDateTv);
        emailTv = itemView.findViewById(R.id.emailTv);
        amountTv = itemView.findViewById(R.id.amountTv);
        statusTv = itemView.findViewById(R.id.statusTv);
    }
    private void bindingAction(){
        itemView.setOnClickListener(this::onOrderClicked);
    }

    private void onOrderClicked(View view) {
        ModelOrder mo = new ModelOrder();
        setOrderShop(mo);
        String orderId = mo.getOrderId();
        String orderBy =    mo.getOrderBy();
        Intent intent = new Intent(context, OrderDetailsActivity.class );
        intent.putExtra("orderId", orderId);
        intent.putExtra("orderBy", orderBy);
        context.startActivity(intent);
    }

    public OrderShopViewHolder(@NonNull View itemView, Context context) {
        super(itemView);
        this.context = context;
        bindingView();
       // bindingAction();

    }
    public void setOrderShop(ModelOrder modelOrder){
        loadUserInfo(modelOrder);
        String orderId = modelOrder.getOrderId();
        String orderBy = modelOrder.getOrderBy();
        String orderCost = modelOrder.getOrderCost();
        String orderStatus = modelOrder.getOrderStatus();
        String orderTime = modelOrder.getOrderTime();
        String orderTo = modelOrder.getOrderTo();
        //set data
        amountTv.setText( "Tổng giá trị: $" + orderCost);
        statusTv.setText(orderStatus);
        orderIdTv.setText( "Mã đơn hàng: "+orderId);
        //change order status text color
        if (orderStatus. equals("Đang trong quá trình xử lý")) {
            statusTv.setTextColor(context.getResources().getColor(R.color.colorPrimary));
        }
        else if (orderStatus.equals("Đã hoàn thành")){
                    statusTv. setTextColor(context.getResources().getColor(R.color.colorGreen));
        }
        else if (orderStatus.equals("Đã hủy")){
                   statusTv.setTextColor(context.getResources().getColor(R.color.colorRed));
        }
        //convert time to proper format e.g. dd/mm/yyyy
        Calendar calendar = Calendar.getInstance();
        calendar. setTimeInMillis(Long.parseLong((orderTime)));
        String formatedDate =DateFormat.format( "dd/MM/yyyy", calendar).toString();
        orderDateTv.setText(formatedDate);

   /*     itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });*/
    }
    private void loadUserInfo(ModelOrder modelOrder){

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(modelOrder.getOrderBy())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String email = ""+ snapshot.child("email").getValue();
                        emailTv.setText(email);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
    });
}
}
