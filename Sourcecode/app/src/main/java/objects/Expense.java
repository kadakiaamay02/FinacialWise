package objects;

import java.util.ArrayList;
import java.util.List;
import java.util.Calendar;

public class Expense {
    private String name;
    private double amount;
    private Calendar date;
    private ExpenseSubcategory subcategory;

    public Expense() {
        this("", 0, ExpenseCategory.None);
    }

    public Expense(
            String name,
            double amount,
            ExpenseCategory category
    ) {
        this(name, amount, ExpenseSubcategory.defaultSubcategory(category));
    }

    public Expense(
            String name,
            double amount,
            ExpenseSubcategory subcategory
    ) {
        this(name, amount, Calendar.getInstance(), subcategory);
    }

    public Expense(
            String name,
            double amount,
            Calendar date,
            ExpenseSubcategory subcategory
    ) {
        this.name = name;
        this.amount = amount;
        this.date = date;
        this.subcategory = subcategory;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public double getAmount() {
        return amount;
    }
    public void setAmount(double amount) {
        this.amount = amount;
    }

    public Calendar getDate() { return date; }
    public void setDate(Calendar date) { this.date = date; }

    public ExpenseSubcategory getSubcategory() {
        return subcategory;
    }
    public void setSubcategory(ExpenseSubcategory subcategory) {
        this.subcategory = subcategory;
    }

    /**
     * Determines whether the expense occurred anytime within the start and end range, inclusive.
     * @param start the inclusive start date of search. If equals null, then there is no start date.
     * @param end the inclusive end date of search. If equals null, then there is no end date.
     * @return Whether or not this expense happened anytime within the start and end date range.
     */
    public boolean didHappenWithin(Calendar start, Calendar end) {
        return (
                (start == null || CalendarHelper.isDateOnOrAfter(start, getDate())) &&
                        (end == null || CalendarHelper.isDateOnOrAfter(getDate(), end))
        );
    }

    /**
     * Searches through the given expenses for those matching the given criteria.
     * Note: not all criteria need to be supplied; they can just be replaced with null or their
     * default value.
     * @param expenses The expenses to search through.
     * @param name If this argument is not null, then all expenses returned must contain
     *             this argument in their names.
     * @param category If this argument is not ExpenseCategory.None, then all expenses returned
     *                 must be within this category.
     * @param subcategory If this argument is not null, then all expenses returned must have
     *                    this argument equal to their subcategories. Also, if this argument is
     *                    not null, then this argument's category must equal the previous
     *                    category argument.
     * @param start If this argument is not null, then all expenses returned must happen at least
     *              once on or after this argument.
     * @param end If this argument is not null, then all expenses returned must happen at least
     *            once on or before this argument.
     * @return an array of Expense instances that match the given criteria.
     */
    public static Expense[] search(
            List<Expense> expenses,
            String name,
            ExpenseCategory category,
            ExpenseSubcategory subcategory,
            Calendar start,
            Calendar end
        ) {
        if(subcategory != null && subcategory.getCategory() != category) {
            throw new IllegalArgumentException("If the subcategory is supplied, its category must" +
                    "equal the given category argument.");
        }

        List<Expense> filteredList = new ArrayList<Expense>();

        for (Expense expense : expenses) {
            if(name != null) {
                if(!expense.name.contains(name))
                    continue;
            }

            if(category != ExpenseCategory.None) {
                if(expense.subcategory.getCategory() != category)
                    continue;
            }

            if(subcategory != null) {
                if(expense.subcategory != subcategory && subcategory.getName() != "")
                    continue;
            }

            if(start != null || end != null) {
                if(!expense.didHappenWithin(start, end))
                    continue;
            }

            filteredList.add(expense);
        }

        Expense[] filteredArray = new Expense[filteredList.size()];
        filteredList.toArray(filteredArray);
        return filteredArray;
    }
}
