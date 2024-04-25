
package com.example.grocerystore.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.grocerystore.FilterProduct;
import com.example.grocerystore.models.ModelProduct;
import com.example.grocerystore.R;
import com.example.grocerystore.activities.EditProductActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class AdapterProductSeller extends RecyclerView.Adapter<AdapterProductSeller.HolderProductSeller> implements Filterable {
    private Context context;
    public ArrayList<ModelProduct> productList, filterList;
    private FilterProduct filter;
    //ModelProduct modelProduct = new ModelProduct();
    ImageButton btnBack, btnDeleteProduct, btnEditProduct;
    TextView tvProductName, tvQuantity, tvDiscountedNote, tvTitle, tvDescription, tvCategory, tvDiscountedPrice, tvOriginalPrice;
    ImageView ivProductIcon;

    public AdapterProductSeller(Context context, ArrayList<ModelProduct> productList) {
        this.context = context;
        this.productList = productList;
        this.filterList = productList;
    }

    @NonNull
    @Override
    public HolderProductSeller onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_product_seller, parent, false);
        return new HolderProductSeller(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderProductSeller holder, int position) {
        ModelProduct modelProduct = productList.get(position);
        String id = modelProduct.getProductId();
        String uid = modelProduct.getUid();
        boolean discountAvailable = modelProduct.getDiscountAvailable();
        String discountNote = modelProduct.getDiscountNote();
        String discountPrice = modelProduct.getDiscountPrice();
        String productCategory = modelProduct.getProductCategory();
        String productDescription = modelProduct.getProductDescription();
        String productIcon = modelProduct.getProductIcon();
        String productQuantity = modelProduct.getProductQuantity();
        String productTitle = modelProduct.getProductTitle();
        String timestamp = modelProduct.getTimestamp();
        String originalPrice = modelProduct.getOriginalPrice();

        holder.tvTitle.setText(productTitle);
        holder.tvQuantity.setText(productQuantity);
        holder.tvDiscountedNote.setText(discountNote);
        holder.tvDiscountedPrice.setText(discountPrice);
        holder.tvOriginalPrice.setText(originalPrice);
        if (discountAvailable) {
            holder.tvDiscountedPrice.setVisibility(View.VISIBLE);
            holder.tvDiscountedNote.setVisibility(View.VISIBLE);
            holder.tvOriginalPrice.setPaintFlags(holder.tvOriginalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG); //add strike though on original price
        } else {
            holder.tvDiscountedPrice.setVisibility(View.GONE);
            holder.tvDiscountedNote.setVisibility(View.GONE);
            holder.tvOriginalPrice.setPaintFlags(0);
        }
        try {
            Picasso.get().load(productIcon).placeholder(R.drawable.ic_add_shopping_primary).into(holder.ivIconProduct);
        } catch (Exception e) {
            holder.ivIconProduct.setImageResource(R.drawable.ic_add_shopping_primary);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                detailsBottom(modelProduct);
            }
        });
    }

    private void detailsBottom(ModelProduct modelProduct) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
        View view = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_product_detail_seller, null);
        bottomSheetDialog.setContentView(view);
        bindingView(view);
        //get data

        String id = modelProduct.getProductId();
        String uid = modelProduct.getUid();
        boolean discountAvailable = modelProduct.getDiscountAvailable();
        String discountNote = modelProduct.getDiscountNote();
        String discountPrice = modelProduct.getDiscountPrice();
        String productCategory = modelProduct.getProductCategory();
        String productDescription = modelProduct.getProductDescription();
        String productIcon = modelProduct.getProductIcon();
        String productQuantity = modelProduct.getProductQuantity();
        String productTitle = modelProduct.getProductTitle();
        String timestamp = modelProduct.getTimestamp();
        String originalPrice = modelProduct.getOriginalPrice();

        //set data
        tvTitle.setText(productTitle);
        tvDescription.setText(productDescription);
        tvCategory.setText(productCategory);
        tvDiscountedNote.setText(discountNote);
        tvDiscountedPrice.setText(discountPrice);
        tvOriginalPrice.setText(originalPrice);
        tvQuantity.setText(productQuantity);
        if (discountAvailable) {
            tvDiscountedPrice.setVisibility(View.VISIBLE);
            tvDiscountedNote.setVisibility(View.VISIBLE);
            tvOriginalPrice.setPaintFlags(tvOriginalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG); //add strike though on original price
        } else {
            tvDiscountedPrice.setVisibility(View.GONE);
            tvDiscountedNote.setVisibility(View.GONE);
        }
        try {
            Picasso.get().load(productIcon).placeholder(R.drawable.ic_add_shopping_primary).into(ivProductIcon);
        } catch (Exception e) {
            ivProductIcon.setImageResource(R.drawable.ic_add_shopping_primary);
        }
        bottomSheetDialog.show();

        btnEditProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetDialog.dismiss();
                Intent intent = new Intent(context, EditProductActivity.class);
                intent.putExtra("productId", id);
                context.startActivity(intent);
            }
        });
        btnDeleteProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetDialog.dismiss();
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Thông báo")
                        .setMessage("Bạn có muốn xóa sản phẩm này?")
                        .setPositiveButton("Xóa", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                deleteProduct(id);
                            }
                        })
                        .setNegativeButton("Không xóa", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).show();
            }
        });
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetDialog.dismiss();
            }
        });
    }
//test
    private void deleteProduct(String id) {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("Product").child(id).removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(context, "Xóa thành công", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void bindingView(View view) {
        btnBack = view.findViewById(R.id.btnBack);
        btnDeleteProduct = view.findViewById(R.id.btnDeleteProduct);
        btnEditProduct = view.findViewById(R.id.btnEditProduct);
        tvProductName = view.findViewById(R.id.tvProductName);
        tvDiscountedNote = view.findViewById(R.id.tvDiscountedNote);
        tvTitle = view.findViewById(R.id.tvTitle);
        tvDescription = view.findViewById(R.id.tvDescription);
        tvCategory = view.findViewById(R.id.tvCategory);
        tvDiscountedPrice = view.findViewById(R.id.tvDiscountedPrice);
        tvOriginalPrice = view.findViewById(R.id.tvOriginalPrice);
        ivProductIcon = view.findViewById(R.id.ivProductIcon);
        tvQuantity = view.findViewById(R.id.tvQuantity);
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    @Override
    public Filter getFilter() {
        if (filter == null) {
            filter = new FilterProduct(this, filterList);
        }
        return null;
    }

    class HolderProductSeller extends RecyclerView.ViewHolder {

        private ImageView ivIconProduct;

        private TextView tvDiscountedNote, tvTitle, tvQuantity, tvDiscountedPrice, tvOriginalPrice;

        private void bindingView() {
            ivIconProduct = itemView.findViewById(R.id.ivIconProduct);
            tvDiscountedNote = itemView.findViewById(R.id.tvDiscountedNote);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvDiscountedPrice = itemView.findViewById(R.id.tvDiscountedPrice);
            tvOriginalPrice = itemView.findViewById(R.id.tvOriginalPrice);
        }

        public HolderProductSeller(@NonNull View itemView) {
            super(itemView);
            bindingView();
        }
    }
}
