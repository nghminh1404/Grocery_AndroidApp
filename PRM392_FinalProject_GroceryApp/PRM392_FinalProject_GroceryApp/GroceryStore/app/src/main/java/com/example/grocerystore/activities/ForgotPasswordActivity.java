package com.example.grocerystore.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.grocerystore.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {

        private ImageButton btnBack;
        private EditText edtEmail;
        private Button recoverBtn;
        private TextView noAccountTv;

    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        bindingView();
        bindingAction();

        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Vui lòng đợi");
        progressDialog.setCanceledOnTouchOutside(false);
    }

    private void bindingView() {
        btnBack =(ImageButton) findViewById(R.id.btnBack);
        edtEmail =(EditText) findViewById(R.id.edtEmail);
        recoverBtn =(Button) findViewById(R.id.recoverBtn);
        noAccountTv = (TextView) findViewById(R.id.noAccountTv);
    }
    private void bindingAction() {
        btnBack.setOnClickListener(this:: onbtnBackClick);
        noAccountTv.setOnClickListener(this:: onNoAccountTvClick);
        recoverBtn.setOnClickListener(this:: onRecoverBtnClick);
    }

    private void onRecoverBtnClick(View view) {
        recoverPassword();
        email = edtEmail.getText().toString().trim();
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            Toast.makeText(this, "Email không hợp lệ.", Toast.LENGTH_SHORT).show();
            return;
        }
        progressDialog.setMessage("Đang gửi thông tin để đổi mật khẩu...");
        progressDialog.show();
        firebaseAuth.sendPasswordResetEmail(email)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // instructions sent
                        progressDialog.dismiss();
                        Toast.makeText(ForgotPasswordActivity.this, "Thông tin reset mật khẩu đã được gửi đến email...", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed sending instructions
                        progressDialog.dismiss();
                        Toast.makeText(ForgotPasswordActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private String email;
    private void recoverPassword() {
    }

    private void onbtnBackClick(View view) {
        onBackPressed();
    }
    private void onNoAccountTvClick(View view) {
        startActivity(new Intent(ForgotPasswordActivity.this, RegisterUserActivity.class));

    }
}