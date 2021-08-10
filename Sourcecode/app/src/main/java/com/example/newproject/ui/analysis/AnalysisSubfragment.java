package com.example.newproject.ui.analysis;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.fragment.app.Fragment;

import java.util.Calendar;
import java.util.Observable;

import objects.*;
import utilty.UserLoginManager;

public abstract class AnalysisSubfragment extends Fragment {
    private Observer<Calendar> analysisViewModel_observer_date =
            new Observer<Calendar>() {
                @Override
                public void onChanged(Calendar calendar) {
                    updateDisplay();
                }
            };
    private Observer<ExpenseSubcategory> analysisViewModel_observer_categorySubcategoryFilter =
            new Observer<ExpenseSubcategory>() {
                @Override
                public void onChanged(ExpenseSubcategory expenseSubcategory) {
                    updateDisplay();
                }
            };
    private java.util.Observer user_observer = new java.util.Observer() {
        @Override
        public void update(Observable observable, Object o) {
                updateDisplay();
            }
        };

    private MutableLiveData<ExpenseSubcategory> filterCategorySubcategory = new MutableLiveData<>(ExpenseSubcategory.DEFAULT_NONE);

    private AnalysisViewModel analysisViewModel;
    private boolean observer_enabled = false;

    private CurrencyValueFormatter currencyValueFormatter = CurrencyValueFormatter.localInstance;

    public AnalysisViewModel getAnalysisViewModel() { return analysisViewModel; }

    public void setAnalysisViewModel(AnalysisViewModel analysisViewModel) {
        if(observer_enabled) {
            if (this.analysisViewModel != null) {
                observer_disable();
            }

            if (analysisViewModel != null) {
                this.analysisViewModel = null;
                observer_enable();
            }
        }

        this.analysisViewModel = analysisViewModel;
    }

    public LiveData<ExpenseSubcategory> getFilterCategorySubcategory() {
        return filterCategorySubcategory;
    }

    public void setFilterCategorySubcategory(ExpenseSubcategory filterCategorySubcategory) {
        this.filterCategorySubcategory.setValue(filterCategorySubcategory);
    }

    private void observer_enable() {
        LifecycleOwner lifecycleOwner = getViewLifecycleOwner();
        analysisViewModel.getDate()
            .observe(lifecycleOwner, analysisViewModel_observer_date);
        getFilterCategorySubcategory()
            .observe(lifecycleOwner, analysisViewModel_observer_categorySubcategoryFilter);
        UserLoginManager.getInstance().getLoggedInUser().addObserver(user_observer);
    }

    private void observer_disable() {
        analysisViewModel.getDate()
            .removeObserver(analysisViewModel_observer_date);
        getFilterCategorySubcategory()
            .removeObserver(analysisViewModel_observer_categorySubcategoryFilter);
        UserLoginManager.getInstance().getLoggedInUser().deleteObserver(user_observer);
    }

    public CurrencyValueFormatter getCurrencyValueFormatter() {
        return currencyValueFormatter;
    }

    public void setCurrencyValueFormatter(CurrencyValueFormatter currencyValueFormatter) {
        this.currencyValueFormatter = currencyValueFormatter;
    }

    public void updateDisplay() {
        if(analysisViewModel != null) {
            updateDisplayImplementation(analysisViewModel.getMonthAnalysis(filterCategorySubcategory.getValue()));
        }
        else {
            updateDisplayImplementation(null);
        }
    }

    @Override
    public void onStart() {
        this.observer_enabled = true;
        if(this.analysisViewModel != null) {
            observer_enable();
        }
        updateDisplay();

        super.onStart();
    }

    protected abstract void updateDisplayImplementation(MonthAnalysis monthAnalysis);
}
