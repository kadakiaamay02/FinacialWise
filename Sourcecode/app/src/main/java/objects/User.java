package objects;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Observable;

public final class User extends Observable {
    private String name;
    private long id;
    private String email;
    private List<Expense> expenses;
    private double monthlyIncome;
    private List<ExpenseSubcategory> subcategories;

    public User() {
        this("", 0, "", null, null);
    }

    public User(String name, long id, String email, List<Expense> expenses, List<ExpenseSubcategory> subcategories) {
        this.name = name;
        this.id = id;
        this.email = email;
        this.expenses = expenses != null ? expenses : new ArrayList<>();
        this.subcategories = subcategories != null ? subcategories : new ArrayList<>(Arrays.asList(ExpenseSubcategory.DEFAULT_ARRAY));
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        if(!this.name.equals(name)) {
            setChanged();
        }

        this.name = name;

        if(hasChanged()) {
            notifyObservers();
        }
    }

    public long getId() {
        return id;
    }
    public void setId(long id) {
        if(this.id != id) {
            setChanged();
        }

        this.id = id;

        if(hasChanged()) {
            notifyObservers();
        }
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        if(!this.email.equals(email)) {
            setChanged();
        }

        this.email = email;

        if(hasChanged()) {
            notifyObservers();
        }
    }

    public List<Expense> getExpenses() {
        return expenses;
    }
    public void setExpenses(List<Expense> expenses) {
        if(!this.expenses.equals(expenses)) {
            setChanged();
        }

        this.expenses = expenses;

        if(hasChanged()) {
            notifyObservers();
        }
    }

    public List<ExpenseSubcategory> getSubcategories() {
        return subcategories;
    }

    public double getMonthIncome() {
        return this.monthlyIncome;
    }

    public void setMonthlyIncome(double monthlyIncome) {
        if(this.monthlyIncome != monthlyIncome) {
            setChanged();
        }

        this.monthlyIncome = monthlyIncome;

        if(hasChanged()) {
            notifyObservers();
        }
    }

    public MonthAnalysis analyze(
            Calendar date,
            ExpenseSubcategory filterCategorySubcategory
    ) {
        List<ExpenseSubcategory> subSubcategories = new ArrayList<>();
        if(filterCategorySubcategory.getName().isEmpty()) {
            if(filterCategorySubcategory.getCategory() == ExpenseCategory.None) {
                subSubcategories.addAll(Arrays.asList(ExpenseSubcategory.DEFAULT_ARRAY));
            }
            else {
                for(ExpenseSubcategory subcategory : getSubcategories()) {
                    if(subcategory.getCategory() == filterCategorySubcategory.getCategory()) {
                        subSubcategories.add(subcategory);
                    }
                }
            }
        }

        ExpenseSubcategory[] subSubcategories_array = new ExpenseSubcategory[subSubcategories.size()];
        subSubcategories.toArray(subSubcategories_array);

        return MonthAnalysis.analyze(
                this.getMonthIncome(),
                date,
                getExpenses(),
                filterCategorySubcategory,
                subSubcategories_array
            );
    }
}
