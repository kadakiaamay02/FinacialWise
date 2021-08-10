package objects;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import utilty.ApiLogin;

public class ExpenseModel
{
    String itemName, itemAmount, itemDate, itemMethod, itemID;

    public ExpenseModel() {
    }

    public ExpenseModel(String itemName, String itemAmount, String itemDate, String itemMethod) {
        this.itemName = itemName;
        this.itemAmount = itemAmount;
        this.itemDate = itemDate;
        this.itemMethod = itemMethod;
        this.itemID = itemID;
    }

    public String getItemName() {
        return itemName;
    }

    public String getItemAmount() {
        return itemAmount;
    }

    public String getItemDate() {
        return itemDate;
    }

    public String getItemMethod() {
        return itemMethod;
    }

    public String getitemID() { return itemID; }

    public void setitemID(String itemID) { this.itemID = itemID; }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public void setItemAmount(String itemAmount) {
        this.itemAmount = itemAmount;
    }

    public void setItemDate(String itemDate) {
        this.itemDate = itemDate;
    }

    public void setItemMethod(String itemMethod) {
        this.itemMethod = itemMethod;
    }

    public Expense toExpense() {
        Calendar expenseDate = Calendar.getInstance();
        expenseDate.setTimeInMillis(Date.parse(getItemDate()));

        ExpenseCategory expenseCategory = ExpenseCategory.None;
        if (getItemMethod().equals("Need"))
            expenseCategory = ExpenseCategory.Needs;
        else if (getItemMethod().equals("Want"))
            expenseCategory = ExpenseCategory.Wants;
        else if (getItemMethod().equals("Savings"))
            expenseCategory = ExpenseCategory.Saves;
        else {
            throw new Error("Error, unknown expense type: " + getItemMethod());
        }

        return new Expense(
                getItemName(),
                Double.parseDouble(getItemAmount()),
                expenseDate,
                ExpenseSubcategory.defaultSubcategory(expenseCategory)
            );
    }
}
