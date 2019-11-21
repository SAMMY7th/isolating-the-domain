package example.presentation.controller.timerecord;

import example.domain.model.timerecord.evaluation.WorkDate;
import example.domain.model.timerecord.timefact.StartDateTime;
import example.domain.type.datetime.DateTime;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Min;
import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class StartTimeForm {

    @Min(0)
    int hour;

    @Min(0)
    int minute;

    public StartTimeForm() {
    }

    public StartTimeForm(int hour, int minute) {
        this.hour = hour;
        this.minute = minute;
    }

    public StartDateTime startDateTime(WorkDate workDate) {
        return new StartDateTime(new DateTime(LocalDateTime.of(workDate.toDate().value(), startTime())));
    }

    private LocalTime startTime() {
        return LocalTime.of(hour, minute);
    }

    boolean startTimeValid;

    @AssertTrue(message = "開始時刻が不正です")
    public boolean isStartTimeValid() {
        try {
            startTime();
        } catch (DateTimeException ex) {
            return false;
        }

        return true;
    }
}
