package com.example.newproject;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.jetbrains.annotations.NotNull;

import java.util.Date;

import objects.Expense;
import utilty.ApiLogin;

public class ExpensesExample extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "ExpensesExample";
    //Xml Variables
    private Button saveButton;
    private Button profileButton;
    private ProgressBar progressBar;
    private EditText expenseEditText;
    private EditText expenseTypeEditText;
    private TextView currentUserTextView;

    //User Variables
    private String currentUserId;
    private String currentUserName;

    //Firebase Config
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser user;

    //Connection To Firestore db
    private FirebaseFirestore db = FirebaseFirestore.getInstance(); //Getting instance to Firestore db
    //Get Reference to Storage db
    private StorageReference storageReference;

    //Creating a new collection in the database for journal entries (This can be used for expenses)
    private CollectionReference collectionReference = db.collection("Expenses");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense_example);

        //Disable Actionbar
        getSupportActionBar().hide();

        //Firebase Setup
        storageReference = FirebaseStorage.getInstance().getReference(); //Have to get a reference to this to work
        firebaseAuth = FirebaseAuth.getInstance();

        progressBar = findViewById(R.id.post_progressBar);
        expenseEditText = findViewById(R.id.expenses_field);
        currentUserTextView = findViewById(R.id.post_username_textview);
        expenseTypeEditText = findViewById(R.id.expense_type_field);

        //Buttons
        saveButton = findViewById(R.id.expenses_save_button);
        saveButton.setOnClickListener(this);
        profileButton = findViewById(R.id.expenses_goto_profile_button);
        profileButton.setOnClickListener(this);


        progressBar.setVisibility(View.INVISIBLE); //Make progress Bar invisible at the start

        //---- Fetch Username and User Id from our API (These should be available at all times throughout program)
        if (ApiLogin.getInstance() != null ){
            currentUserId = ApiLogin.getInstance().getUserId();
            currentUserName = ApiLogin.getInstance().getUsername();
            currentUserTextView.setText(currentUserName); //Filling in the textview with user name
        }


        //Set Up Auth State Listener
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user = firebaseAuth.getCurrentUser();
                if( user != null){

                }

                else{

                }
            }
        };




    }//End onCreate Event



    @Override
    public void onClick(View v) {
        switch (v.getId() ) {
            case R.id.expenses_save_button:
                //Save Expenses
                saveExpense();
                break;

            case R.id.expenses_goto_profile_button:
                //go to profile page
                startActivity(new Intent(ExpensesExample.this, ProfileActivity.class));
                finish();
                break;
        }
    }//End onClick Event

    private void saveExpense()
    {
        String expenseString = expenseEditText.getText().toString().trim();
        //Convert expenseString to integer
        int expenseInteger = Integer.parseInt(expenseString);

        String expenseType = expenseTypeEditText.getText().toString().trim();

        progressBar.setVisibility(View.VISIBLE); //Set ProgressBar Visible



        if( !TextUtils.isEmpty(expenseString) )
        {
            //Create Expenses object
            Expense expense = new Expense();
            expense.setExpense(expenseInteger);
            expense.setTimeAdded(new Timestamp(new Date() ));
            expense.setUserName(currentUserName);
            expense.setUserId(currentUserId);
            expense.setExpenseType(expenseType);

            //Pass our object on to the collection reference
            collectionReference.add(expense)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            progressBar.setVisibility(View.INVISIBLE);
                            startActivity(new Intent(ExpensesExample.this, ExpensesExample.class) );
                            finish();
                        }
                    })

                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull @NotNull Exception e) {
                            Log.d(TAG, "failed to save expense: " + e.getMessage() );
                        }
                    });
            //Save Expense object instance

        }

        else
        {
            progressBar.setVisibility(View.VISIBLE);
        }
    }



    @Override
    protected void onStart() {
        super.onStart();
        user = firebaseAuth.getCurrentUser();
        firebaseAuth.addAuthStateListener(authStateListener);
    }//End onStart Event

    @Override
    protected void onStop() {
        super.onStop();
        if( firebaseAuth != null ){
            //We don't want listener to keep listening after activity is closed
            firebaseAuth.removeAuthStateListener(authStateListener);
        }
    }




}//End Application