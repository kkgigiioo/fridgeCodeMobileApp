package com.example.fridgecodemobileapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fridgecodemobileapp.userSource.UserData;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int GOOGLE_SIGN = 1;
    FirebaseAuth mAuth;
    Button btn_login, btn_logout;
    TextView loginTV;
    ImageView iV;
    ProgressBar progressBar;
    GoogleSignInClient mGoogleSignInClient;

    private static final int MY_PERMISSION_REQUEST_INTERNET = 3;

    List<UserData> dataList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED)
        {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.INTERNET))
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Request Permission");
                builder.setMessage("You should enable this permission to INTERNET so we can to that and that...");
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.INTERNET}, MY_PERMISSION_REQUEST_INTERNET);
                    }
                });
                builder.setPositiveButton("Grant Permission", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.INTERNET}, MY_PERMISSION_REQUEST_INTERNET);
                    }
                });

                builder.show();
            }
            else
            {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.INTERNET}, MY_PERMISSION_REQUEST_INTERNET);
                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
        else
        {
            // Permission has already been granted
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.INTERNET}, MY_PERMISSION_REQUEST_INTERNET);
        }

        btn_login = findViewById(R.id.loginBtn);
        btn_logout = findViewById(R.id.logoutBtn);
        loginTV = findViewById(R.id.loginApp);
        iV = findViewById(R.id.imageFirebaseLogo);
        progressBar = findViewById(R.id.progress_circular);

        mAuth = FirebaseAuth.getInstance();

        GoogleSignInOptions gSIP = new GoogleSignInOptions
                .Builder()
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gSIP);

        btn_login.setOnClickListener(v -> SignInGoogle());
        btn_logout.setOnClickListener(v -> Logout());

        if(mAuth.getCurrentUser() != null){
            FirebaseUser fUser = mAuth.getCurrentUser();
            updateUI(fUser);
        }
    }

    void SignInGoogle() {
        progressBar.setVisibility(View.VISIBLE);
        Intent signIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signIntent, GOOGLE_SIGN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == GOOGLE_SIGN){
            Task<GoogleSignInAccount> task = GoogleSignIn
                    .getSignedInAccountFromIntent(data);

            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);

                if(account != null) {
                    firebasAuthWithGoogle(account);
                }
            }
            catch(ApiException aEx) {
                aEx.printStackTrace();
            }
        }
    }

    private void firebasAuthWithGoogle(GoogleSignInAccount account) {
        Log.d("TAG", "firebaseAuthWithGoogle: " + account.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if(task.isSuccessful()) {
                        progressBar.setVisibility(View.INVISIBLE);
                        Log.d("TAG", "SignIn Success!");

                        FirebaseUser fUser = mAuth.getCurrentUser();

                        updateUI(fUser);
                    }
                    else {
                        progressBar.setVisibility(View.INVISIBLE);

                        Log.w("TAG", "SignIn Failure", task.getException());

                        Toast.makeText(this, "SignIn Failed!", Toast.LENGTH_SHORT).show();

                        updateUI(null);
                    }
                });
    }

    private void updateUI(FirebaseUser fUser) {
        if(fUser != null) {
            String name = fUser.getDisplayName();
            String email = fUser.getEmail();
            String photo = String.valueOf((fUser.getPhotoUrl()));

            loginTV.append("Info: \n");
            loginTV.append(name + "\n");
            loginTV.append(email);

            Picasso.get().load(photo).into(iV);
            btn_login.setVisibility(View.INVISIBLE);
            btn_logout.setVisibility(View.VISIBLE);
        }
        else {
            loginTV.setText(getString(R.string.google_login));
            Picasso.get().load(R.drawable.ic_firebase_logo).into(iV);
            btn_login.setVisibility(View.VISIBLE);
            btn_logout.setVisibility(View.INVISIBLE);
        }
    }

    void Logout() {
        FirebaseAuth.getInstance().signOut();
        mGoogleSignInClient.signOut().addOnCompleteListener(this, task -> {
            updateUI(null);
        });
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    {
        switch (requestCode)
        {
            case MY_PERMISSION_REQUEST_INTERNET:
            {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    Toast.makeText(this, "Permission is granted, now we can do the functionality.", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, "Permission not granted. We need to disable the functionality.", Toast.LENGTH_SHORT).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }
}
