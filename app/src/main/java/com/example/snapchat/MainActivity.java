package com.example.snapchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {

    EditText emailEditText;
    EditText passwordEditText;
    private FirebaseAuth mAuth;
    String email;
    String password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);

        email = emailEditText.getText().toString();
        password = passwordEditText.getText().toString();


//       Checking Already Logged in
        if(mAuth.getCurrentUser() != null){                                                         //1.checking that the current user is logged or not; if it is not equal to null i.e. we have to move to next activity without asking for email and password(Already Logged in)
            login();
        }

    }

//    2.1.Login
    public void logInUser(View view){

        if(TextUtils.isEmpty(email) || TextUtils.isEmpty(password)){
            Toast.makeText(this, "Email or Password Field is Empty", Toast.LENGTH_SHORT).show();
        }
        else {
            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        login();
                    } else {
                        Toast.makeText(MainActivity.this, "Login Failed, Try Again", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

//  2.2.Sign Up
    public void signUpUser(View view){

        if(TextUtils.isEmpty(email) || TextUtils.isEmpty(password)){
            Toast.makeText(this, "Email or Password Field is Empty", Toast.LENGTH_SHORT).show();
        }
        else {
            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {

                        //4.Adding new users to the firebase database
                        Log.i("USER ", task.getResult().getUser().getUid());
                        FirebaseDatabase.getInstance().getReference().child("users").child(task.getResult().getUser().getUid()).child("email").setValue(emailEditText.getText().toString());

//                    FirebaseDatabase mDatabase;
//                    DatabaseReference mDatabase;
//                    mDatabase = FirebaseDatabase.getInstance().getReference().child("users");
                        login();
                    } else {
                        Toast.makeText(MainActivity.this, "Signup Failed, Try Again", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

//  3.Moving to SnapsActivity
    public void login(){
        /*
        Intent intent = new Intent(getApplicationContext(),SnapsActivity.class);
        startActivity(intent);
         */
        startActivity(new Intent(getApplicationContext(),SnapsActivity.class));
    }
}
