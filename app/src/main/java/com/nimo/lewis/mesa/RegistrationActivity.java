package com.nimo.lewis.mesa;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

public class RegistrationActivity extends AppCompatActivity {
    private Button mSignup;
    private EditText mName, mEmail,mPassword;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        //Check Authorization State
        firebaseAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    Intent intent = new Intent(getApplication(), MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                    return;
                }
            }
        };
        //Get Auth Instance
        mAuth = FirebaseAuth.getInstance();

        mName = findViewById(R.id.name);
        mSignup = findViewById(R.id.signUpButton);
        mEmail = findViewById(R.id.emailToSign);
        mPassword = findViewById(R.id.passwordtoSign);
        //OnClick Listener for completion of Sign up data
        mSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String name = mName.getText().toString();
                final String email = mEmail.getText().toString();
                final String password = mPassword.getText().toString();
                mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(RegistrationActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()){
                            Toast.makeText(RegistrationActivity.this, "Register Failed", Toast.LENGTH_LONG).show();
                        }
                        //Link USer and USer Database
                        else{
                            String userId = mAuth.getCurrentUser().getUid();
                            User user = new User(name, email, userId);
                            DatabaseReference userdb = FirebaseDatabase.getInstance().getReference();
                            //String key = userdb.child("users").getKey();

                            userdb.child("users").child(userId).setValue(user);

                            Intent intent = new Intent(getApplication(), MainActivity.class);
                            Toast.makeText(RegistrationActivity.this, "Success", Toast.LENGTH_LONG).show();
                            startActivity(intent);

                        }
                    }
                });

            }
        });
    }


    @Override
    public void onStart(){
        super.onStart();
        mAuth.addAuthStateListener(firebaseAuthListener);
    }
    @Override
    public void onStop(){
        super.onStop();
        mAuth.removeAuthStateListener(firebaseAuthListener);
    }

}
