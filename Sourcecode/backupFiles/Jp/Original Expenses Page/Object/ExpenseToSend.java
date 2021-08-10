package objects;

import com.google.firebase.Timestamp;

public class ExpenseToSend {
    private double amount;
    private String userId;
    private Timestamp timeAdded;
    private String userName;
    private ExpenseCategory category;
    private String expenseDescription;


    public ExpenseToSend() { //Empty constructor needed for working with firestore

    }

    public ExpenseToSend(int amount, String userId, Timestamp timeAdded, String userName, ExpenseCategory category, String expenseDescription) {
        this.amount = amount;
        this.userId = userId;
        this.timeAdded = timeAdded;
        this.userName = userName;
        this.category = category;
        this.expenseDescription = expenseDescription;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Timestamp getTimeAdded() {
        return timeAdded;
    }

    public void setTimeAdded(Timestamp timeAdded) {
        this.timeAdded = timeAdded;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getExpenseDescription() {
        return expenseDescription;
    }

    public void setExpenseDescription(String expenseDescription) {
        this.expenseDescription = expenseDescription;
    }


    public ExpenseCategory getCategory() {
        return category;
    }

    public void setCategory(ExpenseCategory category) {
        this.category = category;
    }
}
