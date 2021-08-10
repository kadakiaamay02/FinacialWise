package objects;

import java.util.Calendar;
import java.util.List;

public class MonthAnalysis {
    private double monthlyIncome;
    private Calendar focusDayInMonth;
    private Calendar firstDayOfMonth;
    private int daysInMonth;
    private ExpenseSubcategory filterSubcategory;
    private double monthAllowance;
    private double monthExpenseTotal;
    private double monthAllowanceRemaining;
    private double[] dayAllowances;
    private double[] dayExpenseTotals;
    private double[] dayAllowancesRemaining;
    private double[] monthToDateAllowanceTotals;
    private double[] monthToDateExpenseTotals;
    private double[] monthToDateAllowanceRemainingTotals;
    private MonthAnalysis[] subcategoryAnalyses;

    private MonthAnalysis(
            double monthlyIncome,
            Calendar focusDayInMonth,
            Calendar firstDayOfMonth,
            int daysInMonth,
            ExpenseSubcategory filterSubcategory,
            double monthAllowance,
            double monthExpenseTotal,
            double monthAllowanceRemaining,
            double[] dayAllowances,
            double[] dayExpenseTotals,
            double[] dayAllowancesRemaining,
            double[] monthToDateAllowanceTotals,
            double[] monthToDateExpenseTotals,
            double[] monthToDateAllowanceRemainingTotals,
            MonthAnalysis[] subcategoryAnalyses
        ) {
        this.monthlyIncome = monthlyIncome;
        this.focusDayInMonth = focusDayInMonth;
        this.firstDayOfMonth = firstDayOfMonth;
        this.daysInMonth = daysInMonth;
        this.filterSubcategory = filterSubcategory;
        this.monthAllowance = monthAllowance;
        this.monthExpenseTotal = monthExpenseTotal;
        this.monthAllowanceRemaining = monthAllowanceRemaining;
        this.dayAllowances = dayAllowances;
        this.dayExpenseTotals = dayExpenseTotals;
        this.dayAllowancesRemaining = dayAllowancesRemaining;
        this.monthToDateAllowanceTotals = monthToDateAllowanceTotals;
        this.monthToDateExpenseTotals = monthToDateExpenseTotals;
        this.monthToDateAllowanceRemainingTotals = monthToDateAllowanceRemainingTotals;
        this.subcategoryAnalyses = subcategoryAnalyses;
    }

    public double getMonthlyIncome() {
        return monthlyIncome;
    }

    public Calendar getFocusDayInMonth() {
        return focusDayInMonth;
    }

    public Calendar getFirstDayOfMonth() {
        return firstDayOfMonth;
    }

    public int getDaysInMonth() {
        return daysInMonth;
    }

    public ExpenseSubcategory getFilterSubcategory() {
        return filterSubcategory;
    }

    public double getMonthAllowance() {
        return monthAllowance;
    }

    public double getMonthExpenseTotal() {
        return monthExpenseTotal;
    }

    public double getMonthAllowanceRemaining() {
        return monthAllowanceRemaining;
    }

    public double[] getDayAllowances() {
        return dayAllowances;
    }

    public double[] getDayExpenseTotals() {
        return dayExpenseTotals;
    }

    public double[] getDayAllowancesRemaining() {
        return dayAllowancesRemaining;
    }

    public double[] getMonthToDateAllowanceTotals() {
        return monthToDateAllowanceTotals;
    }

    public double[] getMonthToDateExpenseTotals() {
        return monthToDateExpenseTotals;
    }

    public double[] getMonthToDateAllowanceRemainingTotals() {
        return monthToDateAllowanceRemainingTotals;
    }

    public MonthAnalysis[] getSubcategoryAnalyses() {
        return subcategoryAnalyses;
    }

