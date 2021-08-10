package com.example.newproject;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

import objects.User;
import utilty.ApiLogin;
import utilty.UserLoginManager;

public class EditProfileActivity extends AppCompatActivity {
    //Variable Setup
    //Setup Variables
    private CircularImageView icon;
    private EditText userEditText;
    //private EditText emailEditText; //Not used because this is a high security field, along with password.
    private EditText incomeEditText;
    private EditText studyEditText;
    private EditText collegeEditText;
    private EditText idEditText;
    private EditText professionEditText;

    //Button Variables
    private Button saveButton;
    private ImageButton backButton;
    private ImageView photoButton;
    private ProgressBar progressBar;

    //User String Variables
    private String currentUserName;
    private String currentUserEmail;
    private String currentUserId;
    //Other Strings
    private String currentUserUTAid;
    private String currentUserIncome;
    private String currentUserStudy;
    private String currentUserCollege;
    private String currentUserProfession;

    //Firebase Variables
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser user;


    //FireStore Connection
    private FirebaseFirestore db = FirebaseFirestore.getInstance(); //Getting instance to Firestore db
    private CollectionReference collectionReference = db.collection("Users");
    private StorageReference storageReference;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        //--- Begin Profile Stuff Here ---//
        //Disable ActionBar
        getSupportActionBar().hide();

        //Firebase Stuff
        storageReference = FirebaseStorage.getInstance().getReference(); //need a reference to firebase storage
        firebaseAuth = FirebaseAuth.getInstance();

        //--- Begin Profile Stuff Here ---//
        firebaseAuth = FirebaseAuth.getInstance();
        icon = findViewById(R.id.edit_prof_icon);
        userEditText = findViewById(R.id.edit_profile_username);
        incomeEditText = findViewById(R.id.edit_profile_income);
        studyEditText = findViewById(R.id.edit_profile_study);
        collegeEditText = findViewById(R.id.edit_profile_college);
        idEditText = findViewById(R.id.edit_profile_ID);
        professionEditText = findViewById(R.id.edit_profile_profession);

        //Button
        saveButton = findViewById(R.id.edit_profile_save_btn);
        backButton = findViewById(R.id.edit_prof_backBtn);
        photoButton = findViewById(R.id.edit_prof_changeIconBtn);


        //ProgressBar
        progressBar = findViewById(R.id.edit_prof_progress);
        progressBar.setVisibility(View.INVISIBLE);

