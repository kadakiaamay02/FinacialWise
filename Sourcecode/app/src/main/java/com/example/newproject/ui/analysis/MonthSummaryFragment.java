package com.example.newproject.ui.analysis;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.newproject.R;
import com.example.newproject.databinding.FragmentAnalysisMonthSummaryBinding;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;

import objects.ExpenseCategory;
import objects.MonthAnalysis;

public class MonthSummaryFragment extends AnalysisSubfragment {
    private FragmentAnalysisMonthSummaryBinding binding;

    private TextView monthLabel;
    private TextView summaryLabel;
    private PieChart chart;

    private Dictionary<ExpenseCategory, Integer> analysis_category_colors = new Hashtable<ExpenseCategory, Integer>();
    private String congradulation_string;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentAnalysisMonthSummaryBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        this.monthLabel = binding.monthLabel;
        this.summaryLabel = binding.summaryLabel;

        this.chart = binding.chart;
        PieData pieData = new PieData();
        pieData.setDataSet(new PieDataSet(new ArrayList<>(), "Percent of month's expense amounts"));
        this.chart.setData(pieData);
        chart.getDescription().setEnabled(false);

        chart.setUsePercentValues(true);

        pieData.setValueTextSize(getResources().getDimension(R.dimen.analysis_pie_value));
        pieData.setValueTextColor(getResources().getColor(R.color.analysis_category_pie_text));

        analysis_category_colors.put(ExpenseCategory.None, Color.GRAY);
        analysis_category_colors.put(ExpenseCategory.Needs, getResources().getColor(R.color.analysis_category_needs));
        analysis_category_colors.put(ExpenseCategory.Wants, getResources().getColor(R.color.analysis_category_wants));
        analysis_category_colors.put(ExpenseCategory.Saves, getResources().getColor(R.color.analysis_category_saves));

        congradulation_string = getString(R.string.analysis_congradulations_msg_50_20_30);

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    protected void updateDisplayImplementation(MonthAnalysis monthAnalysis) {
        if(monthAnalysis == null) {
            monthLabel.setText("Select a month");
            summaryLabel.setText("\n");
            chart.getData().getDataSet().clear();
        }
        else {
            monthLabel.setText(monthAnalysis.getFocusDayInMonth().getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault()));

            StringBuilder summaryBuilder = new StringBuilder();

            summaryBuilder.append("Total income: ");
            summaryBuilder.append(getCurrencyValueFormatter().getFormattedValue(monthAnalysis.getMonthlyIncome()));
            summaryBuilder.append('\n');

            summaryBuilder.append("Total expense: ");
            summaryBuilder.append(getCurrencyValueFormatter().getFormattedValue(monthAnalysis.getMonthExpenseTotal()));

            Dictionary<ExpenseCategory, Double> category_amounts_percentages = new Hashtable<>();

            List<Integer> colors_order = new ArrayList<>();

            if(monthAnalysis.getFilterSubcategory().getCategory() == ExpenseCategory.None) {
                chart.getData().getDataSet().clear();
                for (MonthAnalysis subAnalysis: monthAnalysis.getSubcategoryAnalyses()) {
                    if (subAnalysis.getMonthExpenseTotal() > 0) {
                        PieEntry entry = new PieEntry((float) subAnalysis.getMonthExpenseTotal());
                        entry.setLabel(subAnalysis.getFilterSubcategory().getDisplayName());
                        chart.getData().getDataSet().addEntry(entry);

                        colors_order.add(analysis_category_colors.get(subAnalysis.getFilterSubcategory().getCategory()));
                        category_amounts_percentages.put(subAnalysis.getFilterSubcategory().getCategory(), subAnalysis.getMonthExpenseTotal() / monthAnalysis.getMonthExpenseTotal());
                    }
                }

                ((PieDataSet)chart.getData().getDataSet()).setColors(colors_order);

                if((category_amounts_percentages.get(ExpenseCategory.Needs) != null && category_amounts_percentages.get(ExpenseCategory.Needs) >= 0.5) &&
                    (category_amounts_percentages.get(ExpenseCategory.Wants) == null || category_amounts_percentages.get(ExpenseCategory.Wants) <= 0.2) &&
                    (category_amounts_percentages.get(ExpenseCategory.Saves) != null && category_amounts_percentages.get(ExpenseCategory.Saves) >= 0.3)) {
                    summaryBuilder.append(congradulation_string);
                }

                summaryLabel.setText(summaryBuilder.toString());
            }
            else {
                chart.getData().getDataSet().clear();
            }

            chart.notifyDataSetChanged();
            chart.postInvalidate();
        }
    }
}