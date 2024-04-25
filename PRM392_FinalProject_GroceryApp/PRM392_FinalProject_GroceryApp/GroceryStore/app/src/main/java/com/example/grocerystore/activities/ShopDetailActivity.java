package com.example.grocerystore.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.grocerystore.Constants;
import com.example.grocerystore.R;
import com.example.grocerystore.adapters.AdapterCartItem;
import com.example.grocerystore.adapters.AdapterProductUser;
import com.example.grocerystore.models.ModelCartItem;
import com.example.grocerystore.models.ModelProduct;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import p32929.androideasysql_library.Column;
import p32929.androideasysql_library.EasyDB;

public class ShopDetailActivity extends AppCompatActivity {

    //declare ui views
    private ImageView ivShop;
    public TextView tvsTotal, tvdFee, tvAllTotalPrice; //need to access these views in adapter so making public
    private TextView tvShopName, tvPhone, tvEmail, tvOpenClose, tvDeliveryFee, tvAddress, tvFilteredProduct,
            tvShopName2, tvsTotalLabel, tvdFeeLabel, tvTotalLabel , cartCountTv;
    private ImageButton btnCall, btnMap, btnCart, btnBack, btnFilterProduct;
    private Button btnCheckout;
    private EditText edtSearchProduct;
    private RecyclerView rvProducts, rcvCartItems;
    private String shopUid, myLatitude, myLongitude, shopLatitude, shopLongitude, shopName, shopEmail, shopPhone, shopAddress, myPhone;
    public String deliveryFee;
    private FirebaseAuth firebaseAuth;

    //progress dialog
    private ProgressDialog progressDialog;
    private ArrayList<ModelProduct> productList;
    private AdapterProductUser adapterProductUser;
    //cart
    private ArrayList<ModelCartItem> cartItemList;
    private AdapterCartItem adapterCartItem;
    public double allTotalPrice = 0.0;
    private EasyDB easyDB;

    private void bindingView() {
        ivShop = findViewById(R.id.ivShop);
        tvShopName = findViewById(R.id.tvShopName);
        tvPhone = findViewById(R.id.tvPhone);
        tvEmail = findViewById(R.id.tvEmail);
        tvOpenClose = findViewById(R.id.tvOpenClose);
        tvDeliveryFee = findViewById(R.id.tvDeliveryFee);
        tvAddress = findViewById(R.id.tvAddress);
        tvFilteredProduct = findViewById(R.id.tvFilteredProduct);
        btnCall = findViewById(R.id.btnCall);
        btnMap = findViewById(R.id.btnMap);
        btnCart = findViewById(R.id.btnCart);
        btnBack = findViewById(R.id.btnBack);
        btnFilterProduct = findViewById(R.id.btnFilterProduct);
        edtSearchProduct = findViewById(R.id.edtSearchProduct);
        rvProducts = findViewById(R.id.rvProducts);
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Xin chờ");
        progressDialog.setCanceledOnTouchOutside(false);
        cartCountTv = findViewById(R.id.cartCountTv);
        easyDB = EasyDB.init(this, "ITEMS_DB")
                .setTableName("ITEMS_TABLE")
                .addColumn(new Column("Item_Id", new String[]{"text", "unique"}))
                .addColumn(new Column("Item_PID", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Name", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Price_Each", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Price", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Quantity", new String[]{"text", "not null"}))
                .doneTableColumn();

        //search
        edtSearchProduct.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    adapterProductUser.getFilter().filter(s);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void loadMyInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("uid").equalTo(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            //get user data
                            String name = "" + ds.child("name").getValue();
                            String email = "" + ds.child("email").getValue();
                            myPhone = "" + ds.child("phone").getValue();
                            String profileImage = "" + ds.child("profileImage").getValue();
                            String accountType = "" + ds.child("accountType").getValue();
                            String city = "" + ds.child("city").getValue();
                            myLatitude = "" + ds.child("latitude").getValue();
                            myLongitude = "" + ds.child("longitude").getValue();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void loadShopDetails() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(shopUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //get shop data
                String name = "" + snapshot.child("name").getValue();
                shopName = "" + snapshot.child("shopName").getValue();
                shopEmail = "" + snapshot.child("email").getValue();
                shopPhone = "" + snapshot.child("phone").getValue();
                shopAddress = "" + snapshot.child("address").getValue();
                shopLatitude = "" + snapshot.child("latitude").getValue();
                shopLongitude = "" + snapshot.child("longitude").getValue();
                deliveryFee = "" + snapshot.child("deliveryFee").getValue();
                String profileImage = "" + snapshot.child("profileImage").getValue();
                String shopOpen = "" + snapshot.child("shopOpen").getValue();

                //set data
                tvShopName.setText(shopName);
                tvEmail.setText(shopEmail);
                tvDeliveryFee.setText("Phí vận chuyển: " + deliveryFee + "");
                tvAddress.setText(shopAddress);
                tvPhone.setText(shopPhone);
                if (String.valueOf(shopOpen).equals("true")) {
                    tvOpenClose.setText("Mở cửa");
                } else {
                    tvOpenClose.setText("Đóng cửa");
                }
                try {
                    Picasso.get().load(profileImage).into(ivShop);
                } catch (Exception e) {

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadShopProduct() {
        //init list
        productList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(shopUid).child("Product")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //clear list before adding items
                        productList.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            ModelProduct modelProduct = ds.getValue(ModelProduct.class);
                            productList.add(modelProduct);
                        }

                        //setup adapter
                        adapterProductUser = new AdapterProductUser(ShopDetailActivity.this, productList);

                        //set adapter
                        rvProducts.setAdapter(adapterProductUser);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void bindingAction() {
        btnBack.setOnClickListener(this::onBtnBackClick);
        btnCart.setOnClickListener(this::onBtnCartClick);
        btnCall.setOnClickListener(this::onBtnCallClick);
        btnMap.setOnClickListener(this::onBtnMapClick);
        btnFilterProduct.setOnClickListener(this::onBtnFilterProductClick);
    }

    private void onBtnFilterProductClick(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ShopDetailActivity.this);
        builder.setTitle("Chọn danh mục sản phẩm: ")
                .setItems(Constants.productCategory1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String selectedCategory = Constants.productCategory1[which];
                        tvFilteredProduct.setText(selectedCategory);
                        if (selectedCategory.equals("All")) {
                            loadShopProduct();
                        } else {
                            adapterProductUser.getFilter().filter(selectedCategory);
                        }
                    }
                }).show();
    }

    private void onBtnMapClick(View view) {
        openMap();
    }

    private void openMap() {
        String address = "https://maps.google.com/maps?saddr=" + myLatitude + "," + myLongitude + "&daddr=" + shopLatitude + "," + shopLongitude;
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(address));
        startActivity(intent);
    }

