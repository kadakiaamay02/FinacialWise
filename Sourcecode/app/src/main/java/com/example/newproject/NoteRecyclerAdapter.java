package com.example.newproject;



import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.jetbrains.annotations.NotNull;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import objects.Notes;
import utilty.ApiLogin;

public class NoteRecyclerAdapter extends RecyclerView.Adapter<NoteRecyclerAdapter.ViewHolder> {
    private Context context;
    private List<Notes> notesList;
    //Connection To Firestore db
    private FirebaseFirestore db = FirebaseFirestore.getInstance(); //Getting instance to Firestore db
    private CollectionReference collectionReference = db.collection("Users");

    public NoteRecyclerAdapter(Context context, List<Notes> notesList) {
        this.context = context;
        this.notesList = notesList;
    }


    @NonNull
    @NotNull
    @Override
    public NoteRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.note_row, viewGroup, false);


        return new ViewHolder(view, context);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull NoteRecyclerAdapter.ViewHolder viewHolder, int position) {
        Notes notes = notesList.get(position);

        viewHolder.title.setText( notes.getTitle() ); //Set the title from note_row with current object in noteList
        viewHolder.noteDetail.setText( notes.getNotes() );

        //Todo: Cannot deal with current or future dates. Need to fix in NotesFragment or here!!!
        //Convert timestamp into date
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTime( notes.getTimeAdded().toDate() );
        Date date = cal.getTime();
        DateFormat formatDate = new SimpleDateFormat("MMMM d, yyyy", Locale.ENGLISH);

        //Convert to string!
        String dateString = formatDate.format(date); //Careful with future dates, can crash program!
        //update the date textview
        viewHolder.dateAdded.setText(dateString); //Place string in xml note_row

    }

    @Override
    public int getItemCount() {
        return notesList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView
            title,
            noteDetail,
            dateAdded;
        public String currentUserId;

        public ImageButton noteDelete;


        public ViewHolder(@NonNull View itemView, Context ctx) {
            super(itemView);
            context = ctx;

            title = itemView.findViewById(R.id.note_title_textbox);
            noteDetail = itemView.findViewById(R.id.note_textbox);
            dateAdded = itemView.findViewById(R.id.note_timestamp_textbox);
            noteDelete = itemView.findViewById(R.id.note_row_del_btn);

            //---- Fetch Username and User Id from our API (These should be available at all times throughout program)
            if ( ApiLogin.getInstance() != null )
            {
                currentUserId = ApiLogin.getInstance().getUserId();
            }


            //Todo: This is glitchy, and crashes sometimes
            noteDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Remove From Database
                    String TempTitle = notesList.get(getAdapterPosition()).getTitle();//Will need this string

                    collectionReference.document(currentUserId).collection("Notes")
                            .whereEqualTo("title",TempTitle)
                            .get()
                            .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                @Override
                                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                    for(QueryDocumentSnapshot snapshot : queryDocumentSnapshots)
                                    {
                                        //Remove From Db
                                        snapshot.getReference().delete();

                                        //Remove From Dataset
                                        Toast.makeText(context.getApplicationContext(), "Deleting Note...",Toast.LENGTH_SHORT).show();
                                        notesList.remove(getAdapterPosition());
                                        notifyItemRemoved(getAdapterPosition());
                                        notifyItemRangeChanged(getAdapterPosition(),notesList.size());
                                        break;

                                    }

                                }

                            });

                }
            });

        }

    }
}
