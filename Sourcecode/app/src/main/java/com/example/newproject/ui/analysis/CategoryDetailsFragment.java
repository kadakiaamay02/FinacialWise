package com.example.newproject.ui.analysis;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.example.newproject.R;
import com.example.newproject.databinding.FragmentAnalysisCategoryDetailsBinding;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.BarLineChartBase;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.IDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import org.jetbrains.annotations.NotNull;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.function.Function;

import objects.*;

public class CategoryDetailsFragment extends AnalysisSubfragment {
    private static final String SERIES_PLANNED = "Planned";
    private static final String SERIES_ACTUAL = "Actual";

    private MutableLiveData<DisplayMode> selectedDisplayMode = new MutableLiveData<>(DisplayMode.Individual);

    private enum DisplayMode {
        Individual,
        Cumulative
    }

    private FragmentAnalysisCategoryDetailsBinding binding;

    private TextView header_label;
    private TextView summary_label;
    private CheckBox option_cumulative;
    private BarChart chart_individual;
    private LineChart chart_cumulative;

    public CategoryDetailsFragment() {
        this(ExpenseSubcategory.DEFAULT_NONE);
    }

    public CategoryDetailsFragment(ExpenseSubcategory subcategory) {
        this.setFilterCategorySubcategory(subcategory);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentAnalysisCategoryDetailsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        this.header_label = binding.headerLabel;
        this.summary_label = binding.summaryLabel;
        this.option_cumulative = binding.optionCumulative;
        this.chart_individual = binding.chartIndividual;
        this.chart_cumulative = binding.chartCumulative;

        configureChart(chart_individual);
        configureChart(chart_cumulative);
        configureChart_individual();
        configureChart_cumulative();

        option_cumulative.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(option_cumulative.isChecked()) {
                    selectedDisplayMode.setValue(DisplayMode.Cumulative);
                }
                else {
                    selectedDisplayMode.setValue(DisplayMode.Individual);
                }
            }
        });

        selectedDisplayMode.observe(getViewLifecycleOwner(), new Observer<DisplayMode>() {
            @Override
            public void onChanged(DisplayMode displayMode) {
                BarLineChartBase chart_active, chart_inactive;

                switch (displayMode) {
                    case Individual:
                        option_cumulative.setChecked(false);
                        chart_active = chart_individual;
                        chart_inactive = chart_cumulative;
                        break;

                    case Cumulative:
                        option_cumulative.setChecked(true);
                        chart_active = chart_cumulative;
                        chart_inactive = chart_individual;

                        break;

                    default:
                        throw new Error("Invalid display mode");
                }

                updateDisplay();

                chart_inactive.setVisibility(View.GONE);
                chart_active.setVisibility(View.VISIBLE);

                chart_active.resetZoom();
                chart_active.forceLayout();
                chart_active.computeScroll();
                chart_active.postInvalidate();

                chart_active.post(new Runnable() {
                    @Override
                    public void run() {
                        chart_active.forceLayout();
                        chart_active.postInvalidate();
                    }
                });
            }
        });

        updateDisplay();

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void configureChart(BarLineChartBase chart) {
        chart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                int dayNumber = (int)e.getX() + 1;

                AnalysisViewModel analysisViewModel = getAnalysisViewModel();
                Calendar currentDate = (Calendar)analysisViewModel.getDate().getValue().clone();
                if(currentDate.get(Calendar.DAY_OF_MONTH) != dayNumber) {
                    currentDate.set(Calendar.DAY_OF_MONTH, dayNumber);
                    analysisViewModel.setDate(currentDate);
                }
            }

            @Override
            public void onNothingSelected() {
            }
        });

        getAnalysisViewModel().getDate().observe(
                getViewLifecycleOwner(),
                new Observer<Calendar>() {
                    @Override
                    public void onChanged(Calendar newDate) {
                        int dayNumber = newDate.get(Calendar.DAY_OF_MONTH);

                        if(chart.getData().getDataSets().size() > 0) {
                            chart.highlightValue(dayNumber - 1, 0);
                        }
                    }
                }
            );

        chart.getDescription().setEnabled(false);

        chart.setDrawGridBackground(false);
        chart.getLegend().setEnabled(true);

        YAxis yaxis = chart.getAxisLeft();
        yaxis.setAxisMinimum(0);
        yaxis.setLabelCount(10);
        yaxis.setSpaceTop(15f);
        yaxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        yaxis.setValueFormatter(CurrencyValueFormatter.localInstance);
        yaxis.setTextColor(Color.BLACK); //TODO: need to change to use a resource
        yaxis.setEnabled(true);

        chart.getAxisRight().setEnabled(false);

        chart.setAutoScaleMinMaxEnabled(true);

        XAxis xaxis = chart.getXAxis();
        xaxis.setEnabled(true);
        xaxis.setPosition(XAxis.XAxisPosition.BOTTOM);
    }

    private void configureChart_individual() {
        BarChart chart = chart_individual;

        BarDataSet series_planned = new BarDataSet(new ArrayList<>(), SERIES_PLANNED);
        BarDataSet series_actual = new BarDataSet(new ArrayList<>(), SERIES_ACTUAL);

        series_planned.setColor(getResources().getColor(R.color.analysis_planned_vs_actual_chart_individual_series_planned));
        series_actual.setColor(getResources().getColor(R.color.analysis_planned_vs_actual_chart_individual_series_actual));

        series_planned.setDrawValues(false);
        series_actual.setDrawValues(false);

        chart.setData(new BarData());
        chart.getData().addDataSet(series_planned);
        chart.getData().addDataSet(series_actual);
    }

    private void configureChart_cumulative() {
        LineChart chart = chart_cumulative;

        LineDataSet series_planned = new LineDataSet(new ArrayList<>(), SERIES_PLANNED);
        LineDataSet series_actual = new LineDataSet(new ArrayList<>(), SERIES_ACTUAL);
        series_planned.setColor(getResources().getColor(R.color.analysis_planned_vs_actual_chart_individual_series_planned));
        series_actual.setColor(getResources().getColor(R.color.analysis_planned_vs_actual_chart_individual_series_actual));
        series_planned.setHighLightColor(getResources().getColor(R.color.analysis_planned_vs_actual_chart_individual_series_planned));
        series_actual.setHighLightColor(getResources().getColor(R.color.analysis_planned_vs_actual_chart_individual_series_actual));
        series_planned.setCircleColor(getResources().getColor(R.color.analysis_planned_vs_actual_chart_individual_series_planned));
        series_actual.setCircleColor(getResources().getColor(R.color.analysis_planned_vs_actual_chart_individual_series_actual));

        series_planned.setDrawValues(false);
        series_actual.setDrawValues(false);

        chart.setData(new LineData());
        chart.getData().addDataSet(series_planned);
        chart.getData().addDataSet(series_actual);
    }

    @Override
    protected void updateDisplayImplementation(MonthAnalysis monthAnalysis) {
        BarLineChartBase activeChart;
        Function<Integer, Entry> entryFactory;

        double[] series_planned_values, series_actual_values;
        boolean[] series_overspent = new boolean[monthAnalysis.getDaysInMonth()];

        switch (selectedDisplayMode.getValue()) {
            default:
            case Individual:
                activeChart = chart_individual;
                entryFactory = (Integer day) -> new BarEntry(day, 0);

                series_planned_values = monthAnalysis.getDayAllowances();
                series_actual_values = monthAnalysis.getDayExpenseTotals();

                for (int i = 0; i < series_overspent.length; i++) {
                    series_overspent[i] = series_actual_values[i] > series_planned_values[i];
                }

                break;

            case Cumulative:
                activeChart = chart_cumulative;
                entryFactory = (Integer day) -> new Entry(day, 0);

                series_planned_values = monthAnalysis.getMonthToDateAllowanceTotals();
                series_actual_values = monthAnalysis.getMonthToDateExpenseTotals();

                for (int i = 0; i < series_overspent.length; i++) {
                    series_overspent[i] = series_actual_values[i] > series_planned_values[i];
                    if(series_overspent[i]) {
                        series_planned_values[i] = Math.min(monthAnalysis.getMonthAllowance(), series_actual_values[i]);
                    }
                }

                break;
        }

        if(monthAnalysis == null) {
            header_label.setText("(None selected)");
            summary_label.setText("Loading...");

            resetChartData_series(activeChart, SERIES_PLANNED, new double[0], entryFactory);
            resetChartData_series(activeChart, SERIES_ACTUAL, new double[0], entryFactory);
        }
        else {
            if (monthAnalysis.getFilterSubcategory().getCategory() == ExpenseCategory.None) {
                header_label.setText("All expenses");
            }
            else {
                header_label.setText(monthAnalysis.getFilterSubcategory().getDisplayName());
            }

            String month = monthAnalysis.getFocusDayInMonth().getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault());
            StringBuilder report = new StringBuilder();
            reportMonthAnalysis(monthAnalysis, report);
            summary_label.setText(report.toString());

            resetChartData_series(activeChart, SERIES_PLANNED, series_planned_values, entryFactory);
            resetChartData_series(activeChart, SERIES_ACTUAL, series_actual_values, entryFactory);

            activeChart.notifyDataSetChanged();
            activeChart.postInvalidate();
        }
    }

    private void reportMonthAnalysis(MonthAnalysis monthAnalysis, StringBuilder report) {
        String month = monthAnalysis.getFocusDayInMonth().getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault());
        String dayInMonthString = DateFormat.getDateInstance().format(monthAnalysis.getFocusDayInMonth().getTime());
        int dayInMonthIndex = monthAnalysis.getFocusDayInMonth().get(Calendar.DAY_OF_MONTH) - 1;

        switch(selectedDisplayMode.getValue()) {
            case Individual:
                report.append("Initial allowance for " + dayInMonthString + ": ");
                report.append(getCurrencyValueFormatter().getFormattedValue(monthAnalysis.getDayAllowances()[dayInMonthIndex]));
                report.append('\n');
                report.append("Total spent for " + dayInMonthString + ": ");
                report.append(getCurrencyValueFormatter().getFormattedValue(monthAnalysis.getDayExpenseTotals()[dayInMonthIndex]));
                report.append('\n');
                report.append("Remaining allowance for " + dayInMonthString + ": ");
                report.append(getCurrencyValueFormatter().getFormattedValue(monthAnalysis.getDayAllowancesRemaining()[dayInMonthIndex]));

                break;

            case Cumulative:
                report.append("Total allowance for " + month + ": ");
                report.append(getCurrencyValueFormatter().getFormattedValue(monthAnalysis.getMonthAllowance()));
                report.append('\n');
                report.append("Total spent as of " + dayInMonthString + " so far: ");
                report.append(getCurrencyValueFormatter().getFormattedValue(monthAnalysis.getMonthToDateExpenseTotals()[dayInMonthIndex]));

                break;
        }
    }

    private void resetChartData_series(
            @NotNull BarLineChartBase chart,
            String seriesName,
            @NotNull double[] values,
            Function<Integer, Entry> entryFactory
        ) {
        IDataSet dataSet = chart.getData().getDataSetByLabel(seriesName, false);

        for(int i = 0; i < Math.min(values.length, dataSet.getEntryCount()); i++) {
            dataSet.getEntryForIndex(i).setY((float)values[i]);
        }

        if(values.length > dataSet.getEntryCount()) {
            for(int i = dataSet.getEntryCount(); i < values.length; i++) {
                Entry newEntry = entryFactory.apply(i);
                dataSet.addEntryOrdered(newEntry);
                newEntry.setY((float)values[i]);

                //TODO: warn if overspent
                //newEntry.setIcon(getResources().getDrawable(R.drawable.usericon));
            }
        } else if (values.length < dataSet.getEntryCount()) {
            for (int i = dataSet.getEntryCount() - 1; i >= values.length; i--) {
                dataSet.removeEntry(i);
            }
        }
    }
}