    private void onBtnCallClick(View view) {
        dialPhone();
    }

    private void dialPhone() {
        startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + Uri.encode(shopPhone))));
        Toast.makeText(this, "" + shopPhone, Toast.LENGTH_LONG).show();
    }

    private void onBtnCartClick(View view) {
        //show cart dialog
        showCartDialog();
    }

    private void showCartDialog() {
        //init list
        cartItemList = new ArrayList<>();
        //inflate cart layout
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_cart, null);
        //init views
        bindingView(view);

        //dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //set view to dialog
        builder.setView(view);

        tvShopName2.setText(shopName);

        EasyDB easyDB = EasyDB.init(this, "ITEMS_DB")
                .setTableName("ITEMS_TABLE")
                .addColumn(new Column("Item_Id", new String[]{"text", "unique"}))
                .addColumn(new Column("Item_PID", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Name", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Price_Each", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Price", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Quantity", new String[]{"text", "not null"}))
                .doneTableColumn();

        //get all records from db
        Cursor res = easyDB.getAllData();
        while (res.moveToNext()) {
            String id = res.getString(1);
            String pId = res.getString(2);
            String name = res.getString(3);
            String price = res.getString(4);
            String cost = res.getString(5);
            String quantity = res.getString(6);

            allTotalPrice = allTotalPrice + Double.parseDouble(cost);

            ModelCartItem modelCartItem = new ModelCartItem(id, pId, name, price, cost, quantity);

            cartItemList.add(modelCartItem);
        }

        //setup adapter
        adapterCartItem = new AdapterCartItem(this, cartItemList);
        //set to rcv
        rcvCartItems.setAdapter(adapterCartItem);

        tvdFee.setText(deliveryFee + "");
        tvsTotal.setText(String.format("%.2f", allTotalPrice) + "");
        tvAllTotalPrice.setText((allTotalPrice + Double.parseDouble(deliveryFee.replace("$", ""))) + "");

        //show dialog
        AlertDialog dialog = builder.create();
        dialog.show();

        //reset total price on dialog dismiss
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                allTotalPrice = 0.0;
            }
        });
        //place order
        btnCheckout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //first validate delivery address
                if(myLatitude.equals("") || myLatitude.equals("null") || myLongitude.equals("") || myLongitude.equals("null")){
                    //user did not enter delivery address
                    Toast.makeText(ShopDetailActivity.this,
                            "Xin hãy nhập địa chỉ của bạn vào trong trang thông tin cá nhân trước khi đặt hàng...", Toast.LENGTH_SHORT).show();
                    return;
                }
                //user did not enter phone number
                if(myPhone.equals("") || myPhone.equals("null") ){
                    Toast.makeText(ShopDetailActivity.this,
                            "Xin hãy nhập số đện thoại của bạn vào trong trang thông tin cá nhân trước khi đặt hàng...", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (cartItemList.size()==0){
                    Toast.makeText(ShopDetailActivity.this,"Không có sản phẩm nào trong giỏ hàng", Toast.LENGTH_SHORT).show();
                    return;
                }
                submitOrder();
            }
        });
    }

    private void submitOrder() {
        progressDialog.setMessage("Đặt hàng");
        progressDialog.show();
        String timeStamp = ""+ System.currentTimeMillis();
        String cost = tvAllTotalPrice.getText().toString().trim().replace("$", "");// remove đ ì contains
        //setup order data
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("orderId","" + timeStamp);
        hashMap.put("orderTime","" + timeStamp);
        hashMap.put("orderStatus","" + "Đang trong quá trình xử lý");
        hashMap.put("orderCost","" + cost);
        hashMap.put("orderBy","" + firebaseAuth.getUid());
        hashMap.put("orderTo","" + shopUid);
        hashMap.put("latitude","" + myLatitude);
        hashMap.put("longitude","" + myLongitude);
        //add to db
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Users").child(shopUid).child("Orders");
        ref.child(timeStamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                for (int  i=0; i<cartItemList.size(); i++){
                    String pId= cartItemList.get(i).getpId();
                    String id = cartItemList.get(i).getId();
                    String name = cartItemList.get(i).getName();
                    String cost = cartItemList.get(i).getCost();
                    String price = cartItemList.get(i).getPrice();
                    String quantity = cartItemList.get(i).getQuantity();

                    HashMap<String,String> hashMap1 = new HashMap<>();
                    hashMap1.put("pId",pId);
                    hashMap1.put("name",name);
                    hashMap1.put("cost",cost);
                    hashMap1.put("price",price);
                    hashMap1.put("quantity",quantity);
                    ref.child(timeStamp).child("Items").child(pId).setValue(hashMap1);

                }
                progressDialog.dismiss();
                Toast.makeText(ShopDetailActivity.this,"Đặt hàng thành công", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(ShopDetailActivity.this, OrderDetailsUsersActivity.class);
                intent.putExtra("orderTo", shopUid);
                intent.putExtra("orderId", timeStamp);
                startActivity(intent);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(ShopDetailActivity.this,"Đặt hàng không thành công"+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void bindingView(View view) {
        tvShopName2 = view.findViewById(R.id.tvShopName);
        tvsTotalLabel = view.findViewById(R.id.tvsTotalLabel);
        tvsTotal = view.findViewById(R.id.tvsTotal);
        tvdFeeLabel = view.findViewById(R.id.tvdFeeLabel);
        tvdFee = view.findViewById(R.id.tvdFee);
        tvTotalLabel = view.findViewById(R.id.tvTotalLabel);
        tvAllTotalPrice = view.findViewById(R.id.tvAllTotalPrice);
        btnCheckout = view.findViewById(R.id.btnCheckout);
        rcvCartItems = view.findViewById(R.id.rcvCartItems);
    }


    private void onBtnBackClick(View view) {
        //go previous activity
        onBackPressed();
    }

    private void deletaCartData() {
        //declare it to class level and init in oncreate
        easyDB.deleteAllDataFromTable(); //delete all records from cart
    }
    public void cartCount(){
        //access adapter
        //get cart count
        int count = easyDB.getAllData().getCount();
        if (count <=0){
            //no item , hide cart count
            cartCountTv.setVisibility(View.GONE);
        }else{
            //have item, show cart count and set count
            cartCountTv.setVisibility(View.VISIBLE);
            cartCountTv.setText(""+count);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_detail);

        //init ui views
        bindingView();
        bindingAction();

        //get uid of the shop from intent
        shopUid = getIntent().getStringExtra("shopUid");
        firebaseAuth = FirebaseAuth.getInstance();
        loadMyInfo();
        loadShopDetails();
        loadShopProduct();

        //each shop have its own products and orders so if user add items to cart and go back and open cart in different shop the cart should be different
        //so delete cart data whenever user open this activity
        deletaCartData();
        cartCount();
    }


}