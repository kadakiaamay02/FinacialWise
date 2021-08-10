package com.example.newproject.ui.expenses;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.newproject.R;
import com.example.newproject.databinding.FragmentExpensesBinding;

import android.widget.ImageButton;
import android.widget.SearchView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import utilty.ApiLogin;
import objects.ExpenseModel;

public class ExpensesFragment extends Fragment  {

    private ExpensesViewModel expensesViewModel;
    private FragmentExpensesBinding binding;

    private FloatingActionButton button;
    private RecyclerView recyclerView;

    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private DatabaseReference dbRoot = db.getReference().child("Users");

    private MyAdapter adapter;
    private ArrayList<ExpenseModel> list;
    private SearchView searchView;
    private ImageButton deleteButton;

    private String currentUserId;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        expensesViewModel =
                new ViewModelProvider(this).get(ExpensesViewModel.class);

        binding = FragmentExpensesBinding.inflate(inflater, container, false);
        View root = binding.getRoot();



       //---- Start Expenses Code Here ----//

        //---- Fetch Username and User Id from our API (These should be available at all times throughout program)
        if ( ApiLogin.getInstance() != null )
        {
            currentUserId = ApiLogin.getInstance().getUserId();
        }

        dbRoot = db.getReference().child("Users").child(currentUserId);


        //Setup Variables
        deleteButton = (ImageButton) root.findViewById(R.id.deleteButton);
        searchView = root.findViewById(R.id.itemSearch);

        recyclerView = root.findViewById(R.id.userList);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        FirebaseRecyclerOptions<ExpenseModel> options =
                new FirebaseRecyclerOptions.Builder<ExpenseModel>()
                        .setQuery(dbRoot, ExpenseModel.class)
                        .build();

        list = new ArrayList<>();
        adapter = new MyAdapter(getActivity(), list );

        recyclerView.setAdapter(adapter);


        dbRoot.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull  DataSnapshot snapshot) {

                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    ExpenseModel expenseModel = dataSnapshot.getValue(ExpenseModel.class);
                    list.add(expenseModel);
                }
                adapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull  DatabaseError error) {

            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                search(s);
                return true;
            }
        });

        button = (FloatingActionButton) root.findViewById(R.id.floatingActionButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSecondActivity();
            }
        });


        return root;

    }//End onCreateView





    //---- Helper Functions ----//
    //Todo: This travels to the second fragment
    // (not sure if this is the best implementation)

    public void openSecondActivity() {
        //Travel to Second Fragment
        getParentFragmentManager().beginTransaction().replace(R.id.nav_host_fragment_content_profile, new ExpensesSecondPage()).commit();

        //Intent intent = new Intent (this, secondActivity.class);
        //startActivity(intent);
    }

    private void search(String str)
    {
        ArrayList<ExpenseModel> myList = new ArrayList<>();
        for(ExpenseModel object : list)
        {
            if(object.getItemName().toLowerCase().contains(str.toLowerCase()))
            {
                myList.add(object);
            }
        }
        MyAdapter myAdapter = new MyAdapter(getActivity(),myList);
        recyclerView.setAdapter(myAdapter);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }





}//End Fragment