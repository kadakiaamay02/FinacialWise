package com.example.newproject.ui.expenses;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.example.newproject.R;
import com.example.newproject.databinding.FragmentExpensesBinding;
import com.example.newproject.ui.profile.ProfileFragment;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.jetbrains.annotations.NotNull;

import java.util.Calendar;

import objects.Expense;
import objects.ExpenseCategory;
import objects.ExpenseSubcategory;
import utilty.ApiLogin;

public class ExpensesFragment extends Fragment implements View.OnClickListener {

    private ExpensesViewModel expensesViewModel;
    private FragmentExpensesBinding binding;


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
    //private StorageReference storageReference; //Not needed until we upload images

    //Creating a new collection in the database for journal entries (This can be used for expenses)
    private CollectionReference collectionReference = db.collection("Expenses");


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        expensesViewModel =
                new ViewModelProvider(this).get(ExpensesViewModel.class);

        binding = FragmentExpensesBinding.inflate(inflater, container, false);
        View root = binding.getRoot();



        //Firebase Setup
        //storageReference = FirebaseStorage.getInstance().getReference(); //Have to get a reference to this to work
        firebaseAuth = FirebaseAuth.getInstance();

        progressBar = root.findViewById(R.id.post_progressBar);
        expenseEditText = root.findViewById(R.id.expenses_field);
        currentUserTextView = root.findViewById(R.id.post_username_textview);
        expenseTypeEditText = root.findViewById(R.id.expense_type_field);

        //Buttons
        saveButton = root.findViewById(R.id.expenses_save_button);
        saveButton.setOnClickListener(this);
        profileButton = root.findViewById(R.id.expenses_goto_profile_button);
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


        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        user = firebaseAuth.getCurrentUser();
        firebaseAuth.addAuthStateListener(authStateListener);
    }//End onStart Event

    @Override
    public void onStop() {
        super.onStop();
        if( firebaseAuth != null ){
            //We don't want listener to keep listening after activity is closed
            firebaseAuth.removeAuthStateListener(authStateListener);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId() ) {
            case R.id.expenses_save_button:
                //Save Expenses
                saveExpense();
                break;

            case R.id.expenses_goto_profile_button:
                //go to profile page
                //TODO: how can we make it navigate to the profile page? Maybe this page doesn't
                // need to navigate to the profile page?
                //startActivity(new Intent(ExpensesExample.this, MainActivity.class));
                //finish();

                //TODO: Here are some ways to travel between fragments ( Title will not change currently )
                //TODO: I know this page will be replaced, I just left this for reference
                //--- To Go to another fragment from within a fragment ---//
                //Fragment fragment = new ProfileFragment();
                //replaceFragment(fragment); //This works but the top title is not changed

                //----An Alternate Way to switch between fragments----//
                //For some reason the title will not change with either of these methods
                getParentFragmentManager().beginTransaction().replace(R.id.nav_host_fragment_content_profile, new ProfileFragment()).commit();
                break;
        }
    }//End onClick Event

    //TODO: Helper function to navigate to another fragment within a fragment
   /* public void replaceFragment(Fragment someFragment) {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.nav_host_fragment_content_profile, someFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }*/

    private void saveExpense()
    {
        String expenseString = expenseEditText.getText().toString().trim();
        //Convert expenseString to integer
        //Need to CHECK if string is an integer, or decimal here. No negative numbers should be allowed
        int expenseInteger = Integer.parseInt(expenseString);

        String expenseType = expenseTypeEditText.getText().toString().trim();

        progressBar.setVisibility(View.VISIBLE); //Set ProgressBar Visible



        if( !TextUtils.isEmpty(expenseString) )
        {
            //Create Expenses object
            //Currently will not work because no empty constructor in Expense class
            //Sends nested values in each expense document in the "Expenses" collection in firestore
            //This may make it difficult to search for values
            Expense.OneTime expense = new Expense.OneTime();
            expense.setAmount(expenseInteger);
            expense.setDate(Calendar.getInstance());
            //We need a username and userid attached to the object so we know where to find.
            expense.setNotes("Made by: " + currentUserName + " (used ID: " + currentUserId + ")");
            expense.setSubcategory(expenseType == "Needs" ? ExpenseSubcategory.DEFAULT_NEEDS : ExpenseSubcategory.DEFAULT_WANTS);

            //TODO: this was just filled in to make the code build, it needs more work.

            //Pass our object on to the collection reference
            collectionReference.add(expense)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            progressBar.setVisibility(View.INVISIBLE);

                            Toast.makeText(null, "To do, need to clear these fields so a new item_expense can be entered", Toast.LENGTH_LONG).show();
                            //startActivity(new Intent(ExpensesFragment.this, ExpensesExample.class) );
                            //finish();
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
}