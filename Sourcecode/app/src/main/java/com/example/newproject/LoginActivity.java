package com.example.newproject;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import utilty.ApiLogin;

public class LoginActivity extends AppCompatActivity {
    private Button loginButton;
    private Button createAcctButton;
    private Button forgotButton;

    private AutoCompleteTextView emailAddress;
    private EditText password;

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    //private FirebaseUser currentUser;
    private ProgressBar progressBar;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference = db.collection("Users");



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        progressBar = findViewById(R.id.login_progress);

        //Disable actionbar
        getSupportActionBar().hide();

        firebaseAuth = FirebaseAuth.getInstance();

        emailAddress = findViewById(R.id.email);
        password = findViewById(R.id.password);
        loginButton = findViewById(R.id.email_sign_in_button);
        createAcctButton = findViewById(R.id.create_account_button_login);
        forgotButton = findViewById(R.id.forgot_password_button);


        createAcctButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, CreateAccountActivity.class));
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                loginEmailPasswordUser( emailAddress.getText().toString().trim(), password.getText().toString().trim()  );
            }
        });

        forgotButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(LoginActivity.this, "oops...No functionality for this yet...", Toast.LENGTH_LONG).show();
                startActivity(new Intent(LoginActivity.this, ResetPasswordActivity.class));
            }
        });


    }


    private void loginEmailPasswordUser(String email, String pwd)
    {
        progressBar.setVisibility(View.VISIBLE);

        if( !TextUtils.isEmpty(email)
            && !TextUtils.isEmpty(pwd))
        {
            //successful login
            firebaseAuth.signInWithEmailAndPassword(email, pwd)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task)
                        {
                            if( task.isSuccessful() )
                            {
                                FirebaseUser user = firebaseAuth.getCurrentUser();
                                assert user != null;
                                final String currentUserId = user.getUid();

                                collectionReference
                                        .whereEqualTo("userId", currentUserId)
                                        .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                            @Override
                                            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                                                if (e != null) { //If exception is null then everything is fine
                                                    Log.d("loginWarn", "login failed: ");
                                                    Toast.makeText(LoginActivity.this, "Login Failed!!!", Toast.LENGTH_LONG).show();
                                                }

                                                assert queryDocumentSnapshots != null;
                                                if (!queryDocumentSnapshots.isEmpty())
                                                {
                                                    progressBar.setVisibility(View.INVISIBLE);


                                                    //Loop through queryDocumentSnapshots and use each snapshot to pull username values
                                                    for (QueryDocumentSnapshot snapshot : queryDocumentSnapshots) {
                                                        //Update the API which contains global username and id;
                                                        ApiLogin apiLogin = ApiLogin.getInstance();
                                                        apiLogin.setUsername(snapshot.getString("username"));
                                                        apiLogin.setUserId(currentUserId);
                                                        apiLogin.setUserEmail(email);


                                                        //Go to profile Activity
                                                        //startActivity(new Intent(LoginActivity.this, ExpensesExample.class));
                                                        boolean emailVerified = user.isEmailVerified();
                                                        if ( emailVerified ) //Check if email is verified!
                                                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                                        else
                                                            Toast.makeText(LoginActivity.this, "Sorry, This email has not been verified yet!", Toast.LENGTH_SHORT).show();

                                                    }//End For Loop

                                                }//End If Snapshots list is empty

                                            }//End onEvent
                                        });//End SnapshotListener
                            }//End Successful Task (User has Logged in successfully)

                            else
                            {
                                Log.d("loginWarn", "login failed: ");
                                Toast.makeText(LoginActivity.this, "Login Failed!!!", Toast.LENGTH_LONG).show();
                            }

                        }//End onComplete
                    })

                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressBar.setVisibility(View.INVISIBLE);

                        }
                    });

        }

        else
        {
            progressBar.setVisibility(View.INVISIBLE);
            Toast.makeText(LoginActivity.this, "Please enter your email and password", Toast.LENGTH_LONG).show();
        }
    }
}