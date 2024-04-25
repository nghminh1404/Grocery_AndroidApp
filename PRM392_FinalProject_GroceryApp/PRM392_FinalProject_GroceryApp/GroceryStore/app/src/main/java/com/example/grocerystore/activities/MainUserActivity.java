package com.example.grocerystore.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.grocerystore.R;
import com.example.grocerystore.adapters.AdapterOrderUser;
import com.example.grocerystore.adapters.AdapterShop;
import com.example.grocerystore.models.ModelOrderUser;
import com.example.grocerystore.models.ModelShop;
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

public class MainUserActivity extends AppCompatActivity {

    private TextView tvName, tvEmail, tvPhone, tvTabShops, tvTabOrders;
    private ImageButton btnLogout, btnEditProfile;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;
    private ImageView ivProfile;
    private RelativeLayout rlShops, rlOrders;
    private RecyclerView rvShops, ordersRv;
    private ArrayList<ModelShop> shopsList;
    private ArrayList<ModelOrderUser> orderList;
    private AdapterShop adapterShop;
    private AdapterOrderUser adapterOrderUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_user);

        bindingView();
        bindingAction();

        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Vui lòng đợi");
        progressDialog.setCanceledOnTouchOutside(false);
        checkUser();

        // at start show shops ui
        showShopUI();
    }

    @SuppressLint("ResourceType")
    private void showShopUI() {
        //show shops ui, hide orders ui
        rlShops.setVisibility(View.VISIBLE);
        rlOrders.setVisibility(View.GONE);

        tvTabShops.setTextColor(getResources().getColor(R.color.colorBlack));
        tvTabShops.setBackgroundResource(R.drawable.shape_rect04);

        tvTabOrders.setTextColor(getResources().getColor(R.color.colorWhite));
        tvTabOrders.setBackgroundResource(getResources().getColor(android.R.color.transparent));
    }

    @SuppressLint("ResourceType")
    private void showOrderUI() {
        //show orders ui, hide shop ui
        rlShops.setVisibility(View.GONE);
        rlOrders.setVisibility(View.VISIBLE);

        tvTabOrders.setTextColor(getResources().getColor(R.color.colorBlack));
        tvTabOrders.setBackgroundResource(R.drawable.shape_rect04);

        tvTabShops.setTextColor(getResources().getColor(R.color.colorWhite));
        tvTabShops.setBackgroundResource(getResources().getColor(android.R.color.transparent));
    }

    private void checkUser() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null){
            startActivity(new Intent(MainUserActivity.this, LoginActivity.class));
            finish();
        }
        else {
            loadMyInfo();
        }
    }
    private void loadMyInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("uid").equalTo(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds : dataSnapshot.getChildren())
                        {
                            //get user data
                            String name = ""+ds.child("name").getValue();
                            String email = ""+ds.child("email").getValue();
                            String phone = ""+ds.child("phone").getValue();
                            String profileImage = ""+ds.child("profileImage").getValue();
                            String accountType = ""+ds.child("accountType").getValue();
                            String city = ""+ds.child("city").getValue();

                            //set user data
                            tvName.setText(name);
                            tvEmail.setText(email);
                            tvPhone.setText(phone);
                            try {
                                Picasso.get().load(profileImage).placeholder(R.drawable.ic_person_grey).into(ivProfile);
                            }
                            catch (Exception e) {
                                ivProfile.setImageResource(R.drawable.ic_person_grey);
                            }

                            //load only those shops that are in the city of user
                            loadShops(city);
                            loadOrders();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }
////
    private void loadOrders() {
        //init order list
        orderList = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                orderList.clear();
                for (DataSnapshot ds : snapshot.getChildren()){
                    String uid = ""+ds.getRef()
                            .getKey();
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users").child(uid).child("Orders");
                    ref.orderByChild("orderBy").equalTo(firebaseAuth.getUid()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()){
                                for (DataSnapshot ds : snapshot.getChildren()){
                                    ModelOrderUser modelOrderUser = ds.getValue(ModelOrderUser.class);

                                    orderList.add(modelOrderUser);
                                }
                                adapterOrderUser = new AdapterOrderUser(MainUserActivity.this, orderList);
                                ordersRv.setAdapter(adapterOrderUser);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadShops(String city) {
        //init list
        shopsList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("accountType").equalTo("Seller").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //clear list before adding
                shopsList.clear();
                for (DataSnapshot ds: snapshot.getChildren()) {
                    ModelShop modelShop = ds.getValue(ModelShop.class);

                    String shopCity = ds.child("city").getKey();

                    //show only user city shops
//                    if (shopCity.equals(city)) {
//                        shopsList.add(modelShop);
//                    }

                    //if you want to display all shops, skip the if statement and add this
                    shopsList.add(modelShop);
                }
                //setup adapter
                adapterShop = new AdapterShop(MainUserActivity.this, shopsList);

                //set adapter to rv
                rvShops.setAdapter(adapterShop);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void bindingView() {
        tvName = findViewById(R.id.tvName);
        tvEmail = findViewById(R.id.tvEmail);
        tvPhone = findViewById(R.id.tvPhone);
        tvTabShops = findViewById(R.id.tvTabShops);
        tvTabOrders = findViewById(R.id.tvTabOrders);
        btnLogout = findViewById(R.id.btnLogout);
        btnEditProfile = findViewById(R.id.btnEditProfile);
        ivProfile = findViewById(R.id.ivProfile);
        rlShops = findViewById(R.id.rlShops);
        rlOrders = findViewById(R.id.rlOrders);
        rvShops = findViewById(R.id.rvShops);
        ordersRv = findViewById(R.id.ordersRv);
    }

    private void bindingAction() {
        btnLogout.setOnClickListener(this:: onbtnLogoutClick);
        btnEditProfile.setOnClickListener(this:: onbtnEditProfileClick);
        tvTabShops.setOnClickListener(this:: ontvTabShopsClick);
        tvTabOrders.setOnClickListener(this:: ontvTabOrdersClick);

    }


    private void ontvTabOrdersClick(View view) {
        //show orders
        showOrderUI();
    }

    private void ontvTabShopsClick(View view) {
        //show shops
        showShopUI();
    }

    private void onbtnEditProfileClick(View view) {
        //open edit profile activity
        startActivity(new Intent(MainUserActivity.this, ProfileEditUserActivity.class));
    }
    private void onbtnLogoutClick(View view) {
        // make offline
        //sign out
        //go to login activity
        makeMeOffline();
    }
    private void makeMeOffline() {
        //after logging in, make user online
        progressDialog.setTitle("Đang đăng xuất...");

        HashMap<String, Object> hashMap  = new HashMap<>();
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
                        Toast.makeText(MainUserActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}