package com.example.newproject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.text.HtmlCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.newproject.databinding.ActivityMainBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import utilty.ApiLogin;


public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;

    //Firebase Variables
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser user;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    //User Variables
    private String currentUserId;
    private String currentUserName;
    private String currentUserEmail;
    private TextView userTextView;
    private TextView emailTextView;
    private ImageView userIcon;
    private String currentUserUrl;


    //FireStore Connection
    private CollectionReference collectionReference = db.collection("Users");
    private StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Setup Nav Bar stuff
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ////Disable main fab for main activity ( Only Really Needed for Profile I think, we can add individual FAB to each fragment )
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setVisibility(View.INVISIBLE);

        setSupportActionBar(binding.appBarProfile.toolbar);
        binding.appBarProfile.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "This will make user go to edit profile page.", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_expenses, R.id.nav_analysis, R.id.nav_notes)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_profile);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        //Setup Firebase config
        storageReference = FirebaseStorage.getInstance().getReference();
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();

        //Setup TextViews
        //Must get navigation view before using setText() for TextView in navigation header
        View headerView = navigationView.getHeaderView(0);
        userTextView = (TextView) headerView.findViewById(R.id.header_username);
        emailTextView = (TextView) headerView.findViewById(R.id.header_email); //R.id.header_icon
        userIcon = (ImageView) headerView.findViewById(R.id.header_icon);
        //userIcon.setImageResource(R.drawable.ic_arrow); //Can actually replace the image source manually


        //Using API login to get values
        if (ApiLogin.getInstance() == null)
            Toast.makeText(MainActivity.this, "API is null!!!", Toast.LENGTH_LONG).show();

        //---- Fetch Username and User Id from our API (These should be available at all times throughout program)
        if (ApiLogin.getInstance() != null ){
            //Toast.makeText(MainActivity.this, "API is good!", Toast.LENGTH_LONG).show();

            currentUserId = ApiLogin.getInstance().getUserId();
            currentUserName = ApiLogin.getInstance().getUsername();
            currentUserEmail = ApiLogin.getInstance().getUserEmail();
            //Toast.makeText(MainActivity.this, "username is " + currentUserName, Toast.LENGTH_LONG).show();

            userTextView.setText(currentUserName);
            emailTextView.setText(currentUserEmail);
        }


        collectionReference.document(currentUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<DocumentSnapshot> task) {
                if( task.isSuccessful() )
                {
                    DocumentSnapshot document = task.getResult();
                    //Check if document actually exists
                    if ( document.exists() )
                    {
                        //Get data
                        currentUserUrl = document.getString("imageUrl");
                        if( currentUserUrl!= null )
                        {
                            Picasso.get()
                                    .load(currentUserUrl)
                                    .placeholder(R.mipmap.ic_launcher_round)
                                    .fit()
                                    .into(userIcon);
                        }

                    }
                    else
                        Log.d("DOCDATA", "No such document");

                }

                else
                    Log.d("Failed", "Retrieval of Data Failed");

            }
        });




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

    //Set Menu xml here
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch( item.getItemId() )
        {
            case R.id.action_signout:
                //sign user out
                if( user != null && firebaseAuth != null)
                {
                    firebaseAuth.signOut(); //Sign out the user
                    startActivity( new Intent(MainActivity.this, SplashActivity.class) ); //Go to splash page

                    //finish(); //Leave this commented for now
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_profile);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
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




}//End Activity