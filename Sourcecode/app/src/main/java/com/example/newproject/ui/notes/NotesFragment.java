package com.example.newproject.ui.notes;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.newproject.EditProfileActivity;
import com.example.newproject.MainActivity;
import com.example.newproject.NoteListActivity;
import com.example.newproject.databinding.NotesFragmentBinding;

import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.newproject.R;
import com.example.newproject.databinding.FragmentProfileBinding;
import com.example.newproject.ui.profile.ProfileViewModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import objects.Notes;
import utilty.ApiLogin;

public class NotesFragment extends Fragment {

    private NotesViewModel notesViewModel;
    private NotesFragmentBinding binding;

    //Progress Bar
    private ProgressBar progressBar;

    //Button Variables
    private Button btnChangeDate;
    private Button btnSaveNote;
    private Button btnViewNotes;

    //EditText Variables
    private EditText noteDetailsEditText;
    private EditText noteTitleEditText;

    //String Variables
    private String dateString;
    private String currentUserId;
    private String currentUserName;

    //Other user Variables
    private Timestamp currentUserTimestamp;

    //Firebase Variables
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser user;

    //FireStore Connection
    //Connection To Firestore db
    private FirebaseFirestore db = FirebaseFirestore.getInstance(); //Getting instance to Firestore db
    //private CollectionReference collectionReference = db.collection("Users");

    public static NotesFragment newInstance() {
        return new NotesFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        notesViewModel =
                new ViewModelProvider(this).get(NotesViewModel.class);

        binding = NotesFragmentBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        //--- Begin Here ---//

        firebaseAuth = FirebaseAuth.getInstance();

        //---- Get Id's ----//
        progressBar = root.findViewById(R.id.notes_progress_bar);
        btnChangeDate = root.findViewById(R.id.btn_change_date);
        btnSaveNote = root.findViewById(R.id.btn_save_note);
        btnViewNotes = root.findViewById(R.id.btn_goto_notes);
        noteDetailsEditText = root.findViewById(R.id.notes_details);
        noteTitleEditText = root.findViewById(R.id.notes_title);

        //---- Retrieve Date For Timestamp ----//
        Calendar currentDate = Calendar.getInstance();
        btnChangeDate.setText(String.format("%1$te %1$tb %1$tY", currentDate));//Sets Current Date to button

        dateString = new SimpleDateFormat("EEE MMM dd hh:mm:ss 'GMT'Z yyyy").format(currentDate.getTime());
        //Toast.makeText(getActivity(), dateStr, Toast.LENGTH_SHORT).show(); //Display current date for DEBUG
        btnChangeDate.setText("Set Date");

        progressBar.setVisibility(View.INVISIBLE); //Make progress Bar invisible at the start

        //---- Fetch Username and User Id from our API (These should be available at all times throughout program)
        if (ApiLogin.getInstance() != null ){
            currentUserId = ApiLogin.getInstance().getUserId();
            currentUserName = ApiLogin.getInstance().getUsername();
        }



        //---- Setup Buttons ----//

        //Setup onClick Listener for Change date Button
        this.btnChangeDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar currentDate = Calendar.getInstance(Locale.ENGLISH);

                DatePickerDialog dialog = new DatePickerDialog(view.getContext());

                //Todo: prevents future dates, but current date is also not showing up for some reason!
                dialog.getDatePicker().setMaxDate(currentDate.getTimeInMillis() ); //Do not allow future dates to be picked!
                dialog.updateDate(
                        currentDate.get(Calendar.YEAR),
                        currentDate.get(Calendar.MONTH),
                        currentDate.get(Calendar.DAY_OF_MONTH)
                );

                dialog.setOnDateSetListener(new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
                        Calendar calUpdate = Calendar.getInstance();
                        calUpdate.set(year, month, dayOfMonth);

                        btnChangeDate.setText(String.format("%1$te %1$tb %1$tY", calUpdate));

                        currentUserTimestamp = new Timestamp ( calUpdate.getTime() );

                        //Toast.makeText(getActivity(), dateString, Toast.LENGTH_SHORT).show(); //Show current date
                    }
                });

                dialog.show();

            }//End onClick

        });//End Date onClickListener


        //Setup onClick Listener for Save Button
        //Check that datesString is not empty before sending to db
        this.btnSaveNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /*if(TextUtils.isEmpty())
                {

                }*/


                saveNote(currentUserTimestamp);
            }
        });//End SaveNote onClick

        this.btnViewNotes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(getActivity(), "Now Viewing Notes!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getActivity(), NoteListActivity.class));
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
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
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

    private void saveNote(Timestamp userTimestamp)
    {
        //Make sure to check date string before passing in, can also maybe pass a Timestamp with now time just in case the string is empty
        String title ;
        String notes;

        progressBar.setVisibility(View.VISIBLE);
        //Check that textfields are NOT empty
        if(!TextUtils.isEmpty(noteDetailsEditText.getText().toString())
            && !TextUtils.isEmpty(noteTitleEditText.getText().toString()) )
        {
            title = noteTitleEditText.getText().toString().trim();
            notes = noteDetailsEditText.getText().toString().trim();

            //Convert timestamp from calendar object above
            if (userTimestamp == null)
                userTimestamp = new Timestamp( new Date() ); //Set timestamp to current date if it is NULL

            //Create Object to Send to db
            Notes note = new Notes();
            note.setTitle(title);
            note.setNotes(notes);
            note.setUserId(currentUserId);
            note.setUserName(currentUserName);
            note.setTimeAdded(userTimestamp);

            //collectionReference.document(currentUserId).set(note) //use set for nesting
            //collectionReference.add(not) //Use this if i'm not adding nested documents!!
            CollectionReference collectionReference = db.collection("Users").document(currentUserId).collection("Notes");
            collectionReference.add(note)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                             progressBar.setVisibility(View.INVISIBLE);

                            Toast.makeText(getActivity(), "Now saving your note...", Toast.LENGTH_LONG).show();
                            //TODO: (TEMPORARY) Go to notelist activity and show notes (will make a button to go here)
                            startActivity(new Intent(getActivity(), NoteListActivity.class));

                            //Todo: Go to back to notes fragment, reset text fields!
                            //getParentFragmentManager().beginTransaction().replace(R.id.nav_host_fragment_content_profile, new NotesFragment()).commit();
                        }
                    })

                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull @NotNull Exception e) {
                            Log.d("FAIL", "failed to save note: " + e.getMessage() );
                            Toast.makeText(getActivity(),"Failed to Save note!",Toast.LENGTH_SHORT).show();
                        }
                    });
        }

        else {
            Toast.makeText(getActivity(),"Empty Fields Not Allowed!",Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.VISIBLE);
        }


    }



}//End Fragment