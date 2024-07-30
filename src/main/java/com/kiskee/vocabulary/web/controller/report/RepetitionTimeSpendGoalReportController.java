package com.kiskee.vocabulary.web.controller.report;

import com.kiskee.vocabulary.model.dto.report.ReportDto;
import com.kiskee.vocabulary.service.report.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/report/repetition-time")
public class RepetitionTimeSpendGoalReportController {

    private final ReportService repetitionTimeSpendGoalReportService;

    @GetMapping
    public ReportDto getReport() {
        return repetitionTimeSpendGoalReportService.getReport();
    }
}
