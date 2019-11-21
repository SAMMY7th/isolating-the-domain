package example.presentation.controller.timerecord;

import example.domain.model.employee.EmployeeNumber;
import example.domain.model.timerecord.evaluation.*;
import example.domain.model.timerecord.timefact.EndDateTime;
import example.domain.model.timerecord.timefact.StartDateTime;
import example.domain.model.timerecord.timefact.WorkRange;
import example.domain.type.datetime.DateTime;

import javax.validation.Valid;

public class AttendanceForm {

    @Valid
    EmployeeNumber employeeNumber;

    @Valid
    ActualWorkDateTimeForm actualWorkDateTimeForm;

    boolean overlapWithPreviousWorkRange;
    boolean overlapWithNextWorkRange;

    public AttendanceForm() {
    }

    public TimeRecord toTimeRecord() {
        return new TimeRecord(employeeNumber, actualWorkDateTimeForm.toActualWorkDateTime());
    }

    // テストへの流出がキツイので一旦ここに集める。最終domainに持っていきたい。
    @Deprecated
    public static ActualWorkDateTime toActualWorkDateTime(String startDate, String startTime, String endTime, String daytimeBreak, String nightBreak) {
        StartDateTime startDateTime = new StartDateTime(DateTime.parse(startDate, startTime));
        EndDateTime endDateTime = EndTimeForm.from(endTime).endDateTime(startDateTime);
        return new ActualWorkDateTime(
                new WorkRange(startDateTime, endDateTime),
                new DaytimeBreakTime(daytimeBreak),
                new NightBreakTime(nightBreak));
    }

    public void apply(TimeRecord timeRecord) {
        this.employeeNumber = timeRecord.employeeNumber();
        WorkDate workDate = timeRecord.workDate();

        String[] startClockTime = timeRecord.actualWorkDateTime().workRange().start().toString().split(" ")[1].split(":");
        StartTimeForm startTimeForm = new StartTimeForm(Integer.parseInt(startClockTime[0]), Integer.parseInt(startClockTime[1]));

        String[] endClockTime = timeRecord.actualWorkDateTime().workRange().endTimeText().split(":");
        EndTimeForm endTimeForm = new EndTimeForm(Integer.parseInt(endClockTime[0]), Integer.parseInt(endClockTime[1]));

        WorkRangeForm workRangeForm = new WorkRangeForm(workDate, startTimeForm, endTimeForm);

        DaytimeBreakTime daytimeBreakTime = timeRecord.actualWorkDateTime().daytimeBreakTime();
        NightBreakTime nightBreakTime = timeRecord.actualWorkDateTime().nightBreakTime();

        this.actualWorkDateTimeForm = new ActualWorkDateTimeForm(workRangeForm, daytimeBreakTime, nightBreakTime);
    }
}