        //---- Fetch Username and User Id from our API (These should be available at all times throughout program)
        if ( ApiLogin.getInstance() != null )
        {
            currentUserId = ApiLogin.getInstance().getUserId();
            currentUserName = ApiLogin.getInstance().getUsername();
            currentUserEmail = ApiLogin.getInstance().getUserEmail();
            currentUserIncome = ((Double) UserLoginManager.getInstance().getLoggedInUser().getMonthIncome()).toString();
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
        }; //End AuthStateListener

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Go Back to previous activity
                Intent intent = new Intent(EditProfileActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        //PhotoButton Here, use activity launcher
        photoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                someActivityResultLauncher.launch(galleryIntent);
            }
        });



        //Setup Button Here
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ( !TextUtils.isEmpty(userEditText.getText().toString())
                    && !TextUtils.isEmpty(incomeEditText.getText().toString())
                    && !TextUtils.isEmpty(incomeEditText.getText().toString())
                    && !TextUtils.isEmpty(studyEditText.getText().toString())
                    && !TextUtils.isEmpty(collegeEditText.getText().toString())
                    && !TextUtils.isEmpty(idEditText.getText().toString())
                    && !TextUtils.isEmpty(professionEditText.getText().toString())
                    && imageUri != null )
                {
                    progressBar.setVisibility(View.VISIBLE);

                    StorageReference filepath = storageReference
                            .child("profile_images")
                            .child("my_image_" + Timestamp.now().getSeconds() ); //Append timestamp to image files uploaded

                    //TODO: find out why this can't work if the image came from our own firebase storage
                    if(!imageUri.toString().startsWith("https://firebasestorage.googleapis.com/v0/b/newproject-130f4.appspot.com/o/profile_images")) {
                        filepath.putFile(imageUri)
                                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                        filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                            @Override
                                            public void onSuccess(Uri uri) {
                                                String imageUrl = uri.toString();// Getting the needed URL
                                                documentCreate(imageUrl); //Execute the function to create and send a document to the database
                                            }
                                        })

                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@androidx.annotation.NonNull @NotNull Exception e) {
                                                        Log.d("IMGFAIL", "Failed to acquire URL: " + e.getMessage());
                                                    }
                                                });
                                    }
                                })

                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@androidx.annotation.NonNull @NotNull Exception e) {
                                        progressBar.setVisibility(View.INVISIBLE);
                                        Log.d("FILE", "Failed to putFile " + e.getMessage());
                                    }
                                });
                    }
                    else {
                        documentCreate(null);
                    }
                }

                else
                {
                    progressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(EditProfileActivity.this, "Empty Fields Not Allowed!", Toast.LENGTH_LONG).show();
                }
            }
        });//EndOnclickListenerSaveButton

        loadCurrentProfile();
    }//End onCreate

    //Instead of Activity Result for Image URI
    ActivityResultLauncher<Intent> someActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // Here, no request code
                        Intent data = result.getData();
                        //After capturing data, now do operations
                        assert data != null;
                        imageUri = data.getData(); //Convert intent to image uri
                        icon.setImageURI(imageUri);//show image
                    }
                }
            });


    private void loadCurrentProfile() {
        //---- Retrieve User information here and place the rest into remaining textview!
        collectionReference.document(currentUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@androidx.annotation.NonNull @NotNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        //Check if document actually exists
                        if (document.exists()) {
                            //Get data
                            currentUserId = ApiLogin.getInstance().getUserId();
                            currentUserName = ApiLogin.getInstance().getUsername();
                            currentUserEmail = ApiLogin.getInstance().getUserEmail();
                            currentUserProfession = document.getString("profession");
                            currentUserCollege = document.getString("college");
                            currentUserStudy = document.getString("study");
                            currentUserUTAid = document.getString("utaId");
                            try {
                                imageUri = Uri.parse(document.getString("imageUrl"));
                            }
                            catch(Exception x){}
                            try {
                                currentUserIncome = document.getDouble("income").toString();
                            }
                            catch(Exception x){}
                            //currentUserUrl = document.getString("imageUrl");

                            userEditText.setText(currentUserName);
                            incomeEditText.setText(currentUserIncome);
                            studyEditText.setText(currentUserStudy);
                            collegeEditText.setText(currentUserCollege);
                            idEditText.setText(currentUserUTAid);
                            professionEditText.setText(currentUserProfession);

                            //Set the icon for the image
                            if(imageUri != null )
                            {
                                Picasso.get()
                                        .load(imageUri)
                                        .placeholder(R.mipmap.ic_launcher_round)
                                        .fit()
                                        .into(icon);
                            }
                        }
                    }
                }
            });
    }

    //--- Helper Functions ---//
    public void documentCreate (String url) {

        progressBar.setVisibility(View.VISIBLE);
        //TODO: Testing to add extra user info to database
        user = firebaseAuth.getCurrentUser();
        assert user != null;
        final String currentUserId = user.getUid();

       //Deal With Income First

        //Check that string is indeed an integer
        currentUserIncome = incomeEditText.getText().toString().replaceAll(",", "").trim();
        if ( !(isNumeric(currentUserIncome)) )
        {
            progressBar.setVisibility(View.INVISIBLE);
            incomeEditText.setError("Field is not a proper integer");
        }

        else
        {
            currentUserName = userEditText.getText().toString().trim();
            currentUserUTAid = idEditText.getText().toString().trim();
            currentUserStudy = studyEditText.getText().toString().trim();
            currentUserCollege = collegeEditText.getText().toString().trim();
            currentUserProfession = professionEditText.getText().toString().trim();
            currentUserIncome = incomeEditText.getText().toString().trim();

            //Convert Income to double
            Double incomeConvert = Double.parseDouble(currentUserIncome);

            //TODO: Noticed a glitch, Some fields need to be shorter, or text cuts off, User income in API has issues
            //Create a user map so we can create a user in the user collection in firestore db
            Map<String, Object> userObj = new HashMap<>();
            //Place Values into Map object
            userObj.put("username", currentUserName);
            userObj.put("income", incomeConvert); //use this to change income, having difficulty keeping track of this in API, will just write to document for now instead
            userObj.put("utaId", currentUserUTAid);
            userObj.put("study", currentUserStudy);
            userObj.put("college", currentUserCollege);
            userObj.put("profession", currentUserProfession);

            if(url != null) {
                userObj.put("imageUrl", url);
            }

            //Send the object off to the database
            //collectionReference.document(currentUserId).update(userObj); //Update user obj without overwrite!!!

            collectionReference.document(currentUserId).update(userObj)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            progressBar.setVisibility(View.INVISIBLE);

                            //Update Login API
                            ApiLogin apiLogin = ApiLogin.getInstance(); //Global API
                            apiLogin.setUsername(currentUserName);
                            apiLogin.setutaId(currentUserUTAid);
                            apiLogin.setImageUrl(url);
                            apiLogin.setUserIncome(incomeConvert);
                            apiLogin.setUserEmail(currentUserEmail);

                            Intent intent = new Intent(EditProfileActivity.this, MainActivity.class);
                            startActivity(intent);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@androidx.annotation.NonNull @NotNull Exception e) {
                            progressBar.setVisibility(View.INVISIBLE);
                            Log.d("MSG", "Task is not successful!");
                            Toast.makeText(EditProfileActivity.this, "Failed To Update Database!", Toast.LENGTH_SHORT).show();
                        }
                    });

        }//End Else Statement





    }//End documentCreate Function

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