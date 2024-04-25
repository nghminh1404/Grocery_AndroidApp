package com.example.grocerystore.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

public class AddProductActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private ImageView ivIconProduct;
    private EditText edtTitle, edtDescription, edtQuantity, edtPrice, edtDiscountedPrice, edtDiscountedNote;
    private TextView tvCategory;
    private SwitchCompat swDiscount;
    private Button btnAddProduct;
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
        setContentView(R.layout.activity_add_product);
        bindingView();
        bindingAction();
        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Xin chờ");
        progressDialog.setCanceledOnTouchOutside(false);
        edtDiscountedPrice.setVisibility(View.GONE);
        edtDiscountedNote.setVisibility(View.GONE);
    }

    @SuppressLint("WrongViewCast")
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
        btnAddProduct = findViewById(R.id.btnAddProduct);
        cameraPermissions = new String[]{android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{android.Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    }

    private void bindingAction() {
        ivIconProduct.setOnClickListener(this::onIvIconProductClick);
        tvCategory.setOnClickListener(this::onTvCategoryClick);
        btnAddProduct.setOnClickListener(this::onBtnAddProduct);
        swDiscount.setOnCheckedChangeListener(this::onSwDiscountCheckedChange);
        btnBack.setOnClickListener(this::onBtnBackClick);
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

    private void onBtnAddProduct(View view) {
        inputData();
    }

    private void inputData() {
        productTitle = edtTitle.getText().toString().trim();
        productDescription = edtDescription.getText().toString().trim();
        productCategory = tvCategory.getText().toString().trim();
        productQuantity = edtQuantity.getText().toString().trim();
        originalPrice = edtPrice.getText().toString().trim();
        discountAvailable=swDiscount.isChecked();


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
        addProduct();
    }
//
    private void addProduct() {
        progressDialog.setMessage("Đang thêm sản phẩm");
        progressDialog.show();
        String timestamp = ""+ System.currentTimeMillis();
        if (image_uri == null) {
            addProductInfo(null);
        } else {
            String filePathAndName = "product_images/" + timestamp;
            StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathAndName);
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
                                        addProductInfo(downloadImgUri);
                                    } else {
                                        progressDialog.dismiss();
                                        Toast.makeText(AddProductActivity.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(AddProductActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
private void addProductInfo(Uri imageUri){

    String timestamp = ""+ System.currentTimeMillis();
    HashMap<String, Object> hashMap = new HashMap<>();
    hashMap.put("productId", timestamp);
    hashMap.put("productTitle", productTitle);
    hashMap.put("productDescription", productDescription);
    hashMap.put("productCategory", productCategory);
    hashMap.put("productQuantity", productQuantity);
    hashMap.put("originalPrice", originalPrice);
    hashMap.put("discountPrice", discountPrice);
    hashMap.put("discountNote", discountNote);
    hashMap.put("discountAvailable", discountAvailable);
    hashMap.put("timestamp", timestamp);
    hashMap.put("uid", firebaseAuth.getUid());
    String uid = firebaseAuth.getUid();
    if (imageUri != null) {
        hashMap.put("productIcon", imageUri.toString());
    }
    else {
        hashMap.put("productIcon", "");
    }
    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
    ref.child(firebaseAuth.getUid()).child("Product").child(timestamp).setValue(hashMap)
            .addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    //db updated
                    progressDialog.dismiss();
                    Toast.makeText(AddProductActivity.this, "Sản phẩm đã được thêm", Toast.LENGTH_SHORT).show();
                    clearData();
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    //failed updating db
                    progressDialog.dismiss();
                    Toast.makeText(AddProductActivity.this, e.getMessage().toString(), Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
}
    private void clearData() {
        edtTitle.setText("");
        edtDescription.setText("");
        edtPrice.setText("");
        tvCategory.setText("");
        edtQuantity.setText("");
        edtDiscountedNote.setText("");
        edtDiscountedPrice.setText("");
        ivIconProduct.setImageResource(R.drawable.ic_add_shopping_primary);
        image_uri = null;
    }

    private void onTvCategoryClick(View view) {
        categoryDialog();
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

    private void onIvIconProductClick(View view) {
        showImagePickDialog();
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