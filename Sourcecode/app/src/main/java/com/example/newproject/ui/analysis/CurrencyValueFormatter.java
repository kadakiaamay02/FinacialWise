package com.example.newproject.ui.analysis;

import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.DecimalFormat;
import java.util.Currency;
import java.util.Locale;

public class CurrencyValueFormatter extends ValueFormatter {
    Currency currency;

    DecimalFormat format;

    private static DecimalFormat wholeFormat = new DecimalFormat("0");

    public CurrencyValueFormatter() {
        this(Currency.getInstance(Locale.getDefault()));
    }

    public CurrencyValueFormatter(Currency currency) {
        this.currency = currency;

        updateFormat();
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;

        updateFormat();
    }

    private void updateFormat() {
        StringBuilder builder = new StringBuilder();
        builder.append("0.");
        for (int i = 0; i < getCurrency().getDefaultFractionDigits(); i++) {
            builder.append('0');
        }

        format = new DecimalFormat(builder.toString());
    }

    @Override
    public String getFormattedValue(float value) {
        if(value >= 1_000_000) {
            return currency.getSymbol() + wholeFormat.format(value / 1_000_000) + "M";
        }
        else if(value >= 1_000) {
            return currency.getSymbol() + wholeFormat.format(value / 1_000) + "k";
        }

        return currency.getSymbol() + format.format(value);
    }

    public String getFormattedValue(double value) {
        return getFormattedValue((float)value);
    }

    public static CurrencyValueFormatter localInstance = new CurrencyValueFormatter();
}
