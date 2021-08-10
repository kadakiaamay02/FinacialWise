package com.example.newproject.ui.expenses;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ExpensesViewModel extends ViewModel {
    private MutableLiveData<String> mText;

    public ExpensesViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is the expenses fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}