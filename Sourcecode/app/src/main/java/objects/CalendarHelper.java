package objects;

import java.util.Calendar;

public class CalendarHelper {
    /**
     * Tests whether the given date is within a range of two dates.
     * @param date the date to test
     * @param start the start of the range, including the start date
     * @param end the end of the range, including the end date
     * @return
     */
    public static boolean isDateInRange(Calendar date, Calendar start, Calendar end) {
        int date_year = date.get(Calendar.YEAR),
            start_year = start.get(Calendar.YEAR),
            end_year = start.get(Calendar.YEAR);
        int date_dayOfYear = date.get(Calendar.DAY_OF_YEAR),
            start_dayOfYear = date.get(Calendar.DAY_OF_YEAR),
            end_dayOfYear = date.get(Calendar.DAY_OF_YEAR);

        assert (start_year < end_year) || (start_year == end_year && start_dayOfYear <= end_dayOfYear);

        if(date_year < start_year || date_year > end_year)
            return false;

        if(date_year == start_year && date_dayOfYear < start_dayOfYear)
            return false;

        if(date_year == end_year && date_dayOfYear > end_dayOfYear)
            return false;

        return true;
    }

    public static boolean isDateOnOrAfter(Calendar before, Calendar onOrAfter) {
        int before_year = before.get(Calendar.YEAR),
            after_year = onOrAfter.get(Calendar.YEAR);

        int before_dayOfYear = before.get(Calendar.DAY_OF_YEAR),
            after_dayOfyear = onOrAfter.get(Calendar.DAY_OF_YEAR);

        if(before_year < after_year)
            return true;
        else if(before_year > after_year)
            return false;
        else {
            return before_dayOfYear <= after_dayOfyear;
        }
    }
}
