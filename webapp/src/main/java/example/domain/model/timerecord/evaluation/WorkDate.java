package example.domain.model.timerecord.evaluation;

import example.domain.type.date.Date;
import example.domain.type.date.DayOfWeek;
import example.domain.type.date.WeekOfMonth;

import javax.validation.Valid;

/**
 * 勤務日付
 */
public class WorkDate {

    @Valid
    Date value;

    @Deprecated
    public WorkDate() {
    }

    public WorkDate(Date date) {
        value = date;
    }

    public WorkDate(String value) {
        this(new Date(value));
    }

    public Date value() {
        return value;
    }

    public int dayOfMonth() {
        return value.dayOfMonth();
    }

    public DayOfWeek dayOfWeek() {
        return value.dayOfWeek();
    }

    public WeekOfMonth weekOfMonth() {
        return value.weekOfMonth();
    }

    public boolean hasSameValue(WorkDate other) {
        return value.hasSameValue(other.value);
    }

    @Override
    public String toString() {
        return value.toString();
    }

    public Date toDate() {
        return value;
    }
}
