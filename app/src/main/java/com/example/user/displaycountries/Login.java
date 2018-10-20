package com.example.user.displaycountries;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.user.displaycountries.Model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Login extends AppCompatActivity {

    private EditText userEmail, userPassowrd;
    private Button loginBtn, signUpBtn;

    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference reference;
    private FirebaseAuth.AuthStateListener authStateListener;
    public FirebaseUser currentUser;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_screen);

        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        reference = firebaseDatabase.getReference();

        sharedPreferences = getApplicationContext().getSharedPreferences("MyPref", 0);

        if (sharedPreferences.getBoolean("isLogin", false)) {
            Toast.makeText(getApplicationContext(), "You are already Logged in ", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(Login.this, Home.class);
            startActivity(intent);
            finish();
        }

        userEmail = (EditText) findViewById(R.id.email);
        userPassowrd = (EditText) findViewById(R.id.password);

        loginBtn = (Button) findViewById(R.id.login_in_btn);
        signUpBtn = (Button) findViewById(R.id.sign_up_btn);


        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user != null){
                    Toast.makeText(getApplicationContext(), "You are Logged in ", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(Login.this, Home.class);
                    startActivity(intent);
                    finish();
                }
            }
        };
        //login button
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkEmptyData();
            }
        });

        //if new user sign up
        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Login.this, SignUp.class);
                startActivity(intent);
                finish();
            }
        });

    }

    //Check if user data that entered is valid (not empty) then called check user Data
    private void checkEmptyData() {
        String email = "", pass = "";
        View foucsView = null;
        Boolean cancel = false;

        email = userEmail.getText().toString();
        pass = userPassowrd.getText().toString();

        if(TextUtils.isEmpty(email)){
            foucsView = userEmail;
            cancel = true;
            userEmail.setError(getString(R.string.error_for_empty_field));
        }

        if(TextUtils.isEmpty(pass)){
            foucsView = userPassowrd;
            cancel = true;
            userPassowrd.setError(getString(R.string.error_for_empty_field));
        }

        if(cancel){
            foucsView.requestFocus();
        }
        else

            checkUserData(email, pass);
    }

    //Check if data entered by user is correspond with your data on data base
    //if email and password not found on data base inform user to re enter it
    private void checkUserData(final String email, final String password){

        reference.child("users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot dSnap : dataSnapshot.getChildren()){
                    User user = new User();
                    user.setEmail(dSnap.getValue(User.class).getEmail());
                    user.setPassword(dSnap.getValue(User.class).getPassword());

                    if(email.equals(user.getEmail()) && password.equals(user.getPassword())){
                        Toast.makeText(Login.this, "Login Successful", Toast.LENGTH_LONG).show();

                        SharedPreferences sharedPreferences = getApplicationContext().
                                getSharedPreferences("MyPref", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putBoolean("isLogin", true);
                        editor.commit();

                        Intent intent = new Intent(Login.this, Home.class);
                        startActivity(intent);
                        finish();
                        return;
                    }
                }
                Toast.makeText(Login.this, getString(R.string.error_invalid_email_or_pass), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


}
