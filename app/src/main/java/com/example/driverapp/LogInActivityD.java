package com.example.driverapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class LogInActivityD extends AppCompatActivity {

    private EditText email;
    private EditText password;
    private CheckBox showHidePassword;
    private TextView forgotPassword;
    private Button loginBtn;

    private ProgressDialog loadingBar;

    private FirebaseAuth mAuth;

    DriverData driverData;
    FirebaseDatabase firebaseDatabase;
    String driverStatus = "Driver";

    DatabaseReference databaseReference;


    private Animation mShakeAnimation;
    private LinearLayout linearLayout;
    private int LOCATION_PERMISSION_CODE = 1;

    //***************ENABLE AND DISABLE GPS************************
    protected static final String TAG = "LocationOnOff";
    private GoogleApiClient googleApiClient;
    final static int REQUEST_LOCATION = 199;
    //**************END GPS PORTION HERE**************************

    @Override
    protected void onStart() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(LogInActivityD.this,
                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            } else {
                requestLocationPermission();
            }
        }
        super.onStart();
    }

    private void requestLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            new AlertDialog.Builder(this)
                    .setTitle("Permission needed")
                    .setMessage("This Permission needed otherwise you will be not able to use this app...")
                    .setIcon(R.drawable.warning)
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ActivityCompat.requestPermissions(LogInActivityD.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_CODE);
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    })
                    .create().show();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_CODE);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permission not Granted", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_ind);



        linearLayout = findViewById(R.id.linear_layout_idd);
        mShakeAnimation = AnimationUtils.loadAnimation(this,R.anim.shake);

        loadingBar = new ProgressDialog(this);

        email = findViewById(R.id.email_login_id);
        password = findViewById(R.id.password_login_id);
        showHidePassword = findViewById(R.id.login_show_hide_pass_id);
        showHidePassword.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b)
                {
                    showHidePassword.setText("Hide Password");
                    password.setInputType(InputType.TYPE_CLASS_TEXT);
                    showHidePassword.setInputType(InputType.TYPE_CLASS_TEXT);
                    password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                }
                else
                {
                    showHidePassword.setText("Show Password");
                    password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    password.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
            }
        });
        forgotPassword = findViewById(R.id.forgot_password);
        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LogInActivityD.this, ForgotPasswordActivityD.class);
                startActivity(intent);
            }
        });
        loginBtn = findViewById(R.id.login_btn_id);
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LoginFunction();
            }
        });


        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        driverData = new DriverData();
        firebaseDatabase = FirebaseDatabase.getInstance();



    }
    private boolean gpsEnabled(){
        //***********************GPS start*************
        this.setFinishOnTouchOutside(true);
        // Todo Location Already on  ... start
        final LocationManager manager = (LocationManager) LogInActivityD.this.getSystemService(Context.LOCATION_SERVICE);
        if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER) && hasGPSDevice(LogInActivityD.this)) {
            //Toast.makeText(LogInActivityD.this,"Gps already enabled",Toast.LENGTH_SHORT).show();
            return true;
        }
        // Todo Location Already on  ... end

        if(!hasGPSDevice(LogInActivityD.this)){
            Toast.makeText(LogInActivityD.this,"Gps not Supported",Toast.LENGTH_SHORT).show();
        }

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER) && hasGPSDevice(LogInActivityD.this)) {
            Toast.makeText(LogInActivityD.this,"Gps not enabled",Toast.LENGTH_LONG).show();
            return false;
        }else{
           // Toast.makeText(LogInActivityD.this,"Gps already enabled",Toast.LENGTH_SHORT).show();
            return true;
        }
        //*************GPS ENDS******************
    }
    //*********code for on the gps if its off***************************

    private boolean hasGPSDevice(Context context) {
        final LocationManager mgr = (LocationManager) context
                .getSystemService(Context.LOCATION_SERVICE);
        if (mgr == null)
            return false;
        final List<String> providers = mgr.getAllProviders();
        if (providers == null)
            return false;
        return providers.contains(LocationManager.GPS_PROVIDER);
    }
    //ends here

    //back press function start here

    @Override
    public void onBackPressed() {
        new android.app.AlertDialog.Builder(this)
                .setTitle("EXIT?")
                .setIcon(R.drawable.exit)
                .setMessage("Are u sure you want to exit")
                .setCancelable(false)
                .setPositiveButton("Exit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finishAffinity();
                    }
                })
                .setNegativeButton("Stay", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .create().show();
    }

    //back press ends.....

    private boolean validateEmail(){
        String Email = email.getText().toString().trim();
        if (Email.isEmpty())
        {
            Toast.makeText(this, "Field can't be empty", Toast.LENGTH_SHORT).show();
            return false;
        }
        else
        {
            return true;
        }
    }

    private boolean validatePassword(){
        String Password = password.getText().toString().trim();
        if (Password.isEmpty())
        {
            Toast.makeText(this, "Field can't be empty", Toast.LENGTH_SHORT).show();
            return false;
        }

        else
        {
            return true;
        }
    }

    private void LoginFunction() {
        if (!isOnline())
        {
            linearLayout.startAnimation(mShakeAnimation);
            return;
        }
        if (!gpsEnabled())
        {
            linearLayout.startAnimation(mShakeAnimation);
            return;
        }
        if (!validateEmail() | !validatePassword())
        {
            linearLayout.startAnimation(mShakeAnimation);
            return;
        }


        loadingBar.setTitle("Login Driver");
        loadingBar.setMessage("Please wait!");
        loadingBar.show();
        loadingBar.setCanceledOnTouchOutside(false);
        loginBtn.setEnabled(false);

        String Email = email.getText().toString().trim();
        String Password = password.getText().toString().trim();

        mAuth.signInWithEmailAndPassword(Email,Password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful())
                        {

                            //get data from firebase database
                            databaseReference = FirebaseDatabase.getInstance().getReference().child("DriverData");
                            databaseReference.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                                    for (DataSnapshot child: children){
                                        final DriverData   driverInfo = child.getValue(driverData.getClass());
                                        assert driverInfo != null;
                                        String status = driverInfo.getStatus();
                                        loadingBar.dismiss();
                                        Toast.makeText(LogInActivityD.this, ""+status, Toast.LENGTH_SHORT).show();
                                        if (driverStatus.equals(status))
                                        {
                                            loadingBar.dismiss();
                                            Intent intent = new Intent(LogInActivityD.this,MapsActivityD.class);
                                            startActivity(intent);
                                           // finish();
                                            email.setText("");
                                            password.setText("");
                                        }else
                                        {
                                            loadingBar.dismiss();
                                            Toast.makeText(LogInActivityD.this, "Incorrect Email or Password", Toast.LENGTH_SHORT).show();
                                            linearLayout.startAnimation(mShakeAnimation);
                                        }

                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    Toast.makeText(LogInActivityD.this, "database Error", Toast.LENGTH_SHORT).show();
                                    loadingBar.dismiss();

                                }
                            });
                        } else
                        {
                            Toast.makeText(LogInActivityD.this, "Something went wrong...", Toast.LENGTH_SHORT).show();
                            loadingBar.dismiss();
                            linearLayout.startAnimation(mShakeAnimation);
                            loginBtn.setEnabled(true);
                        }

                    }
                });

    }
    protected boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            //Toast.makeText(this, "Yor are online", Toast.LENGTH_SHORT).show();
            return true;
        } else {
            Toast.makeText(this, "You are offline", Toast.LENGTH_LONG).show();
            return false;
        }
    }
}
