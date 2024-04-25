package com.example.grocerystore.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.grocerystore.FilterProductUser;
import com.example.grocerystore.R;
import com.example.grocerystore.activities.ShopDetailActivity;
import com.example.grocerystore.models.ModelProduct;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import p32929.androideasysql_library.Column;
import p32929.androideasysql_library.EasyDB;

public class AdapterProductUser extends RecyclerView.Adapter<AdapterProductUser.HolderProductUser> implements Filterable {
    private Context context;
    public ArrayList<ModelProduct> productList, filterList;
    private FilterProductUser filter;
    private ImageView ivProduct;
    private TextView tvTitle, tvpQuantity, tvDescription, tvDiscountedNote, tvDiscountedPrice, tvOriginalPrice, tvFinalPrice, tvQuantity;
    private ImageButton btnDecrement, btnIncrement;
    private Button btnContinue;
    private double cost = 0, finalCost = 0;
    private int quantity = 0;
    private int itemId = 1;

    public AdapterProductUser(Context context, ArrayList<ModelProduct> productList) {
        this.context = context;
        this.productList = productList;
        this.filterList = productList;
    }

    @NonNull
    @Override
    public HolderProductUser onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout
        View view = LayoutInflater.from(context).inflate(R.layout.row_product_user, parent, false);
        return new HolderProductUser(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderProductUser holder, int position) {
        //get data
        ModelProduct modelProduct = productList.get(position);
        String discountNote = modelProduct.getDiscountNote();
        boolean discountAvailable = modelProduct.getDiscountAvailable();
        String discountPrice = modelProduct.getDiscountPrice();
        String productCategory = modelProduct.getProductCategory();
        String originalPrice = modelProduct.getOriginalPrice();
        String productDescription = modelProduct.getProductDescription();
        String productTitle = modelProduct.getProductTitle();
        String productQuantity = modelProduct.getProductQuantity();
        String productId = modelProduct.getProductId();
        String timestamp = modelProduct.getTimestamp();
        String productIcon = modelProduct.getProductIcon();

        //set data
        holder.tvTitle.setText(productTitle);
        holder.tvDiscountedNote.setText(discountNote);
        holder.tvDescription.setText(productDescription);
        holder.tvOriginalPrice.setText("$" + originalPrice);
        holder.tvDiscountedPrice.setText("$" + discountPrice);

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

        holder.tvAddToCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //add product to cart
                showQuantityDialog(modelProduct);
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //show product details
            }
        });
    }

    private void showQuantityDialog(ModelProduct modelProduct) {
        //inflate layout for dialog
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_quantity, null);
        //init layout view
        bindingLayoutView(view);
        bindingLayoutAction(view);
        //get data from model
        String productId = modelProduct.getProductId();
        String title = modelProduct.getProductTitle();
        String productQuantity = modelProduct.getProductQuantity();
        String description = modelProduct.getProductDescription();
        String discountNote = modelProduct.getDiscountNote();
        String image = modelProduct.getProductIcon();

        final String price;
        if (modelProduct.getDiscountAvailable()) {
            //product have discount
            price = modelProduct.getDiscountPrice();
            tvDiscountedNote.setVisibility(View.VISIBLE);
            tvOriginalPrice.setPaintFlags(tvOriginalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG); //add strike through on original price
        } else {
            //product don't have discount
            tvDiscountedNote.setVisibility(View.GONE);
            tvDiscountedPrice.setVisibility(View.GONE);
            price = modelProduct.getOriginalPrice();
        }

        cost = Double.parseDouble(price.replaceAll("$", ""));
        finalCost = Double.parseDouble(price.replaceAll("$", ""));
        quantity = 1;

        //dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(view);

        //set data
        try {
            Picasso.get().load(image).placeholder(R.drawable.ic_cart_grey).into(ivProduct);
        } catch (Exception e) {
            ivProduct.setImageResource(R.drawable.ic_cart_grey);
        }

        tvTitle.setText("" + title);
        tvpQuantity.setText("" + productQuantity);
        tvDescription.setText("" + description);
        tvDiscountedNote.setText("" + discountNote);
        tvQuantity.setText("" + quantity);
        tvOriginalPrice.setText("" + modelProduct.getOriginalPrice());
        tvDiscountedPrice.setText("$" + modelProduct.getDiscountPrice());
        tvFinalPrice.setText("$" + finalCost);

        AlertDialog dialog = builder.create();
        dialog.show();

        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = tvTitle.getText().toString().trim();
                String priceEach = price;
                String totalPrice = tvFinalPrice.getText().toString().trim().replace("$", "");
                String quantity = tvQuantity.getText().toString().trim();

                //add to db(SQLite)
                addToCart(productId, title, priceEach, totalPrice, quantity);
            }
        });
    }


    private void addToCart(String productId, String title, String priceEach, String totalPrice, String quantity) {
        itemId++;

        EasyDB easyDB = EasyDB.init(context, "ITEMS_DB")
                .setTableName("ITEMS_TABLE")
                .addColumn(new Column("Item_Id", new String[]{"text", "unique"}))
                .addColumn(new Column("Item_PID", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Name", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Price_Each", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Price", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Quantity", new String[]{"text", "not null"}))
                .doneTableColumn();

        Boolean b = easyDB.addData("Item_Id", itemId)
                .addData("Item_PID", productId)
                .addData("Item_Name", title)
                .addData("Item_Price_Each", priceEach)
                .addData("Item_Price", totalPrice)
                .addData("Item_Quantity", quantity)
                .doneDataAdding();

        Toast.makeText(context, b ? "Sản phẩm đã được thêm vào giỏ" : "Thêm sản phẩm không thành công", Toast.LENGTH_SHORT).show();
        //update cart count
        ((ShopDetailActivity)context).cartCount();
    }

    private void bindingLayoutAction(View view) {
        //increase quantity of the product
        btnIncrement.setOnClickListener(this::onBtnIncrementClick);
        //decrease quantity of the product
        btnDecrement.setOnClickListener(this::onBtnDecrementClick);
    }

    private void onBtnDecrementClick(View view) {
        if (quantity>1) {
            finalCost = finalCost - cost;
            quantity--;

            tvFinalPrice.setText("$" + finalCost);
            tvQuantity.setText("" + quantity);
        }

    }

    private void onBtnIncrementClick(View view) {
        finalCost = finalCost + cost;
        quantity++;

        tvFinalPrice.setText("$" + finalCost);
        tvQuantity.setText("" + quantity);
    }

    private void bindingLayoutView(View view) {
        ivProduct = view.findViewById(R.id.ivProduct);
        tvTitle = view.findViewById(R.id.tvTitle);
        tvpQuantity = view.findViewById(R.id.tvpQuantity);
        tvDescription = view.findViewById(R.id.tvDescription);
        tvDiscountedNote = view.findViewById(R.id.tvDiscountedNote);
        tvDiscountedPrice = view.findViewById(R.id.tvDiscountedPrice);
        tvOriginalPrice = view.findViewById(R.id.tvOriginalPrice);
        tvFinalPrice = view.findViewById(R.id.tvFinalPrice);
        tvQuantity = view.findViewById(R.id.tvQuantity);
        btnDecrement = view.findViewById(R.id.btnDecrement);
        btnIncrement = view.findViewById(R.id.btnIncrement);
        btnContinue = view.findViewById(R.id.btnContinue);

    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    @Override
    public Filter getFilter() {
        if (filter == null) {
            filter = new FilterProductUser(this, filterList);
        }
        return filter;
    }

    class HolderProductUser extends RecyclerView.ViewHolder {

        //uid views
        private ImageView ivIconProduct, ivNext;
        private TextView tvDiscountedNote, tvTitle, tvDescription, tvAddToCart, tvDiscountedPrice, tvOriginalPrice;

        private void bindingView(View view) {
            ivIconProduct = view.findViewById(R.id.ivIconProduct);
            ivNext = view.findViewById(R.id.ivNext);
            tvDiscountedNote = view.findViewById(R.id.tvDiscountedNote);
            tvTitle = view.findViewById(R.id.tvTitle);
            tvDescription = view.findViewById(R.id.tvDescription);
            tvAddToCart = view.findViewById(R.id.tvAddToCart);
            tvDiscountedPrice = view.findViewById(R.id.tvDiscountedPrice);
            tvOriginalPrice = view.findViewById(R.id.tvOriginalPrice);
        }


        public HolderProductUser(@NonNull View itemView) {
            super(itemView);
            bindingView(itemView);
        }
    }
}
