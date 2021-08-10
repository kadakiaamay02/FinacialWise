package com.example.newproject.ui.expenses;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newproject.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

import objects.ExpenseModel;
import utilty.ApiLogin;


//Todo: I think the adapter is fine now

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder>
{
    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private DatabaseReference root = db.getReference().child("Users");
    private String currentUserId;
    ArrayList<ExpenseModel> mList;
    Context context;
    String key = db.getReference("Users").getKey();

    public MyAdapter (Context context , ArrayList<ExpenseModel> mList)
    {
        this.mList = mList;
        this.context = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_expense, parent, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        //---- Fetch Username and User Id from our API (These should be available at all times throughout program)
        if ( ApiLogin.getInstance() != null )
        {
            currentUserId = ApiLogin.getInstance().getUserId();
        }

        root = db.getReference().child("Users").child(currentUserId);

        ExpenseModel expenseModel = mList.get(position);
        holder.itemName.setText(expenseModel.getItemName());
        holder.itemAmount.setText(expenseModel.getItemAmount());
        holder.itemDate.setText(expenseModel.getItemDate());
        holder.itemMethod.setText(expenseModel.getItemMethod());




        holder.deleteButton.setOnClickListener(v -> {
            mList.remove(position);
            root.child(expenseModel.getitemID()).removeValue();
            notifyDataSetChanged();

            //Here is what I added
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, mList.size());
            notifyItemRemoved(position);
            notifyItemRangeChanged(position,mList.size());
            mList.clear();


        });




    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        TextView itemName, itemAmount, itemDate, itemMethod;
        ImageView deleteButton;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            itemName = itemView.findViewById(R.id.itemNameText);
            itemAmount = itemView.findViewById(R.id.itemAmountText);
            itemDate = itemView.findViewById(R.id.itemDateText);
            itemMethod = itemView.findViewById(R.id.itemMethodText);
            deleteButton = itemView.findViewById(R.id.deleteButton);


             /*  //Todo: This is for use when not in onBind
        holder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                root.child(model.getitemID()).removeValue();

                mList.remove(getAdapterPosition());
                notifyItemRemoved(getAdapterPosition());
                notifyItemRangeChanged(getAdapterPosition(), mList.size());
                notifyItemRemoved(getAdapterPosition());
                notifyItemRangeChanged(getAdapterPosition(),mList.size());
                mList.clear();

            }
        });*/

        }
    }
}
