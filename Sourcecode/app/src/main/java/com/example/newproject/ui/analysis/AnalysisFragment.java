package com.example.newproject.ui.analysis;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.newproject.R;
import com.example.newproject.databinding.FragmentAnalysisBinding;

import java.text.DateFormat;
import java.util.Calendar;

import objects.ExpenseSubcategory;
import objects.User;
import utilty.UserLoginManager;

/**
 * A placeholder fragment containing a simple view.
 */
public class AnalysisFragment extends Fragment {
    private static final String ARG_ANALYSIS_DATE_TIME_MS = "analysis_date_time_ms";

    private AnalysisViewModel analysisViewModel;
    private FragmentAnalysisBinding binding;

    private TextView date_label;
    private Button btnChangeDate;

    private LinearLayout analysisCardsView;

    public static AnalysisFragment newInstance(Calendar date) {
        AnalysisFragment fragment = new AnalysisFragment();
        Bundle bundle = new Bundle();
        bundle.putLong(ARG_ANALYSIS_DATE_TIME_MS, date.getTimeInMillis());
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        analysisViewModel = new ViewModelProvider(this).get(AnalysisViewModel.class);
        Calendar date = Calendar.getInstance();

        if (getArguments() != null) {
            date.setTimeInMillis(getArguments().getLong(ARG_ANALYSIS_DATE_TIME_MS));
        }

        analysisViewModel.setDate(date);
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        binding = FragmentAnalysisBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        UserLoginManager.getInstance().clearCache();

        this.date_label = binding.dateLabel;
        this.btnChangeDate = binding.btnChangeDate;

        this.btnChangeDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar currentDate = analysisViewModel.getDate().getValue();

                DatePickerDialog dialog = new DatePickerDialog(getContext());
                dialog.updateDate(
                        currentDate.get(Calendar.YEAR),
                        currentDate.get(Calendar.MONTH),
                        currentDate.get(Calendar.DAY_OF_MONTH)
                    );

                dialog.setOnDateSetListener(new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
                        Calendar newDate = (Calendar)analysisViewModel.getDate().getValue().clone();
                        newDate.set(year, month, dayOfMonth);
                        analysisViewModel.setDate(newDate);
                    }
                });

                dialog.show();
            }
        });

        analysisViewModel.getDate().observe(getViewLifecycleOwner(), new Observer<Calendar>() {
            @Override
            public void onChanged(Calendar calendar) {
                date_label.setText(DateFormat.getDateInstance().format(calendar.getTime()));
            }
        });

        this.analysisCardsView = binding.analysisCardsView;
        addAnalysisCard(new MonthSummaryFragment());
        addAnalysisCard(new CategoryDetailsFragment());
        addAnalysisCard(new CategoryDetailsFragment(ExpenseSubcategory.DEFAULT_NEEDS));
        addAnalysisCard(new CategoryDetailsFragment(ExpenseSubcategory.DEFAULT_WANTS));
        addAnalysisCard(new CategoryDetailsFragment(ExpenseSubcategory.DEFAULT_SAVES));

        return root;
    }

    private void addAnalysisCard(AnalysisSubfragment subfragment) {
        CardView cardView = new CardView(analysisCardsView.getContext());
        cardView.setId(100000 + analysisCardsView.getChildCount());
        cardView.setRadius(14f);
        cardView.setCardElevation(10f);
        cardView.setContentPadding(
                getResources().getDimensionPixelSize(R.dimen.analysis_card_padding_x),
                getResources().getDimensionPixelSize(R.dimen.analysis_card_padding_y),
                getResources().getDimensionPixelSize(R.dimen.analysis_card_padding_x),
                getResources().getDimensionPixelSize(R.dimen.analysis_card_padding_y)
            );

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(0, 0, 0, getResources().getDimensionPixelSize(R.dimen.analysis_card_margin_y));
        cardView.setLayoutParams(layoutParams);

        analysisCardsView.addView(cardView);

        getChildFragmentManager().beginTransaction().add(cardView.getId(), subfragment).commit();
        subfragment.setAnalysisViewModel(analysisViewModel);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}