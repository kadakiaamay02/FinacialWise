package com.example.newproject;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import utilty.ApiLogin;

public class CreateAccountActivity extends AppCompatActivity {
    //Button Variables
    //private Button loginButton;
    private Button createAcctButton;
    private RadioButton ageButton;
    //Firebase Variables
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser currentUser;

    //Firestore connection
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private CollectionReference collectionReference = db.collection( "Users");
    //Other Variables
    private EditText emailEditText;
    private EditText passwordEditText;
    private ProgressBar progressBar;
    private EditText userNameEditText;
    private EditText utaEditText;
    private EditText incomeEditText;
    private double userIncomeInteger;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        //Disable ActionBar
        getSupportActionBar().hide();

        firebaseAuth = FirebaseAuth.getInstance();



        createAcctButton = findViewById(R.id.create_acct_button);
        ageButton = findViewById(R.id.create_radio_btn);
        progressBar = findViewById(R.id.create_acct_progress);
        emailEditText = findViewById(R.id.email_account);
        passwordEditText = findViewById(R.id.password_account);
        userNameEditText = findViewById(R.id.username_account);
        incomeEditText = findViewById(R.id.income_account);
        utaEditText = findViewById(R.id.uta_account);

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                currentUser = firebaseAuth.getCurrentUser();

                if (currentUser != null)
                {
                    //This means user is already logged in
                }
                else
                {
                    //No user yet
                }

            }
        };

        //Create Account button clicked without Verifying age
        createAcctButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(CreateAccountActivity.this, "You have not verified your age!", Toast.LENGTH_SHORT).show();
            }
        });

        ageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Enable Create account button
                createAcctButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v)
                    {

                        //if(   passwordEditText.length() <6 || emailEditText.length() <6 || userNameEditText.length() <6 )
                        if(   passwordEditText.length() <6 || emailEditText.length() <6  )
                        {
                            progressBar.setVisibility(View.INVISIBLE);
                            Toast.makeText(CreateAccountActivity.this, "Fields must be greater than 6 characters!", Toast.LENGTH_LONG).show();
                            if (passwordEditText.length() <6)
                                passwordEditText.setError("password must be greater than 6 characters!");

                            if (emailEditText.length() <6)
                                emailEditText.setError("email must be greater than 6 characters!");

                    /*if (userNameEditText.length() <6)
                        userNameEditText.setError("username must be greater than 6 characters!");*/
                        }

                        else if( !TextUtils.isEmpty(emailEditText.getText().toString())
                                && !TextUtils.isEmpty(passwordEditText.getText().toString())
                                && !TextUtils.isEmpty(userNameEditText.getText().toString())
                                && !TextUtils.isEmpty(incomeEditText.getText().toString()) )
                        {
                            //Check that string is indeed an integer
                            String userIncome = incomeEditText.getText().toString().replaceAll(",", "").trim();
                            if ( !(isNumeric(userIncome)) )
                                incomeEditText.setError("Field is not a proper integer");

                            else
                            {

                                userIncomeInteger = Double.parseDouble(userIncome);
                                String email = emailEditText.getText().toString().trim();
                                String password = passwordEditText.getText().toString().trim();
                                String username = userNameEditText.getText().toString().trim();


                                //Everything is fine, proceed to create account function
                                createUserEmailAccount(email, password, username, userIncomeInteger);
                            }
                        }

                        else
                        {
                            Toast.makeText(CreateAccountActivity.this, "Empty Fields Not Allowed!", Toast.LENGTH_LONG).show();
                        }

                    }
                });
            } //End Radio onClick
        });




    }//End onCreate

    private void createUserEmailAccount (String email, String password, final String username, double userIncomeInteger)
    {
        //Checking if any fields are empty
        if ( !TextUtils.isEmpty(email)
                && !TextUtils.isEmpty(password)
                && !TextUtils.isEmpty(username) )
        {
            progressBar.setVisibility(View.VISIBLE);

            firebaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>()
                    {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if ( task.isSuccessful() )
                            {
                                //We take user to next Activity, but need to verify first
                                assert currentUser != null;
                                firebaseAuth.getCurrentUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void> () {
                                    @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                        if ( task.isSuccessful() )
                                        {
                                            Toast.makeText(CreateAccountActivity.this, "please check email for verification.", Toast.LENGTH_SHORT).show();
                                            //----- This Should Go in After Verifying Email ----//
                                            //User Variables
                                            final String currentUserId = currentUser.getUid();
                                            String utaId = utaEditText.getText().toString().trim();

                                            //Create a user map so we can create a user in the user collection in firestore db
                                            Map<String, Object> userMap = new HashMap<>();
                                            userMap.put("userId", currentUserId);
                                            userMap.put("username", username);
                                            userMap.put("utaId", utaId);
                                            userMap.put("income", userIncomeInteger);
                                            userMap.put("email", email); //Todo: This can probably be moved to create account
                                            //Filling in remaining values, user can fill these later
                                            userMap.put("study", "N/A");
                                            userMap.put("college", "N/A");
                                            userMap.put("profession", "N/A");

                                            //Attempt to set collect ref with on complete listener
                                            collectionReference.document(currentUserId).set(userMap)
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void unused) {
                                                            progressBar.setVisibility(View.INVISIBLE);
                                                            //String name = task.getResult().getString("username");
                                                            String name = username;

                                                            //---- Set Up Global API to have access to user credentials throughout program----//
                                                            ApiLogin apiLogin = ApiLogin.getInstance(); //Global API
                                                            apiLogin.setUserId(currentUserId);
                                                            apiLogin.setUsername(name);
                                                            //apiLogin.setUserIncome(userIncomeInteger); //Todo: maybe just send this to User document instead of keeping track of it in API, to avoid crashing
                                                            apiLogin.setutaId(utaId);
                                                            apiLogin.setUserEmail(email);

                                                            //---- Set up Intent and pass variables to next activity with putExtra if NOT USING API ----//
                                                            //Todo: This will check that the email has been verified and kick user back to login page
                                                            //Intent intent = new Intent(CreateAccountActivity.this, MainActivity.class); //Without Verification
                                                            Intent intent = new Intent(CreateAccountActivity.this, LoginActivity.class); //For Verification
                                                            startActivity(intent);
                                                        }
                                                    })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull @NotNull Exception e) {
                                                            progressBar.setVisibility(View.INVISIBLE);
                                                            Toast.makeText(CreateAccountActivity.this, "Task Failed!", Toast.LENGTH_LONG).show();
                                                        }
                                                    });



                                        }
                                        else
                                        {
                                            Log.d("MSG", "Task is not successful!");
                                            Toast.makeText(CreateAccountActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                }

                                });


                            // Everything before this point goes in before email verification task is successful
                            }
                            else
                            {
                                Log.d("MSG", "Task is not successful!");
                                progressBar.setVisibility(View.INVISIBLE);
                                Toast.makeText(CreateAccountActivity.this, "Account creation failed, email taken!", Toast.LENGTH_LONG).show();
                            }

                        }
                    }) //End onCompleteListener
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                        }
                    });
        }

        else
        {
            Log.d("MSG", "There are problems with the fields!");
        }
    }//End Create User Account Function


    @Override
    protected void onStart() {
        super.onStart();

        currentUser = firebaseAuth.getCurrentUser();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    public static boolean isNumeric(String strNum) {
        if (strNum == null) {
            return false;
        }
        try {
            double d = Double.parseDouble(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }
}