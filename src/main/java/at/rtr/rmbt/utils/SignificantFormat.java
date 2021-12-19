package at.rtr.rmbt.utils;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.Locale;

public class SignificantFormat extends NumberFormat
{
    private static final long serialVersionUID = 1L;

    private final NumberFormat format;
    private final MathContext mathContext;

    public SignificantFormat(final int significantPlaces)
    {
        this(significantPlaces, Locale.getDefault());
    }

    public SignificantFormat(final int significantPlaces, final Locale locale)
    {
        format = NumberFormat.getNumberInstance(locale);
        mathContext = new MathContext(significantPlaces, RoundingMode.HALF_UP);
    }

    @Override
    public StringBuffer format(final double number, final StringBuffer toAppendTo, final FieldPosition pos)
    {
        return format.format(new BigDecimal(number, mathContext), toAppendTo, pos);
    }

    @Override
    public StringBuffer format(final long number, final StringBuffer toAppendTo, final FieldPosition pos)
    {
        return format.format(new BigDecimal(number, mathContext), toAppendTo, pos);
    }

    @Override
    public Number parse(final String text, final ParsePosition pos)
    {
        return format.parse(text, pos);
    }
}