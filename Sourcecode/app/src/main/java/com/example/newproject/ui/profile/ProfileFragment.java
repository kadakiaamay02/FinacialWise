package com.example.newproject.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.text.HtmlCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.newproject.EditProfileActivity;
import com.example.newproject.R;
import com.example.newproject.databinding.FragmentProfileBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.protobuf.Api;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

import utilty.ApiLogin;

public class ProfileFragment extends Fragment {

    private ProfileViewModel profileViewModel;
    private FragmentProfileBinding binding;

    //Variable Setup
    //Setup Variables
    private CircularImageView icon;
    private TextView profileUsername;
    private TextView profileEmail;
    private TextView profileIncome;
    private TextView profileStudy;
    private TextView profileCollege;
    private TextView profileId;
    private TextView profileProfession;

    //User Variables
    private String currentUserName;
    private String currentUserEmail;
    private String currentUserId;
    private String currentUserUTAid;
    private Double currentUserIncome;
    private String currentUserStudy;
    private String currentUserCollege;
    private String currentUserProfession;
    private String currentUserUrl;


    //Firebase Variables
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser user;

    //FireStore Connection
    //Connection To Firestore db
    private FirebaseFirestore db = FirebaseFirestore.getInstance(); //Getting instance to Firestore db
    private CollectionReference collectionReference = db.collection("Users");
    //private Uri imageUri;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        profileViewModel =
                new ViewModelProvider(this).get(ProfileViewModel.class);

        binding = FragmentProfileBinding.inflate(inflater, container, false);
        View root = binding.getRoot();



        //--- Begin Profile Stuff Here ---//
        firebaseAuth = FirebaseAuth.getInstance();

        //Setting up TextViews
        icon = root.findViewById(R.id.edit_prof_icon);
        profileUsername = root.findViewById(R.id.profile_Username);
        profileId = root.findViewById(R.id.profile_IDnum);
        profileIncome = root.findViewById(R.id.profile_Income);
        profileEmail = root.findViewById(R.id.profile_Email);
        //Object not yet stored in database
        profileStudy = root.findViewById(R.id.profile_AreaOfStudy);
        profileCollege = root.findViewById(R.id.profile_CollegeName);
        profileProfession = root.findViewById(R.id.profile_Profession);

        //---- Fetch Username and User Id from our API (These should be available at all times throughout program)
        if ( ApiLogin.getInstance() != null )
        {
            currentUserId = ApiLogin.getInstance().getUserId();
            currentUserName = ApiLogin.getInstance().getUsername();
            profileUsername.setText(currentUserName); //Filling in the textview with user name
            currentUserEmail = ApiLogin.getInstance().getUserEmail();
            //currentUserIncome = ApiLogin.getInstance().getUserIncome();

            //Change user Image URL
            /*currentUserUrl = ApiLogin.getInstance().getImageUrl();
            if( currentUserUrl!= null )
            {
                Picasso.get()
                        .load(currentUserUrl)
                        .placeholder(R.mipmap.ic_launcher_round)
                        .fit()
                        .into(icon);
            }*/
        }

        //Todo: This will go into the EditProfile Page later
        //documentCreate(); // Create a hardcoded document to send, What happens if i don't use this

        documentRetrieve(); //Retrieve values from "Users" Document in database to be displayed in profile fragment

        //Setup Fab here
        FloatingActionButton fab_profile; //Declare a fab
        fab_profile = root.findViewById(R.id.fab_profile); //Link it to the fab created in main profile xml

        fab_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fab_profile.setVisibility(View.INVISIBLE); //For testing purposes
                //TODO: I will create an activity here that the user will go to when this button is pressed. ( I don't want this activity visible in the activity drawer, this splash page is just a placeholder )
                startActivity(new Intent(getActivity(), EditProfileActivity.class)); //have to use getActivity here because not in main activity
            }
        });

        //Live Text here
        final TextView textView = binding.textHome;
        profileViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
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
        return root;

    }//End onCreateView

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


    //--- Helper Functions ---//
    public void documentCreate () {
        //---- Hard code a profile Document to be sent ----//
        //Todo: This will actually go into the editProfile Page!!
        //TODO: Testing to add extra user info to database
        //We take user to Next Activity -- Profile View
        user = firebaseAuth.getCurrentUser();
        assert user != null;
        final String currentUserId = user.getUid();



        //TODO: Noticed a glitch, Some fields need to be shorter, or text cuts off, User income in API has issues
        //Create a user map so we can create a user in the user collection in firestore db
        Map<String, Object> userObj = new HashMap<>();
        //currentUserIncome = Double.valueOf(1500); //For Testing
        //userObj.put("income", currentUserIncome); //use this to change income, having difficulty keeping track of this in API, will just write to document for now instead
        userObj.put("study", "Computer Engineering");
        userObj.put("college", "University of Texas at Arlington");
        userObj.put("profession", "Student");


        //Save This hardcoded stuff to a document
        collectionReference.document(currentUserId).update(userObj); //Update user obj without overwrite!!!
    }

    public void documentRetrieve() {
        //---- Retrieve User information here and place the rest into remaining textview!
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
                        currentUserProfession = document.getString("profession");
                        currentUserCollege = document.getString("college");
                        currentUserStudy = document.getString("study");
                        currentUserUTAid = document.getString("utaId");
                        currentUserIncome = document.getDouble("income");
                        currentUserUrl = document.getString("imageUrl");

                        ApiLogin.getInstance().setUserIncome(currentUserIncome);

                        //Set the icon for the image
                        if( currentUserUrl!= null )
                        {
                            Picasso.get()
                                    .load(currentUserUrl)
                                    .placeholder(R.mipmap.ic_launcher_round)
                                    .fit()
                                    .into(icon);
                        }




                        //Set TextViews
                        //profileId.setText("1001000000"); //Set manual userId for UTA, For testing

                        profileProfession.setText( HtmlCompat.fromHtml("<b>Profession: </b>" + currentUserProfession, HtmlCompat.FROM_HTML_MODE_LEGACY) );
                        profileCollege.setText( HtmlCompat.fromHtml("<b>College: </b>"+ currentUserCollege, HtmlCompat.FROM_HTML_MODE_LEGACY) );
                        profileStudy.setText( HtmlCompat.fromHtml("<b>Field of study: </b>"+ currentUserStudy, HtmlCompat.FROM_HTML_MODE_LEGACY) );
                        profileIncome.setText( HtmlCompat.fromHtml("<b>Current income: $</b>"+ currentUserIncome.toString().trim() +"0", HtmlCompat.FROM_HTML_MODE_LEGACY) ); //Todo: Use API for this
                        profileEmail.setText( HtmlCompat.fromHtml("<b>Email: </b>"+ currentUserEmail, HtmlCompat.FROM_HTML_MODE_LEGACY) );
                        profileId.setText( HtmlCompat.fromHtml("<b>UTA ID: </b>" + currentUserUTAid, HtmlCompat.FROM_HTML_MODE_LEGACY) );

                    }
                    else
                        Log.d("DOCDATA", "No such document");

                }

                else
                    Log.d("Failed", "Retrieval of Data Failed");

            }
        });
    }





}//End Fragment