    /**
     * Calculates the daily allowances for each day of the month.
     * @param monthlyIncome The total income for the month.
     * @param focusDayInMonth the focus day of the month to analyze expenses & allowance for.
     * @param expenses the expenses to check for overspending to adjust daily allowances accordingly.
     * @param filterSubcategory the category/filterSubcategory to analyze expenses & allowance for.
     * @param subSubcategories the subcategories to analyze within the filter subcategory.
     * @return a MonthAnalysis for the given parameters.
     */
    public static MonthAnalysis analyze(
            double monthlyIncome,
            Calendar focusDayInMonth,
            List<Expense> expenses,
            ExpenseSubcategory filterSubcategory,
            ExpenseSubcategory[] subSubcategories
        ) {
        Calendar firstDayOfMonth = (Calendar) focusDayInMonth.clone();
        firstDayOfMonth.set(Calendar.DAY_OF_MONTH, 1);

        if(filterSubcategory == null) {
            filterSubcategory = ExpenseSubcategory.DEFAULT_NONE;
        }

        final int daysInMonth = firstDayOfMonth.getActualMaximum(Calendar.DAY_OF_MONTH);

        double monthlyAllowance;
        double monthlyAllowanceAccumulating = 0;
        double monthlyExpensesTotal = 0;
        double monthlyAllowanceRemaining;

        double[] dailyAllowances = new double[daysInMonth];
        double[] dailyExpenseTotals = new double[daysInMonth];
        double[] dailyAllowancesRemaining = new double[daysInMonth];

        double[] monthToDateAllowanceTotals = new double[daysInMonth];
        double[] monthToDateExpenseTotals = new double[daysInMonth];
        double[] monthToDateAllowancesRemaining = new double[daysInMonth];

        monthlyAllowance = filterSubcategory
                .getMonthlyAllowanceLimits()
                .calculateMonthlyAllowance(monthlyIncome);

        dailyAllowances[0] = monthlyAllowance / daysInMonth;

        for(int dayNumber = 1; dayNumber <= daysInMonth; dayNumber++) {
            int dayIndex = dayNumber - 1;

            Calendar date = (Calendar) firstDayOfMonth.clone();
            date.set(Calendar.DAY_OF_MONTH, dayNumber);

            double dayAllowance = dailyAllowances[dayIndex];
            double nextDayAllowance = dayAllowance;
            double dayExpensesTotal = 0;
            double dayAllowanceRemaining;

            Expense[] dayExpenses = Expense.search(
                    expenses,
                    null,
                    filterSubcategory.getCategory(),
                    filterSubcategory,
                    date,
                    date
                );
            for (Expense expense : dayExpenses) {
                dayExpensesTotal += expense.getAmount();
            }
            dailyExpenseTotals[dayIndex] = dayExpensesTotal;

            dayAllowanceRemaining = dayAllowance - dayExpensesTotal;
            if(dayAllowanceRemaining < 0) {
                dayAllowanceRemaining = 0;
            }
            dailyAllowancesRemaining[dayIndex] = dayAllowanceRemaining;

            if(dayExpensesTotal > nextDayAllowance) {
                double overspent = dayExpensesTotal - nextDayAllowance;
                int remainingDays = daysInMonth - dayNumber;
                double distributedOverspent = overspent / remainingDays;

                nextDayAllowance -= distributedOverspent;
                if(nextDayAllowance < 0) {
                    nextDayAllowance = 0;
                }
            }

            if(dayNumber != daysInMonth) {
                dailyAllowances[dayIndex + 1] = nextDayAllowance;
            }

            monthlyExpensesTotal += dayExpensesTotal;

            monthlyAllowanceAccumulating += Math.max(dayAllowance, dayExpensesTotal);
            if(monthlyAllowanceAccumulating > monthlyAllowance) {
                monthlyAllowanceAccumulating = monthlyAllowance;
            }

            monthToDateAllowanceTotals[dayIndex] = monthlyAllowanceAccumulating;
            monthToDateAllowancesRemaining[dayIndex] = monthlyAllowance - monthlyAllowanceAccumulating;
            monthToDateExpenseTotals[dayIndex] = monthlyExpensesTotal;
        }

        monthlyAllowanceRemaining = monthlyAllowance - monthlyExpensesTotal;
        if(monthlyAllowanceRemaining < 0) {
            monthlyAllowanceRemaining = 0;
        }

        MonthAnalysis[] subcategoryAnalyses = new MonthAnalysis[subSubcategories != null ? subSubcategories.length : 0];
        for(int i = 0; i < subcategoryAnalyses.length; i++) {
            subcategoryAnalyses[i] = analyze(
                    monthlyIncome,
                    focusDayInMonth,
                    expenses,
                    subSubcategories[i],
                    null
                );
        }

        return new MonthAnalysis(
                monthlyIncome,
                focusDayInMonth,
                firstDayOfMonth,
                daysInMonth,
                filterSubcategory,
                monthlyAllowance,
                monthlyExpensesTotal,
                monthlyAllowanceRemaining,
                dailyAllowances,
                dailyExpenseTotals,
                dailyAllowancesRemaining,
                monthToDateAllowanceTotals,
                monthToDateExpenseTotals,
                monthToDateAllowancesRemaining,
                subcategoryAnalyses
            );
    }
}
