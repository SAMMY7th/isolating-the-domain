package example.presentation.controller.timerecord;

import example.domain.model.timerecord.evaluation.WorkDate;
import example.domain.model.timerecord.timefact.EndDateTime;
import example.domain.model.timerecord.timefact.StartDateTime;

import javax.validation.Valid;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;

public class WorkRangeForm {

    @NotNull
    @Valid
    WorkDate workDate;

    @NotNull
    @Valid
    StartTimeForm startTimeForm;

    @NotNull
    @Valid
    EndTimeForm endTimeForm;

    public WorkRangeForm() {
    }

    public WorkRangeForm(WorkDate workDate, StartTimeForm startTimeForm, EndTimeForm endTimeForm) {
        this.workDate = workDate;
        this.startTimeForm = startTimeForm;
        this.endTimeForm = endTimeForm;
    }

    public StartDateTime startDateTime() {
        return startTimeForm.startDateTime(workDate);
    }

    public EndDateTime endDateTime() {
        return endTimeForm.endDateTime(startDateTime());
    }

    boolean workTimeValid;

    @AssertTrue(message = "終了時刻には開始時刻よりあとの時刻を入力してください")
    public boolean isWorkTimeValid() {
        if (isEmpty()) return true;

        StartDateTime startDateTime = startTimeForm.startDateTime(workDate);
        EndDateTime endDateTime = endTimeForm.endDateTime(startDateTime);
        if (endDateTime.isAfter(startDateTime)) return true;

        return false;
    }

    public boolean isEmpty() {
        return (workDate.isEmpty() || startTimeForm == null || endTimeForm == null);
    }
}
