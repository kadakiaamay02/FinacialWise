package com.example.newproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DownloadManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.StorageReference;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import objects.Notes;
import utilty.ApiLogin;

public class NoteListActivity extends AppCompatActivity {

    //Firebase Variables
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser user;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    //FireStore Connection
    private CollectionReference collectionReference = db.collection("Users");

    //Other Variables
    private TextView noNotes;
    private StorageReference storageReference;
    private List<Notes> noteList;
    private RecyclerView recyclerView;
    private NoteRecyclerAdapter noteRecyclerAdapter;

    //String Variables
    private String currentUserId;
    private String currentUserName;
    private String currentUserEmail;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_list);

        //Disable ActionBar
        Objects.requireNonNull(getSupportActionBar()).hide();

        //Setup Firebase config
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();

        //Instantiate Journal
        noteList = new ArrayList<>();
        noNotes = findViewById(R.id.notes_no_notes);

        //Setup Recycler View
        recyclerView = findViewById(R.id.notes_recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

    }

    @Override
    protected void onStart() {
        super.onStart();



        //---- Fetch Username and User Id from our API (These should be available at all times throughout program)
        if ( ApiLogin.getInstance() != null )
        {
            currentUserId = ApiLogin.getInstance().getUserId();
            currentUserName = ApiLogin.getInstance().getUsername();
            currentUserEmail = ApiLogin.getInstance().getUserEmail();
        }

        /*//Can use this to order by user name, and time added then find only the ones where "userId" field is true
        collectionReference.orderBy("username").orderBy("timeAdded", Query.Direction.DESCENDING)
                .whereEqualTo("userId", JournalApi.getInstance().getUserId())
                .get();*/

        //collectionReference.whereEqualTo("userId", ApiLogin.getInstance().getUserId() ) //find only current user documents
        collectionReference = db.collection("Users").document(currentUserId).collection("Notes"); //Reassign the reference
        collectionReference.orderBy("timeAdded", Query.Direction.DESCENDING) //finds all users documents and sorts them newest first, oldest last
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if( !queryDocumentSnapshots.isEmpty() )
                        {
                            for( QueryDocumentSnapshot notes: queryDocumentSnapshots )
                            {
                                //Go through each time and get ALL journals of the person with currentuserId
                                Notes note = notes.toObject(Notes.class); //Convert Snapshot to Notes Object
                                noteList.add(note); //Add the converted object to the list
                            }
                            //Start RecyclerView
                            noteRecyclerAdapter = new NoteRecyclerAdapter(NoteListActivity.this, noteList);
                            recyclerView.setAdapter(noteRecyclerAdapter);
                            noteRecyclerAdapter.notifyDataSetChanged();
                        }
                        else
                        {
                            noNotes.setVisibility(View.VISIBLE);
                        }

                    }
                })

                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull @NotNull Exception e) {
                        Toast.makeText(NoteListActivity.this, "Something went wrong!", Toast.LENGTH_SHORT);
                    }
                });
    }//End Onstart
}//End Activity