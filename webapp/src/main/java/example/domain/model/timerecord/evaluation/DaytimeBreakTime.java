package example.domain.model.timerecord.evaluation;

import example.domain.type.time.Minute;
import example.domain.type.time.QuarterHour;

import javax.validation.Valid;

/**
 * 日中休憩時間
 */
public class DaytimeBreakTime {

    @Valid
    Minute value;

    @Deprecated
    DaytimeBreakTime() {
    }

    public DaytimeBreakTime(String value) {
        this(Minute.of(value));
    }

    public DaytimeBreakTime(Minute value) {
        this.value = value;
    }

    public Minute minute() {
        return value;
    }

    @Override
    public String toString() {
        return value.toString();
    }

    public QuarterHour quarterHourRoundUp() {
        return value.quarterHourRoundUp();
    }
}
