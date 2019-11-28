package example.presentation.controller.timerecord;

import example.application.coordinator.timerecord.TimeRecordCoordinator;
import example.application.coordinator.timerecord.TimeRecordQueryCoordinator;
import example.application.service.employee.EmployeeQueryService;
import example.application.service.timerecord.TimeRecordRecordService;
import example.domain.model.attendance.WorkMonth;
import example.domain.model.employee.ContractingEmployees;
import example.domain.model.employee.EmployeeNumber;
import example.domain.model.timerecord.evaluation.DaytimeBreakTime;
import example.domain.model.timerecord.evaluation.NightBreakTime;
import example.domain.model.timerecord.evaluation.TimeRecord;
import example.domain.model.timerecord.evaluation.WorkDate;
import example.domain.type.date.Date;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * 勤務時間の登録
 */
@Controller
@RequestMapping("timerecord")
public class TimeRecordRegisterController {

    EmployeeQueryService employeeQueryService;
    TimeRecordRecordService timeRecordRecordService;
    TimeRecordCoordinator timeRecordCoordinator;
    TimeRecordQueryCoordinator timeRecordQueryCoordinator;

    public TimeRecordRegisterController(
            EmployeeQueryService employeeQueryService,
            TimeRecordRecordService timeRecordRecordService,
            TimeRecordCoordinator timeRecordCoordinator,
            TimeRecordQueryCoordinator timeRecordQueryCoordinator) {
        this.employeeQueryService = employeeQueryService;
        this.timeRecordRecordService = timeRecordRecordService;
        this.timeRecordCoordinator = timeRecordCoordinator;
        this.timeRecordQueryCoordinator = timeRecordQueryCoordinator;
    }

    @ModelAttribute("employees")
    ContractingEmployees employees() {
        return employeeQueryService.contractingEmployees();
    }

    @ModelAttribute("attendanceForm")
    AttendanceForm attendanceForm(
        @RequestParam(required = false) EmployeeNumber employeeNumber,
        @RequestParam(required = false) String workDate,
        @RequestParam(required = false) String startHour,
        @RequestParam(required = false) String startMinute,
        @RequestParam(required = false) String endHour,
        @RequestParam(required = false) String endMinute,
        @RequestParam(required = false) DaytimeBreakTime daytimeBreakTime,
        @RequestParam(required = false) NightBreakTime nightBreakTime) {
        AttendanceForm attendanceForm =
            new AttendanceForm(employeeNumber, workDate, startHour, startMinute, endHour, endMinute, daytimeBreakTime, nightBreakTime);
        return attendanceForm;
    }

    @GetMapping
    String init(@RequestParam(value = "employeeNumber", required = false) EmployeeNumber employeeNumber,
                @RequestParam(value = "workDate", required = false) WorkDate workDate,
                @ModelAttribute AttendanceForm attendanceForm,
                Model model) {
        if (workDate != null) {
            attendanceForm.workDate = workDate.toString();
        }
        if (employeeNumber != null && workDate != null) {
            TimeRecord timeRecord = timeRecordQueryCoordinator.timeRecord(employeeNumber, workDate);
            attendanceForm.apply(timeRecord);
        } else {
            TimeRecord timeRecord = timeRecordQueryCoordinator.defaultTimeRecord(new WorkDate(new Date(LocalDate.now())));
            attendanceForm.apply(timeRecord);
        }
        return "timerecord/form";
    }

    @PostMapping
    String register(@Validated @ModelAttribute("attendanceForm") AttendanceForm attendanceForm,
                    BindingResult result) {
        if (result.hasErrors()) return "timerecord/form";
        TimeRecord timeRecord = attendanceForm.toTimeRecord();

        timeRecordCoordinator.isValid(timeRecord).errors().forEach(error -> {
            result.rejectValue(error.field(), "", error.message());
        });

        if (result.hasErrors()) return "timerecord/form";

        timeRecordRecordService.registerTimeRecord(timeRecord);

        WorkMonth workMonth = WorkMonth.from(timeRecord.workDate());

        return "redirect:/attendances/" + timeRecord.employeeNumber().value() + "/" + workMonth.toString();
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setAllowedFields(
                "employeeNumber",
                "workDate",
                "startHour",
                "startMinute",
                "endHour",
                "endMinute",
                "daytimeBreakTime",
                "nightBreakTime"
        );
    }
}
