package example.presentation.controller.timerecord;

import example.domain.model.timerecord.evaluation.ActualWorkDateTime;
import example.domain.model.timerecord.evaluation.DaytimeBreakTime;
import example.domain.model.timerecord.evaluation.NightBreakTime;
import example.domain.model.timerecord.timefact.EndDateTime;
import example.domain.model.timerecord.timefact.StartDateTime;
import example.domain.model.timerecord.timefact.WorkRange;
import example.domain.type.time.Minute;

import javax.validation.Valid;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;

public class ActualWorkDateTimeForm {
    @NotNull
    @Valid
    WorkRangeForm workRangeForm;

    @NotNull
    @Valid
    DaytimeBreakTime daytimeBreakTime;

    @NotNull
    @Valid
    NightBreakTime nightBreakTime;

    public ActualWorkDateTimeForm() {
    }

    public ActualWorkDateTimeForm(WorkRangeForm workRangeForm, DaytimeBreakTime daytimeBreakTime, NightBreakTime nightBreakTime) {
        this.workRangeForm = workRangeForm;
        this.daytimeBreakTime = daytimeBreakTime;
        this.nightBreakTime = nightBreakTime;
    }

    boolean daytimeBreakTimeValid;

    @AssertTrue(message = "休憩時間が不正です")
    public boolean isDaytimeBreakTimeValid() {
        if (workRangeForm.isEmpty() || daytimeBreakTime == null) return true;

        Minute daytimeBindingMinute = toActualWorkDateTime().daytimeBindingTime().quarterHour().minute();
        if (daytimeBindingMinute.lessThan(daytimeBreakTime.minute())) {
            return false;
        }
        return true;
    }

    boolean nightBreakTimeValid;

    @AssertTrue(message = "休憩時間（深夜）が不正です")
    public boolean isNightBreakTimeValid() {
        if (workRangeForm.isEmpty() || nightBreakTime == null) return true;

        Minute nightBindingMinute = toActualWorkDateTime().nightBindingTime().quarterHour().minute();
        if (nightBindingMinute.lessThan(nightBreakTime.minute())) {
            return false;
        }
        return true;
    }

    public ActualWorkDateTime toActualWorkDateTime() {
        return toActualWorkDateTime(workRangeForm.startDateTime(), workRangeForm.endDateTime(), daytimeBreakTime, nightBreakTime);
    }

    private static ActualWorkDateTime toActualWorkDateTime(StartDateTime startDateTime, EndDateTime endDateTime, DaytimeBreakTime daytimeBreakTime, NightBreakTime nightBreakTime) {
        return new ActualWorkDateTime(
                new WorkRange(startDateTime, endDateTime),
                daytimeBreakTime,
                nightBreakTime);
    }
}
