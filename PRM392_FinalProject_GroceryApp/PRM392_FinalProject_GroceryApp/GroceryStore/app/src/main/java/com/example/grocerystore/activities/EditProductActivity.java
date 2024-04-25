package com.example.grocerystore.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.grocerystore.Constants;
import com.example.grocerystore.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class EditProductActivity extends AppCompatActivity {
    private String productId;
    private ImageButton btnBack;
    private ImageView ivIconProduct;
    private EditText edtTitle, edtDescription, edtQuantity, edtPrice, edtDiscountedPrice, edtDiscountedNote;
    private TextView tvCategory;
    private SwitchCompat swDiscount;
    private Button btnUpdate;
    private static final int CAMERA_REQUEST_CODE = 200;
    private static final int STORAGE_REQUEST_CODE = 300;
    private static final int IMAGE_PICK_GALLERY_CODE = 400;
    private static final int IMAGE_PICK_CAMERA_CODE = 400;
    private String[] cameraPermissions;
    private String[] storagePermissions;
    private Uri image_uri;
    private String productTitle, productDescription, productCategory, productQuantity, originalPrice, discountPrice, discountNote;
    private Boolean discountAvailable;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_product);
        productId = getIntent().getStringExtra("productId");
        bindingView();
        bindingAction();
        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Xin chờ");
        progressDialog.setCanceledOnTouchOutside(false);
        edtDiscountedPrice.setVisibility(View.GONE);
        edtDiscountedNote.setVisibility(View.GONE);
        loadProductDetail();
    }

    private void loadProductDetail() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("Product").child(productId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String productId = (snapshot.child("productId").getValue()).toString();
                        String productTitle = (snapshot.child("productTitle").getValue()).toString();
                        String productDescription = (snapshot.child("productDescription").getValue()).toString();
                        String productCategory = (snapshot.child("productCategory").getValue()).toString();
                        String productQuantity = (snapshot.child("productQuantity").getValue()).toString();
                        String productIcon = (snapshot.child("productIcon").getValue()).toString();
                        String originalPrice = (snapshot.child("originalPrice").getValue()).toString();
                        String discountPrice = (snapshot.child("discountPrice").getValue()).toString();
                        String discountNote = (snapshot.child("discountNote").getValue()).toString();
                        Boolean discountAvailable = Boolean.parseBoolean((snapshot.child("discountAvailable").getValue()).toString());
                        String timestamp = (snapshot.child("timestamp").getValue()).toString();
                        String uid = (snapshot.child("uid").getValue()).toString();

                        if (discountAvailable.equals("true")) {
                            swDiscount.setChecked(true);
                            edtDiscountedNote.setVisibility(View.VISIBLE);
                            edtDiscountedPrice.setVisibility(View.VISIBLE);
                        } else {
                            swDiscount.setChecked(false);
                            edtDiscountedNote.setVisibility(View.GONE);
                            edtDiscountedPrice.setVisibility(View.GONE);
                        }
                        edtTitle.setText(productTitle);
                        edtDescription.setText(productDescription);
                        tvCategory.setText(productCategory);
                        edtDiscountedNote.setText(discountNote);
                        edtDiscountedPrice.setText(discountPrice);
                        edtQuantity.setText(productQuantity);
                        edtPrice.setText(originalPrice);
                        try {
                            Picasso.get().load(productIcon).placeholder(R.drawable.ic_add_shopping_primary).into(ivIconProduct);
                        } catch (Exception e) {
                            ivIconProduct.setImageResource(R.drawable.ic_add_shopping_white);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void bindingView() {
        btnBack = findViewById(R.id.btnBack);
        ivIconProduct = findViewById(R.id.ivIconProduct);
        edtTitle = findViewById(R.id.edtTitle);
        edtDescription = findViewById(R.id.edtDescription);
        edtQuantity = findViewById(R.id.edtQuantity);
        edtPrice = findViewById(R.id.edtPrice);
        edtDiscountedPrice = findViewById(R.id.edtDiscountedPrice);
        edtDiscountedNote = findViewById(R.id.edtDiscountedNote);
        tvCategory = findViewById(R.id.tvCategory);
        swDiscount = findViewById(R.id.swDiscount);
        btnUpdate = findViewById(R.id.btnUpdate);
        cameraPermissions = new String[]{android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{android.Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    }

    private void bindingAction() {
        ivIconProduct.setOnClickListener(this::onIvIconProductClick);
        tvCategory.setOnClickListener(this::onTvCategoryClick);
        btnUpdate.setOnClickListener(this::onBtnUpdate);
        swDiscount.setOnCheckedChangeListener(this::onSwDiscountCheckedChange);
        btnBack.setOnClickListener(this::onBtnBackClick);
    }

    private void onBtnUpdate(View view) {
        inputData();
    }

    private void inputData() {
        productTitle = edtTitle.getText().toString().trim();
        productDescription = edtDescription.getText().toString().trim();
        productCategory = tvCategory.getText().toString().trim();
        productQuantity = edtQuantity.getText().toString().trim();
        originalPrice = edtPrice.getText().toString().trim();
        discountAvailable = swDiscount.isChecked();

        if (TextUtils.isEmpty(productTitle)) {
            Toast.makeText(this, "Thiếu tên sản phẩm", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(productCategory)) {
            Toast.makeText(this, "Thiếu danh mục sản phẩm", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(originalPrice)) {
            Toast.makeText(this, "Thiếu giá sản phẩm", Toast.LENGTH_SHORT).show();
            return;
        }
        if (discountAvailable) {
            discountPrice = edtDiscountedPrice.getText().toString().trim();
            discountNote = edtDiscountedNote.getText().toString().trim();
            if (TextUtils.isEmpty(discountPrice)) {
                Toast.makeText(this, "Thiếu thông tin giảm giá", Toast.LENGTH_SHORT).show();
                return;
            }
        } else {
            discountPrice = "0";
            discountNote = "";
        }
        updateProduct();
    }

    private void updateProduct() {
        progressDialog.setMessage("Đang cập nhật thông tin...");
        progressDialog.show();
        if (image_uri == null) {
            updateProductInfo(null);
        } else {
            // New image selected, upload the image and update product information
            String filePath = "product_images/" + productId;
            StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePath);

            storageReference.putFile(image_uri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                            uriTask.addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    if (task.isSuccessful()) {
                                        Uri downloadImgUri = task.getResult();
                                        updateProductInfo(downloadImgUri);
                                    } else {
                                        progressDialog.dismiss();
                                        Toast.makeText(EditProductActivity.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(EditProductActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void updateProductInfo(Uri imageUri) {
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("productTitle", productTitle);
        hashMap.put("productDescription", productDescription);
        hashMap.put("productCategory", productCategory);
        hashMap.put("productQuantity", productQuantity);
        hashMap.put("originalPrice", originalPrice);
        hashMap.put("discountPrice", discountPrice);
        hashMap.put("discountNote", discountNote);
        hashMap.put("discountAvailable", discountAvailable);

        // Update the image URI if a new image was selected
        if (imageUri != null) {
            hashMap.put("productIcon", imageUri.toString());
        }

        // Add other product information to the hashMap

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("Product").child(productId).updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        progressDialog.dismiss();
                        Toast.makeText(EditProductActivity.this, "Đã cập nhật", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(EditProductActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void onIvIconProductClick(View view) {
        showImagePickDialog();
    }

    private void onTvCategoryClick(View view) {
        categoryDialog();
    }

    private void onBtnBackClick(View view) {
        onBackPressed();
    }

    private void onSwDiscountCheckedChange(CompoundButton compoundButton, boolean isChecked) {
        if (isChecked) {
            edtDiscountedPrice.setVisibility(View.VISIBLE);
            edtDiscountedNote.setVisibility(View.VISIBLE);
        } else {
            edtDiscountedPrice.setVisibility(View.GONE);
            edtDiscountedNote.setVisibility(View.GONE);
        }
    }

    private void categoryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Product Category").setItems(Constants.productCategory, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String category = Constants.productCategory[which];
                tvCategory.setText(category);
            }
        }).show();
    }

    private void showImagePickDialog() {
        //options to display in dialog
        String[] options = {"Camera", "Gallery"};
        //dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick Image")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //handle clicks
                        if (which == 0) {
                            //camera clicked
                            if (checkCameraPermission()) {
                                //camera permissions allowed
                                pickFromCamera();
                            } else {
                                //not allowed, request
                                requestCameraPermission();
                            }
                        } else {
                            //gallery clicked
                            if (checkStoragePermission()) {
                                //storage permissions allowed
                                pickFromGallery();
                            } else {
                                //not allowed,request
                                requestStoragePermission();
                            }
                        }
                    }
                })
                .show();
    }

    private boolean checkStoragePermission() {
        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this, storagePermissions, STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermission() {
        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) ==
                (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                (PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, cameraPermissions, CAMERA_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
//            case LOCATION_REQUEST_CODE:{
//                if (grantResults.length >0){
//                    boolean locationAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
//                    if (locationAccepted){
//                        //permission allowed
//                        detectLocation();
//                    }
//                    else {
//                        //permission denied
//                        Toast.makeText(this, "Yêu cầu quyền truy cập vị trí...", Toast.LENGTH_SHORT).show();
//                    }
//                }
//            }
//            break;
            case CAMERA_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && storageAccepted) {
                        //permission allowed
                        pickFromCamera();
                    } else {
                        //permission denied
                        Toast.makeText(this, "Yêu cầu quyền truy cập camera...", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
            case STORAGE_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (storageAccepted) {
                        //permission allowed
                        pickFromGallery();
                    } else {
                        //permission denied
                        Toast.makeText(this, "Yêu cầu quyền truy cập bộ nhớ...", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void pickFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_GALLERY_CODE);
    }

    private void pickFromCamera() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE, "Image Title");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "Image Description");

        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(intent, IMAGE_PICK_CAMERA_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        //handle image pick result
        if (resultCode == RESULT_OK) {
            if (requestCode == IMAGE_PICK_GALLERY_CODE) {
                //get picked image from gallery
                image_uri = data.getData();
                //set to imageview
                ivIconProduct.setImageURI(image_uri);
            } else if (requestCode == IMAGE_PICK_CAMERA_CODE) {
                ivIconProduct.setImageURI(image_uri);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}