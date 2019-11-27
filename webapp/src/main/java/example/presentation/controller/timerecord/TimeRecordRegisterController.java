package example.presentation.controller.timerecord;

import example.application.coordinator.timerecord.TimeRecordCoordinator;
import example.application.coordinator.timerecord.TimeRecordQueryCoordinator;
import example.application.service.employee.EmployeeQueryService;
import example.application.service.timerecord.TimeRecordRecordService;
import example.domain.model.attendance.WorkMonth;
import example.domain.model.employee.ContractingEmployees;
import example.domain.model.employee.EmployeeNumber;
import example.domain.model.timerecord.evaluation.TimeRecord;
import example.domain.model.timerecord.evaluation.WorkDate;
import example.domain.type.date.Date;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.time.LocalDate;
import java.util.Set;

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
    Validator validator;

    public TimeRecordRegisterController(
            EmployeeQueryService employeeQueryService,
            TimeRecordRecordService timeRecordRecordService,
            TimeRecordCoordinator timeRecordCoordinator,
            TimeRecordQueryCoordinator timeRecordQueryCoordinator,
            Validator validator) {
        this.employeeQueryService = employeeQueryService;
        this.timeRecordRecordService = timeRecordRecordService;
        this.timeRecordCoordinator = timeRecordCoordinator;
        this.timeRecordQueryCoordinator = timeRecordQueryCoordinator;

        this.validator = validator;
    }

    @ModelAttribute("employees")
    ContractingEmployees employees() {
        return employeeQueryService.contractingEmployees();
    }

    @ModelAttribute("attendanceForm")
    AttendanceForm attendanceForm() {
        AttendanceForm attendanceForm = new AttendanceForm();
        return attendanceForm;
    }

    @GetMapping
    String init(@RequestParam(value = "employeeNumber", required = false) EmployeeNumber employeeNumber,
                @RequestParam(value = "workDate", required = false) WorkDate workDate,
                Model model) {
        TimeRecord timeRecord;
        if (employeeNumber != null && workDate != null) {
            timeRecord = timeRecordQueryCoordinator.timeRecord(employeeNumber, workDate);
        } else {
            timeRecord = timeRecordQueryCoordinator.defaultTimeRecord(new WorkDate(new Date(LocalDate.now())));
        }
        model.addAttribute(timeRecord);
        return "timerecord/form";
    }

    @PostMapping
    String register(@Validated @ModelAttribute("timeRecord") TimeRecord timeRecord,
                    BindingResult result) {
        if (result.hasErrors()) return "timerecord/form";

        Set<ConstraintViolation<TimeRecord>> violations = validator.validate(timeRecord);
        violations.forEach(violation -> {
            // FIXME: 今は休憩時間しかチェックしていないのでとりあえず動かしている
            result.rejectValue("daytimeBreakTime", "", violation.getMessage());
        });

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
