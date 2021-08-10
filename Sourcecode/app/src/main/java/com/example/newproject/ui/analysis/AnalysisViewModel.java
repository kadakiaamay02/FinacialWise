package com.example.newproject.ui.analysis;

import androidx.arch.core.util.Function;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import java.time.Month;
import java.util.Calendar;

import objects.ExpenseSubcategory;
import objects.MonthAnalysis;
import objects.User;
import utilty.ApiLogin;
import utilty.UserLoginManager;

public class AnalysisViewModel extends ViewModel {
    private MutableLiveData<Integer> mIndex = new MutableLiveData<>();
    private MutableLiveData<Calendar> mDate = new MutableLiveData<>(Calendar.getInstance());

    public void setDate(Calendar date) {
        mDate.setValue(date);
    }
    public LiveData<Calendar> getDate() {
        return mDate;
    }

    public MonthAnalysis getMonthAnalysis(ExpenseSubcategory filterCategorySubcategory) {
        Calendar date = getDate().getValue();

        if(date == null) {
            return null;
        }

        User user = UserLoginManager.getInstance().getLoggedInUser();

        return user.analyze(
                date,
                filterCategorySubcategory
            );
    }
}