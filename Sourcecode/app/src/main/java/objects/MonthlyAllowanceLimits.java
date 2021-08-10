package objects;

public class MonthlyAllowanceLimits {
    /**
     * The maximum amount that monthly allowance can be.
     */
    private double maxAmount;

    /**
     * The maximum fraction of monthly income that can be used for monthly allowance;
     * 0 to 1 inclusive.
     */
    private double maxFraction;

    public MonthlyAllowanceLimits() {
        this(Double.POSITIVE_INFINITY, 1);
    }

    public  MonthlyAllowanceLimits(
            double maxAmount,
            double maxFraction
    ) {
        setMaxAmount(maxAmount);
        setMaxFraction(maxFraction);
    }

    /**
     * Gets the maximum amount that monthly allowance can be.
     */
    public double getMaxAmount() {
        return maxAmount;
    }

    /**
     * Sets the maximum amount that monthly allowance can be.
     */
    public void setMaxAmount(double maxAmount) {
        if(maxAmount < 0 || Double.isNaN(maxAmount)) {
            throw new IllegalArgumentException("maxAmount must be greater than or equal to 0.");
        }

        this.maxAmount = maxAmount;
    }


    /**
     * Gets the maximum fraction of monthly income that can be used for monthly allowance;
     * 0 to 1 inclusive.
     */
    public double getMaxFraction() {
        return maxFraction;
    }


    /**
     * Sets the maximum fraction of monthly income that can be used for monthly allowance;
     * 0 to 1 inclusive
     */
    public void setMaxFraction(double maxFraction) {
        if(maxFraction < 0 || maxFraction > 1 || Double.isNaN(maxAmount)) {
            throw new IllegalArgumentException("maxFraction must be between 0 and 1 inclusive.");
        }

        this.maxFraction = maxFraction;
    }

    public double calculateMonthlyAllowance(double monthlyIncome) {
        return Math.min(monthlyIncome * getMaxFraction(), getMaxAmount());
    }
}
