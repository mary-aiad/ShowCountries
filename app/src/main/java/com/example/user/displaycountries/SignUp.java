package com.example.user.displaycountries;

import android.app.ProgressDialog;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignUp extends AppCompatActivity {

    private EditText userEmail, userPassowrd, confirmPass;
    private Button signUpBtn;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference reference;
    private FirebaseUser currentUser;
    private FirebaseAuth.AuthStateListener authStateListener;

    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        firebaseAuth = FirebaseAuth.getInstance();
        reference = FirebaseDatabase.getInstance().getReference();

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait...");

        currentUser = firebaseAuth.getCurrentUser();

        userEmail = (EditText) findViewById(R.id.email);
        userPassowrd = (EditText) findViewById(R.id.password);
        confirmPass = (EditText) findViewById(R.id.confirm_pass);

        signUpBtn = (Button) findViewById(R.id.sign_up_btn);

        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkValidData();
            }
        });
    }

    //Check if user data that entered is valid (not empty) then called function addUserData
    private void checkValidData() {
        String userEmailStr = "", userPasswordStr = "", confirmPassStr = "";
        View foucsView = null;
        Boolean cancel = false;

        userEmailStr = userEmail.getText().toString();
        userPasswordStr = userPassowrd.getText().toString();
        confirmPassStr = confirmPass.getText().toString();

        if(TextUtils.isEmpty(userEmailStr)){
            foucsView = userEmail;
            cancel = true;
            userEmail.setError(getString(R.string.error_for_empty_field));
        }

        else if (!isEmailValid(userEmailStr)) {
            userEmail.setError(getString(R.string.error_invalid_email));
            foucsView = userEmail;
            cancel = true;
        }

        if(TextUtils.isEmpty(userPasswordStr)){
            foucsView = userPassowrd;
            cancel = true;
            userPassowrd.setError(getString(R.string.error_for_empty_field));
        }

        if(TextUtils.isEmpty(confirmPassStr)){
            foucsView = confirmPass;
            cancel = true;
            confirmPass.setError(getString(R.string.error_for_empty_field));
        }

        else if(!userPasswordStr.equals(confirmPassStr)){
            confirmPass.setError(getString(R.string.error_confirm_password));
            foucsView = confirmPass;
            cancel = true;
        }
        else if(!isPasswordValid(userPasswordStr)){
            foucsView = userPassowrd;
            cancel = true;
        }

        if(cancel){
            foucsView.requestFocus();
        }
        else
            addUserData(userEmailStr, userPasswordStr, confirmPassStr);
    }

    //This function to add new user on database to sign up and try our app
    private void addUserData(final String email, final String pass, String confirmPass){
        progressDialog.show();

        firebaseAuth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener(SignUp.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            currentUser = firebaseAuth.getCurrentUser();
                            User newUser = new User();
                            newUser.setEmail(email);
                            newUser.setPassword(pass);

                            reference.child("users").child(currentUser.getUid()).setValue(newUser)
                                    .addOnCompleteListener(SignUp.this, new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                progressDialog.dismiss();
                                                Toast.makeText(SignUp.this, "Successful sign-up", Toast.LENGTH_LONG).show();
                                                sendVerficationEmail();

                                                SharedPreferences sharedPreferences = getApplicationContext().
                                                        getSharedPreferences("MyPref", Context.MODE_PRIVATE);
                                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                                editor.putBoolean("isLogin", true);
                                                editor.commit();

                                                Intent intent = new Intent(getApplicationContext(), Home.class);
                                                startActivity(intent);
                                                finish();
                                            }
                                            else{
                                                progressDialog.dismiss();
                                                Toast.makeText(SignUp.this, "Failed sign up", Toast.LENGTH_SHORT).show();
                                            }

                                        }
                                    });
                        }
                        else{
                            progressDialog.dismiss();
                            Toast.makeText(SignUp.this, "Authentication failed",
                                    Toast.LENGTH_SHORT).show();
                        }

                    }
                });
    }

    //This function to send verfication email to user to confirm the account
    private void sendVerficationEmail() {
        FirebaseUser user = firebaseAuth.getCurrentUser();

        user.sendEmailVerification()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            FirebaseAuth.getInstance().signOut();
                            startActivity(new Intent(SignUp.this, Login.class));
                            finish();
                        }
                        else
                        {
                            overridePendingTransition(0, 0);
                            finish();
                            overridePendingTransition(0, 0);
                            startActivity(getIntent());
                        }
                    }
                });
    }

    //Check if email is valid or not
    private boolean isEmailValid(String email) {
        return email.contains("@");
    }

    //Check if password is valid or not must have 1 lower and upper case and 1 number to be secure
    private boolean isPasswordValid(String pass) {
        if (pass.length() < 8) {
            userPassowrd.setError(getString(R.string.error_short_password));
            return false;
        } else if (pass.equals(pass.toLowerCase())  || pass.equals(pass.toUpperCase())
                || !pass.matches(".*\\d+.*")) {
            userPassowrd.setError(getString(R.string.error_invalid_password));
            return false;
        }
        return true;
    }

    //to return to login form
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(SignUp.this, Login.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onStart() {
        super.onStart();
        if(firebaseAuth != null) {
            firebaseAuth.addAuthStateListener(authStateListener);
            currentUser = firebaseAuth.getCurrentUser();
        }
    }

    @Override
    protected void onPause () {
        super.onPause();
        firebaseAuth.addAuthStateListener(authStateListener);
        currentUser = firebaseAuth.getCurrentUser();
    }

    @Override
    protected void onStop() {
        super.onStop();
//        firebaseAuth.removeAuthStateListener(authStateListener);
    }
}
