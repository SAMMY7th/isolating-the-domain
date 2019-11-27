package example.presentation.controller.timerecord;

import example.domain.model.employee.EmployeeNumber;
import example.domain.model.timerecord.evaluation.ActualWorkDateTime;
import example.domain.model.timerecord.evaluation.DaytimeBreakTime;
import example.domain.model.timerecord.evaluation.NightBreakTime;
import example.domain.model.timerecord.evaluation.TimeRecord;
import example.domain.model.timerecord.timefact.EndDateTime;
import example.domain.model.timerecord.timefact.StartDateTime;
import example.domain.model.timerecord.timefact.WorkRange;
import example.domain.type.datetime.DateTime;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.HttpServletRequest;

public class TimeRecordArgumentResolver implements HandlerMethodArgumentResolver {
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return TimeRecord.class.isAssignableFrom(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        HttpServletRequest request = (HttpServletRequest) webRequest.getNativeRequest();
        String employeeNumber = request.getParameter("employeeNumber");
        String workDate = request.getParameter("workDate");
        String startHour = request.getParameter("startHour");
        String startMinute = request.getParameter("startMinute");
        String endHour = request.getParameter("endHour");
        String endMinute = request.getParameter("endMinute");
        String daytimeBreakTime = request.getParameter("daytimeBreakTime");
        String nightBreakTime = request.getParameter("nightBreakTime");

        if (employeeNumber != null ||
            workDate != null ||
            startHour != null ||
            startMinute != null ||
            endHour != null ||
            endMinute != null ||
            daytimeBreakTime != null ||
            nightBreakTime != null) {

            EmployeeNumber en = new EmployeeNumber(employeeNumber);
            DaytimeBreakTime dt = new DaytimeBreakTime(daytimeBreakTime);
            NightBreakTime nt = new NightBreakTime(nightBreakTime);
            StartDateTime startDateTime = new StartDateTime(DateTime.parse(workDate, startHour, startMinute));
            InputEndTime inputEndTime = new InputEndTime(Integer.parseInt(endHour), Integer.parseInt(endMinute));
            EndDateTime endDateTime = inputEndTime.endDateTime(startDateTime);

            ActualWorkDateTime actualWorkDateTime = new ActualWorkDateTime(
                    new WorkRange(startDateTime, endDateTime),
                    dt,
                    nt);

            return new TimeRecord(en, actualWorkDateTime);
        }

        return null;
    }
}
