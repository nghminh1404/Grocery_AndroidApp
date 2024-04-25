package com.example.grocerystore.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.grocerystore.adapters.AdapterOrder;
import com.example.grocerystore.adapters.AdapterProductSeller;
import com.example.grocerystore.Constants;
import com.example.grocerystore.models.ModelOrder;
import com.example.grocerystore.models.ModelProduct;
import com.example.grocerystore.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

public class MainSellerActivity extends AppCompatActivity {


    private TextView tvName, tvEmail, tvShopName, tvTabProducts, tvTabOrders, tvFilteredProduct, filteredOrdersTv;
    private EditText edtSearchProduct;
    private ImageButton btnLogout, btnEditProfile, btnAddProduct, btnFilterProduct,filter0rdersBtn;
    private ImageView ivProfile;
    private RelativeLayout rlToolbar, rlProducts, rlOrders, ordersRl;
    private RecyclerView rvProducts, ordersRv;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;
    private ArrayList<ModelProduct> productList;
    private AdapterProductSeller adapterProductSeller;
    private ArrayList<ModelOrder> orderShopArrayList;
    private AdapterOrder adapterOrder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_seller);
        bindingView();
        bindingAction();
        showProductsUI();
        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Vui lòng đợi");
        progressDialog.setCanceledOnTouchOutside(false);
        checkUser();
        loadAllProduct();
        loadAllOrder();
    }

    private void loadAllOrder() {
        orderShopArrayList = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).child("Orders").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                orderShopArrayList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    ModelOrder modelOrder = ds.getValue(ModelOrder.class);
                    //add to list
                    orderShopArrayList.add(modelOrder);
                }
                adapterOrder = new AdapterOrder(MainSellerActivity.this, orderShopArrayList);
                ordersRv.setAdapter(adapterOrder);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadAllProduct() {
        productList = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");

        ref.child(firebaseAuth.getUid()).child("Product")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //reset list before get
                        productList.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            ModelProduct modelProduct = ds.getValue(ModelProduct.class);
                            productList.add(modelProduct);
                        }
                        adapterProductSeller = new AdapterProductSeller(MainSellerActivity.this, productList);
                        rvProducts.setAdapter(adapterProductSeller);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadProductByCategory(String selectedCategory) {
        productList = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");

        ref.child(firebaseAuth.getUid()).child("Product")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //reset list before get
                        productList.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            String productCategory = "" + ds.child("productCategory").getValue();
                            if (selectedCategory.equals(productCategory)) {
                                ModelProduct modelProduct = ds.getValue(ModelProduct.class);
                                productList.add(modelProduct);
                            }

                        }
                        adapterProductSeller = new AdapterProductSeller(MainSellerActivity.this, productList);
                        rvProducts.setAdapter(adapterProductSeller);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }


    private void bindingView() {
        tvName = findViewById(R.id.tvName);
        tvEmail = findViewById(R.id.tvEmail);
        tvShopName = findViewById(R.id.tvShopName);
        btnAddProduct = findViewById(R.id.btnAddProduct);
        btnLogout = findViewById(R.id.btnLogout);
        btnEditProfile = findViewById(R.id.btnEditProfile);
        ivProfile = findViewById(R.id.ivProfile);
        tvTabProducts = findViewById(R.id.tvTabProducts);
        tvTabOrders = findViewById(R.id.tvTabOrders);
        rlToolbar = findViewById(R.id.rlToolbar);
        rlProducts = findViewById(R.id.rlProducts);
        ordersRl = findViewById(R.id.ordersRl);
        tvFilteredProduct = findViewById(R.id.tvFilteredProduct);
        edtSearchProduct = findViewById(R.id.edtSearchProduct);
        rvProducts = findViewById(R.id.rvProducts);
        btnFilterProduct = findViewById(R.id.btnFilterProduct);
        filteredOrdersTv = findViewById(R.id.filteredOrdersTv);
        filter0rdersBtn = findViewById(R.id.filter0rdersBtn);
        ordersRv = findViewById(R.id.ordersRv);
    }

    private void bindingAction() {

        btnLogout.setOnClickListener(this::onBtnLogoutClick);
        btnEditProfile.setOnClickListener(this::onBtnEditProfileClick);
        btnAddProduct.setOnClickListener(this::onBtnAddProductClick);
        tvTabProducts.setOnClickListener(this::onTvTabProducts);
        tvTabOrders.setOnClickListener(this::onTvTabOrders);
        btnFilterProduct.setOnClickListener(this::onBtnFilterProductClick);
        filteredOrdersTv.setOnClickListener(this::onFilterOrderClick);

        edtSearchProduct.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    adapterProductSeller.getFilter().filter(s);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }



    private void onFilterOrderClick(View view) {
        String[] option = {"Tất cả", "Đang trong quá trình xử lý", "Đã hoàn thành", "Đã hủy"};
        AlertDialog.Builder builder = new AlertDialog.Builder(MainSellerActivity.this);
        builder.setTitle("Phân loại đơn hàng:")
                .setItems(option, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(which == 0){
                            filteredOrdersTv.setText("Tất cả");
                            adapterOrder.getFilter().filter("");
                        }
                        else {
                            String optionClicked = option[which];
                            filteredOrdersTv.setText("Hiện các đơn hàng "+optionClicked);
                            adapterOrder.getFilter().filter(optionClicked);
                        }
                    }
                }).show();
    }

    private void onBtnFilterProductClick(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainSellerActivity.this);
        builder.setTitle("Chọn danh mục sản phẩm: ")
                .setItems(Constants.productCategory1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String selectedCategory = Constants.productCategory1[which];
                        tvFilteredProduct.setText(selectedCategory);
                        if (selectedCategory.equals("All")) {
                            loadAllProduct();
                        } else {
                            loadProductByCategory(selectedCategory);
                        }
                    }
                }).show();
    }

    private void onTvTabOrders(View view) {
        showOrdersUI();
    }

    private void onTvTabProducts(View view) {
        showProductsUI();
    }

    private void showOrdersUI() {
        rlProducts.setVisibility(View.GONE);
        ordersRl.setVisibility(View.VISIBLE);

        tvTabOrders.setTextColor(getResources().getColor(R.color.colorBlack));
        tvTabOrders.setBackgroundResource(R.drawable.shape_rect04);

        tvTabProducts.setTextColor(getResources().getColor(R.color.colorWhite));
        tvTabProducts.setBackgroundColor(getResources().getColor(android.R.color.transparent));
    }

    private void showProductsUI() {
        rlProducts.setVisibility(View.VISIBLE);
        ordersRl.setVisibility(View.GONE);

        tvTabProducts.setTextColor(getResources().getColor(R.color.colorBlack));
        tvTabProducts.setBackgroundResource(R.drawable.shape_rect04);

        tvTabOrders.setTextColor(getResources().getColor(R.color.colorWhite));
        tvTabOrders.setBackgroundColor(getResources().getColor(android.R.color.transparent));
    }

    private void checkUser() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null) {
            startActivity(new Intent(MainSellerActivity.this, LoginActivity.class));
            finish();
        } else {
            loadMyInfo();
        }
    }

    private void loadMyInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("uid").equalTo(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            //get data from db
                            String name = "" + ds.child("name").getValue();
                            String accountType = "" + ds.child("accountType").getValue();
                            String email = "" + ds.child("email").getValue();
                            String shopName = "" + ds.child("shopName").getValue();
                            String profileImage = "" + ds.child("profileImage").getValue();

                            //set data ti ui
                            tvName.setText(name);
                            tvEmail.setText(email);
                            tvShopName.setText(shopName);
                            try {
                                Picasso.get().load(profileImage).placeholder(R.drawable.ic_store_grey).into(ivProfile);
                            } catch (Exception e) {
                                ivProfile.setImageResource(R.drawable.ic_store_grey);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void onBtnAddProductClick(View view) {
        startActivity(new Intent(MainSellerActivity.this, AddProductActivity.class));
    }

    private void onBtnEditProfileClick(View view) {
        //open edit profile activity
        startActivity(new Intent(MainSellerActivity.this, ProfileEditSellerActivity.class));
    }

    private void onBtnLogoutClick(View view) {
        // make offline
        //sign out
        //go to login activity
        makeMeOffline();
    }

    private void makeMeOffline() {
        //after logging in, make user online
        progressDialog.setTitle("Đang đăng xuất...");

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("online", "false");

        //update value to db
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // update successfully
                        firebaseAuth.signOut();
                        checkUser();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // failed updating
                        progressDialog.dismiss();
                        Toast.makeText(MainSellerActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}