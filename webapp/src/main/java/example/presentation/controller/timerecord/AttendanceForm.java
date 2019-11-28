package example.presentation.controller.timerecord;

import example.domain.model.employee.EmployeeNumber;
import example.domain.model.timerecord.evaluation.*;
import example.domain.model.timerecord.timefact.EndDateTime;
import example.domain.model.timerecord.timefact.StartDateTime;
import example.domain.model.timerecord.timefact.WorkRange;
import example.domain.type.datetime.DateTime;
import example.domain.type.time.Time;

import javax.validation.Valid;
import javax.validation.constraints.AssertTrue;
import java.time.DateTimeException;

public class AttendanceForm {
    @Valid
    TimeRecord timeRecord;

    EmployeeNumber employeeNumber;
    String workDate;

    String startHour;
    String startMinute;
    String endHour;
    String endMinute;

    boolean overlapWithPreviousWorkRange;
    boolean overlapWithNextWorkRange;

    public AttendanceForm(
            EmployeeNumber employeeNumber,
            String workDate,
            String startHour,
            String startMinute,
            String endHour,
            String endMinute,
            DaytimeBreakTime daytimeBreakTime,
            NightBreakTime nightBreakTime) {

        this.employeeNumber = employeeNumber;
        this.workDate = workDate;

        this.startHour = startHour;
        this.startMinute = startMinute;
        this.endHour = endHour;
        this.endMinute = endMinute;

        if (employeeNumber != null && workDate != null && startHour != null && startMinute != null
                && endHour != null && endMinute != null && daytimeBreakTime != null && nightBreakTime != null) {
            StartDateTime startDateTime = new StartDateTime(DateTime.parse(workDate, startHour, startMinute));
            InputEndTime inputEndTime = new InputEndTime(Integer.parseInt(endHour), Integer.parseInt(endMinute));
            EndDateTime endDateTime = inputEndTime.endDateTime(workStartDateTime());

            ActualWorkDateTime actualWorkDateTime = new ActualWorkDateTime(
                    new WorkRange(startDateTime, endDateTime),
                    daytimeBreakTime,
                    nightBreakTime);

            this.timeRecord = new TimeRecord(employeeNumber, actualWorkDateTime);
        }
    }

    public TimeRecord toTimeRecord() {
        ActualWorkDateTime actualWorkDateTime = toActualWorkDateTime();
        return new TimeRecord(employeeNumber, actualWorkDateTime);
    }

    private ActualWorkDateTime toActualWorkDateTime() {
        return toActualWorkDateTime(workStartDateTime(), workEndDateTime(), timeRecord.actualWorkDateTime().daytimeBreakTime(), timeRecord.actualWorkDateTime().nightBreakTime());
    }

    private static ActualWorkDateTime toActualWorkDateTime(StartDateTime startDateTime, EndDateTime endDateTime, DaytimeBreakTime daytimeBreakTime, NightBreakTime nightBreakTime) {
        return new ActualWorkDateTime(
                new WorkRange(startDateTime, endDateTime),
                daytimeBreakTime,
                nightBreakTime);
    }

    // テストへの流出がキツイので一旦ここに集める。最終domainに持っていきたい。
    @Deprecated
    public static ActualWorkDateTime toActualWorkDateTime(String startDate, String startTime, String endTime, String daytimeBreak, String nightBreak) {
        StartDateTime startDateTime = new StartDateTime(DateTime.parse(startDate, startTime));
        EndDateTime endDateTime = InputEndTime.from(endTime).endDateTime(startDateTime);
        return toActualWorkDateTime(startDateTime, endDateTime, new DaytimeBreakTime(daytimeBreak), new NightBreakTime(nightBreak));
    }

    public void apply(TimeRecord timeRecord) {
        this.employeeNumber = timeRecord.employeeNumber();
        this.workDate = timeRecord.workDate().toString();

        String[] startClockTime = timeRecord.actualWorkDateTime().workRange().start().toString().split(" ")[1].split(":");
        this.startHour = startClockTime[0];
        this.startMinute = startClockTime[1];

        String[] endClockTime = timeRecord.actualWorkDateTime().workRange().endTimeText().split(":");
        this.endHour = endClockTime[0];
        this.endMinute = endClockTime[1];

        this.timeRecord = timeRecord;
    }

    private Time workStartTime() {
        return new Time(Integer.valueOf(startHour), Integer.valueOf(this.startMinute));
    }

    private InputEndTime inputEndTime() {
        return new InputEndTime(Integer.parseInt(endHour), Integer.parseInt(endMinute));
    }

    private StartDateTime workStartDateTime() {
        return new StartDateTime(DateTime.parse(workDate, startHour, startMinute));
    }

    private EndDateTime workEndDateTime() {
        InputEndTime time = inputEndTime();
        return time.endDateTime(workStartDateTime());
    }

    boolean workDateComplete;

    @AssertTrue(message = "勤務日を入力してください")
    boolean isWorkDateComplete() {
        return !workDate.isEmpty();
    }

    boolean workDateValid;

    @AssertTrue(message = "勤務日が不正です")
    boolean isWorkDateValid() {
        if (!isWorkDateComplete()) return true;
        try {
            new WorkDate(this.workDate);
        } catch (DateTimeException ex) {
            return false;
        }
        return true;
    }

    boolean startTimeComplete;

    @AssertTrue(message = "開始時刻を入力してください")
    boolean isStartTimeComplete() {
        if (startHour.isEmpty() || startMinute.isEmpty()) return false;
        return true;
    }

    boolean startTimeValid;

    @AssertTrue(message = "開始時刻が不正です")
    public boolean isStartTimeValid() {
        if (!isStartTimeComplete()) return true;

        try {
            workStartTime();
        } catch (NumberFormatException | DateTimeException ex) {
            return false;
        }

        return true;
    }

    boolean endTimeComplete;

    @AssertTrue(message = "終了時刻を入力してください")
    boolean isEndTimeComplete() {
        if (endHour.isEmpty() || endMinute.isEmpty()) return false;
        return true;
    }

    boolean endTimeValid;

    @AssertTrue(message = "終了時刻が不正です")
    public boolean isEndTimeValid() {
        if (!isEndTimeComplete()) return true;

        try {
            inputEndTime();
        } catch (NumberFormatException ex) {
            return false;
        }

        return true;
    }

    private boolean unnecessaryCalculate() {
        return !isStartTimeComplete() || !isStartTimeValid()
                || !isEndTimeComplete() || !isEndTimeValid()
                || !isWorkDateComplete() || !isWorkDateValid();
    }

    boolean workTimeValid;

    @AssertTrue(message = "終了時刻には開始時刻よりあとの時刻を入力してください")
    public boolean isWorkTimeValid() {
        if (unnecessaryCalculate()) return true;

        StartDateTime startDateTime = workStartDateTime();
        EndDateTime endDateTime = workEndDateTime();
        if (endDateTime.isAfter(startDateTime)) return true;

        return false;
    }
}
