package objects;

public final class ExpenseSubcategory {
    private String name;
    private ExpenseCategory category;
    private MonthlyAllowanceLimits monthlyAllowanceLimits;

    public ExpenseSubcategory() {
        this("", ExpenseCategory.None);
    }

    public ExpenseSubcategory(
            String name,
            ExpenseCategory category
    ) {
        this(name, category, new MonthlyAllowanceLimits());
    }

    public ExpenseSubcategory(
            String name,
            ExpenseCategory category,
            MonthlyAllowanceLimits monthlyAllowanceLimits
    ) {
        this.name = name;
        this.category = category;
        this.monthlyAllowanceLimits = monthlyAllowanceLimits;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public ExpenseCategory getCategory() {
        return category;
    }
    public void setCategory(ExpenseCategory category) {
        this.category = category;
    }

    public MonthlyAllowanceLimits getMonthlyAllowanceLimits() { return monthlyAllowanceLimits; }

    public String getDisplayName() {
        if(category == null) return "(None)";
        else if(this.name == null || this.name.isEmpty()) return category.name();
        else return category.name() + " (" + this.name + ")";
    }

    public static ExpenseSubcategory defaultSubcategory(ExpenseCategory category) {
        if(category == null)
            return DEFAULT_NONE;

        switch (category) {
            case None:
                return DEFAULT_NONE;
            case Needs:
                return DEFAULT_NEEDS;
            case Wants:
                return DEFAULT_WANTS;
            case Saves:
                return DEFAULT_SAVES;
            default:
                throw new IllegalArgumentException("Category must be none, needs, wants, or saves.");
        }
    }

    public static final ExpenseSubcategory DEFAULT_NONE = new ExpenseSubcategory();
    public static final ExpenseSubcategory DEFAULT_NEEDS = new ExpenseSubcategory("", ExpenseCategory.Needs, new MonthlyAllowanceLimits(Float.POSITIVE_INFINITY, 0.5f));
    public static final ExpenseSubcategory DEFAULT_WANTS = new ExpenseSubcategory("", ExpenseCategory.Wants, new MonthlyAllowanceLimits(Float.POSITIVE_INFINITY, 0.2f));
    public static final ExpenseSubcategory DEFAULT_SAVES = new ExpenseSubcategory("", ExpenseCategory.Saves, new MonthlyAllowanceLimits(Float.POSITIVE_INFINITY, 0.3f));
    public static final ExpenseSubcategory[] DEFAULT_ARRAY = new ExpenseSubcategory[] {
            DEFAULT_NEEDS,
            DEFAULT_WANTS,
            DEFAULT_SAVES
        };
}
