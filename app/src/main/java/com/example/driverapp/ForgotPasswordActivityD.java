package com.example.driverapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivityD extends AppCompatActivity {

    private EditText forgotEmail;
    private Button submit;
    private ImageView backArrow;

    private FirebaseAuth mAuth;

    ProgressDialog loadingBar;

    AlertDialog.Builder alertDialoge;
    boolean doubleTap = false;
    Animation shakeAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_passwordd);

        shakeAnimation = AnimationUtils.loadAnimation(this,R.anim.shake);

        alertDialoge = new AlertDialog.Builder(this);
        alertDialoge.setTitle("Reset Password");
        alertDialoge.setMessage("Please check your email, we just send a link to reset your password");
        alertDialoge.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(ForgotPasswordActivityD.this, "Email Send", Toast.LENGTH_SHORT).show();
            }
        });

        alertDialoge.create();

        backArrow = findViewById(R.id.imageViewBack);
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ForgotPasswordActivityD.this, LogInActivityD.class);
                startActivity(intent);
            }
        });

        mAuth = FirebaseAuth.getInstance();

        forgotEmail = findViewById(R.id.forgot_email_id);
        submit = findViewById(R.id.submit_btn_id);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ResetFunction();
            }
        });
    }

    //back press function start here
//    @Override
//    public void onBackPressed() {
//        //super.onBackPressed();
//        if (doubleTap)
//        {
//            finishAffinity();
//        }
//        Toast.makeText(this, "Press again to exit", Toast.LENGTH_SHORT).show();
//        doubleTap = true;
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                doubleTap = true;
//            }
//        },2500);
//    }
    //back press ends.....

    private void ResetFunction() {
        loadingBar = new ProgressDialog(this);
        loadingBar.setTitle("Reset Password");
        loadingBar.setMessage("Please wait!");
        loadingBar.show();
        loadingBar.setCanceledOnTouchOutside(false);
        submit.setEnabled(false);

        String Email = forgotEmail.getText().toString().trim();
        if (Email.isEmpty())
        {
            Toast.makeText(this, "Please enter your Email Id", Toast.LENGTH_SHORT).show();
            loadingBar.dismiss();
            forgotEmail.startAnimation(shakeAnimation);
            submit.setEnabled(true);
        }else {


            mAuth.sendPasswordResetEmail(Email)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(ForgotPasswordActivityD.this, "Email send", Toast.LENGTH_LONG).show();
                                loadingBar.dismiss();
                                alertDialoge.show();
                            } else {
                                Toast.makeText(ForgotPasswordActivityD.this, "Email not Send", Toast.LENGTH_LONG).show();
                                loadingBar.dismiss();
                                submit.setEnabled(true);
                            }
                        }
                    });
        }
    }
}
