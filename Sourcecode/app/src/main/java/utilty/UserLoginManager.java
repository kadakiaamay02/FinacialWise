package utilty;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import objects.Expense;
import objects.ExpenseModel;
import objects.User;

public class UserLoginManager {
    private static UserLoginManager instance = new UserLoginManager();

    public static UserLoginManager getInstance() {
        return instance;
    }

    public void clearCache() {
        if(user != null) {
            user = null;

            dbRoot.removeEventListener(listener);
        }
    }

    private DatabaseReference dbRoot = null;
    private User user = null;
    private ValueEventListener listener = null;
    public User getLoggedInUser() {
        if(user != null && ApiLogin.getInstance().getUserEmail().equals(user.getEmail())) {
            return user;
        }
        else {
            ApiLogin login = ApiLogin.getInstance();

            user = new User();
            user.setName(login.getUsername());
            user.setEmail(login.getUserEmail());
            user.setMonthlyIncome(login.getUserIncome());

            try {
                user.setId(Long.parseLong(login.getUserId()));
            }
            catch(Exception e) {
            }

            FirebaseDatabase db = FirebaseDatabase.getInstance();
            dbRoot = db.getReference().child("Users").child(ApiLogin.getInstance().getUserId());

            listener = dbRoot.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    List<Expense> newExpensesList = new ArrayList<>();

                    for(DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        ExpenseModel model = dataSnapshot.getValue(ExpenseModel.class);
                        if (model.getItemName() != null) {
                            newExpensesList.add(model.toExpense());
                        }
                    }

                    user.setExpenses(newExpensesList);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            return user;
        }
    }
}
